package com.example.alg_gestao_02.ui.contrato

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.service.PdfService
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.utils.Resource
import android.app.ProgressDialog
import com.example.alg_gestao_02.data.dto.AssinaturaRequestDTO
import com.example.alg_gestao_02.ui.contrato.PdfViewerFragment

class SignatureCaptureFragment : DialogFragment() {
    private lateinit var signatureView: SignatureView
    private lateinit var btnLimpar: Button
    private lateinit var btnConfirmar: Button
    private var contratoNumero: String? = null
    private var contratoId: Int = 0
    private val contratoRepository = ContratoRepository()
    private var isGeneratingPdf = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.DialogTheme)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        
        // Configurar o comportamento do botão voltar
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == android.view.KeyEvent.KEYCODE_BACK && event.action == android.view.KeyEvent.ACTION_UP) {
                if (isGeneratingPdf) {
                    LogUtils.debug("SignatureCapture", "Tentativa de fechar diálogo bloqueada (isGeneratingPdf = true)")
                    true // Consumir o evento
                } else {
                    LogUtils.debug("SignatureCapture", "Fechando diálogo (isGeneratingPdf = false)")
                    false // Não consumir o evento, permitir que o diálogo feche
                }
            } else {
                false // Não consumir outros eventos
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signature_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contratoNumero = arguments?.getString("contratoNumero")
        contratoId = arguments?.getInt("contratoId") ?: 0
        
        signatureView = view.findViewById(R.id.signatureView)
        btnLimpar = view.findViewById(R.id.btnLimpar)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        
        setupButtons()
    }

    private fun setupButtons() {
        btnLimpar.setOnClickListener {
            signatureView.clear()
        }

        btnConfirmar.setOnClickListener {
            val bitmap = signatureView.getBitmap()
            if (bitmap != null) {
                val assinaturaBase64 = converterParaBase64(bitmap)
                if (assinaturaBase64 != null) {
                    enviarAssinatura(assinaturaBase64)
                } else {
                    Toast.makeText(context, "Erro ao processar assinatura", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun converterParaBase64(bitmap: Bitmap): String? {
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val byteArray = stream.toByteArray()
            val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
            return "data:image/png;base64,$base64"
        } catch (e: Exception) {
            LogUtils.error("SignatureCapture", "Erro ao converter assinatura para base64", e)
        }
        return null
    }

    private fun enviarAssinatura(assinaturaBase64: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("Enviando assinatura, aguarde...")
                setCancelable(false)
                show()
            }
            try {
                LogUtils.debug("SignatureCapture", "Iniciando envio da assinatura para o contrato #$contratoId")
                val result = contratoRepository.enviarAssinatura(
                    base64Data = assinaturaBase64,
                    contratoId = contratoId
                )

                when (result) {
                    is Resource.Success -> {
                        LogUtils.debug("SignatureCapture", "Assinatura enviada com sucesso: ${result.data.message}")
                        if (result.data.success) {
                            // Aguardar um momento para garantir que o backend processou a assinatura
                            LogUtils.debug("SignatureCapture", "Aguardando 1 segundo para processamento no backend...")
                            kotlinx.coroutines.delay(1000)
                            gerarNovoPdf()
                        } else {
                            progressDialog.dismiss()
                            Toast.makeText(context, result.data.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    is Resource.Error -> {
                        progressDialog.dismiss()
                        val errorMsg = "Erro ao salvar assinatura: ${result.message}"
                        LogUtils.error("SignatureCapture", errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> {
                        // Não precisamos fazer nada aqui pois o loading é tratado pelo ProgressDialog
                    }
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                val errorMsg = "Erro ao processar assinatura: ${e.message}"
                LogUtils.error("SignatureCapture", errorMsg, e)
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun gerarNovoPdf() {
        contratoNumero?.let { numero ->
            viewLifecycleOwner.lifecycleScope.launch {
                isGeneratingPdf = true
                LogUtils.debug("SignatureCapture", "Iniciando geração do PDF (isGeneratingPdf = true)")
                
                val progressDialog = ProgressDialog(requireContext()).apply {
                    setMessage("Gerando PDF, aguarde...")
                    setCancelable(false)
                    show()
                }
                try {
                    LogUtils.debug("SignatureCapture", "Buscando dados atualizados do contrato #$contratoId")
                    val result = contratoRepository.getContratoById(contratoId)
                    if (result is Resource.Success) {
                        val contrato = result.data
                        LogUtils.debug("SignatureCapture", "Dados do contrato obtidos com sucesso. Gerando PDF...")
                        LogUtils.debug("SignatureCapture", "Dados do contrato antes de gerar PDF: id=${contrato.id}, num=${contrato.contratoNum}, assinado=${contrato.isAssinado()}, cliente=${contrato.cliente?.contratante}")
                        
                        // Verificar se o contrato tem cliente
                        if (contrato.cliente == null) {
                            progressDialog.dismiss()
                            isGeneratingPdf = false
                            LogUtils.debug("SignatureCapture", "Finalizando geração do PDF (isGeneratingPdf = false) - Erro: Cliente não encontrado")
                            val errorMsg = "Erro: Dados do cliente não encontrados"
                            LogUtils.error("SignatureCapture", errorMsg)
                            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        LogUtils.debug("SignatureCapture", "Iniciando chamada para o serviço de PDF na porta 8080")
                        val pdfResult = PdfService().gerarPdfContrato(contrato, contrato.cliente)
                        LogUtils.debug("SignatureCapture", "Resposta recebida do serviço de PDF")
                        
                        pdfResult.fold(
                            onSuccess = { pdfResponse ->
                                progressDialog.dismiss()
                                isGeneratingPdf = false
                                LogUtils.debug("SignatureCapture", "Finalizando geração do PDF (isGeneratingPdf = false) - Sucesso")
                                
                                if (pdfResponse.success) {
                                    LogUtils.debug("SignatureCapture", "PDF gerado com sucesso: ${pdfResponse.message}")
                                    val pdfViewer = PdfViewerFragment.newInstance(
                                        pdfBase64 = pdfResponse.pdfBase64,
                                        contratoNumero = numero,
                                        contratoId = contratoId,
                                        htmlUrl = pdfResponse.htmlUrl,
                                        htmlContent = pdfResponse.htmlContent
                                    )
                                    dismiss()
                                    pdfViewer.show(parentFragmentManager, "pdf_viewer")
                                    Toast.makeText(context, "Assinatura salva com sucesso", Toast.LENGTH_SHORT).show()
                                } else {
                                    val errorMsg = "Erro ao gerar PDF: ${pdfResponse.message}"
                                    LogUtils.error("SignatureCapture", errorMsg)
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onFailure = { error ->
                                progressDialog.dismiss()
                                isGeneratingPdf = false
                                LogUtils.debug("SignatureCapture", "Finalizando geração do PDF (isGeneratingPdf = false) - Erro: ${error.message}")
                                val errorMsg = "Erro ao gerar PDF: ${error.message}"
                                LogUtils.error("SignatureCapture", errorMsg, error)
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else if (result is Resource.Error) {
                        progressDialog.dismiss()
                        isGeneratingPdf = false
                        LogUtils.debug("SignatureCapture", "Finalizando geração do PDF (isGeneratingPdf = false) - Erro ao buscar contrato")
                        val errorMsg = "Erro ao buscar dados do contrato: ${result.message}"
                        LogUtils.error("SignatureCapture", errorMsg)
                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    isGeneratingPdf = false
                    LogUtils.debug("SignatureCapture", "Finalizando geração do PDF (isGeneratingPdf = false) - Exceção: ${e.message}")
                    val errorMsg = "Erro ao gerar PDF: ${e.message}"
                    LogUtils.error("SignatureCapture", errorMsg, e)
                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
} 