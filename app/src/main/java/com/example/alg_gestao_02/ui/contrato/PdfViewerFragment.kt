package com.example.alg_gestao_02.ui.contrato

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.WebResourceError
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.PdfUtils
import com.example.alg_gestao_02.service.PdfService

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment para visualização de PDF gerado
 * 
 * OTIMIZAÇÃO: Para evitar TransactionTooLargeException quando o app é minimizado,
 * este fragment usa cache temporário e não salva dados grandes no Bundle de argumentos.
 */
class PdfViewerFragment : DialogFragment() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCompartilhar: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnAssinar: Button
    private lateinit var tvInfo: TextView
    
    // Novos containers para gerenciar estados
    private lateinit var loadingContainer: LinearLayout
    private lateinit var errorContainer: LinearLayout
    
    private var pdfBytes: ByteArray? = null
    private var pdfFile: File? = null
    private var contratoNumero: String = ""
    private var contratoId: Int = 0
    private var onContratoAtualizadoCallback: (() -> Unit)? = null
    
    // Cache IDs para evitar salvar dados grandes no Bundle
    private var cachedPdfId: String? = null
    private var cachedHtmlId: String? = null
    
    private val pdfService = PdfService()
    
    // Método para configurar callback de atualização do contrato
    fun setOnContratoAtualizadoCallback(callback: () -> Unit) {
        this.onContratoAtualizadoCallback = callback
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.debug("PdfViewerFragment", "📱 onCreate - Fragment inicializado")
        
        // Restaurar IDs do cache se existirem
        savedInstanceState?.let { bundle ->
            cachedPdfId = bundle.getString(STATE_CACHED_PDF_ID)
            cachedHtmlId = bundle.getString(STATE_CACHED_HTML_ID)
            contratoNumero = bundle.getString(STATE_CONTRATO_NUMERO, "")
            contratoId = bundle.getInt(STATE_CONTRATO_ID, 0)
            LogUtils.debug("PdfViewerFragment", "🔄 Estado restaurado - PDF ID: $cachedPdfId, HTML ID: $cachedHtmlId")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // CRÍTICO: Não salvar dados grandes para evitar TransactionTooLargeException
        // Salvar apenas IDs pequenos e metadados
        super.onSaveInstanceState(outState)
        
        LogUtils.debug("PdfViewerFragment", "💾 Salvando estado do PdfViewerFragment...")
        
        // Salvar apenas IDs pequenos e metadados essenciais
        cachedPdfId?.let { outState.putString(STATE_CACHED_PDF_ID, it) }
        cachedHtmlId?.let { outState.putString(STATE_CACHED_HTML_ID, it) }
        outState.putString(STATE_CONTRATO_NUMERO, contratoNumero)
        outState.putInt(STATE_CONTRATO_ID, contratoId)
        
        // IMPORTANTE: Limpar argumentos grandes para evitar crash
        arguments?.let { args ->
            if (args.containsKey(ARG_PDF_BASE64)) {
                val pdfSize = args.getString(ARG_PDF_BASE64)?.length ?: 0
                if (pdfSize > 10000) { // Mais de 10KB
                    LogUtils.debug("PdfViewerFragment", "🧹 Limpando PDF base64 dos argumentos (${pdfSize} bytes)")
                    args.remove(ARG_PDF_BASE64)
                }
            }
            if (args.containsKey(ARG_HTML_CONTENT)) {
                val htmlSize = args.getString(ARG_HTML_CONTENT)?.length ?: 0
                if (htmlSize > 10000) { // Mais de 10KB
                    LogUtils.debug("PdfViewerFragment", "🧹 Limpando HTML content dos argumentos (${htmlSize} bytes)")
                    args.remove(ARG_HTML_CONTENT)
                }
            }
        }
        
        LogUtils.debug("PdfViewerFragment", "✅ Estado do PdfViewerFragment salvo com segurança")
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pdf_viewer, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        webView = view.findViewById(R.id.webView)
        progressBar = view.findViewById(R.id.progressBar)
        btnCompartilhar = view.findViewById(R.id.btnCompartilhar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
        btnAssinar = view.findViewById(R.id.btnAssinar)
        tvInfo = view.findViewById(R.id.tvInfo)
        
        // Inicializar novos containers
        loadingContainer = view.findViewById(R.id.loadingContainer)
        errorContainer = view.findViewById(R.id.errorContainer)
        
        // Verificar se o FileProvider está configurado corretamente
        LogUtils.debug("PdfViewerFragment", "🔍 Verificando configuração do FileProvider...")
        if (!PdfUtils.verificarFileProvider(requireContext())) {
            LogUtils.error("PdfViewerFragment", "❌ FileProvider não está configurado corretamente!")
            mostrarErro("Erro de configuração: FileProvider não encontrado")
            return
        }
        LogUtils.debug("PdfViewerFragment", "✅ FileProvider configurado corretamente")
        
        // Inicializar no estado de loading
        mostrarLoading()
        
        // Priorizar cache restaurado sobre argumentos
        if (cachedPdfId != null || cachedHtmlId != null) {
            LogUtils.debug("PdfViewerFragment", "🔄 Restaurando conteúdo do cache...")
            restaurarConteudoDoCache()
        } else {
            LogUtils.debug("PdfViewerFragment", "📥 Carregando conteúdo dos argumentos...")
            carregarConteudoDosArgumentos()
        }
    }
    
    private fun restaurarConteudoDoCache() {
        try {
            var conteudoCarregado = false
            
            // Tentar restaurar HTML primeiro (melhor para visualização)
            cachedHtmlId?.let { htmlId ->
                val htmlFile = File(requireContext().cacheDir, "cached_html_$htmlId.html")
                if (htmlFile.exists()) {
                    val htmlContent = htmlFile.readText()
                    LogUtils.debug("PdfViewerFragment", "📄 HTML restaurado do cache (${htmlContent.length} chars)")
                    carregarHtmlContent(htmlContent)
                    conteudoCarregado = true
                }
            }
            
            // Tentar restaurar PDF se HTML não foi encontrado
            if (!conteudoCarregado) {
                cachedPdfId?.let { pdfId ->
                    val pdfFile = File(requireContext().cacheDir, "cached_pdf_$pdfId.pdf")
                    if (pdfFile.exists()) {
                        val pdfBytes = pdfFile.readBytes()
                        LogUtils.debug("PdfViewerFragment", "📱 PDF restaurado do cache (${pdfBytes.size} bytes)")
                        val pdfBase64 = android.util.Base64.encodeToString(pdfBytes, android.util.Base64.DEFAULT)
                        carregarPdfBase64(pdfBase64)
                        conteudoCarregado = true
                    }
                }
            }
            
            if (!conteudoCarregado) {
                LogUtils.warning("PdfViewerFragment", "⚠️ Nenhum conteúdo em cache encontrado")
                mostrarErro("Conteúdo não disponível após restauração")
            }
            
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "❌ Erro ao restaurar conteúdo do cache", e)
            mostrarErro("Erro ao restaurar conteúdo: ${e.message}")
        }
        
        setupWebView()
        setupButtons()
    }
    
    private fun carregarConteudoDosArgumentos() {
        val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
        val htmlUrl = arguments?.getString(ARG_HTML_URL)
        val htmlContent = arguments?.getString(ARG_HTML_CONTENT)
        contratoNumero = arguments?.getString(ARG_CONTRATO_NUMERO) ?: ""
        contratoId = arguments?.getInt(ARG_CONTRATO_ID) ?: 0
        
        setupWebView()
        setupButtons()
        
        // Carregar o conteúdo - HTML para visualização, mas salvar PDF em background
        when {
            htmlContent != null -> {
                LogUtils.debug("PdfViewerFragment", "📄 Carregando conteúdo HTML para visualização")
                salvarHtmlNoCache(htmlContent)
                carregarHtmlContent(htmlContent)
                // Salvar PDF em background para download/compartilhamento
                if (pdfBase64 != null) {
                    salvarPdfEmBackground(pdfBase64)
                }
            }
            htmlUrl != null -> {
                LogUtils.debug("PdfViewerFragment", "🌐 Carregando URL HTML: $htmlUrl")
                carregarHtmlUrl(htmlUrl)
                // Salvar PDF em background para download/compartilhamento
                if (pdfBase64 != null) {
                    salvarPdfEmBackground(pdfBase64)
                }
            }
            pdfBase64 != null -> {
                LogUtils.debug("PdfViewerFragment", "📱 Carregando PDF base64 (sem HTML disponível)")
                salvarPdfNoCache(pdfBase64)
                carregarPdfBase64(pdfBase64)
            }
            else -> {
                LogUtils.error("PdfViewerFragment", "❌ Nenhum conteúdo disponível para exibição")
                mostrarErro("Nenhum conteúdo disponível para exibição")
            }
        }
    }
    
    private fun salvarHtmlNoCache(htmlContent: String) {
        try {
            cachedHtmlId = "html_${System.currentTimeMillis()}"
            val htmlFile = File(requireContext().cacheDir, "cached_html_$cachedHtmlId.html")
            htmlFile.writeText(htmlContent)
            LogUtils.debug("PdfViewerFragment", "💾 HTML salvo no cache: $cachedHtmlId")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "❌ Erro ao salvar HTML no cache", e)
        }
    }
    
    private fun salvarPdfNoCache(pdfBase64: String) {
        try {
            val pdfBytes = android.util.Base64.decode(pdfBase64, android.util.Base64.DEFAULT)
            cachedPdfId = "pdf_${System.currentTimeMillis()}"
            val pdfFile = File(requireContext().cacheDir, "cached_pdf_$cachedPdfId.pdf")
            pdfFile.writeBytes(pdfBytes)
            this.pdfBytes = pdfBytes
            LogUtils.debug("PdfViewerFragment", "💾 PDF salvo no cache: $cachedPdfId")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "❌ Erro ao salvar PDF no cache", e)
        }
    }
    
    private fun salvarPdfEmBackground(pdfBase64: String) {
        try {
            val pdfBytes = android.util.Base64.decode(pdfBase64, android.util.Base64.DEFAULT)
            this.pdfBytes = pdfBytes
            cachedPdfId = "pdf_${System.currentTimeMillis()}"
            val pdfFile = File(requireContext().cacheDir, "cached_pdf_$cachedPdfId.pdf")
            pdfFile.writeBytes(pdfBytes)
            LogUtils.debug("PdfViewerFragment", "📱 PDF salvo em background para compartilhamento/download")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "❌ Erro ao salvar PDF em background", e)
        }
    }
    
    private fun setupWebView() {
        webView.settings.apply {
            // Configurações básicas
            javaScriptEnabled = true
            domStorageEnabled = true
            
            // Configurações de zoom
            setSupportZoom(true)
            builtInZoomControls = true
            displayZoomControls = false
            
            // Configurações de cache
            cacheMode = WebSettings.LOAD_NO_CACHE
            
            // Configurações de conteúdo
            allowContentAccess = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Configurações de visualização
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // Configurações adicionais para JavaScript
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            setEnableSmoothTransition(true)
            setDatabaseEnabled(true)
            setGeolocationEnabled(true)
            setNeedInitialFocus(true)
            setSupportMultipleWindows(true)
            setLoadsImagesAutomatically(true)
            setBlockNetworkImage(false)
            setBlockNetworkLoads(false)
            setDefaultTextEncodingName("UTF-8")
            setJavaScriptCanOpenWindowsAutomatically(true)
        }

        // Configurar WebViewClient para interceptar requisições
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                mostrarConteudo()
                btnCompartilhar.isEnabled = true
                btnSalvar.isEnabled = true
                LogUtils.debug("PdfViewerFragment", "Página carregada: $url")
            }

            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val url = request?.url?.toString() ?: return null
                LogUtils.debug("PdfViewerFragment", "Interceptando requisição: $url")
                
                // Se for um recurso local, permitir
                if (url.startsWith("file://")) {
                    return null
                }
                
                // Se for um recurso do servidor, tentar carregar
                if (url.contains("192.168.100.195:8080")) {
                    try {
                        val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connect()
                        
                        val inputStream = connection.inputStream
                        val mimeType = connection.contentType ?: "text/plain"
                        
                        return WebResourceResponse(
                            mimeType,
                            "UTF-8",
                            inputStream
                        )
                    } catch (e: Exception) {
                        LogUtils.error("PdfViewerFragment", "Erro ao carregar recurso: $url", e)
                    }
                }
                
                return null
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                LogUtils.error("PdfViewerFragment", "Erro ao carregar página: ${error?.description}")
                mostrarErro("Erro ao carregar página: ${error?.description}")
                super.onReceivedError(view, request, error)
            }
        }

        // Adicionar WebChromeClient para suportar console.log e outros recursos do JavaScript
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                LogUtils.debug("WebView", "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}")
                return true
            }
        }
    }
    
    private fun setupButtons() {
        btnCompartilhar.setOnClickListener {
            compartilharPdf()
        }

        btnSalvar.setOnClickListener {
            verificarPermissaoESalvar()
        }

        btnAssinar.setOnClickListener {
            LogUtils.debug("PdfViewerFragment", "🖊️ Botão assinar clicado")
            val bundle = Bundle().apply {
                putString("contratoNumero", contratoNumero)
                putInt("contratoId", contratoId)
            }
            
            val signatureFragment = SignatureCaptureFragment().apply {
                arguments = bundle
                setOnContratoAtualizadoListener {
                    LogUtils.debug("PdfViewerFragment", "🔔 Callback recebido - contrato atualizado")
                    LogUtils.debug("PdfViewerFragment", "🗙 Fechando PdfViewerFragment")
                    // Fechar este viewer e voltar para a tela de contratos
                    dismiss()
                    LogUtils.debug("PdfViewerFragment", "📞 Chamando callback para ContratosFragment")
                    // Chamar o callback para atualizar a lista de contratos
                    onContratoAtualizadoCallback?.invoke()
                    LogUtils.debug("PdfViewerFragment", "✅ Callback PdfViewer concluído")
                }
            }
            
            LogUtils.debug("PdfViewerFragment", "🔓 Abrindo SignatureCaptureFragment")
            signatureFragment.show(parentFragmentManager, "signature_fragment")
        }
    }
    


    private fun carregarPdfBase64(pdfBase64: String) {
        LogUtils.debug("PdfViewerFragment", "Iniciando carregamento do PDF base64 no WebView")
        try {
            val pdfBytes = android.util.Base64.decode(pdfBase64, android.util.Base64.DEFAULT)
            this.pdfBytes = pdfBytes
            
            // Salvar temporariamente para visualização
            val tempFile = File(requireContext().cacheDir, "temp_${System.currentTimeMillis()}.pdf")
            FileOutputStream(tempFile).use { it.write(pdfBytes) }
            this.pdfFile = tempFile
            
            // Carregar PDF no WebView usando data URL (mais compatível)
            val base64String = android.util.Base64.encodeToString(pdfBytes, android.util.Base64.NO_WRAP)
            val dataUrl = "data:application/pdf;base64,$base64String"
            webView.loadUrl(dataUrl)
            LogUtils.debug("PdfViewerFragment", "PDF carregado no WebView via data URL")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar PDF base64", e)
            mostrarErro("Erro ao carregar PDF: ${e.message}")
        }
    }
    
    private fun carregarHtmlUrl(url: String) {
        webView.loadUrl(url)
    }
    
    private fun carregarHtmlContent(content: String) {
        LogUtils.debug("PdfViewerFragment", "Iniciando carregamento do HTML no WebView. Tamanho do HTML: ${content.length}")
        try {
            // Criar um arquivo temporário com o conteúdo HTML
            val tempFile = File(requireContext().cacheDir, "temp_${System.currentTimeMillis()}.html")
            FileOutputStream(tempFile).use { it.write(content.toByteArray()) }

            // Carregar o arquivo HTML com base URL para permitir carregamento de recursos
            webView.loadDataWithBaseURL(
                "file://${tempFile.absolutePath}",
                content,
                "text/html",
                "UTF-8",
                null
            )

            // Limpar o arquivo temporário quando o fragmento for destruído
            tempFile.deleteOnExit()
            LogUtils.debug("PdfViewerFragment", "HTML carregado no WebView com base URL: file://${tempFile.absolutePath}")

        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar HTML", e)
            mostrarErro("Erro ao carregar HTML: ${e.message}")
        }
    }
    
    private fun verificarPermissaoESalvar() {
        pdfBytes?.let { bytes ->
            val resultado = PdfUtils.salvarPdfNaPastaDownloads(
                requireContext(),
                bytes,
                "contrato_$contratoNumero"
            )
            
            resultado.fold(
                onSuccess = { caminho ->
                    Toast.makeText(
                        requireContext(),
                        "✅ PDF salvo com sucesso: $caminho",
                        Toast.LENGTH_LONG
                    ).show()
                    LogUtils.debug("PdfViewerFragment", "PDF salvo em: $caminho")
                },
                onFailure = { erro ->
                    Toast.makeText(
                        requireContext(),
                        "❌ Erro ao salvar PDF: ${erro.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    LogUtils.error("PdfViewerFragment", "Erro ao salvar PDF", erro)
                }
            )
        } ?: run {
            Toast.makeText(
                requireContext(),
                "PDF não disponível para salvar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Métodos antigos de salvamento removidos - toda lógica está agora no PdfUtils
    
    private fun compartilharPdf() {
        pdfBytes?.let { bytes ->
            val resultadoArquivo = PdfUtils.criarArquivoTemporario(
                requireContext(),
                bytes,
                "contrato_$contratoNumero"
            )
            
            resultadoArquivo.fold(
                onSuccess = { arquivo ->
                    val resultadoCompartilhamento = PdfUtils.compartilharPdf(
                        requireContext(),
                        arquivo,
                        "Compartilhar Contrato PDF",
                        "Contrato #$contratoNumero"
                    )
                    
                    resultadoCompartilhamento.fold(
                        onSuccess = {
                            LogUtils.debug("PdfViewerFragment", "Compartilhamento iniciado com sucesso")
                            // Salvar referência do arquivo para limpeza posterior
                            this.pdfFile = arquivo
                        },
                        onFailure = { erro ->
                            Toast.makeText(
                                requireContext(),
                                "❌ ${erro.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    )
                },
                onFailure = { erro ->
                    Toast.makeText(
                        requireContext(),
                        "❌ Erro ao preparar arquivo: ${erro.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        } ?: run {
            Toast.makeText(
                requireContext(),
                "PDF não disponível para compartilhar",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    
    // Método criarArquivoTemporario removido - PdfUtils.criarArquivoTemporario é usado
    
    override fun onDestroyView() {
        super.onDestroyView()
        
        // Limpar arquivos temporários
        pdfFile?.delete()
        
        // Limpar cache criado por este fragment
        try {
            cachedPdfId?.let { pdfId ->
                val pdfFile = File(requireContext().cacheDir, "cached_pdf_$pdfId.pdf")
                if (pdfFile.exists()) {
                    pdfFile.delete()
                    LogUtils.debug("PdfViewerFragment", "🧹 Cache PDF limpo: $pdfId")
                }
            }
            
            cachedHtmlId?.let { htmlId ->
                val htmlFile = File(requireContext().cacheDir, "cached_html_$htmlId.html")
                if (htmlFile.exists()) {
                    htmlFile.delete()
                    LogUtils.debug("PdfViewerFragment", "🧹 Cache HTML limpo: $htmlId")
                }
            }
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "❌ Erro ao limpar cache", e)
        }
    }
    
    /**
     * Mostra o estado de loading
     */
    private fun mostrarLoading() {
        loadingContainer.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        webView.visibility = View.GONE
    }
    
    /**
     * Mostra o conteúdo (PDF/HTML carregado)
     */
    private fun mostrarConteudo() {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.GONE
        webView.visibility = View.VISIBLE
    }
    
    /**
     * Mostra o estado de erro
     */
    private fun mostrarErro(mensagem: String) {
        loadingContainer.visibility = View.GONE
        errorContainer.visibility = View.VISIBLE
        webView.visibility = View.GONE
        
        // Atualizar a mensagem de erro no container de erro
        val tvError = errorContainer.findViewById<TextView>(R.id.tvError)
        tvError?.text = mensagem
    }
    
    companion object {
        private const val ARG_PDF_BASE64 = "pdf_base64"
        private const val ARG_HTML_URL = "html_url"
        private const val ARG_HTML_CONTENT = "html_content"
        private const val ARG_CONTRATO_NUMERO = "contrato_numero"
        private const val ARG_CONTRATO_ID = "contrato_id"
        
        // Constantes para salvar estado (não salvar dados grandes!)
        private const val STATE_CACHED_PDF_ID = "cached_pdf_id"
        private const val STATE_CACHED_HTML_ID = "cached_html_id"
        private const val STATE_CONTRATO_NUMERO = "contrato_numero"
        private const val STATE_CONTRATO_ID = "contrato_id"
        
        fun newInstance(
            pdfBase64: String? = null,
            htmlUrl: String? = null,
            htmlContent: String? = null,
            contratoNumero: String,
            contratoId: Int
        ): PdfViewerFragment {
            return PdfViewerFragment().apply {
                arguments = Bundle().apply {
                    // OTIMIZAÇÃO: Não salvar dados grandes nos argumentos para evitar TransactionTooLargeException
                    // Salvar apenas se for pequeno (< 10KB) ou usar cache
                    
                    val pdfSize = pdfBase64?.length ?: 0
                    val htmlSize = htmlContent?.length ?: 0
                    
                    LogUtils.debug("PdfViewerFragment", "📊 Criando instância - PDF: ${pdfSize}b, HTML: ${htmlSize}b")
                    
                    // PDF base64: salvar apenas se pequeno
                    if (pdfBase64 != null && pdfSize < 10000) {
                        putString(ARG_PDF_BASE64, pdfBase64)
                    } else if (pdfBase64 != null) {
                        LogUtils.debug("PdfViewerFragment", "⚠️ PDF muito grande (${pdfSize}b) - será salvo no cache")
                        putString(ARG_PDF_BASE64, pdfBase64) // Ainda passa, mas será limpo no onSaveInstanceState
                    }
                    
                    // HTML content: salvar apenas se pequeno
                    if (htmlContent != null && htmlSize < 10000) {
                        putString(ARG_HTML_CONTENT, htmlContent)
                    } else if (htmlContent != null) {
                        LogUtils.debug("PdfViewerFragment", "⚠️ HTML muito grande (${htmlSize}b) - será salvo no cache")
                        putString(ARG_HTML_CONTENT, htmlContent) // Ainda passa, mas será limpo no onSaveInstanceState
                    }
                    
                    // Dados pequenos - sempre seguros
                    putString(ARG_HTML_URL, htmlUrl)
                    putString(ARG_CONTRATO_NUMERO, contratoNumero)
                    putInt(ARG_CONTRATO_ID, contratoId)
                }
            }
        }
    }
}