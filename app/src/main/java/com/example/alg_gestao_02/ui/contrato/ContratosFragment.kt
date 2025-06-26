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
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment para listagem e gestão de contratos
 */
class ContratosFragment : Fragment(), ContratoDetailsDialogFragment.OnEditRequestListener, ContratoDetailsDialogFragment.OnContratoAtualizadoListener {
    
    private lateinit var viewModel: ContratosViewModel
    private lateinit var adapter: ContratosAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabNovoContrato: FloatingActionButton
    private lateinit var viewLoading: View
    private lateinit var viewEmpty: View
    private lateinit var viewError: View
    private lateinit var etSearch: TextInputEditText
    
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
        
        // Verificar se há filtro pendente no FilterManager
        val pendingFilter = com.example.alg_gestao_02.utils.FilterManager.consumePendingContractsFilter()
        if (pendingFilter != null) {
            LogUtils.info("ContratosFragment", "Aplicando filtro pendente para cliente: ${pendingFilter.clienteNome} (ID: ${pendingFilter.clienteId})")
            
            // Aplicar o filtro no campo de busca
            etSearch.setText(pendingFilter.clienteNome)
            
            // Definir termo de busca no ViewModel
            viewModel.setSearchTerm(pendingFilter.clienteNome)
            
            // Mostrar toast informativo sobre o filtro aplicado
            Toast.makeText(requireContext(), "Mostrando contratos de: ${pendingFilter.clienteNome}", Toast.LENGTH_SHORT).show()
        } else {
            // Carregar contratos normalmente se não houver filtro
            LogUtils.debug("ContratosFragment", "Carregando lista de contratos inicialmente")
            viewModel.loadContratos()
        }
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
        
        // Fechar qualquer diálogo que possa estar aberto
        val dialog = childFragmentManager.findFragmentByTag("ContratoDetailsDialog")
        if (dialog != null && dialog is ContratoDetailsDialogFragment) {
            LogUtils.debug("ContratosFragment", "Fechando diálogo de detalhes de contrato aberto")
            dialog.dismiss()
        }
    }
    
    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvContratos)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        fabNovoContrato = view.findViewById(R.id.fabAddContrato)
        viewLoading = view.findViewById(R.id.viewLoading)
        viewEmpty = view.findViewById(R.id.viewEmpty)
        viewError = view.findViewById(R.id.viewError)
        etSearch = view.findViewById(R.id.etSearch)
    }
    
    private fun setupViewModel() {
        val factory = ContratosViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[ContratosViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = ContratosAdapter(
            contratos = emptyList(),
            onItemClick = { contrato ->
                showContratoDetailDialog(contrato)
            },
            onMenuClick = { contrato, view ->
                showPopupMenu(contrato, view)
            }
        )
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }
    
    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("ContratosFragment", "Atualizando lista de contratos via swipe refresh")
            viewModel.loadContratos()
        }
        
        fabNovoContrato.setOnClickListener {
            showCadastroContratoDialog()
        }
        
        // Configurar listener para busca
        etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val searchTerm = etSearch.text.toString().trim()
                LogUtils.debug("ContratosFragment", "Buscando por: $searchTerm")
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
                    LogUtils.debug("ContratosFragment", "Estado: Loading")
                    viewLoading.visibility = View.VISIBLE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    
                    // Definir exibição do ViewFlipper para o estado de carregamento (índice 0)
                    val viewFlipper = view?.findViewById<ViewFlipper>(R.id.viewFlipper)
                    viewFlipper?.displayedChild = 0
                }
                
                is UiState.Success -> {
                    LogUtils.debug("ContratosFragment", "Estado: Success - Contratos: ${state.data.size}")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    
                    // Atualizar o adaptador com os contratos
                    adapter.updateContratos(state.data)
                    
                    // Definir exibição do ViewFlipper para a lista (índice 2)
                    val viewFlipper = view?.findViewById<ViewFlipper>(R.id.viewFlipper)
                    viewFlipper?.displayedChild = 2
                    LogUtils.debug("ContratosFragment", "ViewFlipper exibindo tela de lista (índice 2)")
                }
                
                is UiState.Empty -> {
                    LogUtils.debug("ContratosFragment", "Estado: Empty")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                    
                    // Definir exibição do ViewFlipper para o estado vazio (índice 1)
                    val viewFlipper = view?.findViewById<ViewFlipper>(R.id.viewFlipper)
                    viewFlipper?.displayedChild = 1
                }
                
                is UiState.Error -> {
                    LogUtils.debug("ContratosFragment", "Estado: Error - ${state.message}")
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    
                    // Definir exibição do ViewFlipper para o estado de erro (índice 3)
                    val viewFlipper = view?.findViewById<ViewFlipper>(R.id.viewFlipper)
                    viewFlipper?.displayedChild = 3
                    
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
                        LogUtils.debug("ContratosFragment", "Detalhes do contrato carregados com sucesso. ID: ${state.data.id}, equipamentos: ${state.data.equipamentos?.size ?: 0}")
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
        LogUtils.debug("ContratosFragment", "🔄 Chamando viewModel.loadContratos()")
        viewModel.loadContratos()
        LogUtils.debug("ContratosFragment", "✅ Recarga da lista iniciada")
    }
    
    private fun showCadastroContratoDialog(contratoParaEditar: Contrato? = null) {
        val dialog = CadastroContratoDialogFragment.newInstance(contratoParaEditar)
        dialog.setOnContratoSavedListener {
            LogUtils.debug("ContratosFragment", "Contrato salvo/atualizado, recarregando lista.")
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