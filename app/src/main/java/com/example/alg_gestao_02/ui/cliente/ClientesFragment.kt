package com.example.alg_gestao_02.ui.cliente

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.ui.cliente.adapter.ClientesAdapter
import com.example.alg_gestao_02.ui.cliente.viewmodel.ClientesViewModel
import com.example.alg_gestao_02.ui.cliente.viewmodel.ClientesViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment para listagem e gestão de clientes
 */
class ClientesFragment : Fragment() {
    
    private lateinit var viewModel: ClientesViewModel
    private lateinit var adapter: ClientesAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabNovoCliente: FloatingActionButton
    private lateinit var viewLoading: View
    private lateinit var viewEmpty: View
    private lateinit var viewError: View
    private lateinit var etSearch: TextInputEditText
    private var hasLoadedData: Boolean = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ClientesFragment", "Inicializando tela de clientes")
        
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvClientes)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        fabNovoCliente = view.findViewById(R.id.fabNovoCliente)
        viewLoading = view.findViewById(R.id.viewLoading)
        viewEmpty = view.findViewById(R.id.viewEmpty)
        viewError = view.findViewById(R.id.viewError)
        etSearch = view.findViewById(R.id.etSearch)
    }
    
    private fun setupViewModel() {
        val factory = ClientesViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[ClientesViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = ClientesAdapter(
            clientes = emptyList(),
            onItemClick = { cliente ->
                showClienteDetailDialog(cliente)
            },
            onMenuClick = { cliente, view ->
                showPopupMenu(cliente, view)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("ClientesFragment", "Atualizando lista de clientes via swipe refresh")
            swipeRefresh.isRefreshing = true
            viewModel.loadClientes()
        }
        
        fabNovoCliente.setOnClickListener {
            showCadastroClienteDialog()
        }
        
        // Configurar listener para busca
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val searchTerm = etSearch.text.toString().trim()
                LogUtils.debug("ClientesFragment", "Buscando por: $searchTerm")
                viewModel.setSearchTerm(searchTerm)
                true
            } else {
                false
            }
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchTerm(s?.toString().orEmpty())
            }
        })
    }
    
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    val keepContentVisible = hasLoadedData && adapter.itemCount > 0
                    if (keepContentVisible) {
                        LogUtils.debug("ClientesFragment", "Estado: Loading - mantendo lista durante atualização")
                        viewLoading.visibility = View.GONE
                        viewEmpty.visibility = View.GONE
                        viewError.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        swipeRefresh.isRefreshing = true
                    } else {
                        LogUtils.debug("ClientesFragment", "Estado: Loading - primeira carga")
                        viewLoading.visibility = View.VISIBLE
                        viewEmpty.visibility = View.GONE
                        viewError.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                    }
                }
                
                is UiState.Success -> {
                    LogUtils.debug("ClientesFragment", "Estado: Success - Clientes: ${state.data.size}")
                    hasLoadedData = true
                    swipeRefresh.isRefreshing = false
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    
                    adapter.updateClientes(state.data)
                }
                
                is UiState.Empty -> {
                    LogUtils.debug("ClientesFragment", "Estado: Empty")
                    hasLoadedData = true
                    swipeRefresh.isRefreshing = false
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                
                is UiState.Error -> {
                    LogUtils.debug("ClientesFragment", "Estado: Error - ${state.message}")
                    swipeRefresh.isRefreshing = false
                    val keepContentVisible = hasLoadedData && adapter.itemCount > 0
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = if (keepContentVisible) View.GONE else View.VISIBLE
                    recyclerView.visibility = if (keepContentVisible) View.VISIBLE else View.GONE
                    
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Observar operações de criação/atualização/exclusão
        viewModel.operationState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    LogUtils.debug("ClientesFragment", "Operação em andamento...")
                    // Opcional: mostrar indicador de carregamento
                }
                
                is UiState.Success -> {
                    LogUtils.info("ClientesFragment", "✅ Operação concluída com sucesso")
                    val mensagem = when {
                        state.data.id > 0 -> "Cliente salvo com sucesso!"
                        else -> "Operação concluída com sucesso!"
                    }
                    Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
                }
                
                is UiState.Error -> {
                    LogUtils.error("ClientesFragment", "❌ Erro na operação: ${state.message}")
                    // Exibir a mensagem de erro específica da API
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                
                is UiState.Empty -> {
                    // Estado vazio, não fazer nada
                    LogUtils.debug("ClientesFragment", "Estado vazio na operação")
                }
                
                null -> {
                    // Estado limpo, não fazer nada
                }
            }
        }
    }
    
    private fun showClienteDetailDialog(cliente: Cliente) {
        // Navegar para a tela de detalhes do cliente
        LogUtils.debug("ClientesFragment", "Abrindo detalhes do cliente: ${cliente.id}")
        
        // Navegar para o ClientDetailsFragment com o argumento do ID
        val bundle = Bundle().apply {
            putInt("cliente_id", cliente.id)
        }
        findNavController().navigate(R.id.clientDetailsFragment, bundle)
    }
    
    private fun showCadastroClienteDialog() {
        val dialog = CadastroClienteDialogFragment.newInstance()
        dialog.show(parentFragmentManager, "CadastroClienteDialog")
    }
    
    private fun showPopupMenu(cliente: Cliente, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    LogUtils.debug("ClientesFragment", "Editando cliente: ${cliente.contratante}")
                    val dialog = CadastroClienteDialogFragment.newInstance(cliente)
                    dialog.setOnClienteSavedListener { _ ->
                        // Recarregar a lista quando o cliente for atualizado
                        viewModel.loadClientes()
                    }
                    dialog.show(parentFragmentManager, "EditClienteDialog_${cliente.id}")
                    true
                }
                
                R.id.action_delete -> {
                    LogUtils.debug("ClientesFragment", "Excluindo cliente: ${cliente.contratante}")
                    confirmarExclusao(cliente)
                    true
                }
                
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun confirmarExclusao(cliente: Cliente) {
        // Aqui seria ideal ter um diálogo de confirmação
        // Por simplicidade, vamos excluir diretamente
        viewModel.excluirCliente(cliente.id)
        Toast.makeText(context, "Excluindo cliente...", Toast.LENGTH_SHORT).show()
    }
} 
