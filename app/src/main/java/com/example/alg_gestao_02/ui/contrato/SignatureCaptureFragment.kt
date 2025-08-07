package com.example.alg_gestao_02.ui.contrato

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.service.PdfService
import com.example.alg_gestao_02.service.PdfResponse
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
    private lateinit var signatureContainer: LinearLayout
    private lateinit var orientationBlockContainer: LinearLayout
    private lateinit var tvTituloAssinatura: TextView
    private var contratoNumero: String? = null
    private var contratoId: Int = 0
    private val contratoRepository = ContratoRepository()
    private var isGeneratingPdf = false
    private var onContratoAtualizadoListener: (() -> Unit)? = null
    private var savedSignatureData: ByteArray? = null
    private var isAlteracao: Boolean = false
    
    // Interface para comunicar que o contrato foi atualizado com assinatura
    interface OnContratoAtualizadoListener {
        fun onContratoAtualizadoComAssinatura(contratoId: Int, contratoNumero: String)
    }
    
    fun setOnContratoAtualizadoListener(listener: () -> Unit) {
        this.onContratoAtualizadoListener = listener
    }

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
        isAlteracao = arguments?.getBoolean("isAlteracao") ?: false
        
        // Inicializar views
        signatureView = view.findViewById(R.id.signatureView)
        btnLimpar = view.findViewById(R.id.btnLimpar)
        btnConfirmar = view.findViewById(R.id.btnConfirmar)
        signatureContainer = view.findViewById(R.id.signatureContainer)
        orientationBlockContainer = view.findViewById(R.id.orientationBlockContainer)
        tvTituloAssinatura = view.findViewById(R.id.tvTituloAssinatura)
        
        // Configurar título baseado se é alteração ou nova assinatura
        tvTituloAssinatura.text = if (isAlteracao) {
            "Alterar assinatura do contrato"
        } else {
            "Assine o contrato"
        }
        
        // Restaurar assinatura se houver dados salvos
        savedInstanceState?.getByteArray("signature_data")?.let { data ->
            savedSignatureData = data
            signatureView.restoreFromSavedData(data)
        }
        
        setupButtons()
        checkOrientationAndToggleViews()
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
                        // Verificar se a assinatura foi processada com sucesso
                        // Se temos um assinaturaId, significa que foi salva com sucesso
                        if (result.data.assinaturaId != null && result.data.assinaturaId > 0) {
                            progressDialog.dismiss()
                            // Aguardar um momento para garantir que o backend processou a assinatura
                            LogUtils.debug("SignatureCapture", "Aguardando 1 segundo para processamento no backend...")
                            kotlinx.coroutines.delay(1000)
                            voltarParaContratoAtualizado()
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

    /**
     * [FUNÇÃO REMOVIDA] - Não é mais utilizada no novo fluxo
     * Agora o PDF não é gerado automaticamente após a assinatura
     */
    

    
    /**
     * Volta para a página do contrato após assinatura ser salva
     */
    private fun voltarParaContratoAtualizado() {
        LogUtils.debug("SignatureCapture", "🔄 INÍCIO: Voltando para página do contrato após assinatura")
        LogUtils.debug("SignatureCapture", "📋 Contrato: #$contratoNumero (ID: $contratoId)")
        
        // 1. PRIMEIRO: Mostrar mensagem IMEDIATAMENTE enquanto o context ainda existe
        val mensagem = "✅ Contrato $contratoNumero atualizado! Gere um novo PDF"
        try {
            Toast.makeText(
                requireContext(), 
                mensagem, 
                Toast.LENGTH_LONG
            ).show()
            LogUtils.debug("SignatureCapture", "💬 PASSO 1: Mensagem exibida IMEDIATAMENTE: $mensagem")
        } catch (e: Exception) {
            LogUtils.error("SignatureCapture", "❌ Erro ao exibir mensagem: ${e.message}")
        }
        
        // 2. Notificar o listener ANTES de fechar dialogs para garantir que funcione
        LogUtils.debug("SignatureCapture", "🔔 PASSO 2: Notificando listener sobre atualização do contrato")
        LogUtils.debug("SignatureCapture", "🔍 Listener existe? ${onContratoAtualizadoListener != null}")
        try {
            onContratoAtualizadoListener?.invoke()
            LogUtils.debug("SignatureCapture", "📡 Listener invocado - ContratosFragment deve atualizar a lista")
        } catch (e: Exception) {
            LogUtils.error("SignatureCapture", "❌ Erro ao invocar listener: ${e.message}")
        }
        
        // 3. Fechar APENAS este dialog de assinatura (manter outros dialogs abertos)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Pequeno delay para garantir que o callback foi processado
                kotlinx.coroutines.delay(100)
                
                // Fechar APENAS este dialog de assinatura
                LogUtils.debug("SignatureCapture", "🗙 PASSO 3: Fechando APENAS dialog de assinatura")
                dismiss()
                
                LogUtils.debug("SignatureCapture", "🏁 FIM: Processo concluído - ContratoDetailsDialog MANTIDO ABERTO")
                
            } catch (e: Exception) {
                LogUtils.error("SignatureCapture", "❌ Erro ao fechar dialog: ${e.message}")
                // Mesmo com erro, tentar fechar o dialog
                try {
                    dismiss()
                } catch (dismissError: Exception) {
                    LogUtils.error("SignatureCapture", "❌ Erro crítico ao fechar dialog: ${dismissError.message}")
                }
            }
        }
    }
    
    /**
     * [FUNÇÃO OBSOLETA] - Mantida para referência, mas não mais utilizada
     * Mostra o PDF atualizado com navegação limpa (remove todos os dialogs anteriores)
     */
    private fun mostrarPdfAtualizadoComNavegacaoLimpa(pdfResponse: PdfResponse, contratoNumero: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            LogUtils.debug("SignatureCapture", "Iniciando processo de navegação limpa")
            
            // 1. Fechar este dialog de assinatura
            dismiss()
            
            // 2. Aguardar um momento para garantir que o dismiss foi processado
            kotlinx.coroutines.delay(150)
            
            // 3. Limpar TODOS os outros dialogs que possam estar empilhados
            val fragmentsToRemove = mutableListOf<DialogFragment>()
            parentFragmentManager.fragments.forEach { fragment ->
                if (fragment is DialogFragment && fragment != this@SignatureCaptureFragment) {
                    fragmentsToRemove.add(fragment)
                    LogUtils.debug("SignatureCapture", "Marcando dialog para remoção: ${fragment::class.simpleName}")
                }
            }
            
            // 4. Remover todos os dialogs encontrados
            fragmentsToRemove.forEach { fragment ->
                try {
                    fragment.dismissAllowingStateLoss()
                    LogUtils.debug("SignatureCapture", "Dialog removido: ${fragment::class.simpleName}")
                } catch (e: Exception) {
                    LogUtils.error("SignatureCapture", "Erro ao remover dialog: ${fragment::class.simpleName}", e)
                }
            }
            
            LogUtils.debug("SignatureCapture", "Stack limpo. ${fragmentsToRemove.size} dialogs removidos")
            
            // 5. Aguardar mais um momento para garantir limpeza completa
            kotlinx.coroutines.delay(100)
            
            // 6. Criar e mostrar o PDF viewer atualizado
            LogUtils.debug("SignatureCapture", "Criando PDF viewer com documento assinado")
            
            val pdfViewer = PdfViewerFragment.newInstance(
                pdfBase64 = pdfResponse.pdfBase64,
                contratoNumero = contratoNumero,
                contratoId = contratoId,
                htmlUrl = pdfResponse.htmlUrl,
                htmlContent = pdfResponse.htmlContent
            )
            
            // 7. Mostrar o PDF atualizado (agora será o único dialog na pilha)
            try {
                pdfViewer.show(parentFragmentManager, "pdf_viewer_signed")
                LogUtils.debug("SignatureCapture", "✅ PDF assinado exibido com navegação limpa")
            } catch (e: Exception) {
                LogUtils.error("SignatureCapture", "Erro ao exibir PDF assinado", e)
                Toast.makeText(context, "Erro ao exibir PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Verifica a orientação atual e alterna a visibilidade dos containers
     */
    private fun checkOrientationAndToggleViews() {
        val orientation = resources.configuration.orientation
        
        when (orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                // Modo paisagem - mostrar área de assinatura
                signatureContainer.visibility = View.VISIBLE
                orientationBlockContainer.visibility = View.GONE
                LogUtils.debug("SignatureCapture", "Modo paisagem detectado - liberando assinatura")
            }
            Configuration.ORIENTATION_PORTRAIT -> {
                // Modo retrato - mostrar bloqueio
                signatureContainer.visibility = View.GONE
                orientationBlockContainer.visibility = View.VISIBLE
                LogUtils.debug("SignatureCapture", "Modo retrato detectado - bloqueando assinatura")
            }
            else -> {
                // Orientação indefinida - mostrar bloqueio por segurança
                signatureContainer.visibility = View.GONE
                orientationBlockContainer.visibility = View.VISIBLE
                LogUtils.debug("SignatureCapture", "Orientação indefinida - bloqueando assinatura por segurança")
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LogUtils.debug("SignatureCapture", "Mudança de configuração detectada")
        
        // Salvar assinatura antes da mudança
        savedSignatureData = signatureView.saveSignatureData()
        
        // Verificar nova orientação
        checkOrientationAndToggleViews()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        
        // Salvar dados da assinatura
        val signatureData = signatureView.saveSignatureData()
        if (signatureData != null) {
            outState.putByteArray("signature_data", signatureData)
            LogUtils.debug("SignatureCapture", "Dados da assinatura salvos no bundle")
        }
    }
} 