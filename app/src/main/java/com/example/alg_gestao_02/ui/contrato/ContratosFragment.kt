package com.example.alg_gestao_02.ui.contrato

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
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.ui.contrato.adapter.ContratosAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.alg_gestao_02.utils.Resource

/**
 * Fragment para listagem e gestão de contratos
 */
class ContratosFragment : Fragment(), ContratoDetailsDialogFragment.OnEditRequestListener, ContratoDetailsDialogFragment.OnContratoAtualizadoListener {
    
    private lateinit var viewModel: ContratosViewModel
    private lateinit var adapter: ContratosAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var viewLoading: View
    private lateinit var viewEmpty: View
    private lateinit var viewError: View
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAddContrato: FloatingActionButton
    private lateinit var tabLayout: TabLayout
    private lateinit var chipGroupFilters: ChipGroup
    
    // Enum para as abas
    enum class ContratoTab {
        TODOS, EM_ANDAMENTO, ARQUIVADOS
    }
    
    private var currentTab: ContratoTab = ContratoTab.TODOS
    private var selectedMonth: Int? = null
    private var selectedYear: Int? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contratos, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ContratosFragment", "onViewCreated - Inicializando tela de contratos")
        
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        
        // Garantir que o estado do contrato detalhado está limpo ao inicializar a tela
        viewModel.limparContratoDetalhado()
        
        observeViewModel()
        
