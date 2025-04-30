package com.example.alg_gestao_02.dashboard.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.ProjectDetailActivity
import com.example.alg_gestao_02.dashboard.fragments.contract.ContratosFragment
import com.example.alg_gestao_02.dashboard.fragments.dashboard.adapter.ProjectsAdapter
import com.example.alg_gestao_02.dashboard.fragments.dashboard.model.Project
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.dashboard.repository.ProjectRepository
import com.example.alg_gestao_02.ui.dashboard.viewmodel.DashboardViewModel
import com.example.alg_gestao_02.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils

class DashboardFragment : BaseFragment() {
    
    private lateinit var projectsAdapter: ProjectsAdapter
    private lateinit var viewModel: DashboardViewModel
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var recyclerProjects: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.debug("DashboardFragment", "Inicializando fragmento do dashboard")
        
        initViews(view)
        setupViewModel()
        
        // Agora chamamos o método da classe pai depois de inicializar o viewModel
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners(view)
    }
    
    /**
     * Implementa o método abstrato getErrorViewModels para fornecer
     * a lista de ViewModels que tratam erros
     */
    override fun getErrorViewModels(): List<ErrorViewModel> {
        return listOf(viewModel.errorHandler)
    }
    
    /**
     * Implementa o método onErrorRetry para quando o usuário
     * clica em "Tentar novamente" após um erro
     */
    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {
        viewModel.refreshProjects()
    }
    
    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        recyclerProjects = view.findViewById(R.id.recyclerProjects)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
    }
    
    private fun setupViewModel() {
        val factory = DashboardViewModelFactory(ProjectRepository())
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> showProjects(state.data)
                is UiState.Empty -> showEmpty()
                is UiState.Error -> {
                    // Atualiza UI para parar o loading
                    swipeRefresh.isRefreshing = false
                    // O BaseFragment já cuida de mostrar o erro
                }
            }
        }
    }
    
    private fun setupRecyclerView() {
        projectsAdapter = ProjectsAdapter(
            emptyList(),
            onItemClick = { project ->
                LogUtils.debug("DashboardFragment", "Projeto clicado: ${project.name}")
                // Navegar para tela de detalhes do projeto
                val intent = Intent(requireContext(), ProjectDetailActivity::class.java)
                intent.putExtra("project_id", project.id)
                intent.putExtra("project_name", project.name)
                startActivity(intent)
            }
        )
        
        recyclerProjects.layoutManager = LinearLayoutManager(context)
        recyclerProjects.adapter = projectsAdapter
    }
    
    private fun setupListeners(view: View) {
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("DashboardFragment", "Atualizando projetos via swipe refresh")
            viewModel.refreshProjects()
        }
        
        // Configurar botão de busca
        view.findViewById<View>(R.id.cardSearch)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Busca clicada")
            Toast.makeText(context, "Função de busca em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão "ver todos" dos insights
        view.findViewById<View>(R.id.tvViewAll)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Ver todos os insights clicado")
            Toast.makeText(context, "Ver todos os insights em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Cards de insights
        view.findViewById<View>(R.id.cardMaterials)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de materiais clicado")
            Toast.makeText(context, "Lista de materiais em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.cardWorkers)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de funcionários clicado")
            Toast.makeText(context, "Lista de funcionários em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.cardTasks)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de tarefas clicado")
            Toast.makeText(context, "Lista de tarefas em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão para contratos
        view.findViewById<View>(R.id.fabContratos)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Botão contratos clicado")
            
            // Navegar para a página de contratos
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, ContratosFragment())
            transaction.addToBackStack(null)
            transaction.commit()
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
    }
    
    private fun showLoading() {
        swipeRefresh.isRefreshing = true
        layoutEmpty.visibility = View.GONE
    }
    
    private fun showProjects(projects: List<Project>) {
        swipeRefresh.isRefreshing = false
        layoutEmpty.visibility = View.GONE
        projectsAdapter.updateData(projects)
    }
    
    private fun showEmpty() {
        swipeRefresh.isRefreshing = false
        layoutEmpty.visibility = View.VISIBLE
        projectsAdapter.updateData(emptyList())
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.refreshProjects()
    }
} 