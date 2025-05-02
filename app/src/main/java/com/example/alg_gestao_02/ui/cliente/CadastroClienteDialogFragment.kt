package com.example.alg_gestao_02.ui.cliente

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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.ui.cliente.viewmodel.ClientesViewModel
import com.example.alg_gestao_02.ui.cliente.viewmodel.ClientesViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.ViaCepUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

/**
 * DialogFragment para cadastro e edição de cliente
 */
class CadastroClienteDialogFragment : DialogFragment() {
    
    private lateinit var viewModel: ClientesViewModel
    
    // Views
    private lateinit var toolbar: Toolbar
    private lateinit var tilContratante: TextInputLayout
    private lateinit var etContratante: TextInputEditText
    private lateinit var tilTipoDocumento: TextInputLayout
    private lateinit var actvTipoDocumento: AutoCompleteTextView
    private lateinit var tilCpfCnpj: TextInputLayout
    private lateinit var etCpfCnpj: TextInputEditText
    private lateinit var tilRgIe: TextInputLayout
    private lateinit var etRgIe: TextInputEditText
    private lateinit var tilEndereco: TextInputLayout
    private lateinit var etEndereco: TextInputEditText
    private lateinit var tilBairro: TextInputLayout
    private lateinit var etBairro: TextInputEditText
    private lateinit var tilCep: TextInputLayout
    private lateinit var etCep: TextInputEditText
    private lateinit var tilCidade: TextInputLayout
    private lateinit var etCidade: TextInputEditText
    private lateinit var tilEstado: TextInputLayout
    private lateinit var etEstado: TextInputEditText
    private lateinit var tilTelefone: TextInputLayout
    private lateinit var etTelefone: TextInputEditText
    private lateinit var btnCancelar: Button
    private lateinit var btnSalvar: Button
    
    // Cliente para edição (nulo para novo cliente)
    private var clienteParaEdicao: Cliente? = null
    
    // Tipo de documento selecionado atualmente
    private var tipoDocumentoSelecionado: String = "CPF"
    
    // Flag para evitar consulta duplicada de CEP
    private var buscandoCep: Boolean = false
    private var ultimoCepConsultado: String = ""
    
    // Callback para quando um cliente for salvo
    private var onClienteSavedListener: ((Cliente) -> Unit)? = null
    
    // Controle de jobs em andamento
    private var consultaCepJob: kotlinx.coroutines.Job? = null
    
    companion object {
        private const val ARG_CLIENTE = "arg_cliente"
        
        fun newInstance(): CadastroClienteDialogFragment {
            return CadastroClienteDialogFragment()
        }
        
        fun newInstance(cliente: Cliente): CadastroClienteDialogFragment {
            val fragment = CadastroClienteDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_CLIENTE, cliente)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        
        clienteParaEdicao = arguments?.getParcelable(ARG_CLIENTE)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_cadastro_cliente, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupViewModel()
        setupTipoDocumentoDropdown()
        setupTextInputMasks()
        setupCepConsulta()
        setupToolbar()
        setupListeners()
        
        // Preenche os campos se for edição
        preencherCampos()
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tilContratante = view.findViewById(R.id.tilContratante)
        etContratante = view.findViewById(R.id.etContratante)
        tilTipoDocumento = view.findViewById(R.id.tilTipoDocumento)
        actvTipoDocumento = view.findViewById(R.id.actvTipoDocumento)
        tilCpfCnpj = view.findViewById(R.id.tilCpfCnpj)
        etCpfCnpj = view.findViewById(R.id.etCpfCnpj)
        tilRgIe = view.findViewById(R.id.tilRgIe)
        etRgIe = view.findViewById(R.id.etRgIe)
        tilEndereco = view.findViewById(R.id.tilEndereco)
        etEndereco = view.findViewById(R.id.etEndereco)
        tilBairro = view.findViewById(R.id.tilBairro)
        etBairro = view.findViewById(R.id.etBairro)
        tilCep = view.findViewById(R.id.tilCep)
        etCep = view.findViewById(R.id.etCep)
        tilCidade = view.findViewById(R.id.tilCidade)
        etCidade = view.findViewById(R.id.etCidade)
        tilEstado = view.findViewById(R.id.tilEstado)
        etEstado = view.findViewById(R.id.etEstado)
        tilTelefone = view.findViewById(R.id.tilTelefone)
        etTelefone = view.findViewById(R.id.etTelefone)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
    }
    
