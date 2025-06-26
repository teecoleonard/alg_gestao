package com.example.alg_gestao_02.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
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
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.TaskMetrics
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
    
    // TextViews para exibir as contagens (seção estatísticas rápidas)
    private lateinit var tvContratosCount: TextView
    private lateinit var tvClientesCount: TextView
    private lateinit var tvEquipamentosCount: TextView
    private lateinit var tvDevolucoesCount: TextView
    
    // TextViews para cards de resumo financeiro
    private lateinit var tvReceitaTotal: TextView
    private lateinit var tvContratosAtivos: TextView
    
    // Header elements
    private lateinit var tvCurrentDate: TextView
    
    // Progress bars e TextViews para métricas de performance
    private lateinit var tvContratosProgress: TextView
    private lateinit var tvReceitaProgress: TextView
    private lateinit var tvClientesProgress: TextView
    private lateinit var tvSatisfacaoProgress: TextView
    private lateinit var progressBarContratos: ProgressBar
    private lateinit var progressBarReceita: ProgressBar
    private lateinit var progressBarClientes: ProgressBar
    private lateinit var progressBarSatisfacao: ProgressBar
    
    // TextViews para tarefas pendentes
    private lateinit var tvTotalTarefas: TextView
    private lateinit var tvContratosAssinatura: TextView
    private lateinit var tvDevolucoesAtraso: TextView
    private lateinit var tvEquipamentosManutencao: TextView
    

    
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
        
        // Estatísticas rápidas (seção de visão geral)
        tvContratosCount = view.findViewById(R.id.tvContratosCount)
        tvClientesCount = view.findViewById(R.id.tvClientesCount)
        tvEquipamentosCount = view.findViewById(R.id.tvEquipamentosCount)
        tvDevolucoesCount = view.findViewById(R.id.tvDevolucoesCount)
        
        // Cards de resumo financeiro
        tvReceitaTotal = view.findViewById(R.id.tvReceitaTotal)
        tvContratosAtivos = view.findViewById(R.id.tvContratosAtivos)
        
        // Header elements
        tvCurrentDate = view.findViewById(R.id.tvDataAtual)
        
        // Progress bars e TextViews para métricas de performance
        tvContratosProgress = view.findViewById(R.id.tvContratosProgress)
        tvReceitaProgress = view.findViewById(R.id.tvReceitaProgress)
        tvClientesProgress = view.findViewById(R.id.tvClientesProgress)
        tvSatisfacaoProgress = view.findViewById(R.id.tvSatisfacaoProgress)
        progressBarContratos = view.findViewById(R.id.progressBarContratos)
        progressBarReceita = view.findViewById(R.id.progressBarReceita)
        progressBarClientes = view.findViewById(R.id.progressBarClientes)
        progressBarSatisfacao = view.findViewById(R.id.progressBarSatisfacao)
        
        // TextViews para tarefas pendentes
        tvTotalTarefas = view.findViewById(R.id.tvTotalTarefas)
        tvContratosAssinatura = view.findViewById(R.id.tvContratosAssinatura)
        tvDevolucoesAtraso = view.findViewById(R.id.tvDevolucoesAtraso)
        tvEquipamentosManutencao = view.findViewById(R.id.tvEquipamentosManutencao)
        
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
        
        // Cards financeiros - navegar para tela financeira
        view.findViewById<View>(R.id.cardReceitaTotal)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card receita total clicado")
            
            // Navegar para a página financeira
            findNavController().navigate(R.id.financialFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_financial)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        view.findViewById<View>(R.id.cardContratosAtivos)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card contratos ativos clicado")
            
            // Navegar para a página financeira
            findNavController().navigate(R.id.financialFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_financial)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        

        
        // Botões de ação rápida
        view.findViewById<View>(R.id.btnNovoContrato)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Botão novo contrato clicado")
            
            // Navegar para a página de contratos
            findNavController().navigate(R.id.contratosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        view.findViewById<View>(R.id.btnNovoCliente)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Botão novo cliente clicado")
            
            // Navegar para a página de clientes
            findNavController().navigate(R.id.clientesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_clientes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Cards da seção Visão Geral do Negócio
        view.findViewById<View>(R.id.cardContratosOverview)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card overview de contratos clicado")
            
            // Navegar para a página de contratos
            findNavController().navigate(R.id.contratosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        view.findViewById<View>(R.id.cardClientesOverview)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card overview de clientes clicado")
            
            // Navegar para a página de clientes
            findNavController().navigate(R.id.clientesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_clientes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        view.findViewById<View>(R.id.cardEquipamentosOverview)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card overview de equipamentos clicado")
            
            // Navegar para a página de equipamentos
            findNavController().navigate(R.id.equipamentosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_equipamentos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        view.findViewById<View>(R.id.cardDevolucoesOverview)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card overview de devoluções clicado")
            
            // Navegar para a página de devoluções
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
                
                // Atualizar cards de resumo financeiro
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contratos ativos...")
                tvContratosAtivos.text = stats.contratos.toString()
                

                
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
        
        // Observar métricas financeiras
        viewModel.financialMetrics.observe(viewLifecycleOwner) { metrics ->
            if (metrics != null) {
                LogUtils.info("DashboardFragment", "💰 ========== ATUALIZANDO MÉTRICAS FINANCEIRAS ==========")
                LogUtils.info("DashboardFragment", "💰 Receita Total: R$ ${String.format("%.2f", metrics.valorTotalAtivo)}")
                
                // Atualizar receita total com dados reais da API
                LogUtils.debug("DashboardFragment", "🔄 Atualizando receita total com dados da API...")
                tvReceitaTotal.text = "R$ ${String.format("%.2f", metrics.valorTotalAtivo)}"
                
                LogUtils.info("DashboardFragment", "✅ Métricas financeiras atualizadas na interface!")
        } else {
                LogUtils.warning("DashboardFragment", "⚠️ Métricas financeiras não disponíveis - usando valor padrão")
                // Fallback para valor padrão se a API não retornar dados
                tvReceitaTotal.text = "R$ 45.280,00"
        }
        }
        
        // Observar métricas de progresso
        viewModel.progressMetrics.observe(viewLifecycleOwner) { metrics ->
            if (metrics != null) {
                LogUtils.info("DashboardFragment", "📊 ========== ATUALIZANDO MÉTRICAS DE PROGRESSO ==========")
                
                // Atualizar contratos
                LogUtils.debug("DashboardFragment", "🔄 Atualizando progresso de contratos...")
                tvContratosProgress.text = "${metrics.contratosAtual}/${metrics.contratosMeta}"
                progressBarContratos.progress = metrics.contratosPercentual
                
                // Atualizar receita
                LogUtils.debug("DashboardFragment", "🔄 Atualizando progresso de receita...")
                val receitaAtualFormatted = if (metrics.receitaAtual >= 1000) {
                    "${(metrics.receitaAtual / 1000).toInt()}k"
                } else {
                    String.format("%.0f", metrics.receitaAtual)
                }
                val receitaMetaFormatted = if (metrics.receitaMeta >= 1000) {
                    "${(metrics.receitaMeta / 1000).toInt()}k"
                } else {
                    String.format("%.0f", metrics.receitaMeta)
                }
                tvReceitaProgress.text = "$receitaAtualFormatted/$receitaMetaFormatted"
                progressBarReceita.progress = metrics.receitaPercentual
                
                // Atualizar clientes
                LogUtils.debug("DashboardFragment", "🔄 Atualizando progresso de clientes...")
                tvClientesProgress.text = "${metrics.clientesAtual}/${metrics.clientesMeta}"
                progressBarClientes.progress = metrics.clientesPercentual
                
                // Atualizar satisfação
                LogUtils.debug("DashboardFragment", "🔄 Atualizando satisfação...")
                tvSatisfacaoProgress.text = "${metrics.satisfacaoPercentual}%"
                progressBarSatisfacao.progress = metrics.satisfacaoPercentual
                
                LogUtils.info("DashboardFragment", "✅ Métricas de progresso atualizadas na interface!")
                LogUtils.debug("DashboardFragment", "📊 Contratos: ${metrics.contratosPercentual}%, Receita: ${metrics.receitaPercentual}%, Clientes: ${metrics.clientesPercentual}%, Satisfação: ${metrics.satisfacaoPercentual}%")
        } else {
                LogUtils.warning("DashboardFragment", "⚠️ Métricas de progresso não disponíveis - mantendo valores padrão")
            }
        }
        
        // Observar tarefas pendentes
        viewModel.taskMetrics.observe(viewLifecycleOwner) { tasks ->
            if (tasks != null) {
                LogUtils.info("DashboardFragment", "📋 ========== ATUALIZANDO TAREFAS PENDENTES ==========")
                
                // Atualizar total de tarefas
                LogUtils.debug("DashboardFragment", "🔄 Atualizando total de tarefas...")
                tvTotalTarefas.text = tasks.totalTarefas.toString()
                
                // Atualizar contratos aguardando assinatura
                LogUtils.debug("DashboardFragment", "🔄 Atualizando contratos aguardando assinatura...")
                val contratosText = if (tasks.contratosAguardandoAssinatura == 1) {
                    "${tasks.contratosAguardandoAssinatura} contrato aguardando assinatura"
                } else {
                    "${tasks.contratosAguardandoAssinatura} contratos aguardando assinatura"
                }
                tvContratosAssinatura.text = contratosText
                
                // Atualizar devoluções em atraso
                LogUtils.debug("DashboardFragment", "🔄 Atualizando devoluções em atraso...")
                val devolucoesText = if (tasks.devolucoesEmAtraso == 1) {
                    "${tasks.devolucoesEmAtraso} devolução em atraso"
                } else {
                    "${tasks.devolucoesEmAtraso} devoluções em atraso"
                }
                tvDevolucoesAtraso.text = devolucoesText
                
                // Atualizar equipamentos para manutenção
                LogUtils.debug("DashboardFragment", "🔄 Atualizando equipamentos para manutenção...")
                val equipamentosText = if (tasks.equipamentosManutencao == 1) {
                    "${tasks.equipamentosManutencao} equipamento para manutenção"
                } else {
                    "${tasks.equipamentosManutencao} equipamentos para manutenção"
                }
                tvEquipamentosManutencao.text = equipamentosText
                
                LogUtils.info("DashboardFragment", "✅ Tarefas pendentes atualizadas na interface!")
                LogUtils.debug("DashboardFragment", "📋 Total: ${tasks.totalTarefas}, Contratos: ${tasks.contratosAguardandoAssinatura}, Devoluções: ${tasks.devolucoesEmAtraso}, Equipamentos: ${tasks.equipamentosManutencao}")
        } else {
                LogUtils.warning("DashboardFragment", "⚠️ Tarefas pendentes não disponíveis - mantendo valores padrão")
            }
        }
        
        LogUtils.info("DashboardFragment", "✅ Observação do ViewModel configurada com sucesso")
    }
    

} 