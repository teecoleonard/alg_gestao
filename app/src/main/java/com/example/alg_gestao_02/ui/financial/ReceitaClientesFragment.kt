package com.example.alg_gestao_02.ui.financial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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

    // Controle de timeout de carregamento
    private var loadingTimeoutJob: Job? = null
    private var swipeTimeoutJob: Job? = null

    // Removidos filtros de período - sempre mostra dados gerais

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
        adapter = ReceitaClienteAdapter()

        binding.recyclerClientes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReceitaClientesFragment.adapter
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
            
            // Sempre usar período null (sem filtro específico) para dados gerais
            adapter.updatePeriodo(null)
            LogUtils.debug("ReceitaClientesFragment", "📅 Adapter configurado para dados gerais (sem período específico)")
            
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
            
            // Sempre recarregar dados gerais (sem filtro)
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
        // Sempre usar dados gerais (sem filtro) na lista de clientes
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
        
        // Sempre usar período null para dados gerais
        adapter.updatePeriodo(null)
        LogUtils.debug("ReceitaClientesFragment", "📅 Dados simulados configurados (sem período específico)")
        
        // ========== PARAR LOADING COMPLETAMENTE ==========
        binding.swipeRefresh.isRefreshing = false
        mostrarLoading(false)
        LogUtils.info("ReceitaClientesFragment", "🛑 Loading parado - dados simulados carregados")
        
        // Não há filtros nesta tela - sempre dados gerais
        
        // Atualizar estatísticas
        val totalSimulado = dadosSimulados.sumOf { it.valorMensal }
        atualizarEstatisticas(dadosSimulados, totalSimulado)
        
        // Aplicar filtros
        aplicarFiltros()
        
        val mensagemToast = if (dadosSimulados.isEmpty()) {
            "API indisponível - Nenhum cliente encontrado"
        } else {
            "Dados simulados carregados (API indisponível)"
        }
        Toast.makeText(requireContext(), mensagemToast, Toast.LENGTH_SHORT).show()
    }

    private fun aplicarFiltros() {
        LogUtils.debug("ReceitaClientesFragment", "🔍 Aplicando filtros")
        
        // Por enquanto, apenas copia os dados originais (sem filtros)
        dadosFiltrados = dadosOriginais
        
        // Atualizar adapter
        adapter.updateReceitas(dadosFiltrados)
        
        // Mostrar/esconder estado vazio com mensagem contextual
        atualizarEstadoVazio()

        // Atualizar estatísticas com dados filtrados
        val totalFiltrado = dadosFiltrados.sumOf { it.valorMensal }
        atualizarEstatisticas(dadosFiltrados, totalFiltrado)
        
        LogUtils.debug("ReceitaClientesFragment", "📊 Filtros aplicados: ${dadosFiltrados.size} clientes")
    }

    private fun atualizarEstadoVazio() {
        if (dadosFiltrados.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.recyclerClientes.visibility = View.GONE
            
            // Sempre mostrar mensagem de lista geral vazia
            binding.tvIconeVazio.text = "👥"
            binding.tvMensagemVazia.text = "Nenhum cliente encontrado"
            binding.tvSubtituloVazio.text = "Não há clientes com receita cadastrados no sistema. Clique em um cliente para ver relatórios mensais detalhados."
            binding.tvSubtituloVazio.visibility = View.VISIBLE
            
            LogUtils.info("ReceitaClientesFragment", "📭 Nenhum cliente encontrado na lista geral")
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.recyclerClientes.visibility = View.VISIBLE
        }
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
        loadingTimeoutJob?.cancel()
        swipeTimeoutJob?.cancel()
        _binding = null
    }

} 