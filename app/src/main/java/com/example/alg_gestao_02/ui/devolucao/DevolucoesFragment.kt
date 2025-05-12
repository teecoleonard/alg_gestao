package com.example.alg_gestao_02.ui.devolucao

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.ui.devolucao.adapter.DevolucoesAdapter
import com.example.alg_gestao_02.ui.devolucao.viewmodel.DevolucoesViewModel
import com.example.alg_gestao_02.ui.devolucao.viewmodel.DevolucoesViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment para listagem e gestão de devoluções
 */
class DevolucoesFragment : Fragment(), DevolucaoDetailsDialogFragment.OnProcessarRequestListener {
    
    private lateinit var viewModel: DevolucoesViewModel
    private lateinit var adapter: DevolucoesAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var viewLoading: View
    private lateinit var viewEmpty: View
    private lateinit var viewError: View
    private lateinit var etSearch: TextInputEditText
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_devolucoes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("DevolucoesFragment", "onViewCreated - Inicializando tela de devoluções")
        
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        // Carregar devoluções imediatamente
        LogUtils.debug("DevolucoesFragment", "Carregando lista de devoluções inicialmente")
        viewModel.loadDevolucoes()
    }
    
    override fun onResume() {
        super.onResume()
        LogUtils.debug("DevolucoesFragment", "onResume - Ciclo de vida")
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvDevolucoes)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        fabFilter = view.findViewById(R.id.fabFilter)
        viewFlipper = view.findViewById(R.id.viewFlipper)
        viewLoading = view.findViewById(R.id.viewLoading)
        viewEmpty = view.findViewById(R.id.viewEmpty)
        viewError = view.findViewById(R.id.viewError)
        etSearch = view.findViewById(R.id.etSearch)
    }
    
    private fun setupViewModel() {
        val apiService = ApiClient.apiService
        val repository = DevolucaoRepository(apiService)
        val factory = DevolucoesViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[DevolucoesViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = DevolucoesAdapter(
            devolucoes = emptyList(),
            onItemClick = { devolucao ->
                showDevolucaoDetailDialog(devolucao)
            },
            onMenuClick = { devolucao, view ->
                showPopupMenu(devolucao, view)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("DevolucoesFragment", "Atualizando lista de devoluções via swipe refresh")
            viewModel.loadDevolucoes()
        }
        
        fabFilter.setOnClickListener {
            showFilterDialog()
        }
        
        // Configurar listener para busca
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val searchTerm = etSearch.text.toString().trim()
                LogUtils.debug("DevolucoesFragment", "Buscando por: $searchTerm")
                viewModel.setSearchTerm(searchTerm)
                true
            } else {
                false
            }
        }
    }
    
    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            swipeRefresh.isRefreshing = false
            
            when (state) {
                is UiState.Loading -> {
                    LogUtils.debug("DevolucoesFragment", "Estado: Loading")
                    viewFlipper.displayedChild = 0 // Índice do viewLoading
                }
                
                is UiState.Success -> {
                    LogUtils.debug("DevolucoesFragment", "Estado: Success - Devoluções: ${state.data.size}")
                    viewFlipper.displayedChild = 2 // Índice do viewList
                    
                    // Atualizar o adaptador com as devoluções
                    adapter.updateDevolucoes(state.data)
                }
                
                is UiState.Empty -> {
                    LogUtils.debug("DevolucoesFragment", "Estado: Empty")
                    viewFlipper.displayedChild = 1 // Índice do viewEmpty
                }
                
                is UiState.Error -> {
                    LogUtils.debug("DevolucoesFragment", "Estado: Error - ${state.message}")
                    viewFlipper.displayedChild = 3 // Índice do viewError
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Observar o estado de processamento de devolução
        viewModel.processamentoState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Opcional: mostrar um indicador de carregamento
                    LogUtils.debug("DevolucoesFragment", "Processando devolução...")
                }
                
                is UiState.Success -> {
                    LogUtils.debug("DevolucoesFragment", "Devolução processada com sucesso")
                    Toast.makeText(context, "Devolução processada com sucesso", Toast.LENGTH_SHORT).show()
                    
                    // Limpar o estado de processamento para evitar comportamentos indesejados
                    viewModel.clearProcessamentoState()
                    
                    // Atualizar a lista de devoluções
                    viewModel.loadDevolucoes()
                }
                
                is UiState.Error -> {
                    LogUtils.error("DevolucoesFragment", "Erro ao processar devolução: ${state.message}")
                    Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_SHORT).show()
                    
                    // Limpar o estado de processamento
                    viewModel.clearProcessamentoState()
                }
                
                else -> {
                    // Nada a fazer para outros estados
                }
            }
        }
    }
    
    private fun showDevolucaoDetailDialog(devolucao: Devolucao) {
        LogUtils.debug("DevolucoesFragment", "Exibindo detalhes para a devolução: ${devolucao.devNum}")
        
        val dialog = DevolucaoDetailsDialogFragment.newInstance(devolucao)
        dialog.setOnProcessarRequestListener(this)
        dialog.show(childFragmentManager, "DevolucaoDetailsDialog")
    }
    
    override fun onProcessarRequested(devolucao: Devolucao, quantidade: Int, status: String, observacao: String?) {
        LogUtils.debug("DevolucoesFragment", "Processando devolução ID: ${devolucao.id}, " +
                "Quantidade: $quantidade, Status: $status")
        
        viewModel.processarDevolucao(
            devolucaoId = devolucao.id,
            quantidadeDevolvida = quantidade,
            statusItemDevolucao = status,
            observacaoItemDevolucao = observacao
        )
    }
    
    private fun showFilterDialog() {
        // Criar e exibir um dialog para filtragem de devoluções
        // Implementar conforme necessidades específicas do projeto
        Toast.makeText(context, "Filtros de devoluções", Toast.LENGTH_SHORT).show()
    }
    
    private fun showPopupMenu(devolucao: Devolucao, view: View) {
        val popup = PopupMenu(requireContext(), view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.menu_devolucao_item, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_detail -> {
                    showDevolucaoDetailDialog(devolucao)
                    true
                }
                // Adicionar mais opções conforme necessário
                else -> false
            }
        }
        
        popup.show()
    }
}
