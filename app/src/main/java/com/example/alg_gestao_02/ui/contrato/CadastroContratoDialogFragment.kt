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
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * DialogFragment para cadastro e edição de contrato
 */
class CadastroContratoDialogFragment : DialogFragment() {
    
    private lateinit var viewModel: ContratosViewModel
    
    private lateinit var toolbar: Toolbar
    private lateinit var tilCliente: TextInputLayout
    private lateinit var actvCliente: AutoCompleteTextView
    private lateinit var tilContratoNum: TextInputLayout
    private lateinit var etContratoNum: TextInputEditText
    private lateinit var tilContratoValor: TextInputLayout
    private lateinit var etContratoValor: TextInputEditText
    private lateinit var tilObraLocal: TextInputLayout
    private lateinit var etObraLocal: TextInputEditText
    private lateinit var tilContratoPeriodo: TextInputLayout
    private lateinit var etContratoPeriodo: TextInputEditText
    private lateinit var tilEntregaLocal: TextInputLayout
    private lateinit var etEntregaLocal: TextInputEditText
    private lateinit var tilRespPedido: TextInputLayout
    private lateinit var etRespPedido: TextInputEditText
    private lateinit var tilContratoAss: TextInputLayout
    private lateinit var etContratoAss: TextInputEditText
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var loadingView: View
    
    private var contratoParaEdicao: Contrato? = null
    private var clienteSelecionado: Cliente? = null
    private var onContratoSavedListener: ((Contrato) -> Unit)? = null
    
    companion object {
        private const val ARG_CONTRATO = "arg_contrato"
        
        fun newInstance(contrato: Contrato? = null): CadastroContratoDialogFragment {
            val fragment = CadastroContratoDialogFragment()
            contrato?.let {
                val args = Bundle()
                args.putParcelable(ARG_CONTRATO, it)
                fragment.arguments = args
            }
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        
        // Recupera o contrato para edição, se existir
        contratoParaEdicao = arguments?.getParcelable(ARG_CONTRATO)
        LogUtils.debug("CadastroContratoDialog", "Criando diálogo para ${if (contratoParaEdicao == null) "novo contrato" else "edição"}")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_cadastro_contrato, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupViewModel()
        setupToolbar()
        setupListeners()
        
        // Observar estados e carregamento de dados
        observeViewModel()
        
        // Preencher formulário com dados para edição, se necessário
        preencherFormulario()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tilCliente = view.findViewById(R.id.tilCliente)
        actvCliente = view.findViewById(R.id.actvCliente)
        tilContratoNum = view.findViewById(R.id.tilContratoNum)
        etContratoNum = view.findViewById(R.id.etContratoNum)
        tilContratoValor = view.findViewById(R.id.tilContratoValor)
        etContratoValor = view.findViewById(R.id.etContratoValor)
        tilObraLocal = view.findViewById(R.id.tilObraLocal)
        etObraLocal = view.findViewById(R.id.etObraLocal)
        tilContratoPeriodo = view.findViewById(R.id.tilContratoPeriodo)
        etContratoPeriodo = view.findViewById(R.id.etContratoPeriodo)
        tilEntregaLocal = view.findViewById(R.id.tilEntregaLocal)
        etEntregaLocal = view.findViewById(R.id.etEntregaLocal)
        tilRespPedido = view.findViewById(R.id.tilRespPedido)
        etRespPedido = view.findViewById(R.id.etRespPedido)
        tilContratoAss = view.findViewById(R.id.tilContratoAss)
        etContratoAss = view.findViewById(R.id.etContratoAss)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)
        loadingView = view.findViewById(R.id.loadingView)
    }
    
    private fun setupViewModel() {
        val factory = ContratosViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[ContratosViewModel::class.java]
    }
    
    private fun setupToolbar() {
        toolbar.apply {
            title = if (contratoParaEdicao == null) {
                getString(R.string.title_novo_contrato)
            } else {
                getString(R.string.title_editar_contrato)
            }
            
            setNavigationIcon(R.drawable.ic_close)
            setNavigationOnClickListener {
                dismiss()
            }
        }
    }
    
