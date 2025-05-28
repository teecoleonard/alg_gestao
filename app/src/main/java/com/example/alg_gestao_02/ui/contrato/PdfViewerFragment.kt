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
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.webkit.WebResourceError
import android.webkit.WebChromeClient
import android.webkit.ConsoleMessage
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
import com.example.alg_gestao_02.service.PdfService
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var btnSalvar: FloatingActionButton
    private lateinit var btnFechar: Button
    private lateinit var btnAssinar: Button
    private lateinit var tvInfo: TextView
    
    private var pdfBytes: ByteArray? = null
    private var pdfFile: File? = null
    private var contratoNumero: String = ""
    private var contratoId: Int = 0
    
    private val pdfService = PdfService()
    
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Mantém o padrão do DialogFragment
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
        btnFechar = view.findViewById(R.id.btnFechar)
        btnAssinar = view.findViewById(R.id.btnAssinar)
        tvInfo = view.findViewById(R.id.tvInfo)
        
        webView.visibility = View.VISIBLE // Garante que o WebView está visível
        
        val pdfBase64 = arguments?.getString(ARG_PDF_BASE64)
        val htmlUrl = arguments?.getString(ARG_HTML_URL)
        val htmlContent = arguments?.getString(ARG_HTML_CONTENT)
        contratoNumero = arguments?.getString(ARG_CONTRATO_NUMERO) ?: ""
        contratoId = arguments?.getInt(ARG_CONTRATO_ID) ?: 0
        
        setupWebView()
        setupButtons()
        
        // Carregar o conteúdo
        when {
            htmlContent != null -> {
                LogUtils.debug("PdfViewerFragment", "Carregando conteúdo HTML")
                carregarHtmlContent(htmlContent)
            }
            htmlUrl != null -> {
                LogUtils.debug("PdfViewerFragment", "Carregando URL HTML: $htmlUrl")
                carregarHtmlUrl(htmlUrl)
            }
            pdfBase64 != null -> {
                LogUtils.debug("PdfViewerFragment", "Carregando PDF base64")
                carregarPdfBase64(pdfBase64)
            }
            else -> {
                LogUtils.error("PdfViewerFragment", "Nenhum conteúdo disponível para exibição")
                tvInfo.text = "Nenhum conteúdo disponível"
                tvInfo.visibility = View.VISIBLE
            }
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
                progressBar.visibility = View.GONE
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
                tvInfo.text = "Erro ao carregar página: ${error?.description}"
                tvInfo.visibility = View.VISIBLE
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

        btnFechar.setOnClickListener {
            dismiss()
        }

        btnAssinar.setOnClickListener {
            val bundle = Bundle().apply {
                putString("contratoNumero", contratoNumero)
                putInt("contratoId", contratoId)
            }
            
            val signatureFragment = SignatureCaptureFragment().apply {
                arguments = bundle
            }
            
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
            
            // Carregar no WebView
            webView.loadUrl("file://${tempFile.absolutePath}")
            LogUtils.debug("PdfViewerFragment", "PDF carregado no WebView: file://${tempFile.absolutePath}")
        } catch (e: Exception) {
            LogUtils.error("PdfViewerFragment", "Erro ao carregar PDF base64", e)
            tvInfo.text = "Erro ao carregar PDF: ${e.message}"
            tvInfo.visibility = View.VISIBLE
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
            tvInfo.text = "Erro ao carregar HTML: ${e.message}"
            tvInfo.visibility = View.VISIBLE
        }
    }
    
    private fun verificarPermissaoESalvar() {
        if (verificarPermissaoEscrita()) {
            salvarPdf()
        } else {
            solicitarPermissaoEscrita()
        }
    }
    
    private fun verificarPermissaoEscrita(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun solicitarPermissaoEscrita() {
        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    
    private fun salvarPdf() {
        pdfBytes?.let { bytes ->
            try {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "contrato_${contratoNumero}_$timestamp.pdf"
                val file = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    fileName
                )
                
                FileOutputStream(file).use { it.write(bytes) }
                this.pdfFile = file
                
                Toast.makeText(
                    requireContext(),
                    "PDF salvo em: ${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                LogUtils.error("PdfViewerFragment", "Erro ao salvar PDF", e)
                Toast.makeText(
                    requireContext(),
                    "Erro ao salvar PDF: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "PDF não disponível para salvar",
                Toast.LENGTH_SHORT
            ).show()
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        pdfFile?.delete()
    }
    
    companion object {
        private const val ARG_PDF_BASE64 = "pdf_base64"
        private const val ARG_HTML_URL = "html_url"
        private const val ARG_HTML_CONTENT = "html_content"
        private const val ARG_CONTRATO_NUMERO = "contrato_numero"
        private const val ARG_CONTRATO_ID = "contrato_id"
        
        fun newInstance(
            pdfBase64: String? = null,
            htmlUrl: String? = null,
            htmlContent: String? = null,
            contratoNumero: String,
            contratoId: Int
        ): PdfViewerFragment {
            return PdfViewerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PDF_BASE64, pdfBase64)
                    putString(ARG_HTML_URL, htmlUrl)
                    putString(ARG_HTML_CONTENT, htmlContent)
                    putString(ARG_CONTRATO_NUMERO, contratoNumero)
                    putInt(ARG_CONTRATO_ID, contratoId)
                }
            }
        }
    }
}