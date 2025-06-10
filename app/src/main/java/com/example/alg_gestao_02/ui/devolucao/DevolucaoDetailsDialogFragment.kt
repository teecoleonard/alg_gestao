package com.example.alg_gestao_02.ui.devolucao

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.example.alg_gestao_02.service.PdfService
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.EquipamentoRepository
import com.example.alg_gestao_02.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.app.ProgressDialog
import android.widget.Toast
import com.example.alg_gestao_02.ui.contrato.PdfViewerFragment
import com.example.alg_gestao_02.service.DevolucaoPdfResponse

/**
 * Fragment de diálogo para exibir detalhes de uma devolução
 */
class DevolucaoDetailsDialogFragment : DialogFragment() {
    
    private var devolucao: Devolucao? = null
    private var onProcessarRequestListener: OnProcessarRequestListener? = null
    
    // Views
    private lateinit var tvClienteNome: TextView
    private lateinit var tvDevolucaoNumero: TextView
    private lateinit var tvEquipamentoNome: TextView
    private lateinit var tvQuantidadeContratada: TextView
    private lateinit var tvQuantidadeDevolvida: TextView
    private lateinit var tvQuantidadePendente: TextView
    private lateinit var tvDataPrevista: TextView
    private lateinit var tvDataEfetiva: TextView
    private lateinit var layoutDataEfetiva: LinearLayout
    private lateinit var tvStatus: TextView
    private lateinit var tvObservacao: TextView
    private lateinit var tvContratoInfo: TextView
    private lateinit var btnFechar: Button
    private lateinit var btnProcessar: Button
    private lateinit var btnGerarPdf: Button
    private lateinit var layoutAcoes: LinearLayout
    
    companion object {
        private const val ARG_DEVOLUCAO = "devolucao"
        
        /**
         * Cria uma nova instância do fragmento com os dados da devolução
         */
        fun newInstance(devolucao: Devolucao): DevolucaoDetailsDialogFragment {
            val fragment = DevolucaoDetailsDialogFragment()
            val args = Bundle().apply {
                putParcelable(ARG_DEVOLUCAO, devolucao)
            }
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            devolucao = it.getParcelable(ARG_DEVOLUCAO)
        }
        
        setStyle(STYLE_NORMAL, R.style.EquipamentoDetailDialogStyle)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_devolucao_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        populateViews()
    }
    
    private fun initViews(view: View) {
        tvClienteNome = view.findViewById(R.id.tvDetalhesDevolucaoClienteNome)
        tvDevolucaoNumero = view.findViewById(R.id.tvDetalhesDevolucaoNumero)
        tvEquipamentoNome = view.findViewById(R.id.tvDetalhesDevolucaoEquipamentoNome)
        tvQuantidadeContratada = view.findViewById(R.id.tvDetalhesDevolucaoQuantidadeContratada)
        tvQuantidadeDevolvida = view.findViewById(R.id.tvDetalhesDevolucaoQuantidadeDevolvida)
        tvQuantidadePendente = view.findViewById(R.id.tvDetalhesDevolucaoQuantidadePendente)
        tvDataPrevista = view.findViewById(R.id.tvDetalhesDevolucaoDataPrevista)
        tvDataEfetiva = view.findViewById(R.id.tvDetalhesDevolucaoDataEfetiva)
        layoutDataEfetiva = view.findViewById(R.id.layoutDataEfetiva)
        tvStatus = view.findViewById(R.id.tvDetalhesDevolucaoStatus)
        tvObservacao = view.findViewById(R.id.tvDetalhesDevolucaoObservacao)
        tvContratoInfo = view.findViewById(R.id.tvDetalhesDevolucaoContratoInfo)
        btnFechar = view.findViewById(R.id.btnFecharDetalhesDevolucao)
        btnProcessar = view.findViewById(R.id.btnProcessarDevolucao)
        btnGerarPdf = view.findViewById(R.id.btnGerarPdfDevolucao)
        layoutAcoes = view.findViewById(R.id.layoutAcoes)
    }
    
    private fun setupListeners() {
        btnFechar.setOnClickListener {
            dismiss()
        }
        
        btnProcessar.setOnClickListener {
            devolucao?.let { processarDevolucao(it) }
        }
        
        btnGerarPdf.setOnClickListener {
            gerarPdfDevolucao()
        }
    }
    
