package com.example.alg_gestao_02.ui.contrato

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModel
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * DialogFragment para adicionar ou editar um equipamento em um contrato
 */
class EquipamentoContratoDialogFragment : DialogFragment() {

    private lateinit var equipamentosViewModel: EquipamentosViewModel
    private lateinit var contratosViewModel: ContratosViewModel

    private lateinit var tilEquipamento: TextInputLayout
    private lateinit var actvEquipamento: AutoCompleteTextView
    private lateinit var tilQuantidade: TextInputLayout
    private lateinit var etQuantidade: TextInputEditText
    private lateinit var tilValorUnitario: TextInputLayout
    private lateinit var etValorUnitario: TextInputEditText
    private lateinit var tilValorFrete: TextInputLayout
    private lateinit var etValorFrete: TextInputEditText
    private lateinit var tvValorTotal: TextInputEditText
    private lateinit var btnCancelar: Button
    private lateinit var btnSalvar: Button
    private lateinit var loadingView: View

    private var equipamentoContratoParaEdicao: EquipamentoContrato? = null
    private var contratoId: Int = 0
    private var equipamentoSelecionado: Equipamento? = null
    private var equipamentos: List<Equipamento> = emptyList()
    private var onEquipamentoSalvoListener: ((EquipamentoContrato) -> Unit)? = null

