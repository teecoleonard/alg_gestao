package com.example.alg_gestao_02.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.viewmodel.DashboardViewModel
import com.example.alg_gestao_02.dashboard.fragments.viewmodel.DashboardViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils

class DashboardFragment : Fragment() {
    
    private lateinit var viewModel: DashboardViewModel
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    // TextViews para exibir as contagens
    private lateinit var tvContratosCount: TextView
    private lateinit var tvClientesCount: TextView
    private lateinit var tvEquipamentosCount: TextView
    private lateinit var tvDevolucoesCount: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        LogUtils.debug("DashboardFragment", "Inicializando fragmento do dashboard")
        
        // Inicializar views
        initViews(view)
        
        // Configurar ViewModel
        setupViewModel()
        
        // Configurar listeners
        setupListeners(view)
        
        // Observar mudanças no ViewModel
        observeViewModel()
    }
    
    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        tvContratosCount = view.findViewById(R.id.tvWorkersCount)
        tvClientesCount = view.findViewById(R.id.tvTasksCount)
        tvEquipamentosCount = view.findViewById(R.id.tvEquipamentosCount)
        tvDevolucoesCount = view.findViewById(R.id.tvDevolucoesCount)
    }
    
    private fun setupViewModel() {
        val factory = DashboardViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
    }
    
    private fun setupListeners(view: View) {
        // Configurar listener do SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("DashboardFragment", "Atualizando dashboard via swipe refresh")
            viewModel.refreshDashboard()
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
        
        // Card de Equipamentos
        view.findViewById<View>(R.id.cardEquipamentos)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de equipamentos clicado")
            
            // Navegar para a página de equipamentos usando o NavController
            findNavController().navigate(R.id.equipamentosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_equipamentos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Contratos
        view.findViewById<View>(R.id.cardWorkers)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de contratos clicado")
            
            // Navegar para a página de contratos usando o NavController
            findNavController().navigate(R.id.contratosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Clientes
        view.findViewById<View>(R.id.cardTasks)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de clientes clicado")
            
            // Navegar para a página de clientes usando o NavController
            findNavController().navigate(R.id.clientesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_clientes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Devoluções
        view.findViewById<View>(R.id.cardDevolucoes)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de devoluções clicado")
            
            // Navegar para a página de devoluções usando o NavController
            findNavController().navigate(R.id.devolucoesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_devolucoes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
    }
    
    private fun observeViewModel() {
        // Observar estado da UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Mantém o indicador de carregamento visível
                    LogUtils.debug("DashboardFragment", "Carregando dados do dashboard...")
                }
                
                is UiState.Success -> {
                    // Esconde o indicador de carregamento
                    swipeRefresh.isRefreshing = false
                    LogUtils.debug("DashboardFragment", "Dados do dashboard atualizados com sucesso")
                }
                
                is UiState.Error -> {
                    // Esconde o indicador de carregamento e mostra erro
                    swipeRefresh.isRefreshing = false
                    LogUtils.error("DashboardFragment", "Erro ao atualizar dashboard: ${state.message}")
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                
                else -> {
                    // Para outros estados, esconde o indicador
                    swipeRefresh.isRefreshing = false
                }
            }
        }
        
        // Observar estatísticas do dashboard
        viewModel.dashboardStats.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                LogUtils.debug("DashboardFragment", "Atualizando contadores: contratos=${it.contratos}, clientes=${it.clientes}, equipamentos=${it.equipamentos}, devoluções=${it.devolucoes}")
                
                // Atualizar as contagens nos TextViews
                tvContratosCount.text = it.contratos.toString()
                tvClientesCount.text = it.clientes.toString()
                tvEquipamentosCount.text = it.equipamentos.toString()
                tvDevolucoesCount.text = it.devolucoes.toString()
            }
        }
    }
} 