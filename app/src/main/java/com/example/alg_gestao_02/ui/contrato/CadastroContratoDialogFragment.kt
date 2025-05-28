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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.data.models.temContratoIdTemporario
import com.example.alg_gestao_02.data.models.temContratoIdValido
import com.example.alg_gestao_02.ui.contrato.adapter.EquipamentosContratoAdapter
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var tilClienteSearch: TextInputLayout
    private lateinit var etClienteSearch: TextInputEditText
    private lateinit var tilCliente: TextInputLayout
    private lateinit var actvCliente: AutoCompleteTextView
    private lateinit var tilContratoNum: TextInputLayout
    private lateinit var etContratoNum: TextInputEditText
    private lateinit var tilObraLocal: TextInputLayout
    private lateinit var etObraLocal: TextInputEditText
    private lateinit var tilContratoPeriodo: TextInputLayout
    private lateinit var actvContratoPeriodo: AutoCompleteTextView
    private lateinit var tilEntregaLocal: TextInputLayout
    private lateinit var etEntregaLocal: TextInputEditText
    private lateinit var tilRespPedido: TextInputLayout
    private lateinit var etRespPedido: TextInputEditText
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var loadingView: View
    private lateinit var tvTituloEquipamentos: TextView
    private lateinit var btnAddEquipamento: MaterialButton
    private lateinit var rvEquipamentos: RecyclerView
    private lateinit var tvEmptyEquipamentos: TextView
    private lateinit var tvValorTotalContratoCalculado: TextView
    
    private var contratoParaEdicao: Contrato? = null
    private var clienteSelecionado: Cliente? = null
    private var clientes: List<Cliente> = emptyList()
    private var clientesFiltrados: List<Cliente> = emptyList()
    private var onContratoSavedListener: ((Contrato) -> Unit)? = null
    private var equipamentosContrato: MutableList<EquipamentoContrato> = mutableListOf()
    private lateinit var equipamentosAdapter: EquipamentosContratoAdapter
    
    companion object {
        private const val ARG_CONTRATO = "arg_contrato"
        
        // Opções para o período do contrato
        private val PERIODOS_CONTRATO = listOf("DIARIA", "MENSAL", "QUINZENAL", "ANUAL")
        
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
        setupPeriodoDropdown()
        setupListeners()
        setupRecyclerView()
        
        // Usar lifecycleScope para evitar bloqueio da UI
        lifecycleScope.launch {
            // Observar estados e carregamento de dados
            observeViewModel()
            
            // Preencher formulário com dados para edição, se necessário
            preencherFormulario()
        }
    }
    
    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        tilClienteSearch = view.findViewById(R.id.tilClienteSearch)
        etClienteSearch = view.findViewById(R.id.etClienteSearch)
        tilCliente = view.findViewById(R.id.tilCliente)
        actvCliente = view.findViewById(R.id.actvCliente)
        tilContratoNum = view.findViewById(R.id.tilContratoNum)
        etContratoNum = view.findViewById(R.id.etContratoNum)
        tilObraLocal = view.findViewById(R.id.tilObraLocal)
        etObraLocal = view.findViewById(R.id.etObraLocal)
        tilContratoPeriodo = view.findViewById(R.id.tilContratoPeriodo)
        actvContratoPeriodo = view.findViewById(R.id.actvContratoPeriodo)
        tilEntregaLocal = view.findViewById(R.id.tilEntregaLocal)
        etEntregaLocal = view.findViewById(R.id.etEntregaLocal)
        tilRespPedido = view.findViewById(R.id.tilRespPedido)
        etRespPedido = view.findViewById(R.id.etRespPedido)
        btnCancel = view.findViewById(R.id.btnCancel)
        btnSave = view.findViewById(R.id.btnSave)
        loadingView = view.findViewById(R.id.loadingView)
        tvTituloEquipamentos = view.findViewById(R.id.tvTituloEquipamentos)
        btnAddEquipamento = view.findViewById(R.id.btnAddEquipamento)
        rvEquipamentos = view.findViewById(R.id.rvEquipamentos)
        tvEmptyEquipamentos = view.findViewById(R.id.tvEmptyEquipamentos)
        tvValorTotalContratoCalculado = view.findViewById(R.id.tvValorTotalContratoCalculado)
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
    
    private fun setupPeriodoDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PERIODOS_CONTRATO
        )
        actvContratoPeriodo.setAdapter(adapter)
    }
    
    private fun setupListeners() {
        // Configurar pesquisa de cliente
        etClienteSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                filtrarClientes(s?.toString() ?: "")
            }
        })
        
        // Monitorar seleção de cliente
        actvCliente.setOnItemClickListener { _, _, position, _ ->
            if (clientesFiltrados.isNotEmpty() && position < clientesFiltrados.size) {
                val cliente = clientesFiltrados[position]
                clienteSelecionado = cliente
                LogUtils.debug("CadastroContratoDialog", "Cliente selecionado: ${cliente.contratante}")
                
                // Preencher local de entrega com endereço do cliente
                preencherEnderecoEntrega(cliente)
                
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
        
        // Monitorar seleção de período do contrato
        actvContratoPeriodo.setOnItemClickListener { _, _, _, _ ->
            tilContratoPeriodo.error = null
        }
        
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
        
        // Botão Adicionar Equipamento
        btnAddEquipamento.setOnClickListener {
            if (contratoParaEdicao == null && clienteSelecionado == null) {
                Toast.makeText(context, "Selecione um cliente antes de adicionar equipamentos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val contratoId = contratoParaEdicao?.id ?: -1
            abrirDialogoEquipamento(contratoId = contratoId, equipamentoContrato = null)
        }
    }
    
    private fun preencherEnderecoEntrega(cliente: Cliente) {
        // Formatar o endereço do cliente para o campo de entrega
        val enderecoFormatado = "${cliente.endereco}, ${cliente.bairro}, ${cliente.cidade}/${cliente.estado}"
        etEntregaLocal.setText(enderecoFormatado)
    }
    
    private fun filtrarClientes(termo: String) {
        if (termo.isEmpty()) {
            clientesFiltrados = clientes
        } else {
            clientesFiltrados = clientes.filter {
                it.contratante.contains(termo, ignoreCase = true)
            }
        }
        
        // Atualizar o adapter do dropdown com os clientes filtrados
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            clientesFiltrados.map { it.contratante }
        )
        actvCliente.setAdapter(adapter)
        
        // Mostrar o dropdown se houver resultados
        if (clientesFiltrados.isNotEmpty() && termo.isNotEmpty()) {
            actvCliente.showDropDown()
        }
    }
    
    private fun observeViewModel() {
        // Observar lista de clientes
        viewModel.clientesState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    clientes = state.data
                    clientesFiltrados = clientes
                    
                    // Se estiver editando, preenche e desabilita o campo de cliente
                    if (contratoParaEdicao != null) {
                        val cliente = clientes.find { it.id == contratoParaEdicao!!.clienteId }
                        cliente?.let {
                            clienteSelecionado = it
                            actvCliente.setText(it.contratante, false)
                            preencherEnderecoEntrega(it)
                            etClienteSearch.setText(it.contratante)
                            
                            // Desabilita campos se estiver editando
                            etClienteSearch.isEnabled = false
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
                    
                    // Resetar o estado para evitar problemas de reuso
                    viewModel.resetOperationState()
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
        
        // Adicionado para observar mudanças nos equipamentos e atualizar o valor total
        viewModel.equipamentosContratoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    equipamentosContrato.clear()
                    equipamentosContrato.addAll(state.data)
                    equipamentosAdapter.updateEquipamentos(equipamentosContrato)
                    atualizarDisplayValorTotalContrato()
                    if (equipamentosContrato.isEmpty()) {
                        tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                        tvEmptyEquipamentos.visibility = View.VISIBLE
                    } else {
                        tvEmptyEquipamentos.visibility = View.GONE
                    }
                }
                is UiState.Error -> {
                    atualizarDisplayValorTotalContrato()
                    Toast.makeText(
                        context,
                        "Erro ao carregar equipamentos: ${state.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is UiState.Loading -> {
                    tvEmptyEquipamentos.text = "Carregando equipamentos..."
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                }
                else -> {
                    atualizarDisplayValorTotalContrato()
                    tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                }
            }
        }
    }
    
    private fun preencherFormulario() {
        contratoParaEdicao?.let { contrato ->
            // Preencher dados do cliente
            contrato.cliente?.let { cliente ->
                clienteSelecionado = cliente
                actvCliente.setText(cliente.contratante, false)
            }
            
            // Preencher outros campos
            etContratoNum.setText(contrato.contratoNum)
            etObraLocal.setText(contrato.obraLocal)
            actvContratoPeriodo.setText(contrato.contratoPeriodo, false)
            etEntregaLocal.setText(contrato.entregaLocal)
            etRespPedido.setText(contrato.respPedido)
            
            // Preencher equipamentos
            equipamentosContrato.clear()
            equipamentosContrato.addAll(contrato.equipamentosParaExibicao)
            equipamentosAdapter.notifyDataSetChanged()
            atualizarVisibilidadeListaEquipamentos()
            atualizarValorTotal()
        }
    }
    
    private fun atualizarDisplayValorTotalContrato() {
        val valorTotal = equipamentosContrato.sumOf { it.valorTotal ?: 0.0 }
        val formatoMoeda = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        tvValorTotalContratoCalculado.text = "Valor Total: ${formatoMoeda.format(valorTotal)}"
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
        
        // Validar local da obra
        if (etObraLocal.text.toString().isBlank()) {
            tilObraLocal.error = getString(R.string.campo_obrigatorio)
            valid = false
        } else {
            tilObraLocal.error = null
        }
        
        // Validar período do contrato
        if (actvContratoPeriodo.text.toString().isBlank()) {
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
    
    private fun setupRecyclerView() {
        // Configurar adapter para a lista de equipamentos
        equipamentosAdapter = EquipamentosContratoAdapter(
            equipamentos = equipamentosContrato,
            onEditClick = { equipamentoContrato ->
                editarEquipamento(equipamentoContrato)
            },
            onDeleteClick = { equipamentoContrato ->
                removerEquipamento(equipamentoContrato)
            }
        )
        
        rvEquipamentos.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = equipamentosAdapter
        }
        
            // Mostrar seção de equipamentos para edição e criação de novo contrato
            tvTituloEquipamentos.visibility = View.VISIBLE
            btnAddEquipamento.visibility = View.VISIBLE
            rvEquipamentos.visibility = View.VISIBLE
            tvEmptyEquipamentos.visibility = View.VISIBLE
            
            if (contratoParaEdicao != null) {
                // Para edição, carregar equipamentos existentes
                carregarEquipamentosDoContrato(contratoParaEdicao!!.id)
            } else {
                // Para novo contrato, apenas exibir mensagem
                tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                equipamentosContrato.clear()
                equipamentosAdapter.updateEquipamentos(equipamentosContrato)
                atualizarDisplayValorTotalContrato()
            }
    }

    private fun carregarEquipamentosDoContrato(contratoId: Int) {
        // Mostrar um indicador de carregamento
        tvEmptyEquipamentos.text = "Carregando equipamentos..."
        tvEmptyEquipamentos.visibility = View.VISIBLE
        
        // Usar lifecycleScope para carregar os equipamentos em segundo plano
        lifecycleScope.launch {
            viewModel.getEquipamentosContrato(contratoId)
        }
        
        // Observar o resultado
        viewModel.equipamentosContratoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Success -> {
                    equipamentosContrato.clear()
                    equipamentosContrato.addAll(state.data)
                    equipamentosAdapter.updateEquipamentos(equipamentosContrato)
                    atualizarDisplayValorTotalContrato()
                    
                    // Atualizar visibilidade e texto
                    if (equipamentosContrato.isEmpty()) {
                        tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                        tvEmptyEquipamentos.visibility = View.VISIBLE
                    } else {
                        tvEmptyEquipamentos.visibility = View.GONE
                    }
                    
                    // Mostrar seção de equipamentos
                    tvTituloEquipamentos.visibility = View.VISIBLE
                    btnAddEquipamento.visibility = View.VISIBLE
                    rvEquipamentos.visibility = View.VISIBLE
                }
                is UiState.Error -> {
                    // Mostrar erro e botão de adicionar
                    tvEmptyEquipamentos.text = "Erro ao carregar equipamentos: ${state.message}"
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                    atualizarDisplayValorTotalContrato()
                    
                    tvTituloEquipamentos.visibility = View.VISIBLE
                    btnAddEquipamento.visibility = View.VISIBLE
                    rvEquipamentos.visibility = View.VISIBLE
                }
                is UiState.Loading -> {
                    tvEmptyEquipamentos.text = "Carregando equipamentos..."
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                }
                else -> {
                    // Estado vazio ou outro
                    tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                    
                    tvTituloEquipamentos.visibility = View.VISIBLE
                    btnAddEquipamento.visibility = View.VISIBLE
                    rvEquipamentos.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun abrirDialogoEquipamento(contratoId: Int, equipamentoContrato: EquipamentoContrato? = null) {
        val idUsado = if (contratoId <= 0) {
            contratoParaEdicao?.id ?: -1
        } else {
            contratoId
        }
        
        val equipamentoCorrigido = equipamentoContrato?.let {
            if (it.contratoId != idUsado && 
                (it.contratoId <= 0 || idUsado > 0) &&
                !(it.contratoId > 0 && idUsado <= 0)) {
                LogUtils.debug("CadastroContratoDialog", 
                    "Corrigindo contratoId do equipamento ${it.id} de ${it.contratoId} para $contratoId")
                it.copy(contratoId = contratoId)
            } else {
                it
            }
        }
        
        LogUtils.debug("CadastroContratoDialog", 
            "Abrindo diálogo de equipamento - contratoId usado: $idUsado, equipamentoId: ${equipamentoCorrigido?.id ?: "novo"}")
        
        val dialog = EquipamentoContratoDialogFragment.newInstance(
            idUsado,
            equipamentoCorrigido
        )
        
        dialog.setOnEquipamentoSalvoListener { novoEquipamento ->
            val equipamentoComIdCorreto = if (contratoParaEdicao != null) {
                val idReal = contratoParaEdicao!!.id
                if (novoEquipamento.contratoId != idReal && idReal > 0) {
                    LogUtils.debug("CadastroContratoDialog", 
                        "Corrigindo contratoId do equipamento retornado de ${novoEquipamento.contratoId} para $idReal")
                    novoEquipamento.copy(contratoId = idReal)
                } else {
                    novoEquipamento
                }
            } else {
                if (novoEquipamento.contratoId != idUsado && idUsado != 0) {
                    novoEquipamento.copy(contratoId = idUsado)
                } else {
                    novoEquipamento
                }
            }
            
            val index = equipamentosContrato.indexOfFirst { it.id == equipamentoComIdCorreto.id }
            if (index >= 0) {
                equipamentosContrato[index] = equipamentoComIdCorreto
            } else {
                equipamentosContrato.add(equipamentoComIdCorreto)
            }
            
            equipamentosAdapter.updateEquipamentos(equipamentosContrato)
            atualizarDisplayValorTotalContrato()
            
            if (equipamentosContrato.isEmpty()) {
                tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                tvEmptyEquipamentos.visibility = View.VISIBLE
            } else {
                tvEmptyEquipamentos.visibility = View.GONE
            }
            
            if (tvTituloEquipamentos.visibility != View.VISIBLE) {
                tvTituloEquipamentos.visibility = View.VISIBLE
                btnAddEquipamento.visibility = View.VISIBLE
                rvEquipamentos.visibility = View.VISIBLE
            }
            
            LogUtils.debug("CadastroContratoDialog", "Equipamento adicionado/editado: " +
                    "ID=${equipamentoComIdCorreto.id}, " +
                    "contratoId=${equipamentoComIdCorreto.contratoId}, " +
                    "equipamentoId=${equipamentoComIdCorreto.equipamentoId}, " +
                    "nome=${equipamentoComIdCorreto.equipamentoNome}, " +
                    "valorTotal=${equipamentoComIdCorreto.valorTotal}")
        }
        
        dialog.show(childFragmentManager, "equipamento_contrato_dialog")
    }

    private fun editarEquipamento(equipamentoContrato: EquipamentoContrato) {
        val contratoId = equipamentoContrato.contratoId
        abrirDialogoEquipamento(contratoId = contratoId, equipamentoContrato = equipamentoContrato)
    }

    private fun removerEquipamento(equipamentoContrato: EquipamentoContrato) {
        // Confirmar a remoção
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle("Remover Equipamento")
            .setMessage("Deseja realmente remover este equipamento do contrato?")
            .setPositiveButton("Remover") { _, _ ->
                equipamentosContrato.remove(equipamentoContrato)
                equipamentosAdapter.updateEquipamentos(equipamentosContrato)
                atualizarDisplayValorTotalContrato()
                
                if (equipamentosContrato.isEmpty()) {
                    tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
                    tvEmptyEquipamentos.visibility = View.VISIBLE
                } else {
                    tvEmptyEquipamentos.visibility = View.GONE
                }
                
                // Se for um contrato existente, salvar as alterações
                if (contratoParaEdicao != null) {
                    salvarContrato()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun salvarContrato() {
        val clienteId = clienteSelecionado?.id ?: contratoParaEdicao?.clienteId ?: 0
        val contratoNum = etContratoNum.text.toString()
        val contratoValorCalculado = equipamentosContrato.sumOf { it.valorTotal ?: 0.0 }
        val obraLocal = etObraLocal.text.toString()
        val contratoPeriodo = actvContratoPeriodo.text.toString()
        val entregaLocal = etEntregaLocal.text.toString()
        val respPedido = etRespPedido.text.toString().takeIf { it.isNotBlank() }
        
        // Atualizar o contratoId dos equipamentos para garantir que eles sejam associados ao contrato correto
        val contratoIdAtual = contratoParaEdicao?.id ?: 0
        LogUtils.debug("CadastroContratoDialog", 
            "Preparando para salvar contrato: id=$contratoIdAtual, " +
            "clienteId=$clienteId, " +
            "equipamentos=${equipamentosContrato.size}")
        
        // Log detalhado dos IDs dos equipamentos
        LogUtils.debug("CadastroContratoDialog", "Verificando equipamentos para salvar:")
        equipamentosContrato.forEachIndexed { index, equipamento ->
            LogUtils.debug("CadastroContratoDialog", 
                "Equipamento $index - ID: ${equipamento.id}, " +
                "ContratoID: ${equipamento.contratoId}, " +
                "EquipID: ${equipamento.equipamentoId}, " +
                "Nome: ${equipamento.equipamentoNome}, " +
                "Quantidade: ${equipamento.quantidadeEquip}")
        }
        
        // Criar cópias dos equipamentos com os IDs corretos
        val equipamentosAtualizados = equipamentosContrato.map { equipamento ->
            when {
                // Contrato já existe (ID positivo) - garantir que todos os equipamentos usem esse ID
                contratoIdAtual > 0 -> {
                    if (equipamento.contratoId != contratoIdAtual) {
                        LogUtils.debug("CadastroContratoDialog", 
                            "Atualizando equipamento ${equipamento.id} contratoId de ${equipamento.contratoId} para $contratoIdAtual")
                        equipamento.copy(contratoId = contratoIdAtual)
                    } else {
                        equipamento
                    }
                }
                
                // Contrato novo mas equipamento já tem ID temporário - manter
                equipamento.contratoId < 0 -> {
                    equipamento
                }
                
                // Contrato novo sem ID temporário definido - atribuir ID temporário
                else -> {
                    val tempId = if (equipamento.contratoId <= 0) {
                        // Evitar smart cast usando cópia local e verificações explícitas
                        val contrato = contratoParaEdicao
                        if (contrato != null && contrato.id != null) {
                            contrato.id
                        } else {
                            -1
                        }
                    } else {
                        -1
                    }
                    LogUtils.debug("CadastroContratoDialog", 
                        "Atribuindo contratoId temporário $tempId ao equipamento ${equipamento.id}")
                    equipamento.copy(contratoId = tempId)
                }
            }
        }.toMutableList()
        
        // Verificar e reportar quaisquer inconsistências nos equipamentos
        for (equipamento in equipamentosAtualizados) {
            if (equipamento.equipamentoId <= 0) {
                LogUtils.error("CadastroContratoDialog", 
                    "Equipamento com ID inválido (${equipamento.equipamentoId}) será ignorado")
            }
            if (contratoIdAtual > 0 && equipamento.contratoId != contratoIdAtual) {
                LogUtils.error("CadastroContratoDialog", 
                    "Equipamento com contratoId inconsistente após atualização: ${equipamento.contratoId} != $contratoIdAtual")
            }
        }
        
        // Criar objeto Contrato
        val contrato = Contrato(
            id = contratoParaEdicao?.id ?: 0,
            clienteId = clienteId,
            clienteNome = clienteSelecionado?.contratante ?: contratoParaEdicao?.clienteNome,
            contratoNum = contratoNum,
            dataHoraEmissao = contratoParaEdicao?.dataHoraEmissao ?: viewModel.getDataHoraAtual(),
            dataVenc = contratoParaEdicao?.dataVenc ?: viewModel.getDataVencimento(),
            contratoValor = contratoValorCalculado,
            obraLocal = obraLocal,
            contratoPeriodo = contratoPeriodo,
            entregaLocal = entregaLocal,
            respPedido = respPedido
        )
        
        LogUtils.debug("CadastroContratoDialog", 
            "Salvando contrato: id=${contrato.id}, num=${contrato.contratoNum}, clienteId=${contrato.clienteId}")
        
        // Criar ou atualizar contrato
        if (contratoParaEdicao == null) {
            LogUtils.debug("CadastroContratoDialog", "Criando novo contrato com ${equipamentosAtualizados.size} equipamentos")
            viewModel.criarContrato(contrato, equipamentosAtualizados)
        } else {
            LogUtils.debug("CadastroContratoDialog", "Atualizando contrato existente id=${contratoParaEdicao!!.id} com ${equipamentosAtualizados.size} equipamentos")
            viewModel.atualizarContrato(contratoParaEdicao!!.id, contrato, equipamentosAtualizados)
        }
    }
    
    /**
     * Define um listener para ser chamado quando o contrato for salvo
     */
    fun setOnContratoSavedListener(listener: (Contrato) -> Unit) {
        this.onContratoSavedListener = listener
    }

    private fun atualizarVisibilidadeListaEquipamentos() {
        if (equipamentosContrato.isEmpty()) {
            tvEmptyEquipamentos.text = "Nenhum equipamento adicionado"
            tvEmptyEquipamentos.visibility = View.VISIBLE
            rvEquipamentos.visibility = View.GONE
        } else {
            tvEmptyEquipamentos.visibility = View.GONE
            rvEquipamentos.visibility = View.VISIBLE
        }
    }

    private fun atualizarValorTotal() {
        val valorTotal = equipamentosContrato.sumOf { it.valorTotal ?: 0.0 }
        val formatoMoeda = DecimalFormat("R$ #,##0.00", DecimalFormatSymbols(Locale("pt", "BR")))
        tvValorTotalContratoCalculado.text = "Valor Total: ${formatoMoeda.format(valorTotal)}"
    }
}
