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
        layoutAcoes = view.findViewById(R.id.layoutAcoes)
    }
    
    private fun setupListeners() {
        btnFechar.setOnClickListener {
            dismiss()
        }
        
        btnProcessar.setOnClickListener {
            devolucao?.let { processarDevolucao(it) }
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