package com.example.alg_gestao_02.ui.equipamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.equipamento.adapter.EquipamentosAdapter
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModel
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment para listar e gerenciar equipamentos
 */
class EquipamentosFragment : BaseFragment() {
    
    private lateinit var viewModel: EquipamentosViewModel
    private lateinit var equipamentosAdapter: EquipamentosAdapter
    
    private lateinit var recyclerEquipamentos: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAddEquipamento: FloatingActionButton
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Habilita opções de menu
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_equipamentos, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.debug("EquipamentosFragment", "Inicializando tela de equipamentos")
        
        // Inicializar views
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        setupListeners()
        
        // Agora chamamos o método da classe pai depois de inicializar o viewModel
        super.onViewCreated(view, savedInstanceState)
    }
    
    override fun getErrorViewModels(): List<ErrorViewModel> {
        return listOf(viewModel.errorHandler)
    }
    
    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {
        viewModel.loadEquipamentos()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_equipamentos, menu)
        
        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setTextoBusca(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setTextoBusca(it) }
                return true
            }
        })
        
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadEquipamentos()
                true
            }
            R.id.action_filter_todos -> {
                viewModel.setFiltroTipo(EquipamentosViewModel.FiltroEquipamento.TODOS)
                item.isChecked = true
                true
            }
            R.id.action_filter_disponiveis -> {
                viewModel.setFiltroTipo(EquipamentosViewModel.FiltroEquipamento.DISPONIVEIS)
                item.isChecked = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun initViews(view: View) {
        recyclerEquipamentos = view.findViewById(R.id.recyclerEquipamentos)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        fabAddEquipamento = view.findViewById(R.id.fabAddEquipamento)
    }
    
    private fun setupRecyclerView() {
        equipamentosAdapter = EquipamentosAdapter(
            emptyList(),
            onItemClick = { equipamento ->
                LogUtils.debug("EquipamentosFragment", "Equipamento clicado: ${equipamento.nomeEquip}")
                showEquipamentoDetails(equipamento)
            },
            onMenuClick = { equipamento, view ->
                showEquipamentoOptionsMenu(equipamento, view)
            }
        )
        
        recyclerEquipamentos.adapter = equipamentosAdapter
    }
    
    private fun setupViewModel() {
        val factory = EquipamentosViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[EquipamentosViewModel::class.java]
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    equipamentosAdapter.updateEquipamentos(state.data)
                    showContent()
                }
                is UiState.Empty -> {
                    hideLoading()
                    showEmpty()
                }
                is UiState.Error -> {
                    hideLoading()
                    // BaseFragment já cuida de exibir erros
                }
                else -> { /* Ignorar outros estados */ }
            }
        }
    }
    
    private fun setupListeners() {
        fabAddEquipamento.setOnClickListener {
            LogUtils.debug("EquipamentosFragment", "Botão adicionar equipamento clicado")
            showCadastroEquipamentoDialog()
        }
        
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("EquipamentosFragment", "SwipeRefresh acionado")
            viewModel.loadEquipamentos()
        }
    }
    
    private fun showEquipamentoDetails(equipamento: Equipamento) {
        val dialog = EquipamentoDetailDialog.newInstance(equipamento.id)
        dialog.show(parentFragmentManager, "EquipamentoDetailDialog")
    }
    
    private fun showEquipamentoOptionsMenu(equipamento: Equipamento, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_equipamento_options, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    showEditEquipamentoDialog(equipamento)
                    true
                }
                R.id.action_delete -> {
                    confirmDeleteEquipamento(equipamento)
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun showCadastroEquipamentoDialog() {
        val dialog = CadastroEquipamentoDialogFragment.newInstance()
        dialog.setOnEquipamentoSavedListener { equipamento ->
            viewModel.criarEquipamento(equipamento)
        }
        dialog.show(parentFragmentManager, "CadastroEquipamentoDialog")
    }
    
    private fun showEditEquipamentoDialog(equipamento: Equipamento) {
        val dialog = CadastroEquipamentoDialogFragment.newInstance(equipamento)
        dialog.setOnEquipamentoSavedListener { equipamentoAtualizado ->
            viewModel.atualizarEquipamento(equipamento.id, equipamentoAtualizado)
        }
        dialog.show(parentFragmentManager, "EditEquipamentoDialog")
    }
    
    private fun confirmDeleteEquipamento(equipamento: Equipamento) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir Equipamento")
            .setMessage("Deseja realmente excluir o equipamento '${equipamento.nomeEquip}'?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirEquipamento(equipamento.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showLoading() {
        swipeRefresh.isRefreshing = true
    }
    
    private fun hideLoading() {
        swipeRefresh.isRefreshing = false
    }
    
    private fun showContent() {
        recyclerEquipamentos.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
    }
    
    private fun showEmpty() {
        recyclerEquipamentos.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
    }
} 