    private fun setupListeners() {
        // Monitorar seleção de cliente
        actvCliente.setOnItemClickListener { _, _, position, _ ->
            val clientesState = viewModel.clientesState.value
            if (clientesState is UiState.Success) {
                val cliente = clientesState.data[position]
                clienteSelecionado = cliente
                LogUtils.debug("CadastroContratoDialog", "Cliente selecionado: ${cliente.contratante}")
                
                // Gerar próximo número de contrato para o cliente
                lifecycleScope.launch {
                    try {
                        val proximoNum = viewModel.getNextContratoNum(cliente.id)
                        etContratoNum.setText(proximoNum)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Erro ao gerar número do contrato", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        // Validação do valor do contrato
        etContratoValor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                tilContratoValor.error = if (s.isNullOrBlank()) {
                    getString(R.string.campo_obrigatorio)
                } else null
            }
        })
        
        // Validação do local da obra
        etObraLocal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                tilObraLocal.error = if (s.isNullOrBlank()) {
                    getString(R.string.campo_obrigatorio)
                } else null
            }
        })
        
        // Validação do período do contrato
        etContratoPeriodo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                tilContratoPeriodo.error = if (s.isNullOrBlank()) {
                    getString(R.string.campo_obrigatorio)
                } else null
            }
        })
        
        // Validação do local de entrega
        etEntregaLocal.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                tilEntregaLocal.error = if (s.isNullOrBlank()) {
                    getString(R.string.campo_obrigatorio)
                } else null
            }
        })
        
        // Botão Cancelar
        btnCancel.setOnClickListener {
            dismiss()
        }
        
        // Botão Salvar
        btnSave.setOnClickListener {
            if (validarFormulario()) {
                salvarContrato()
            }
        }
    }
    
    private fun observeViewModel() {
        // Observar lista de clientes
        viewModel.clientesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    val clientes = state.data
                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        clientes.map { it.contratante }
                    )
                    actvCliente.setAdapter(adapter)
                    
                    // Se estiver editando, preenche e desabilita o campo de cliente
                    if (contratoParaEdicao != null) {
                        val cliente = clientes.find { it.id == contratoParaEdicao!!.clienteId }
                        cliente?.let {
                            clienteSelecionado = it
                            actvCliente.setText(it.contratante, false)
                            actvCliente.isEnabled = false
                        }
                    }
                }
                
                is UiState.Error -> {
                    Toast.makeText(
                        context,
                        "Erro ao carregar clientes: ${state.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                
                else -> {
                    // Nada a fazer para outros estados
                }
            }
        }
        
        // Observar resultado da operação
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingView.visibility = View.VISIBLE
                    btnSave.isEnabled = false
                }
                
                is UiState.Success -> {
                    loadingView.visibility = View.GONE
                    btnSave.isEnabled = true
                    
                    // Notificar que o contrato foi salvo e fechar o diálogo
                    onContratoSavedListener?.invoke(state.data)
                    Toast.makeText(
                        context,
                        if (contratoParaEdicao == null) {
                            getString(R.string.contrato_criado_sucesso)
                        } else {
                            getString(R.string.contrato_atualizado_sucesso)
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                    dismiss()
                }
                
                is UiState.Error -> {
                    loadingView.visibility = View.GONE
                    btnSave.isEnabled = true
                    
                    Toast.makeText(
                        context,
                        "Erro: ${state.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
                
                else -> {
                    loadingView.visibility = View.GONE
                    btnSave.isEnabled = true
                }
            }
        }
    }
    
    private fun preencherFormulario() {
        contratoParaEdicao?.let { contrato ->
            // O cliente já é configurado no observer de clientesState
            
            // Número do contrato
            etContratoNum.setText(contrato.contratoNum)
            
            // Valor do contrato
            val formato = DecimalFormat("0.00", DecimalFormatSymbols(Locale("pt", "BR")))
            etContratoValor.setText(formato.format(contrato.contratoValor))
            
            // Local da obra
            etObraLocal.setText(contrato.obraLocal)
            
            // Período do contrato
            etContratoPeriodo.setText(contrato.contratoPeriodo)
            
            // Local de entrega
            etEntregaLocal.setText(contrato.entregaLocal)
            
            // Responsável pelo pedido
            etRespPedido.setText(contrato.respPedido ?: "")
            
            // Assinatura do contrato
            etContratoAss.setText(contrato.contratoAss ?: "")
        }
        
        // Se for um novo contrato, configura os campos que são autopreenchidos
        if (contratoParaEdicao == null) {
            // Os campos de data são preenchidos na API
            // O número do contrato é preenchido quando o cliente é selecionado
        }
    }
    
    private fun validarFormulario(): Boolean {
        var valid = true
        
        // Validar cliente selecionado
        if (clienteSelecionado == null && contratoParaEdicao == null) {
            tilCliente.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            tilCliente.error = null
        }
        
        // Validar valor do contrato
        val valorTexto = etContratoValor.text.toString()
        if (valorTexto.isBlank()) {
            tilContratoValor.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            try {
                valorTexto.replace(",", ".").toDouble()
                tilContratoValor.error = null
            } catch (e: NumberFormatException) {
                tilContratoValor.error = getString(R.string.valor_invalido)
                valid = false
            }
        }
        
        // Validar local da obra
        if (etObraLocal.text.toString().isBlank()) {
            tilObraLocal.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            tilObraLocal.error = null
        }
        
        // Validar período do contrato
        if (etContratoPeriodo.text.toString().isBlank()) {
            tilContratoPeriodo.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            tilContratoPeriodo.error = null
        }
        
        // Validar local de entrega
        if (etEntregaLocal.text.toString().isBlank()) {
            tilEntregaLocal.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            tilEntregaLocal.error = null
        }
        
        return valid
    }
    
    private fun salvarContrato() {
        val valorTexto = etContratoValor.text.toString().replace(",", ".")
        val valor = valorTexto.toDoubleOrNull() ?: 0.0
        
        // Obter as datas iniciais
        val (dataHoraEmissao, dataVencimento) = viewModel.getDadosIniciais()
        
        // Criar objeto Contrato
        val contrato = Contrato(
            id = contratoParaEdicao?.id ?: 0,
            clienteId = contratoParaEdicao?.clienteId ?: clienteSelecionado!!.id,
            contratoNum = etContratoNum.text.toString(),
            dataHoraEmissao = contratoParaEdicao?.dataHoraEmissao ?: dataHoraEmissao,
            dataVenc = contratoParaEdicao?.dataVenc ?: dataVencimento,
            contratoValor = valor,
            obraLocal = etObraLocal.text.toString(),
            contratoPeriodo = etContratoPeriodo.text.toString(),
            entregaLocal = etEntregaLocal.text.toString(),
            respPedido = etRespPedido.text.toString().ifBlank { null },
            contratoAss = etContratoAss.text.toString().ifBlank { null },
            clienteNome = clienteSelecionado?.contratante
        )
        
        // Salvar contrato
        if (contratoParaEdicao == null) {
            viewModel.criarContrato(contrato)
        } else {
            viewModel.atualizarContrato(contratoParaEdicao!!.id, contrato)
        }
    }
    
    /**
     * Define um listener para ser chamado quando o contrato for salvo
     */
    fun setOnContratoSavedListener(listener: (Contrato) -> Unit) {
        this.onContratoSavedListener = listener
    }
} 