    private fun setupViewModel() {
        val factory = ClientesViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[ClientesViewModel::class.java]
        
        // Observar estado da operação
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Mostrar loading se necessário
                }
                is UiState.Success -> {
                    val mensagem = if (clienteParaEdicao == null) {
                        "Cliente cadastrado com sucesso"
                    } else {
                        "Cliente atualizado com sucesso"
                    }
                    Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
                    
                    // Notifica o listener
                    onClienteSavedListener?.invoke(state.data)
                    
                    dismiss()
                }
                is UiState.Error -> {
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }
    
    private fun setupTipoDocumentoDropdown() {
        // Configurar adapter para o dropdown de tipo de documento
        val tiposDocumento = resources.getStringArray(R.array.tipos_documento)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, tiposDocumento)
        actvTipoDocumento.setAdapter(adapter)
        
        // Listener para alterações no tipo de documento
        actvTipoDocumento.setOnItemClickListener { _, _, position, _ ->
            tipoDocumentoSelecionado = tiposDocumento[position]
            atualizarCamposDocumento()
        }
    }
    
    private fun setupTextInputMasks() {
        // Adicionar máscara para CPF/CNPJ
        etCpfCnpj.addTextChangedListener(object : TextWatcher {
            private var anterior = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().replace("[^\\d]".toRegex(), "")
                
                if (str == anterior || str.isEmpty()) return
                
                anterior = str
                
                val resultado = if (tipoDocumentoSelecionado == "CPF") {
                    formatarCpf(str)
                } else {
                    formatarCnpj(str)
                }
                
                etCpfCnpj.removeTextChangedListener(this)
                etCpfCnpj.setText(resultado)
                etCpfCnpj.setSelection(resultado.length)
                etCpfCnpj.addTextChangedListener(this)
            }
        })
        
        // Adicionar máscara para CEP
        etCep.addTextChangedListener(object : TextWatcher {
            private var anterior = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString().replace("[^\\d]".toRegex(), "")
                
                if (str == anterior || str.isEmpty()) return
                
                anterior = str
                
                val resultado = formatarCep(str)
                
                etCep.removeTextChangedListener(this)
                etCep.setText(resultado)
                etCep.setSelection(resultado.length)
                etCep.addTextChangedListener(this)
            }
        })
    }
    
    private fun formatarCep(str: String): String {
        if (str.length > 8) return str
        
        return if (str.length > 5) {
            "${str.substring(0, 5)}-${str.substring(5)}"
        } else {
            str
        }
    }
    
    /**
     * Configura a consulta automática de endereço por CEP
     */
    private fun setupCepConsulta() {
        // Configurar o botão de buscar CEP
        tilCep.setEndIconOnClickListener {
            val cep = etCep.text.toString().replace("[^\\d]".toRegex(), "")
            if (cep.length == 8) {
                consultarCep(cep)
            } else {
                Toast.makeText(requireContext(), "CEP deve conter 8 dígitos", Toast.LENGTH_SHORT).show()
            }
        }

        // Listener para fazer a consulta quando o CEP estiver completo
        etCep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val cep = s.toString().replace("[^\\d]".toRegex(), "")
                
                // Só consulta se tiver 8 dígitos e não estiver já consultando um CEP
                if (cep.length == 8 && !buscandoCep && cep != ultimoCepConsultado) {
                    consultarCep(cep)
                }
            }
        })
    }
    
    /**
     * Consulta o endereço pelo CEP e preenche os campos automaticamente
     */
    private fun consultarCep(cep: String) {
        // Cancelar qualquer consulta anterior
        consultaCepJob?.cancel()
        
        // Atualiza estado
        buscandoCep = true
        ultimoCepConsultado = cep
        
        // Adiciona mensagem de carregando
        tilCep.helperText = "Buscando endereço..."
        tilCep.isHelperTextEnabled = true
        
        consultaCepJob = lifecycleScope.launch {
            try {
                // Faz a consulta
                val endereco = ViaCepUtils.consultarCep(cep)
                
                // Verifica se o Fragment ainda está anexado
                if (!isAdded) {
                    return@launch
                }
                
                // Preenche os campos
                etEndereco.setText(endereco.logradouro)
                etBairro.setText(endereco.bairro)
                etCidade.setText(endereco.localidade)
                etEstado.setText(endereco.uf)
                
                // Verifica novamente se o Fragment está anexado antes de usar o contexto
                if (!isAdded) {
                    return@launch
                }
                
                // Remove mensagem de carregando e adiciona mensagem de sucesso
                tilCep.helperText = "Endereço encontrado"
                context?.let { ctx ->
                    tilCep.setHelperTextColor(ResourcesCompat.getColorStateList(
                        ctx.resources,
                        android.R.color.holo_green_dark,
                        ctx.theme
                    ))
                }
                
                // Foca no próximo campo que precisa de atenção
                if (endereco.logradouro.isBlank()) {
                    etEndereco.requestFocus()
                } else if (endereco.bairro.isBlank()) {
                    etBairro.requestFocus()
                } else {
                    etTelefone.requestFocus()
                }
                
                // Limpa erros relacionados ao endereço
                tilEndereco.error = null
                tilBairro.error = null
                tilCidade.error = null
                tilEstado.error = null
                
            } catch (e: Exception) {
                // Verifica se o Fragment ainda está anexado
                if (!isAdded) {
                    return@launch
                }
                
                // Em caso de erro, mostra uma mensagem
                LogUtils.error("CadastroClienteDialog", "Erro ao consultar CEP: ${e.message}")
                tilCep.helperText = "CEP não encontrado"
                context?.let { ctx ->
                    tilCep.setHelperTextColor(ResourcesCompat.getColorStateList(
                        ctx.resources,
                        android.R.color.holo_red_dark,
                        ctx.theme
                    ))
                }
            } finally {
                // Atualiza estado
                buscandoCep = false
                consultaCepJob = null
            }
        }
    }
    
    private fun formatarCpf(str: String): String {
        val sb = StringBuilder()
        var i = 0
        
        for (c in str) {
            if (i == 3 || i == 6) {
                sb.append('.')
            } else if (i == 9) {
                sb.append('-')
            }
            
            sb.append(c)
            i++
            
            if (i >= 11) break
        }
        
        return sb.toString()
    }
    
    private fun formatarCnpj(str: String): String {
        val sb = StringBuilder()
        var i = 0
        
        for (c in str) {
            if (i == 2 || i == 5) {
                sb.append('.')
            } else if (i == 8) {
                sb.append('/')
            } else if (i == 12) {
                sb.append('-')
            }
            
            sb.append(c)
            i++
            
            if (i >= 14) break
        }
        
        return sb.toString()
    }
    
    private fun atualizarCamposDocumento() {
        // Atualizar o hint baseado no tipo de documento
        if (tipoDocumentoSelecionado == "CPF") {
            tilCpfCnpj.hint = "CPF"
            tilRgIe.hint = "RG (opcional)"
        } else {
            tilCpfCnpj.hint = "CNPJ"
            tilRgIe.hint = "IE (opcional)"
        }
        
        // Limpar o campo de CPF/CNPJ para aplicar a formatação correta
        val textoAtual = etCpfCnpj.text.toString()
        if (textoAtual.isNotEmpty()) {
            // Manter apenas os números e reformatar
            val soNumeros = textoAtual.replace("[^\\d]".toRegex(), "")
            etCpfCnpj.setText("")  // Limpar para evitar loops de formatação
            etCpfCnpj.setText(soNumeros)  // Aplicar a formatação
        }
    }
    
    private fun setupToolbar() {
        toolbar.title = if (clienteParaEdicao == null) {
            "Novo Cliente"
        } else {
            "Editar Cliente"
        }
        
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }
    
    private fun setupListeners() {
        btnCancelar.setOnClickListener {
            dismiss()
        }
        
        btnSalvar.setOnClickListener {
            if (validarCampos()) {
                salvarCliente()
            }
        }
    }
    
    private fun preencherCampos() {
        clienteParaEdicao?.let { cliente ->
            etContratante.setText(cliente.contratante)
            
            // Determinar o tipo de documento (CPF ou CNPJ)
            tipoDocumentoSelecionado = if (cliente.isPessoaFisica()) "CPF" else "CNPJ"
            actvTipoDocumento.setText(tipoDocumentoSelecionado, false)
            atualizarCamposDocumento()
            
            // Formatar o documento conforme o tipo
            etCpfCnpj.setText(cliente.cpfCnpj)
            etRgIe.setText(cliente.rgIe)
            etEndereco.setText(cliente.endereco)
            etBairro.setText(cliente.bairro)
            etCep.setText(cliente.cep)
            etCidade.setText(cliente.cidade)
            etEstado.setText(cliente.estado)
            etTelefone.setText(cliente.telefone)
        }
    }
    
    private fun validarCampos(): Boolean {
        var isValid = true
        
        // Validar contratante
        if (etContratante.text.isNullOrBlank()) {
            tilContratante.error = "Nome do contratante é obrigatório"
            isValid = false
        } else {
            tilContratante.error = null
        }
        
        // Validar CPF/CNPJ
        if (etCpfCnpj.text.isNullOrBlank()) {
            tilCpfCnpj.error = "${tipoDocumentoSelecionado} é obrigatório"
            isValid = false
        } else {
            val documento = etCpfCnpj.text.toString().replace("[^\\d]".toRegex(), "")
            val tamanhoEsperado = if (tipoDocumentoSelecionado == "CPF") 11 else 14
            
            if (documento.length != tamanhoEsperado) {
                tilCpfCnpj.error = "${tipoDocumentoSelecionado} incompleto"
                isValid = false
            } else {
                tilCpfCnpj.error = null
            }
        }
        
        // Não validamos RG/IE pois não é obrigatório
        
        // Validar endereço
        if (etEndereco.text.isNullOrBlank()) {
            tilEndereco.error = "Endereço é obrigatório"
            isValid = false
        } else {
            tilEndereco.error = null
        }
        
        // Validar bairro
        if (etBairro.text.isNullOrBlank()) {
            tilBairro.error = "Bairro é obrigatório"
            isValid = false
        } else {
            tilBairro.error = null
        }
        
        // Validar cidade
        if (etCidade.text.isNullOrBlank()) {
            tilCidade.error = "Cidade é obrigatória"
            isValid = false
        } else {
            tilCidade.error = null
        }
        
        // Validar estado
        if (etEstado.text.isNullOrBlank()) {
            tilEstado.error = "Estado é obrigatório"
            isValid = false
        } else if (etEstado.text.toString().length != 2) {
            tilEstado.error = "Estado deve ter 2 letras (UF)"
            isValid = false
        } else {
            tilEstado.error = null
        }
        
        return isValid
    }
    
    private fun salvarCliente() {
        val cliente = Cliente(
            id = clienteParaEdicao?.id ?: 0,
            contratante = etContratante.text.toString().trim(),
            cpfCnpj = etCpfCnpj.text.toString().trim(),
            rgIe = etRgIe.text.toString().trim().takeIf { it.isNotBlank() },
            endereco = etEndereco.text.toString().trim(),
            bairro = etBairro.text.toString().trim(),
            cep = etCep.text.toString().trim().takeIf { it.isNotBlank() },
            cidade = etCidade.text.toString().trim(),
            estado = etEstado.text.toString().trim().uppercase(),
            telefone = etTelefone.text.toString().trim().takeIf { it.isNotBlank() }
        )
        
        LogUtils.debug("CadastroClienteDialog", "Salvando cliente: ${cliente.contratante}")
        
        if (clienteParaEdicao == null) {
            viewModel.criarCliente(cliente)
        } else {
            viewModel.atualizarCliente(clienteParaEdicao!!.id, cliente)
        }
    }
    
    /**
     * Define o callback para quando um cliente for salvo
     */
    fun setOnClienteSavedListener(listener: (Cliente) -> Unit) {
        this.onClienteSavedListener = listener
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cancela qualquer consulta de CEP pendente
        consultaCepJob?.cancel()
    }
} 