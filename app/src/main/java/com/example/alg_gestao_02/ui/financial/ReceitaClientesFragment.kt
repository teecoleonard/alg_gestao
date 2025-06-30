package com.example.alg_gestao_02.ui.financial

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ReceitaCliente
import com.example.alg_gestao_02.databinding.FragmentReceitaClientesBinding
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.chip.Chip
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class ReceitaClientesFragment : Fragment() {

    private var _binding: FragmentReceitaClientesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FinancialViewModel
    private lateinit var adapter: ReceitaClienteAdapter

    // Dados originais e filtrados
    private var dadosOriginais: List<ReceitaCliente> = emptyList()
    private var dadosFiltrados: List<ReceitaCliente> = emptyList()

    // Controle de busca com debounce
    private var searchJob: Job? = null
    
    // Controle de timeout de carregamento
    private var loadingTimeoutJob: Job? = null
    private var swipeTimeoutJob: Job? = null

    // Status dos filtros
    private var filtroTexto: String = ""
    private var filtroStatus: StatusPagamento = StatusPagamento.TODOS

    enum class StatusPagamento {
        TODOS, PAGOS, DEVENDO, VENCIDOS
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReceitaClientesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.info("ReceitaClientesFragment", "🚀 ========== INICIANDO RECEITA CLIENTES ==========")
        
        setupViewModel()
        setupToolbar()
        setupRecyclerView()
        setupFilters()
        setupObservers()
        setupSwipeRefresh()
        setupNotificacao()
        
        // Verificar se dados já estão disponíveis no ViewModel
        val dadosExistentes = viewModel.receitaPorCliente.value
        if (dadosExistentes != null && dadosExistentes.clientes.isNotEmpty()) {
            LogUtils.info("ReceitaClientesFragment", "✅ Dados já disponíveis no ViewModel: ${dadosExistentes.clientes.size} clientes")
            dadosOriginais = dadosExistentes.clientes
            dadosFiltrados = dadosOriginais
            atualizarEstatisticas(dadosExistentes.clientes, dadosExistentes.totalGeral)
            aplicarFiltros()
            // Garantir que loading esteja desabilitado
            mostrarLoading(false)
        } else {
            LogUtils.info("ReceitaClientesFragment", "📥 Carregando dados iniciais...")
            // Mostrar loading inicial
            mostrarLoading(true)
            
            // Carregar dados iniciais apenas uma vez
            viewModel.refreshFinancialData()
            
            // Timeout de segurança de 8 segundos
            loadingTimeoutJob = lifecycleScope.launch {
                delay(8000)
                if (dadosOriginais.isEmpty()) {
                    LogUtils.warning("ReceitaClientesFragment", "⚠️ Timeout: Usando dados simulados")
                    usarDadosSimulados()
                }
            }
        }
    }

    private fun setupViewModel() {
        val factory = FinancialViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[FinancialViewModel::class.java]
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = ReceitaClienteAdapter { cliente ->
            LogUtils.debug("ReceitaClientesFragment", "Cliente selecionado: ${cliente.clienteNome}")
            // TODO: Implementar ação quando cliente for selecionado (dialog de detalhes)
        }

        binding.recyclerClientes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReceitaClientesFragment.adapter
        }
    }

    private fun setupFilters() {
        // Filtro de texto com debounce
        binding.etBuscarCliente.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(300) // Debounce de 300ms
                    filtroTexto = s.toString().trim()
                    aplicarFiltros()
                }
            }
        })

        // Filtros de status (chips)
        binding.chipGroupStatus.setOnCheckedStateChangeListener { _, checkedIds ->
            when {
                binding.chipTodos.isChecked -> filtroStatus = StatusPagamento.TODOS
                binding.chipPagos.isChecked -> filtroStatus = StatusPagamento.PAGOS
                binding.chipDevendo.isChecked -> filtroStatus = StatusPagamento.DEVENDO
                binding.chipVencidos.isChecked -> filtroStatus = StatusPagamento.VENCIDOS
                else -> {
                    // Se nenhum chip está selecionado, selecionar "Todos"
                    binding.chipTodos.isChecked = true
                    filtroStatus = StatusPagamento.TODOS
                }
            }
            aplicarFiltros()
        }
    }

    private fun setupObservers() {
        // Observer para receita por cliente - ESTE É O PRINCIPAL!
        viewModel.receitaPorCliente.observe(viewLifecycleOwner) { response ->
            LogUtils.info("ReceitaClientesFragment", "✅ Dados de receita recebidos: ${response.clientes.size} clientes")
            
            // Cancelar timeouts já que dados chegaram
            loadingTimeoutJob?.cancel()
            swipeTimeoutJob?.cancel()
            
            dadosOriginais = response.clientes
            dadosFiltrados = dadosOriginais
            
            // ========== PARAR LOADING IMEDIATAMENTE ==========
            binding.swipeRefresh.isRefreshing = false
            mostrarLoading(false)
            LogUtils.info("ReceitaClientesFragment", "🛑 Loading parado - dados recebidos")
            
            // Atualizar estatísticas
            atualizarEstatisticas(response.clientes, response.totalGeral)
            
            // Aplicar filtros atuais
            aplicarFiltros()
        }

        // Observer para UI State - SECUNDÁRIO
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    LogUtils.debug("ReceitaClientesFragment", "⏳ Estado: Loading")
                    // Só mostrar loading se ainda não temos dados
                    if (dadosOriginais.isEmpty()) {
                        binding.swipeRefresh.isRefreshing = true
                        mostrarLoading(true)
                    }
                }
                is UiState.Success -> {
                    LogUtils.debug("ReceitaClientesFragment", "✅ Estado: Success")
                    // Se chegamos aqui mas ainda não temos dados, usar simulados
                    if (dadosOriginais.isEmpty()) {
                        LogUtils.warning("ReceitaClientesFragment", "⚠️ Success mas sem dados, usando simulados")
                        usarDadosSimulados()
                    }
                }
                is UiState.Error -> {
                    LogUtils.error("ReceitaClientesFragment", "❌ Estado: Error - ${state.message}")
                    binding.swipeRefresh.isRefreshing = false
                    mostrarLoading(false)
                    
                    // Em caso de erro, usar dados simulados
                    if (dadosOriginais.isEmpty()) {
                        LogUtils.info("ReceitaClientesFragment", "🔄 Erro na API, usando dados simulados...")
                        usarDadosSimulados()
                    } else {
                        Toast.makeText(requireContext(), "Erro ao atualizar: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    LogUtils.debug("ReceitaClientesFragment", "🤷 Estado desconhecido: $state")
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            LogUtils.debug("ReceitaClientesFragment", "🔄 Atualizando dados via swipe...")
            
            // Limpar dados atuais para forçar recarregamento
            dadosOriginais = emptyList()
            dadosFiltrados = emptyList()
            adapter.updateReceitas(emptyList())
            
            // Carregar dados novamente
            viewModel.refreshFinancialData()
            
            // Timeout de segurança para SwipeRefresh
            swipeTimeoutJob?.cancel()
            swipeTimeoutJob = lifecycleScope.launch {
                delay(8000) // 8 segundos para swipe refresh
                if (binding.swipeRefresh.isRefreshing) {
                    LogUtils.warning("ReceitaClientesFragment", "⚠️ SwipeRefresh timeout, parando...")
                    binding.swipeRefresh.isRefreshing = false
                    usarDadosSimulados()
                }
            }
        }
        
        // Configurar cores do SwipeRefresh
        binding.swipeRefresh.setColorSchemeResources(
            R.color.primary,
            R.color.success,
            R.color.warning
        )
    }


    
    private fun usarDadosSimulados() {
        val dadosSimulados = listOf(
            ReceitaCliente(
                clienteId = 1,
                clienteNome = "CENTRO DE TREINAMENTO BASE SPORTING CLUBE LTDA",
                valorMensal = 990.0,
                totalContratos = 2,
                contratosAtivos = 2,
                ticketMedio = 495.0
            ),
            ReceitaCliente(
                clienteId = 2,
                clienteNome = "EMPRESA EXEMPLO DOIS LTDA",
                valorMensal = 450.0,
                totalContratos = 1,
                contratosAtivos = 1,
                ticketMedio = 450.0
            ),
            ReceitaCliente(
                clienteId = 3,
                clienteNome = "CLIENTE TESTE TRÊS",
                valorMensal = 840.0,
                totalContratos = 3,
                contratosAtivos = 3,
                ticketMedio = 280.0
            )
        )
        
        LogUtils.info("ReceitaClientesFragment", "📋 Usando ${dadosSimulados.size} clientes simulados")
        
        dadosOriginais = dadosSimulados
        dadosFiltrados = dadosOriginais
        
        // ========== PARAR LOADING COMPLETAMENTE ==========
        binding.swipeRefresh.isRefreshing = false
        mostrarLoading(false)
        LogUtils.info("ReceitaClientesFragment", "🛑 Loading parado - dados simulados carregados")
        
        // Atualizar estatísticas
        val totalSimulado = dadosSimulados.sumOf { it.valorMensal }
        atualizarEstatisticas(dadosSimulados, totalSimulado)
        
        // Aplicar filtros
        aplicarFiltros()
        
        Toast.makeText(requireContext(), "Dados simulados carregados (API indisponível)", Toast.LENGTH_SHORT).show()
    }

    private fun aplicarFiltros() {
        LogUtils.debug("ReceitaClientesFragment", "🔍 Aplicando filtros: texto='$filtroTexto', status=$filtroStatus")
        
        var listaFiltrada = dadosOriginais

        // Filtro por texto (nome do cliente)
        if (filtroTexto.isNotEmpty()) {
            listaFiltrada = listaFiltrada.filter { cliente ->
                cliente.clienteNome.contains(filtroTexto, ignoreCase = true)
            }
        }

        // Filtro por status de pagamento
        listaFiltrada = when (filtroStatus) {
            StatusPagamento.TODOS -> listaFiltrada
            StatusPagamento.PAGOS -> listaFiltrada.filter { simularStatusPagamento(it) == "PAGO" }
            StatusPagamento.DEVENDO -> listaFiltrada.filter { simularStatusPagamento(it) == "DEVENDO" }
            StatusPagamento.VENCIDOS -> listaFiltrada.filter { simularStatusPagamento(it) == "VENCIDO" }
        }

        dadosFiltrados = listaFiltrada
        
        // Atualizar adapter
        adapter.updateReceitas(dadosFiltrados)
        
        // Mostrar/esconder estado vazio
        if (dadosFiltrados.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerClientes.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerClientes.visibility = View.VISIBLE
        }

        // Atualizar estatísticas com dados filtrados
        val totalFiltrado = dadosFiltrados.sumOf { it.valorMensal }
        atualizarEstatisticas(dadosFiltrados, totalFiltrado)
        
        LogUtils.debug("ReceitaClientesFragment", "📊 Filtros aplicados: ${dadosFiltrados.size} clientes")
    }

    private fun atualizarEstatisticas(clientes: List<ReceitaCliente>, totalReceita: Double) {
        val formato = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        
        binding.tvTotalClientes.text = clientes.size.toString()
        binding.tvTotalReceita.text = formato.format(totalReceita)
    }

    private fun mostrarLoading(mostrar: Boolean) {
        try {
            LogUtils.debug("ReceitaClientesFragment", "🔄 Mostrando loading: $mostrar")
            
            // Controlar o loading overlay
            binding.loadingOverlay.root.visibility = if (mostrar) View.VISIBLE else View.GONE
            
            // Controlar o SwipeRefreshLayout apenas se não estivermos fazendo swipe
            if (!binding.swipeRefresh.isRefreshing) {
                binding.swipeRefresh.isRefreshing = mostrar
            }
        } catch (e: Exception) {
            LogUtils.warning("ReceitaClientesFragment", "⚠️ Erro ao mostrar loading: ${e.message}")
        }
    }

    // Função temporária para simular status de pagamento
    // TODO: Implementar lógica real baseada em dados do servidor
    private fun simularStatusPagamento(cliente: ReceitaCliente): String {
        // Por enquanto, vou simular baseado no ID do cliente
        return when {
            cliente.clienteId % 3 == 0 -> "PAGO"
            cliente.clienteId % 3 == 1 -> "DEVENDO"
            else -> "VENCIDO"
        }
    }

    /**
     * Configura a notificação explicativa
     */
    private fun setupNotificacao() {
        // Verificar se deve mostrar a notificação
        verificarNotificacaoReceita()
        
        // Configurar listener do botão fechar
        binding.btnFecharNotificacaoReceita.setOnClickListener {
            fecharNotificacaoReceita()
        }
    }
    
    /**
     * Verifica se deve mostrar a notificação explicativa
     */
    private fun verificarNotificacaoReceita() {
        val sharedPrefs = requireContext().getSharedPreferences("alg_gestao_notifications", android.content.Context.MODE_PRIVATE)
        val notificacaoFechada = sharedPrefs.getBoolean("receita_explanation_closed", false)
        
        if (!notificacaoFechada) {
            LogUtils.debug("ReceitaClientesFragment", "📢 Mostrando notificação explicativa")
            binding.cardNotificacaoReceita.visibility = View.VISIBLE
        } else {
            LogUtils.debug("ReceitaClientesFragment", "🔕 Notificação já foi fechada pelo usuário")
            binding.cardNotificacaoReceita.visibility = View.GONE
        }
    }
    
    /**
     * Fecha a notificação explicativa com animação
     */
    private fun fecharNotificacaoReceita() {
        LogUtils.info("ReceitaClientesFragment", "🔕 Fechando notificação explicativa")
        
        // Animação de fade out
        val fadeOut = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                binding.cardNotificacaoReceita.visibility = View.GONE
            }
        })
        
        binding.cardNotificacaoReceita.startAnimation(fadeOut)
        
        // Salvar no SharedPreferences que foi fechada
        val sharedPrefs = requireContext().getSharedPreferences("alg_gestao_notifications", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("receita_explanation_closed", true).apply()
        
        LogUtils.info("ReceitaClientesFragment", "✅ Notificação marcada como fechada permanentemente")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        loadingTimeoutJob?.cancel()
        swipeTimeoutJob?.cancel()
        _binding = null
    }

} 