    private fun populateViews() {
        devolucao?.let { d ->
            // Definir o nome do cliente
            tvClienteNome.text = d.resolverNomeCliente()
            
            // Definir o número da devolução
            tvDevolucaoNumero.text = "Devolução #${d.devNum}"
            
            // Definir o nome do equipamento
            tvEquipamentoNome.text = d.resolverNomeEquipamento()
            
            // Definir as quantidades
            tvQuantidadeContratada.text = "${d.quantidadeContratada} unidades"
            tvQuantidadeDevolvida.text = "${d.quantidadeDevolvida} unidades"
            
            val pendente = d.getQuantidadePendente()
            tvQuantidadePendente.text = "$pendente unidades"
            
            // Colorir a quantidade pendente baseado no valor
            if (pendente > 0) {
                tvQuantidadePendente.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.warning)
                )
            } else {
                tvQuantidadePendente.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.success)
                )
            }
            
            // Definir as datas
            tvDataPrevista.text = d.getDataPrevistaFormatada()
            
            // Mostrar data efetiva apenas se existir
            if (d.dataDevolucaoEfetiva != null) {
                tvDataEfetiva.text = d.getDataEfetivaFormatada()
                layoutDataEfetiva.visibility = View.VISIBLE
            } else {
                layoutDataEfetiva.visibility = View.GONE
            }
            
            // Definir o status
            tvStatus.text = d.getStatusFormatado()
            
            // Colorir o status
            when (d.statusItemDevolucao) {
                "Pendente" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.warning))
                }
                "Devolvido" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success))
                }
                "Avariado", "Faltante" -> {
                    tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
                }
            }
            
            // Definir observação
            tvObservacao.text = d.observacaoItemDevolucao ?: "Nenhuma observação registrada"
            
            // Definir informações do contrato
            val contratoNum = d.contrato?.contratoNum ?: "Desconhecido"
            val dataEmissao = d.contrato?.getDataEmissaoFormatada() ?: "Data desconhecida"
            tvContratoInfo.text = "Contrato #$contratoNum - Emissão: $dataEmissao"
            
            // Mostrar/ocultar botão de processamento
            if (d.isPendente() && d.temQuantidadePendente()) {
                btnProcessar.visibility = View.VISIBLE
            } else {
                btnProcessar.visibility = View.GONE
            }
        }
    }
    
    /**
     * Exibe diálogo para processar uma devolução
     */
    private fun processarDevolucao(devolucao: Devolucao) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_processar_devolucao, null)
        
        val etQuantidade = dialogView.findViewById<TextInputEditText>(R.id.etQuantidade)
        val etObservacao = dialogView.findViewById<TextInputEditText>(R.id.etObservacao)
        
        // Preencher quantidade com o valor pendente por padrão
        etQuantidade.setText(devolucao.getQuantidadePendente().toString())
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Processar Devolução")
            .setView(dialogView)
            .setPositiveButton("Devolvido") { _, _ ->
                val quantidade = etQuantidade.text.toString().toIntOrNull() ?: 0
                val observacao = etObservacao.text.toString()
                
                onProcessarRequestListener?.onProcessarRequested(
                    devolucao,
                    quantidade,
                    "Devolvido",
                    observacao.ifEmpty { null }
                )
                
                dismiss()
            }
            .setNeutralButton("Avariado") { _, _ ->
                val quantidade = etQuantidade.text.toString().toIntOrNull() ?: 0
                val observacao = etObservacao.text.toString()
                
                onProcessarRequestListener?.onProcessarRequested(
                    devolucao,
                    quantidade,
                    "Avariado",
                    observacao.ifEmpty { null }
                )
                
                dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .create()
        
        dialog.show()
    }
    
    /**
     * Gera o PDF da devolução
     */
    private fun gerarPdfDevolucao() {
        devolucao?.let { devolucaoNaoNula ->
            // Mostrar dialog de progresso
            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("Gerando PDF da devolução, aguarde...")
                setCancelable(false)
                show()
            }

            // Chamar o serviço em uma coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    LogUtils.debug("DevolucaoDetailsDialog", "🚀 Iniciando geração de PDF para devolução #${devolucaoNaoNula.devNum}")
                    
                    // Buscar dados atualizados da devolução e entidades relacionadas
                    val devolucaoRepository = DevolucaoRepository()
                    val contratoRepository = ContratoRepository()
                    val clienteRepository = ClienteRepository()
                    val equipamentoRepository = EquipamentoRepository()
                    
                    LogUtils.debug("DevolucaoDetailsDialog", "📊 Carregando dados relacionados...")
                    
                    // Buscar devolução atualizada
                    val devolucaoAtualizada = when (val result = devolucaoRepository.getDevolucaoById(devolucaoNaoNula.id)) {
                        is Resource.Success -> {
                            LogUtils.debug("DevolucaoDetailsDialog", "✅ Devolução atualizada obtida com sucesso")
                            result.data
                        }
                        is Resource.Error -> {
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                                val errorMsg = "Erro ao buscar dados da devolução: ${result.message}"
                                LogUtils.error("DevolucaoDetailsDialog", errorMsg)
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                        else -> {
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "Erro inesperado ao buscar devolução", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                    }
                    
                    // Buscar dados do contrato relacionado
                    val contrato = when (val result = contratoRepository.getContratoById(devolucaoAtualizada.contratoId)) {
                        is Resource.Success -> {
                            LogUtils.debug("DevolucaoDetailsDialog", "✅ Contrato obtido com sucesso")
                            result.data
                        }
                        is Resource.Error -> {
                            LogUtils.warning("DevolucaoDetailsDialog", "⚠️ Não foi possível obter contrato: ${result.message}")
                            null
                        }
                        else -> null
                    }
                    
                    // Buscar dados do cliente relacionado
                    val cliente = try {
                        clienteRepository.getClienteById(devolucaoAtualizada.clienteId)
                    } catch (e: Exception) {
                        LogUtils.warning("DevolucaoDetailsDialog", "⚠️ Não foi possível obter cliente: ${e.message}")
                        null
                    }
                    
                    // Buscar dados do equipamento relacionado
                    val equipamento = when (val result = equipamentoRepository.getEquipamentoById(devolucaoAtualizada.equipamentoId)) {
                        is Resource.Success -> {
                            LogUtils.debug("DevolucaoDetailsDialog", "✅ Equipamento obtido com sucesso")
                            result.data
                        }
                        is Resource.Error -> {
                            LogUtils.warning("DevolucaoDetailsDialog", "⚠️ Não foi possível obter equipamento: ${result.message}")
                            null
                        }
                        else -> null
                    }
                    
                    LogUtils.debug("DevolucaoDetailsDialog", "📋 Dados coletados:")
                    LogUtils.debug("DevolucaoDetailsDialog", "  - Devolução: ${devolucaoAtualizada.devNum}")
                    LogUtils.debug("DevolucaoDetailsDialog", "  - Cliente: ${cliente?.contratante ?: "Dados da devolução"}")
                    LogUtils.debug("DevolucaoDetailsDialog", "  - Equipamento: ${equipamento?.nomeEquip ?: "Dados da devolução"}")
                    LogUtils.debug("DevolucaoDetailsDialog", "  - Contrato: ${contrato?.contratoNum ?: "Dados da devolução"}")
                    
                    // Gerar PDF usando o serviço
                    LogUtils.debug("DevolucaoDetailsDialog", "📄 Iniciando chamada para o serviço de PDF...")
                    val pdfService = PdfService()
                    val result = pdfService.gerarPdfDevolucao(
                        devolucao = devolucaoAtualizada,
                        cliente = cliente,
                        equipamento = equipamento,
                        contrato = contrato
                    )
                    
                    LogUtils.debug("DevolucaoDetailsDialog", "📥 Resposta recebida do serviço de PDF")
                    
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        
                        if (result.isSuccess) {
                            val pdfResponse = result.getOrNull()
                            if (pdfResponse != null && pdfResponse.success) {
                                LogUtils.debug("DevolucaoDetailsDialog", "✅ PDF de devolução gerado com sucesso: ${pdfResponse.message}")
                                
                                // Fechar este dialog primeiro  
                                dismiss()
                                
                                // Limpar outros dialogs que possam estar abertos
                                parentFragmentManager.fragments.forEach { fragment ->
                                    if (fragment is DialogFragment && fragment != this@DevolucaoDetailsDialogFragment) {
                                        fragment.dismissAllowingStateLoss()
                                    }
                                }
                                
                                // Mostrar o PDF/HTML no visualizador
                                val pdfViewer = PdfViewerFragment.newInstance(
                                    pdfBase64 = pdfResponse.pdfBase64,
                                    contratoNumero = "DEV_${devolucaoAtualizada.devNum}",
                                    contratoId = devolucaoAtualizada.id,
                                    htmlUrl = pdfResponse.htmlUrl,
                                    htmlContent = pdfResponse.htmlContent
                                )
                                
                                // Adicionar logs para verificar o conteúdo
                                LogUtils.debug("DevolucaoDetailsDialog", "📄 htmlUrl recebido: ${pdfResponse.htmlUrl}")
                                LogUtils.debug("DevolucaoDetailsDialog", "📝 htmlContent recebido: ${pdfResponse.htmlContent?.substring(0, minOf(50, pdfResponse.htmlContent.length))}...")
                                
                                pdfViewer.show(parentFragmentManager, "pdf_viewer_devolucao")
                                
                                // Feedback para o usuário
                                val mensagem = "📄 PDF da devolução gerado com sucesso!"
                                Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMsg = "Erro ao gerar PDF: ${pdfResponse?.message ?: "Resposta inválida"}"
                                LogUtils.error("DevolucaoDetailsDialog", errorMsg)
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val error = result.exceptionOrNull()?.message ?: "Erro desconhecido"
                            val errorMsg = "Falha ao gerar PDF da devolução: $error"
                            LogUtils.error("DevolucaoDetailsDialog", errorMsg)
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.error("DevolucaoDetailsDialog", "❌ Erro crítico ao gerar PDF da devolução", e)
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Erro ao gerar PDF da devolução: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Erro: Dados da devolução não disponíveis",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Interface para comunicar solicitação de processamento
     */
    interface OnProcessarRequestListener {
        fun onProcessarRequested(devolucao: Devolucao, quantidade: Int, status: String, observacao: String?)
    }
    
    /**
     * Define o listener para processamento
     */
    fun setOnProcessarRequestListener(listener: OnProcessarRequestListener) {
        this.onProcessarRequestListener = listener
    }
} 