    companion object {
        private const val ARG_EQUIPAMENTO_CONTRATO = "arg_equipamento_contrato"
        private const val ARG_CONTRATO_ID = "arg_contrato_id"
        
        // Gera ID temporário único baseado em timestamp
        private fun generateTempId(): Int {
            val timestamp = System.currentTimeMillis()
            // Garante ID > 1.000.000 para evitar conflitos com IDs reais
            return (timestamp % 9_000_000).toInt() + 1_000_001
        }

        fun newInstance(
            contratoId: Int,
            equipamentoContrato: EquipamentoContrato? = null
        ): EquipamentoContratoDialogFragment {
            val fragment = EquipamentoContratoDialogFragment()
            val args = Bundle().apply {
                putInt(ARG_CONTRATO_ID, contratoId)
                equipamentoContrato?.let { putParcelable(ARG_EQUIPAMENTO_CONTRATO, it) }
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomDialogStyle)

        // Recupera dados dos argumentos
        contratoId = arguments?.getInt(ARG_CONTRATO_ID) ?: 0
        equipamentoContratoParaEdicao = arguments?.getParcelable(ARG_EQUIPAMENTO_CONTRATO)
        
        LogUtils.debug("EquipamentoContratoDialog", 
            "Iniciando diálogo: contratoId=$contratoId, " +
            "editando=${equipamentoContratoParaEdicao != null}, " +
            "equipamentoId=${equipamentoContratoParaEdicao?.id ?: "novo"}")
            
        // Verificar se o contratoId é válido ou temporário
        if (contratoId == 0) {
            LogUtils.warning("EquipamentoContratoDialog", 
                "ContratoId inválido (0) recebido nos argumentos!")
        } else if (contratoId < 0) {
            LogUtils.debug("EquipamentoContratoDialog", 
                "ContratoId temporário ($contratoId) recebido - contrato ainda não foi salvo")
        }
    }

    override fun onStart() {
        super.onStart()
        
        // Ajustar o tamanho do diálogo
        dialog?.window?.apply {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            setLayout(width, height)
            setBackgroundDrawableResource(android.R.color.white)
            
            // Garantir que o fundo do diálogo seja opaco
            decorView.setBackgroundResource(android.R.color.white)
            // Definir animação de entrada e saída
            attributes.windowAnimations = android.R.style.Animation_Dialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.dialog_equipamento_contrato, container, false)
        rootView.setBackgroundResource(android.R.color.white)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupViewModels()
        setupListeners()
        carregarEquipamentos()

        // Se estiver editando, preenche o formulário
        equipamentoContratoParaEdicao?.let {
            preencherFormularioEdicao(it)
        }
    }

    private fun initViews(view: View) {
        tilEquipamento = view.findViewById(R.id.tilEquipamento)
        actvEquipamento = view.findViewById(R.id.actvEquipamento)
        tilQuantidade = view.findViewById(R.id.tilQuantidade)
        etQuantidade = view.findViewById(R.id.etQuantidade)
        tilValorUnitario = view.findViewById(R.id.tilValorUnitario)
        etValorUnitario = view.findViewById(R.id.etValorUnitario)
        tilValorFrete = view.findViewById(R.id.tilValorFrete)
        etValorFrete = view.findViewById(R.id.etValorFrete)
        tvValorTotal = view.findViewById(R.id.tvValorTotal)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
        loadingView = view.findViewById(R.id.loadingView)
    }

    private fun setupViewModels() {
        try {
            // ViewModel para equipamentos
            val equipamentosFactory = EquipamentosViewModelFactory()
            equipamentosViewModel = ViewModelProvider(requireActivity(), equipamentosFactory)[EquipamentosViewModel::class.java]
    
            // ViewModel para contratos
            contratosViewModel = ViewModelProvider(requireActivity())[ContratosViewModel::class.java]
        } catch (e: Exception) {
            LogUtils.error("EquipamentoContratoDialog", "Erro ao configurar ViewModels", e)
            Toast.makeText(context, "Erro ao inicializar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        // Listener para seleção de equipamento
        actvEquipamento.setOnItemClickListener { _, _, position, _ ->
            equipamentoSelecionado = equipamentos[position]
            equipamentoSelecionado?.let { equipamento ->
                // Preenche valor unitário com o valor diário do equipamento selecionado
                val valorFormatado = DecimalFormat("0.00", DecimalFormatSymbols(Locale("pt", "BR")))
                etValorUnitario.setText(valorFormatado.format(equipamento.precoDiaria))
                
                // Se for novo, pré-preenche quantidade com 1
                if (equipamentoContratoParaEdicao == null) {
                    etQuantidade.setText("1")
                }
                
                // Recalcula o valor total
                calcularValorTotal()
            }
        }

        // TextWatcher para quantidade
        etQuantidade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorTotal()
            }
        })

        // TextWatcher para valor unitário
        etValorUnitario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorTotal()
            }
        })

        // TextWatcher para valor do frete
        etValorFrete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorTotal()
            }
        })

        // Botão cancelar
        btnCancelar.setOnClickListener {
            dismiss()
        }

        // Botão salvar
        btnSalvar.setOnClickListener {
            if (validarFormulario()) {
                salvarEquipamentoContrato()
            }
        }
    }

    private fun carregarEquipamentos() {
        try {
            equipamentosViewModel.loadEquipamentos()
            equipamentosViewModel.uiState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is UiState.Success -> {
                        equipamentos = state.data
                        
                        // Preenche o dropdown com os equipamentos
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_dropdown_item_1line,
                            equipamentos.map { it.nomeEquip }
                        )
                        actvEquipamento.setAdapter(adapter)
                        
                        // Se estiver editando, seleciona o equipamento no dropdown
                        equipamentoContratoParaEdicao?.let { equipamento ->
                            val equipamentoAtual = equipamentos.find { it.id == equipamento.equipamentoId }
                            equipamentoAtual?.let {
                                equipamentoSelecionado = it
                                actvEquipamento.setText(it.nomeEquip, false)
                            }
                        }
                    }
                    
                    is UiState.Error -> {
                        Toast.makeText(
                            requireContext(),
                            "Erro ao carregar equipamentos: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    
                    else -> {
                        // Estados de loading ou others são tratados pela UI
                    }
                }
            }
        } catch (e: Exception) {
            LogUtils.error("EquipamentoContratoDialog", "Erro ao carregar equipamentos", e)
            Toast.makeText(context, "Erro ao carregar equipamentos: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun preencherFormularioEdicao(equipamentoContrato: EquipamentoContrato) {
        // O equipamento é preenchido pelo observer do carregarEquipamentos()
        
        // Quantidade
        etQuantidade.setText(equipamentoContrato.quantidadeEquip.toString())
        
        // Valor unitário
        val formato = DecimalFormat("0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        etValorUnitario.setText(formato.format(equipamentoContrato.valorUnitario))
        
        // Valor frete
        etValorFrete.setText(formato.format(equipamentoContrato.valorFrete))
        
        // O valor total é calculado automaticamente pelo TextWatcher
    }

    private fun calcularValorTotal() {
        try {
            val quantidade = etQuantidade.text.toString().toIntOrNull() ?: 0
            val valorUnitario = etValorUnitario.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            val valorFrete = etValorFrete.text.toString().replace(",", ".").toDoubleOrNull() ?: 0.0
            
            val valorTotal = (quantidade * valorUnitario) + valorFrete
            
            val formato = DecimalFormat("0.00", DecimalFormatSymbols(Locale("pt", "BR")))
            tvValorTotal.setText(formato.format(valorTotal))
        } catch (e: Exception) {
            LogUtils.error("EquipamentoContratoDialog", "Erro ao calcular valor total", e)
            tvValorTotal.setText("0,00")
        }
    }

    private fun validarFormulario(): Boolean {
        var isValid = true
        
        // Validar equipamento selecionado
        if (equipamentoSelecionado == null) {
            tilEquipamento.error = getString(R.string.campo_obrigatorio)
            isValid = false
        } else {
            tilEquipamento.error = null
        }
        
        // Validar quantidade
        if (etQuantidade.text.isNullOrBlank()) {
            tilQuantidade.error = getString(R.string.campo_obrigatorio)
            isValid = false
        } else {
            val quantidade = etQuantidade.text.toString().toIntOrNull()
            if (quantidade == null || quantidade <= 0) {
                tilQuantidade.error = "Quantidade deve ser maior que zero"
                isValid = false
            } else {
                tilQuantidade.error = null
            }
        }
        
        // Validar valor unitário
        if (etValorUnitario.text.isNullOrBlank()) {
            tilValorUnitario.error = getString(R.string.campo_obrigatorio)
            isValid = false
        } else {
            try {
                val valor = etValorUnitario.text.toString().replace(",", ".").toDouble()
                if (valor <= 0) {
                    tilValorUnitario.error = "Valor deve ser maior que zero"
                    isValid = false
                } else {
                    tilValorUnitario.error = null
                }
            } catch (e: NumberFormatException) {
                tilValorUnitario.error = getString(R.string.valor_invalido)
                isValid = false
            }
        }
        
        // Validar valor do frete (pode ser zero)
        if (etValorFrete.text.isNullOrBlank()) {
            etValorFrete.setText("0,00")
        } else {
            try {
                etValorFrete.text.toString().replace(",", ".").toDouble()
                tilValorFrete.error = null
            } catch (e: NumberFormatException) {
                tilValorFrete.error = getString(R.string.valor_invalido)
                isValid = false
            }
        }
        
        return isValid
    }

    private fun salvarEquipamentoContrato() {
        val quantidade = etQuantidade.text.toString().toInt()
        val valorUnitario = etValorUnitario.text.toString().replace(",", ".").toDouble()
        val valorFrete = etValorFrete.text.toString().replace(",", ".").toDouble()
        val valorTotal = (quantidade * valorUnitario) + valorFrete
        
        // Verificar validade do contratoId
        when {
            contratoId == 0 -> LogUtils.warning("EquipamentoContratoDialog", 
                "Tentativa de salvar equipamento com contratoId inválido: $contratoId")
            
            contratoId < 0 -> LogUtils.debug("EquipamentoContratoDialog", 
                "Salvando equipamento para contrato temporário ID=$contratoId")
            
            else -> LogUtils.debug("EquipamentoContratoDialog", 
                "Salvando equipamento para contrato real ID=$contratoId")
        }
        
        // Usar ID temporário único para novos equipamentos
        val equipamentoId = equipamentoContratoParaEdicao?.id ?: generateTempId()
        
        val equipamentoContrato = EquipamentoContrato(
            id = equipamentoId,
            contratoId = contratoId,
            equipamentoId = equipamentoSelecionado?.id ?: 0,
            quantidadeEquip = quantidade,
            valorUnitario = valorUnitario,
            valorTotal = valorTotal,
            valorFrete = valorFrete,
            equipamentoNome = equipamentoSelecionado?.nomeEquip,
            equipamento = equipamentoSelecionado
        )
        
        LogUtils.debug("EquipamentoContratoDialog", "Salvando equipamento: " +
                "ID=${equipamentoContrato.id}, " +
                "contratoId=${equipamentoContrato.contratoId}, " +
                "equipamentoId=${equipamentoContrato.equipamentoId}, " +
                "nome=${equipamentoContrato.equipamentoNome}, " +
                "quantidade=${equipamentoContrato.quantidadeEquip}, " +
                "valorUnitario=${equipamentoContrato.valorUnitario}, " +
                "valorTotal=${equipamentoContrato.valorTotal}")
        
        onEquipamentoSalvoListener?.invoke(equipamentoContrato)
        dismiss()
    }

    /**
     * Define um listener para quando o equipamento for salvo
     */
    fun setOnEquipamentoSalvoListener(listener: (EquipamentoContrato) -> Unit) {
        this.onEquipamentoSalvoListener = listener
    }
}
