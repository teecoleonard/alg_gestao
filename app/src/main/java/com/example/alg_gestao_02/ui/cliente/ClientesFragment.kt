package com.example.alg_gestao_02.ui.cliente

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
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
            viewModel.loadClientes()
        }
        
        fabNovoCliente.setOnClickListener {
            showCadastroClienteDialog()
        }
    }
    
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            swipeRefresh.isRefreshing = false
            
            when (state) {
                is UiState.Loading -> {
                    viewLoading.visibility = View.VISIBLE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                
                is UiState.Success -> {
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    
                    adapter.updateClientes(state.data)
                }
                
                is UiState.Empty -> {
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                
                is UiState.Error -> {
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun showClienteDetailDialog(cliente: Cliente) {
        // Aqui poderia ser implementado um diálogo de detalhes do cliente
        // Por enquanto, vamos apenas abrir para edição
        val dialog = CadastroClienteDialogFragment.newInstance(cliente)
        dialog.show(parentFragmentManager, "ClienteDetailDialog")
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
                    dialog.setOnClienteSavedListener { clienteAtualizado ->
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