        // Carregar contratos e clientes
        viewModel.loadContratos()
        viewModel.loadClientes()
    }
    
    override fun onResume() {
        super.onResume()
        LogUtils.debug("ContratosFragment", "onResume - Ciclo de vida")
        // Não precisamos recarregar os contratos aqui, pois já fazemos isso em onViewCreated
        // e também quando ocorrem operações de CRUD
    }
    
    override fun onPause() {
        super.onPause()
        LogUtils.debug("ContratosFragment", "onPause - Ciclo de vida")
        
        // Limpar o estado do contrato detalhado para evitar que o diálogo reaparece
        // quando voltar para a tela de contratos
        viewModel.limparContratoDetalhado()
        
        // Limpar qualquer filtro pendente quando sair da tela
        com.example.alg_gestao_02.utils.FilterManager.clearPendingFilter()
        
        // COMENTADO: Não fechar dialogs automaticamente em onPause
        // para permitir que dialogs persistam durante mudanças de orientação
        // e após operações de assinatura
        /*
        val dialog = childFragmentManager.findFragmentByTag("ContratoDetailsDialog")
        if (dialog != null && dialog is ContratoDetailsDialogFragment) {
            LogUtils.debug("ContratosFragment", "Fechando diálogo de detalhes de contrato aberto")
            dialog.dismiss()
        }
        */
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvContratos)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        viewLoading = view.findViewById(R.id.viewLoading)
        viewEmpty = view.findViewById(R.id.viewEmpty)
        viewError = view.findViewById(R.id.viewError)
        etSearch = view.findViewById(R.id.etSearch)
        fabAddContrato = view.findViewById(R.id.fabAddContrato)
        tabLayout = view.findViewById(R.id.tabLayout)
        chipGroupFilters = view.findViewById(R.id.chipGroupFilters)
        
        setupTabs()
        setupMonthFilters()
    }
    
    private fun setupViewModel() {
        val factory = ContratosViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[ContratosViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        LogUtils.debug("ContratosFragment", "Configurando RecyclerView")
        LogUtils.debug("ContratosFragment", "RecyclerView encontrado: $recyclerView")
        
        adapter = ContratosAdapter(
            contratos = emptyList(),
            onItemClick = { contrato ->
                showContratoDetailDialog(contrato)
            },
            onMenuClick = { contrato, view ->
                showPopupMenu(contrato, view)
            }
        )
        
        LogUtils.debug("ContratosFragment", "Adapter criado: $adapter")
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        
        LogUtils.debug("ContratosFragment", "RecyclerView configurado com adapter e layout manager")
    }
    
    /**
     * Configura as abas do TabLayout
     */
    private fun setupTabs() {
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentTab = ContratoTab.TODOS
                        LogUtils.debug("ContratosFragment", "Aba selecionada: TODOS")
                    }
                    1 -> {
                        currentTab = ContratoTab.EM_ANDAMENTO
                        LogUtils.debug("ContratosFragment", "Aba selecionada: EM_ANDAMENTO")
                    }
                    2 -> {
                        currentTab = ContratoTab.ARQUIVADOS
                        LogUtils.debug("ContratosFragment", "Aba selecionada: ARQUIVADOS")
                    }
                }
                aplicarFiltros()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }
    
    /**
     * Configura os chips de filtro por mês
     */
    private fun setupMonthFilters() {
        chipGroupFilters.removeAllViews()
        
        // Chip "Todos" (remove filtro de mês)
        val chipTodos = Chip(requireContext()).apply {
            text = "Todos os meses"
            isCheckable = true
            isChecked = true
            setOnClickListener {
                selectedMonth = null
                selectedYear = null
                LogUtils.debug("ContratosFragment", "Filtro de mês removido")
                aplicarFiltros()
            }
        }
        chipGroupFilters.addView(chipTodos)
        
        // Adicionar chips para os últimos 12 meses
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("MMM/yy", Locale("pt", "BR"))
        
        for (i in 0 until 12) {
            val mes = calendar.get(Calendar.MONTH) + 1
            val ano = calendar.get(Calendar.YEAR)
            val dataFormatada = formatter.format(calendar.time)
            
            val chip = Chip(requireContext()).apply {
                text = dataFormatada.replaceFirstChar { it.uppercase() }
                isCheckable = true
                tag = Pair(ano, mes)
                setOnClickListener {
                    selectedYear = ano
                    selectedMonth = mes
                    LogUtils.debug("ContratosFragment", "Filtro de mês selecionado: $mes/$ano")
                    aplicarFiltros()
                }
            }
            chipGroupFilters.addView(chip)
            
            calendar.add(Calendar.MONTH, -1)
        }
    }
    
    /**
     * Aplica os filtros de aba e mês aos contratos
     */
    private fun aplicarFiltros() {
        viewModel.aplicarFiltros(currentTab, selectedYear, selectedMonth, etSearch.text.toString())
    }
    
    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("ContratosFragment", "Atualizando lista de contratos via swipe refresh")
            viewModel.loadContratos()
        }
        
        // Configurar listener do FAB
        fabAddContrato.setOnClickListener {
            LogUtils.debug("ContratosFragment", "FAB adicionar contrato clicado")
            showCadastroContratoDialog()
        }
        
        // Configurar listener para busca
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val searchTerm = etSearch.text.toString().trim()
                LogUtils.debug("ContratosFragment", "Buscando por: $searchTerm")
                aplicarFiltros()
                true
            } else {
                false
            }
        }
        
        // Listener para mudanças no texto de busca (busca em tempo real)
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                aplicarFiltros()
            }
        })
    }
    
    private fun observeViewModel() {
        LogUtils.debug("ContratosFragment", "Configurando observadores do ViewModel")
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            LogUtils.debug("ContratosFragment", "Observer uiState ativado: ${state.javaClass.simpleName}")
            swipeRefresh.isRefreshing = false
            
            when (state) {
                is UiState.Loading -> {
                    LogUtils.debug("ContratosFragment", "Estado: Loading - Mostrando tela de carregamento")
                    LogUtils.debug("ContratosFragment", "viewLoading visibilidade: ${viewLoading.visibility}")
                    viewLoading.visibility = View.VISIBLE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    LogUtils.debug("ContratosFragment", "viewLoading visibilidade após: ${viewLoading.visibility}")
                }
                
                is UiState.Success -> {
                    LogUtils.debug("ContratosFragment", "Estado: Success - Contratos: ${state.data.size}")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    
                    LogUtils.debug("ContratosFragment", "RecyclerView visibilidade: ${recyclerView.visibility}")
                    LogUtils.debug("ContratosFragment", "Adapter atual: ${adapter}")
                    
                    // Atualizar o adaptador com os contratos
                    adapter.updateContratos(state.data)
                    LogUtils.debug("ContratosFragment", "Adapter atualizado com ${state.data.size} itens")
                    
                    // Forçar notificação do adapter
                    adapter.notifyDataSetChanged()
                    LogUtils.debug("ContratosFragment", "Adapter.notifyDataSetChanged() chamado")
                }
                
                is UiState.Empty -> {
                    LogUtils.debug("ContratosFragment", "Estado: Empty")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                
                is UiState.Error -> {
                    LogUtils.debug("ContratosFragment", "Estado: Error - ${state.message}")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        // Observar o contratoDetalhado para exibir o diálogo de detalhes quando estiver pronto
        viewModel.contratoDetalhado.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    // Opcional: mostrar um indicador de carregamento enquanto carrega os detalhes
                    LogUtils.debug("ContratosFragment", "Carregando detalhes do contrato...")
                }
                
                is UiState.Success -> {
                    if (state.data != null) {
                        LogUtils.debug("ContratosFragment", "Detalhes do contrato carregados com sucesso. ID: ${state.data.id}, equipamentos: ${state.data.equipamentos.size}")
                        showContratoDetailDialogWithDetails(state.data)
                    } else {
                        LogUtils.error("ContratosFragment", "Contrato carregado é nulo")
                        Toast.makeText(context, "Erro ao carregar detalhes do contrato", Toast.LENGTH_SHORT).show()
                    }
                }
                
                is UiState.Error -> {
                    LogUtils.error("ContratosFragment", "Erro ao carregar detalhes do contrato: ${state.message}")
                    Toast.makeText(context, "Erro: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                
                else -> {
                    // Nada a fazer para outros estados
                }
            }
        }
    }
    
    private fun showContratoDetailDialog(contrato: Contrato) {
        LogUtils.debug("ContratosFragment", "Exibindo detalhes para o contrato: ${contrato.contratoNum}")
        // Em vez de exibir o diálogo diretamente com o contrato da lista,
        // carregamos os detalhes completos primeiro
        viewModel.carregarContratoComDetalhes(contrato.id)
        // O diálogo será exibido no observer de contratoDetalhado quando os dados estiverem prontos
    }
    
    // Nova função para exibir o diálogo com o contrato que já tem os detalhes completos
    private fun showContratoDetailDialogWithDetails(contratoCompleto: Contrato) {
        LogUtils.debug("ContratosFragment", "Exibindo dialog com contrato completo: ${contratoCompleto.contratoNum}")
        val dialog = ContratoDetailsDialogFragment.newInstance(contratoCompleto)
        dialog.setOnEditRequestListener(this)
        dialog.setOnContratoAtualizadoListener(this)
        dialog.show(childFragmentManager, "ContratoDetailsDialog")
    }
    
    override fun onEditRequested(contrato: Contrato) {
        LogUtils.debug("ContratosFragment", "Pedido de edição recebido para o contrato: ${contrato.contratoNum}")
        showCadastroContratoDialog(contrato)
    }
    
    override fun onContratoAtualizado() {
        LogUtils.debug("ContratosFragment", "🔔 CALLBACK RECEBIDO: Contrato foi atualizado, recarregando lista")
        viewModel.loadContratos()
        LogUtils.debug("ContratosFragment", "✅ Recarga da lista iniciada")
    }
    
    /**
     * Callback chamado quando um contrato é editado/modificado
     * Verifica se precisa regenerar devoluções
     */
    fun onContratoEditado(contratoId: Int, contratoNum: String) {
        LogUtils.debug("ContratosFragment", "🔧 Contrato editado: #$contratoNum (ID: $contratoId)")
        
        // Verificar se precisa regenerar devoluções
        lifecycleScope.launch {
            try {
                val regenerarRepository = com.example.alg_gestao_02.data.repository.RegenerarDevolucaoRepository()
                val result = regenerarRepository.verificarNecessidadeRegeneracao(contratoId)
                
                when (result) {
                    is Resource.Success -> {
                        val precisaRegenerar = result.data
                        LogUtils.debug("ContratosFragment", "🔍 Precisa regenerar devoluções: $precisaRegenerar")
                        
                        if (precisaRegenerar) {
                            mostrarDialogRegenerarDevolucoes(contratoId, contratoNum)
                        }
                    }
                    is Resource.Error -> {
                        LogUtils.error("ContratosFragment", "Erro ao verificar regeneração: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Não fazer nada
                    }
                }
            } catch (e: Exception) {
                LogUtils.error("ContratosFragment", "Erro ao verificar regeneração", e)
            }
        }
    }
    
    /**
     * Mostra dialog perguntando se quer regenerar devoluções
     */
    private fun mostrarDialogRegenerarDevolucoes(contratoId: Int, contratoNum: String) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contrato Modificado")
            .setMessage("O contrato #$contratoNum foi modificado. Deseja regenerar as devoluções para refletir as mudanças?\n\n• Devoluções antigas serão marcadas como obsoletas\n• Novas devoluções serão criadas com base nos equipamentos atuais")
            .setPositiveButton("Sim, Regenerar") { _, _ ->
                regenerarDevolucoes(contratoId, contratoNum)
            }
            .setNegativeButton("Não, Manter Como Está") { _, _ ->
                LogUtils.debug("ContratosFragment", "Usuário optou por não regenerar devoluções")
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * Executa a regeneração das devoluções
     */
    private fun regenerarDevolucoes(contratoId: Int, contratoNum: String) {
        lifecycleScope.launch {
            val progressDialog = android.app.ProgressDialog(requireContext()).apply {
                setMessage("Regenerando devoluções do contrato #$contratoNum...")
                setCancelable(false)
                show()
            }
            
            try {
                val regenerarRepository = com.example.alg_gestao_02.data.repository.RegenerarDevolucaoRepository()
                val result = regenerarRepository.regenerarDevolucoes(contratoId)
                
                progressDialog.dismiss()
                
                when (result) {
                    is Resource.Success -> {
                        val data = result.data.get("data") as? Map<String, Any>
                        val novasCount = (data?.get("novasDevolucoesS") as? Double)?.toInt() ?: 0
                        val obsoletasCount = (data?.get("devolucoesSObsoletas") as? Double)?.toInt() ?: 0
                        
                        val mensagem = "✅ Devoluções regeneradas com sucesso!\n\n" +
                                "• $obsoletasCount devoluções marcadas como obsoletas\n" +
                                "• $novasCount novas devoluções criadas"
                        
                        android.widget.Toast.makeText(requireContext(), mensagem, android.widget.Toast.LENGTH_LONG).show()
                        
                        LogUtils.debug("ContratosFragment", "✅ Devoluções regeneradas: $novasCount novas, $obsoletasCount obsoletas")
                    }
                    is Resource.Error -> {
                        android.widget.Toast.makeText(
                            requireContext(), 
                            "Erro ao regenerar devoluções: ${result.message}", 
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        LogUtils.error("ContratosFragment", "Erro na regeneração: ${result.message}")
                    }
                    is Resource.Loading -> {
                        // Não fazer nada
                    }
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                android.widget.Toast.makeText(
                    requireContext(), 
                    "Erro ao regenerar devoluções: ${e.message}", 
                    android.widget.Toast.LENGTH_LONG
                ).show()
                LogUtils.error("ContratosFragment", "Erro na regeneração", e)
            }
        }
    }
    
    private fun showCadastroContratoDialog(contratoParaEditar: Contrato? = null) {
        val dialog = CadastroContratoDialogFragment.newInstance(contratoParaEditar)
        dialog.setOnContratoSavedListener { contratoSalvo ->
            LogUtils.debug("ContratosFragment", "Contrato salvo/atualizado, recarregando lista.")
            
            // Se é uma edição (não criação), verificar regeneração de devoluções
            if (contratoParaEditar != null) {
                LogUtils.debug("ContratosFragment", "🔧 Contrato editado detectado, verificando regeneração...")
                onContratoEditado(contratoSalvo.id, contratoSalvo.contratoNum ?: "")
            }
            
            viewModel.loadContratos()
        }
        val dialogTag = if (contratoParaEditar == null) "CadastroContratoDialog" else "EditContratoDialog_${contratoParaEditar.id}"
        dialog.show(childFragmentManager, dialogTag)
    }
    
    private fun showPopupMenu(contrato: Contrato, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_contract_options, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    LogUtils.debug("ContratosFragment", "Menu Editar clicado para contrato: ${contrato.contratoNum}")
                    showCadastroContratoDialog(contrato)
                    true
                }
                
                R.id.menu_delete -> {
                    LogUtils.debug("ContratosFragment", "Excluindo contrato: ${contrato.contratoNum}")
                    confirmarExclusao(contrato)
                    true
                }
                
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun confirmarExclusao(contrato: Contrato) {
        // Verificar role do usuário
        val sessionManager = SessionManager(requireContext())
        val userRole = sessionManager.getUserRole()
        val isAdmin = userRole == "admin"
        
        LogUtils.debug("ContratosFragment", "👤 Usuário role: $userRole")
        LogUtils.debug("ContratosFragment", "🔐 É admin: $isAdmin")
        LogUtils.debug("ContratosFragment", "📋 Status contrato: ${contrato.status_assinatura}")
        
        // Verificar se pode excluir
        val (podeExcluir, mensagem) = contrato.podeExcluir(isAdmin, forcar = isAdmin)
        
        if (!podeExcluir && !isAdmin) {
            LogUtils.warning("ContratosFragment", "❌ Exclusão negada: $mensagem")
            
            // Mostrar diálogo informativo para não-admins
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("❌ Exclusão Não Permitida")
                .setMessage(mensagem)
                .setPositiveButton("Entendi") { dialog, _ ->
                    dialog.dismiss()
                }
                .setIcon(R.drawable.ic_error)
                .show()
            return
        }
        
        // Configurar mensagem do diálogo baseada no status do contrato
        val titulo: String
        val mensagemDialog: String
        val icone: Int
        
        when (contrato.status_assinatura) {
            "ASSINADO" -> {
                titulo = "Excluir Contrato Assinado"
                mensagemDialog = if (isAdmin) {
                    "Este contrato possui assinatura digital!\n\n" +
                    "📋 Contrato: #${contrato.contratoNum}\n" +
                    "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                    "📅 Status: ASSINADO ✍️\n\n" +
                    "Deseja realmente excluir este contrato assinado?"
                } else {
                    "❌ Este contrato possui assinatura digital e não pode ser excluído.\n\n" +
                    "📞 Entre em contato com o administrador do sistema."
                }
                icone = R.drawable.ic_warning
            }
            
            "PENDENTE" -> {
                titulo = "🔄 Excluir Contrato Pendente"
                mensagemDialog = "📋 Contrato: #${contrato.contratoNum}\n" +
                        "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                        "📅 Status: PENDENTE ⏳\n\n" +
                        "ℹ️ Este contrato está aguardando assinatura.\n\n" +
                        "❓ Deseja realmente excluir este contrato?"
                icone = R.drawable.ic_info
            }
            
            else -> {
                titulo = "🗑️ Excluir Contrato"
                mensagemDialog = "📋 Contrato: #${contrato.contratoNum}\n" +
                        "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                        "📅 Status: NÃO ASSINADO 📝\n\n" +
                        "❓ Deseja realmente excluir este contrato?"
                icone = R.drawable.ic_delete
            }
        }
        
        // Mostrar diálogo de confirmação
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensagemDialog)
            .setPositiveButton(if (contrato.status_assinatura == "ASSINADO") "⚠️ Forçar Exclusão" else "🗑️ Sim, Excluir") { dialog, _ ->
                dialog.dismiss()
                
                // Mostrar mensagem informativa para admin
                if (isAdmin && contrato.status_assinatura == "ASSINADO") {
                    LogUtils.info("ContratosFragment", "⚠️ Admin confirmou exclusão de contrato assinado")
                    Toast.makeText(context, "⚠️ Forçando exclusão de contrato assinado...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "🗑️ Excluindo contrato...", Toast.LENGTH_SHORT).show()
                }
                
                LogUtils.debug("ContratosFragment", "▶️ Usuário confirmou exclusão do contrato ${contrato.contratoNum}")
                viewModel.excluirContrato(contrato.id)
            }
            .setNegativeButton("🚫 Cancelar") { dialog, _ ->
                LogUtils.debug("ContratosFragment", "🚫 Usuário cancelou exclusão do contrato ${contrato.contratoNum}")
                dialog.dismiss()
            }
            .setIcon(icone)
            .setCancelable(true)
            .show()
    }
} 