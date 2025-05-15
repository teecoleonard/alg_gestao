package com.example.alg_gestao_02.ui.contrato

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.utils.LogUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Fragment para visualização de PDF gerado
 */
class PdfViewerFragment : DialogFragment() {
    
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCompartilhar: Button
    private lateinit var btnSalvar: Button
    private lateinit var btnFechar: Button
    private lateinit var tvInfo: TextView
    
    private var pdfBytes: ByteArray? = null
    private var pdfFile: File? = null
    private var contratoNumero: String = ""
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            salvarPdf()
        } else {
            Toast.makeText(
                requireContext(),
                "Permissão necessária para salvar o PDF",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    companion object {
        private const val ARG_PDF_BASE64 = "pdf_base64"
        private const val ARG_CONTRATO_NUMERO = "contrato_numero"
        private const val ARG_HTML_URL = "html_url"
        private const val ARG_HTML_CONTENT = "html_content"
        private const val SERVER_BASE_URL = "http://192.168.100.195:8080"
        
        fun newInstance(
            pdfBase64: String, 
            contratoNumero: String,
            htmlUrl: String? = null, 
            htmlContent: String? = null
        ): PdfViewerFragment {
            val fragment = PdfViewerFragment()
            val args = Bundle()
            args.putString(ARG_PDF_BASE64, pdfBase64)
            args.putString(ARG_CONTRATO_NUMERO, contratoNumero)
            htmlUrl?.let { args.putString(ARG_HTML_URL, it) }
            htmlContent?.let { args.putString(ARG_HTML_CONTENT, it) }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
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
        btnFechar = view.findViewById(R.id.btnFechar)
        tvInfo = view.findViewById(R.id.tvInfo)
        
        val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
        val htmlUrl = arguments?.getString(ARG_HTML_URL)
        val htmlContent = arguments?.getString(ARG_HTML_CONTENT)
        contratoNumero = arguments?.getString(ARG_CONTRATO_NUMERO) ?: ""
        
        btnFechar.setOnClickListener { dismiss() }
        btnCompartilhar.isEnabled = false
        btnSalvar.isEnabled = false
        
        // Aplicar cores explicitamente para garantir que apareçam corretamente
        btnCompartilhar.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.primary))
        btnCompartilhar.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        
        btnSalvar.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        btnFechar.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
        
        if (pdfBase64 != null) {
            try {
                // Decodificar o PDF de base64 para bytes
                pdfBytes = android.util.Base64.decode(pdfBase64, android.util.Base64.DEFAULT)
                
                // Criar arquivo temporário para o PDF
                val tempFile = File.createTempFile("contrato_temp_", ".pdf", requireContext().cacheDir)
                FileOutputStream(tempFile).use { it.write(pdfBytes) }
                pdfFile = tempFile
                
                // Configurar botões de ação
                btnCompartilhar.setOnClickListener { compartilharPdf() }
                btnSalvar.setOnClickListener { verificarPermissao() }
                
                // Decidir como exibir o contrato (prioridade: URL HTML > Conteúdo HTML > PDF em Base64)
                when {
                    // 1. Se tiver URL HTML, carrega diretamente do servidor
                    htmlUrl != null && htmlUrl.isNotEmpty() -> {
                        carregarHtmlExterno(htmlUrl)
                    }
                    // 2. Se tiver conteúdo HTML, exibe direto
                    htmlContent != null && htmlContent.isNotEmpty() -> {
                        carregarHtmlDireto(htmlContent)
                    }
                    // 3. Fallback para exibição do PDF via HTML incorporado
                    else -> {
                        exibirPdfEmHtml(pdfBase64)
                    }
                }
            } catch (e: Exception) {
                mostrarErro("Erro ao processar PDF: ${e.message}", e)
            }
        } else {
            mostrarErro("Não foi possível carregar o PDF", null)
        }
    }
    
    private fun carregarHtmlExterno(url: String) {
        try {
            // Configurar WebView
            webView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    btnCompartilhar.isEnabled = true
                    btnSalvar.isEnabled = true
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    LogUtils.error("PdfViewerFragment", "Erro ao carregar HTML externo: $description")
                    // Fallback para mostrar PDF via HTML incorporado
                    val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
                    if (pdfBase64 != null) {
                        exibirPdfEmHtml(pdfBase64)
                    } else {
                        mostrarErro("Não foi possível carregar o contrato", null)
                    }
                }
            }
            
            // Verificar se a URL é relativa (começa com /) e adicionar o domínio base
            val urlCompleta = if (url.startsWith("/")) {
                // Adicionar o domínio base (mesmo do servidor PDF)
                SERVER_BASE_URL + url
            } else {
                url
            }
            
            webView.loadUrl(urlCompleta)
            LogUtils.debug("PdfViewerFragment", "Carregando HTML externo: $urlCompleta")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar HTML externo", e)
            // Fallback para exibição do PDF
            val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
            if (pdfBase64 != null) {
                exibirPdfEmHtml(pdfBase64)
            } else {
                mostrarErro("Erro ao carregar o contrato", e)
            }
        }
    }
    
    private fun carregarHtmlDireto(htmlContent: String) {
        try {
            // Configurar WebView
            webView.visibility = View.VISIBLE
            progressBar.visibility = View.VISIBLE
            
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    btnCompartilhar.isEnabled = true
                    btnSalvar.isEnabled = true
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    LogUtils.error("PdfViewerFragment", "Erro ao carregar HTML direto: $description")
                    // Fallback para mostrar PDF via HTML incorporado
                    val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
                    if (pdfBase64 != null) {
                        exibirPdfEmHtml(pdfBase64)
                    } else {
                        mostrarErro("Não foi possível carregar o contrato", null)
                    }
                }
            }
            
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            LogUtils.debug("PdfViewerFragment", "Carregando HTML direto")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar HTML direto", e)
            // Fallback para exibição do PDF
            val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
            if (pdfBase64 != null) {
                exibirPdfEmHtml(pdfBase64)
            } else {
                mostrarErro("Erro ao carregar o contrato", e)
            }
        }
    }
    
    private fun mostrarPdf(pdfFile: File) {
        try {
            // Configurar WebView
            webView.visibility = View.VISIBLE
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    btnCompartilhar.isEnabled = true
                    btnSalvar.isEnabled = true
                }
            }
            
            // Obter URI para o arquivo
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                pdfFile
            )
            
            // Usar o drive.google.com para exibir o PDF online
            val googleDriveUrl = "https://drive.google.com/viewerng/viewer?embedded=true&url=${Uri.encode(uri.toString())}"
            webView.loadUrl(googleDriveUrl)
            
            LogUtils.debug("PdfViewerFragment", "Carregando PDF via Google Drive Viewer")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar PDF via WebView", e)
            abrirComVisualizadorExterno()
        }
    }
    
    private fun exibirPdfEmHtml(pdfBase64: String) {
        try {
            // Configurar WebView
            webView.visibility = View.VISIBLE
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                builtInZoomControls = true
                displayZoomControls = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    progressBar.visibility = View.GONE
                    btnCompartilhar.isEnabled = true
                    btnSalvar.isEnabled = true
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    // Fallback para visualizador nativo se falhar o carregamento
                    LogUtils.error("PdfViewerFragment", "Erro ao carregar PDF via HTML: $description")
                    tentarAbrirComPdfNativo()
                }
            }
            
            // Criando um HTML que carrega o PDF como base64 diretamente
            val htmlData = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body, html {
                            margin: 0;
                            padding: 0;
                            height: 100%;
                            width: 100%;
                            overflow: hidden;
                        }
                        embed {
                            width: 100%;
                            height: 100%;
                        }
                    </style>
                </head>
                <body>
                    <embed src="data:application/pdf;base64,$pdfBase64" type="application/pdf" />
                </body>
                </html>
            """.trimIndent()
            
            webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
            LogUtils.debug("PdfViewerFragment", "Carregando PDF via HTML incorporado")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar PDF via WebView", e)
            tentarAbrirComPdfNativo()
        }
    }
    
    private fun tentarAbrirComPdfNativo() {
        pdfFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                    // Mantemos o fragment aberto para permitir compartilhar/salvar
                    Toast.makeText(
                        requireContext(),
                        "Abrindo PDF com visualizador externo",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    mostrarErro("Nenhum aplicativo encontrado para visualizar PDFs", null)
                }
            } catch (e: Exception) {
                mostrarErro("Erro ao abrir PDF. Tente usar o botão compartilhar.", e)
            }
        } ?: run {
            mostrarErro("PDF não disponível", null)
        }
    }
    
    private fun abrirComVisualizadorExterno() {
        // Manter compatibilidade com código existente
        tentarAbrirComPdfNativo()
    }
    
    private fun mostrarErro(mensagem: String, erro: Exception?) {
        progressBar.visibility = View.GONE
        tvInfo.text = mensagem
        tvInfo.visibility = View.VISIBLE
        
        if (erro != null) {
            LogUtils.error("PdfViewerFragment", mensagem, erro)
        } else {
            LogUtils.error("PdfViewerFragment", mensagem)
        }
    }
    
    private fun verificarPermissao() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                salvarPdf()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                Toast.makeText(
                    requireContext(),
                    "É necessário permissão para salvar arquivos",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    
    private fun salvarPdf() {
        if (pdfBytes == null) {
            Toast.makeText(requireContext(), "Erro: PDF não disponível", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            // Criar nome do arquivo
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val nomeArquivo = if (contratoNumero.isNotEmpty()) {
                "Contrato_${contratoNumero.replace("/", "_")}.pdf"
            } else {
                "Contrato_$timestamp.pdf"
            }
            
            // Diretório para salvar o PDF (Documentos)
            val diretorio = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            // Criar subdiretório ALG
            val algDir = File(diretorio, "ALG")
            if (!algDir.exists()) {
                algDir.mkdirs()
            }
            
            // Criar o arquivo
            val file = File(algDir, nomeArquivo)
            FileOutputStream(file).use {
                it.write(pdfBytes)
            }
            
            // Notificar o usuário
            Toast.makeText(
                requireContext(),
                "PDF salvo em Documentos/ALG/$nomeArquivo",
                Toast.LENGTH_LONG
            ).show()
            
        } catch (e: IOException) {
            Toast.makeText(requireContext(), "Erro ao salvar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            LogUtils.error("PdfViewerFragment", "Erro ao salvar PDF", e)
        }
    }
    
    private fun compartilharPdf() {
        pdfFile?.let { file ->
            try {
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.provider",
                    file
                )
                
                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    type = "application/pdf"
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                startActivity(Intent.createChooser(shareIntent, "Compartilhar PDF"))
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
                LogUtils.error("PdfViewerFragment", "Erro ao compartilhar PDF", e)
            }
        } ?: run {
            Toast.makeText(requireContext(), "PDF não disponível para compartilhar", Toast.LENGTH_SHORT).show()
        }
    }
}