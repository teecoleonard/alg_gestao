package com.example.alg_gestao_02.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R

import com.example.alg_gestao_02.dashboard.fragments.viewmodel.DashboardViewModel
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.dashboard.fragments.viewmodel.DashboardViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {
    
    private lateinit var viewModel: DashboardViewModel
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    // TextViews para exibir as contagens
    private lateinit var tvContratosCount: TextView
    private lateinit var tvClientesCount: TextView
    private lateinit var tvEquipamentosCount: TextView
    private lateinit var tvDevolucoesCount: TextView
    
    // TextViews para estatísticas expandidas
    private lateinit var tvContratosExtras: TextView
    private lateinit var tvClientesExtras: TextView
    private lateinit var tvEquipamentosExtras: TextView
    private lateinit var tvDevolucoesExtras: TextView
    
    // Header elements
    private lateinit var tvCurrentDate: TextView
    

    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.info("DashboardFragment", "🚀 ========== DASHBOARD FRAGMENT INICIADO ==========")
        LogUtils.info("DashboardFragment", "📱 Fragment: ${this.javaClass.simpleName}")
        LogUtils.info("DashboardFragment", "🔧 Versão Android: ${android.os.Build.VERSION.SDK_INT}")
        LogUtils.info("DashboardFragment", "⏰ Timestamp: ${System.currentTimeMillis()}")
        
        // Inicializar views
        LogUtils.debug("DashboardFragment", "🎨 Inicializando views...")
        initViews(view)
        
        // Configurar ViewModel
        LogUtils.debug("DashboardFragment", "🧠 Configurando ViewModel...")
        setupViewModel()
        
        // Configurar listeners
        LogUtils.debug("DashboardFragment", "👂 Configurando listeners...")
        setupListeners(view)
        
        // Observar mudanças no ViewModel
        LogUtils.debug("DashboardFragment", "👀 Configurando observadores...")
        observeViewModel()
        
        LogUtils.info("DashboardFragment", "✅ Dashboard Fragment configurado com sucesso!")
    }
    
    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        tvContratosCount = view.findViewById(R.id.tvWorkersCount)
        tvClientesCount = view.findViewById(R.id.tvTasksCount)
        tvEquipamentosCount = view.findViewById(R.id.tvEquipamentosCount)
        tvDevolucoesCount = view.findViewById(R.id.tvDevolucoesCount)
        
        // TextViews para estatísticas expandidas
        tvContratosExtras = view.findViewById(R.id.tvContratosExtras)
        tvClientesExtras = view.findViewById(R.id.tvClientesExtras)
        tvEquipamentosExtras = view.findViewById(R.id.tvEquipamentosExtras)
        tvDevolucoesExtras = view.findViewById(R.id.tvDevolucoesExtras)
        
        // Header elements
        tvCurrentDate = view.findViewById(R.id.tvCurrentDate)
        
        // Setup current date
        setupCurrentDate()
    }
    
    private fun setupCurrentDate() {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("pt", "BR"))
        val formattedDate = dateFormat.format(currentDate)
        tvCurrentDate.text = formattedDate.replaceFirstChar { it.uppercase() }
    }
    

    
    private fun setupViewModel() {
        // ✅ VERIFICAÇÃO DE DEBUG - CHECANDO SESSÃO E TOKEN
        val sessionManager = SessionManager(requireContext())
        LogUtils.info("DashboardFragment", "🔍 ========== VERIFICANDO SESSÃO ==========")
        LogUtils.info("DashboardFragment", "📋 isLoggedIn: ${sessionManager.isLoggedIn()}")
        LogUtils.info("DashboardFragment", "🔑 Token existe: ${!sessionManager.getToken().isNullOrEmpty()}")
        LogUtils.info("DashboardFragment", "👤 User ID: ${sessionManager.getUserId()}")
        LogUtils.info("DashboardFragment", "🎭 User Role: ${sessionManager.getUserRole()}")
        LogUtils.info("DashboardFragment", "👨‍💼 User Name: ${sessionManager.getUserName()}")
        LogUtils.info("DashboardFragment", "📄 User CPF: ${sessionManager.getUserCpf()}")
        
        if (sessionManager.getToken() != null) {
            val token = sessionManager.getToken()!!
            LogUtils.info("DashboardFragment", "🔐 Token (primeiros 50 chars): ${token.take(50)}...")
            LogUtils.info("DashboardFragment", "📏 Tamanho do token: ${token.length} caracteres")
            LogUtils.info("DashboardFragment", "🔑 TOKEN COMPLETO: $token")
        } else {
            LogUtils.error("DashboardFragment", "❌ TOKEN É NULO!")
            LogUtils.error("DashboardFragment", "🚨 PROBLEMA: Usuário logado mas sem token!")
        }
        LogUtils.info("DashboardFragment", "🔍 ======================================")
        // ✅ FIM DA VERIFICAÇÃO

        val factory = DashboardViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
    }
    
    private fun setupListeners(view: View) {
        // Configurar listener do SwipeRefreshLayout
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("DashboardFragment", "Atualizando dashboard via swipe refresh")
            viewModel.refreshDashboard()
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
        LogUtils.info("DashboardFragment", "🔗 ========== INICIANDO OBSERVAÇÃO DO VIEWMODEL ==========")
        
        // Observar estado da UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            LogUtils.debug("DashboardFragment", "🔄 Estado da UI alterado: ${state.javaClass.simpleName}")
            
            when (state) {
                is UiState.Loading -> {
                    LogUtils.info("DashboardFragment", "⏳ CARREGANDO dados do dashboard...")
                    LogUtils.debug("DashboardFragment", "📱 Mantendo indicador de carregamento visível")
                }
                
                is UiState.Success -> {
                    LogUtils.info("DashboardFragment", "✅ SUCESSO: Dados do dashboard carregados!")
                    swipeRefresh.isRefreshing = false
                    LogUtils.debug("DashboardFragment", "🔄 SwipeRefresh desabilitado")
                    LogUtils.debug("DashboardFragment", "📊 Dados recebidos: ${state.data}")
                }
                
                is UiState.Error -> {
                    LogUtils.error("DashboardFragment", "❌ ERRO ao carregar dashboard:")
                    LogUtils.error("DashboardFragment", "📝 Mensagem de erro: ${state.message}")
                    swipeRefresh.isRefreshing = false
                    LogUtils.debug("DashboardFragment", "🔄 SwipeRefresh desabilitado")
                    LogUtils.debug("DashboardFragment", "🚨 Exibindo toast de erro para o usuário")
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                
                else -> {
                    LogUtils.debug("DashboardFragment", "❓ Estado desconhecido: ${state}")
                    swipeRefresh.isRefreshing = false
                }
            }
        }
        
        // Observar estatísticas do dashboard
        viewModel.dashboardStats.observe(viewLifecycleOwner) { stats ->
            if (stats != null) {
                LogUtils.info("DashboardFragment", "📊 ========== ATUALIZANDO INTERFACE COM NOVOS DADOS ==========")
                
                // Log dos dados básicos recebidos
                LogUtils.info("DashboardFragment", "📋 DADOS BÁSICOS RECEBIDOS:")
                LogUtils.info("DashboardFragment", "   📋 Contratos: ${stats.contratos} (antes: ${tvContratosCount.text})")
                LogUtils.info("DashboardFragment", "   👥 Clientes: ${stats.clientes} (antes: ${tvClientesCount.text})")
                LogUtils.info("DashboardFragment", "   ⚙️ Equipamentos: ${stats.equipamentos} (antes: ${tvEquipamentosCount.text})")
                LogUtils.info("DashboardFragment", "   📦 Devoluções: ${stats.devolucoes} (antes: ${tvDevolucoesCount.text})")
                
                // Atualizar as contagens nos TextViews com logs individuais
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contador de contratos...")
                tvContratosCount.text = stats.contratos.toString()
                
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contador de clientes...")
                tvClientesCount.text = stats.clientes.toString()
                
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contador de equipamentos...")
                tvEquipamentosCount.text = stats.equipamentos.toString()
                
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contador de devoluções...")
                tvDevolucoesCount.text = stats.devolucoes.toString()
                
                // Atualizar estatísticas expandidas
                LogUtils.info("DashboardFragment", "📈 ATUALIZANDO ESTATÍSTICAS DETALHADAS:")
                LogUtils.info("DashboardFragment", "   📋 Contratos esta semana: ${stats.contratosEstaSemana}")
                LogUtils.info("DashboardFragment", "   👥 Clientes hoje: ${stats.clientesHoje}")
                LogUtils.info("DashboardFragment", "   ⚙️ Equipamentos disponíveis: ${stats.equipamentosDisponiveis}")
                LogUtils.info("DashboardFragment", "   📦 Devoluções pendentes: ${stats.devolucoesPendentes}")
                
                updateEstatisticasExpandidas(stats)
                

                
                // Log final de confirmação
                LogUtils.info("DashboardFragment", "✅ INTERFACE ATUALIZADA COM SUCESSO!")
                LogUtils.debug("DashboardFragment", "📱 Valores finais na UI:")
                LogUtils.debug("DashboardFragment", "   📋 tvContratosCount.text = '${tvContratosCount.text}'")
                LogUtils.debug("DashboardFragment", "   👥 tvClientesCount.text = '${tvClientesCount.text}'")
                LogUtils.debug("DashboardFragment", "   ⚙️ tvEquipamentosCount.text = '${tvEquipamentosCount.text}'")
                LogUtils.debug("DashboardFragment", "   📦 tvDevolucoesCount.text = '${tvDevolucoesCount.text}'")
                
            } else {
                LogUtils.warning("DashboardFragment", "⚠️ Estatísticas recebidas são nulas")
                LogUtils.debug("DashboardFragment", "🤔 Isso pode acontecer durante o primeiro carregamento")
            }
        }
        
        LogUtils.info("DashboardFragment", "✅ Observação do ViewModel configurada com sucesso")
    }
    
    /**
     * Atualiza as estatísticas expandidas com dados dinâmicos
     */
    private fun updateEstatisticasExpandidas(stats: DashboardStats) {
        LogUtils.debug("DashboardFragment", "🔧 Atualizando estatísticas expandidas...")
        
        // Contratos esta semana
        val textContratosEstaSemana = if (stats.contratosEstaSemana > 0) {
            "${stats.contratosEstaSemana} novos esta semana"
        } else {
            "Nenhum novo esta semana"
        }
        tvContratosExtras.text = textContratosEstaSemana
        tvContratosExtras.setTextColor(
            if (stats.contratosEstaSemana > 0) 
                ContextCompat.getColor(requireContext(), R.color.success)
            else 
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
        )
        LogUtils.debug("DashboardFragment", "   📋 Contratos: '$textContratosEstaSemana'")
        
        // Clientes hoje
        val textClientesHoje = if (stats.clientesHoje > 0) {
            "${stats.clientesHoje} cadastrados hoje"
        } else {
            "Nenhum cadastrado hoje"
        }
        tvClientesExtras.text = textClientesHoje
        tvClientesExtras.setTextColor(
            if (stats.clientesHoje > 0) 
                ContextCompat.getColor(requireContext(), R.color.success)
            else 
                ContextCompat.getColor(requireContext(), R.color.text_secondary)
        )
        LogUtils.debug("DashboardFragment", "   👥 Clientes: '$textClientesHoje'")
        
        // Equipamentos disponíveis
        val textEquipamentosDisponiveis = "${stats.equipamentosDisponiveis} disponíveis"
        tvEquipamentosExtras.text = textEquipamentosDisponiveis
        tvEquipamentosExtras.setTextColor(
            if (stats.equipamentosDisponiveis > 0) 
                ContextCompat.getColor(requireContext(), R.color.success)
            else 
                ContextCompat.getColor(requireContext(), R.color.warning)
        )
        LogUtils.debug("DashboardFragment", "   ⚙️ Equipamentos: '$textEquipamentosDisponiveis'")
        
        // Devoluções pendentes
        val textDevolucoesPendentes = if (stats.devolucoesPendentes > 0) {
            "${stats.devolucoesPendentes} pendentes"
        } else {
            "Nenhuma pendente"
        }
        tvDevolucoesExtras.text = textDevolucoesPendentes
        tvDevolucoesExtras.setTextColor(
            if (stats.devolucoesPendentes > 0) 
                ContextCompat.getColor(requireContext(), R.color.warning)
            else 
                ContextCompat.getColor(requireContext(), R.color.success)
        )
        LogUtils.debug("DashboardFragment", "   📦 Devoluções: '$textDevolucoesPendentes'")
        
        LogUtils.info("DashboardFragment", "✅ Estatísticas expandidas atualizadas com sucesso!")
    }
} 