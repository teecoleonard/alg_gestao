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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment para listagem e gestão de contratos
 */
class ContratosFragment : Fragment(), ContratoDetailsDialogFragment.OnEditRequestListener {
    
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
        observeViewModel()
        
        // Carregar contratos imediatamente
        LogUtils.debug("ContratosFragment", "Carregando lista de contratos inicialmente")
        viewModel.loadContratos()
    }
    
    override fun onResume() {
        super.onResume()
        LogUtils.debug("ContratosFragment", "onResume - Ciclo de vida")
        // Não precisamos recarregar os contratos aqui, pois já fazemos isso em onViewCreated
        // e também quando ocorrem operações de CRUD
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
        dialog.show(childFragmentManager, "ContratoDetailsDialog")
    }
    
    override fun onEditRequested(contrato: Contrato) {
        LogUtils.debug("ContratosFragment", "Pedido de edição recebido para o contrato: ${contrato.contratoNum}")
        showCadastroContratoDialog(contrato)
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
        popup.menuInflater.inflate(R.menu.menu_item_options, popup.menu)
        
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    LogUtils.debug("ContratosFragment", "Menu Editar clicado para contrato: ${contrato.contratoNum}")
                    showCadastroContratoDialog(contrato)
                    true
                }
                
                R.id.action_delete -> {
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
        // Aqui seria ideal ter um diálogo de confirmação
        // Por simplicidade, vamos excluir diretamente
        viewModel.excluirContrato(contrato.id)
        Toast.makeText(context, "Excluindo contrato...", Toast.LENGTH_SHORT).show()
    }
} 