package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.adapter.ProjectContractsAdapter
import com.example.alg_gestao_02.data.repository.ProjectSummaryRepository
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.ui.summary.viewmodel.ProjectSummaryViewModel
import com.example.alg_gestao_02.ui.summary.viewmodel.ProjectSummaryViewModelFactory
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.NetworkUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import android.widget.ViewFlipper

class ProjectSummaryFragment : Fragment() {
    
    private lateinit var contractsAdapter: ProjectContractsAdapter
    private lateinit var viewModel: ProjectSummaryViewModel
    private var viewFlipper: ViewFlipper? = null
    private var swipeRefresh: SwipeRefreshLayout? = null
    
    private var projectId: String = ""
    
    // Constantes para os índices dos estados do ViewFlipper
    companion object {
        private const val VIEW_LIST = 0
        private const val VIEW_EMPTY = 1
        private const val VIEW_LOADING = 2
        private const val ARG_PROJECT_ID = "project_id"
        
        fun newInstance(projectId: String): ProjectSummaryFragment {
            return ProjectSummaryFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID, "")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_project_summary, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ProjectSummaryFragment", "Inicializando fragmento de sumário do projeto: $projectId")
        
        // Inicializar ViewFlipper de forma segura
        try {
            viewFlipper = view.findViewById(R.id.viewFlipper)
            LogUtils.debug("ProjectSummaryFragment", "ViewFlipper inicializado com sucesso")
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryFragment", "Erro ao inicializar ViewFlipper: ${e.message}")
        }
        
        // Inicializar SwipeRefreshLayout
        try {
            swipeRefresh = view.findViewById(R.id.swipeRefresh)
            swipeRefresh?.setOnRefreshListener {
                syncData()
            }
            LogUtils.debug("ProjectSummaryFragment", "SwipeRefreshLayout inicializado com sucesso")
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryFragment", "Erro ao inicializar SwipeRefreshLayout: ${e.message}")
        }
        
        setupViewModel()
        setupRecyclerView(view)
        setupListeners(view)
        
        if (projectId.isNotEmpty()) {
            viewModel.loadContracts(projectId)
        }
        
        // Verificar conectividade
        checkConnectivity(view)
    }
    
    private fun setupViewModel() {
        // Passar o contexto da aplicação para o repositório
        val repository = ProjectSummaryRepository(requireContext().applicationContext)
        val factory = ProjectSummaryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProjectSummaryViewModel::class.java]
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> {
                    showContracts(state.data)
                    swipeRefresh?.isRefreshing = false
                }
                is UiState.Empty -> {
                    showEmptyState()
                    swipeRefresh?.isRefreshing = false
                }
                is UiState.Error -> {
                    showError(state.message)
                    swipeRefresh?.isRefreshing = false
                }
            }
        }
    }
    
    private fun setupRecyclerView(view: View) {
        val rvContracts = view.findViewById<RecyclerView>(R.id.rvContracts)
        
        if (rvContracts == null) {
            LogUtils.error("ProjectSummaryFragment", "RecyclerView não encontrado no layout")
            return
        }
        
        contractsAdapter = ProjectContractsAdapter(emptyList()) { contract ->
            LogUtils.debug("ProjectSummaryFragment", "Contrato clicado: ${contract.name}")
            Toast.makeText(context, "Contrato selecionado: ${contract.name}", Toast.LENGTH_SHORT).show()
        }
        
        rvContracts.layoutManager = LinearLayoutManager(context)
        rvContracts.adapter = contractsAdapter
    }
    
    private fun setupListeners(view: View) {
        // Configurar busca
        view.findViewById<View>(R.id.cardSearch)?.setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Busca clicada")
            // Implementar diálogo de busca
            showSearchDialog()
        }
        
        // Configurar filtro
        view.findViewById<View>(R.id.cardFilter)?.setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Filtro clicado")
            // Implementar diálogo de filtro
            showFilterDialog()
        }
        
        // Botão de pagamento
        view.findViewById<MaterialButton>(R.id.btnPayment)?.setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Botão de pagamento clicado")
            viewModel.loadPaymentContracts()
        }
        
        // Botão de devedor
        view.findViewById<MaterialButton>(R.id.btnDebt)?.setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Botão de devedor clicado")
            viewModel.loadDebtContracts()
        }
    }
    
    private fun showSearchDialog() {
        // Placeholder - implementar um diálogo de busca
        Toast.makeText(context, "Busca em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun showFilterDialog() {
        // Placeholder - implementar um diálogo de filtro
        Toast.makeText(context, "Filtro em desenvolvimento", Toast.LENGTH_SHORT).show()
    }
    
    private fun syncData() {
        if (!NetworkUtils.isNetworkAvailable(requireContext())) {
            showConnectivityMessage(false)
            swipeRefresh?.isRefreshing = false
            return
        }
        
        LogUtils.debug("ProjectSummaryFragment", "Iniciando sincronização...")
        viewModel.syncWithApi()
    }
    
    private fun checkConnectivity(view: View) {
        val isConnected = NetworkUtils.isNetworkAvailable(requireContext())
        showConnectivityMessage(isConnected)
    }
    
    private fun showConnectivityMessage(isConnected: Boolean) {
        val message = if (isConnected) {
            "Conectado à internet"
        } else {
            "Sem conexão. Usando dados locais."
        }
        
        // Usar Toast em vez de Snackbar para evitar problemas com o ScrollView
        context?.let {
            Toast.makeText(it, message, Toast.LENGTH_LONG).show()
        }
        
        // Registrar no log
        LogUtils.debug("ProjectSummaryFragment", message)
    }
    
    private fun showLoading() {
        viewFlipper?.let {
            it.displayedChild = VIEW_LOADING
            LogUtils.debug("ProjectSummaryFragment", "Mostrando estado de carregamento")
        } ?: run {
            LogUtils.error("ProjectSummaryFragment", "ViewFlipper é null ao tentar mostrar loading")
        }
    }
    
    private fun showEmptyState() {
        viewFlipper?.let {
            it.displayedChild = VIEW_EMPTY
            LogUtils.debug("ProjectSummaryFragment", "Mostrando estado vazio")
        } ?: run {
            LogUtils.error("ProjectSummaryFragment", "ViewFlipper é null ao tentar mostrar estado vazio")
        }
    }
    
    private fun showContracts(contracts: List<com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem>) {
        contractsAdapter.updateData(contracts)
        LogUtils.debug("ProjectSummaryFragment", "Atualizando lista com ${contracts.size} contratos")
        
        viewFlipper?.let {
            it.displayedChild = VIEW_LIST
            LogUtils.debug("ProjectSummaryFragment", "Mostrando lista de contratos")
        } ?: run {
            LogUtils.error("ProjectSummaryFragment", "ViewFlipper é null ao tentar mostrar lista")
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        LogUtils.error("ProjectSummaryFragment", "Erro: $message")
        
        // Se tivermos dados antigos, mostramos eles; senão, mostramos estado vazio
        viewFlipper?.let {
            if (contractsAdapter.itemCount > 0) {
                it.displayedChild = VIEW_LIST
                LogUtils.debug("ProjectSummaryFragment", "Mostrando lista existente após erro")
            } else {
                it.displayedChild = VIEW_EMPTY
                LogUtils.debug("ProjectSummaryFragment", "Mostrando estado vazio após erro")
            }
        } ?: run {
            LogUtils.error("ProjectSummaryFragment", "ViewFlipper é null ao tentar mostrar erro")
        }
    }
} 