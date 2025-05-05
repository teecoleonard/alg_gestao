package com.example.alg_gestao_02.dashboard.fragments.contract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.contract.adapter.ContratosAdapter
import com.example.alg_gestao_02.dashboard.fragments.contract.model.Contrato
import com.example.alg_gestao_02.data.repository.ContractRepository
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.contract.viewmodel.ContratosViewModel
import com.example.alg_gestao_02.ui.contract.viewmodel.ContratosViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

/**
 * Fragment que exibe a lista de contratos.
 * Implementa o padrão MVVM com ViewModel e observáveis.
 */
class ContratosFragment : BaseFragment() {
    
    private lateinit var contratosAdapter: ContratosAdapter
    private lateinit var viewModel: ContratosViewModel
    private lateinit var rvContratos: RecyclerView
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var searchView: SearchView
    
    companion object {
        private const val STATE_LOADING = 0
        private const val STATE_EMPTY = 1
        private const val STATE_LIST = 2
        private const val STATE_ERROR = 3
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Habilita opções de menu
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contratos, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.debug("ContratosFragment", "Inicializando fragmento de contratos")
        
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        
        // Chama o método da classe pai depois de inicializar o viewModel
        super.onViewCreated(view, savedInstanceState)
        
        setupAddButton(view)
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
        viewModel.loadContratos()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_contratos, menu)
        
        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        
        searchView.queryHint = getString(R.string.search_contratos_hint)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setTextoBusca(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { 
                    if (it.isEmpty() || it.length > 2) {
                        viewModel.setTextoBusca(it)
                    }
                }
                return true
            }
        })
        
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadContratos()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun initViews(view: View) {
        rvContratos = view.findViewById(R.id.rvContratos)
        viewFlipper = view.findViewById(R.id.viewFlipper)
    }
    
    private fun setupRecyclerView() {
        contratosAdapter = ContratosAdapter(
            emptyList(),
            onItemClick = { contrato ->
                LogUtils.debug("ContratosFragment", "Contrato clicado: ${contrato.contractNumber}")
                showContratoDetalhes(contrato)
            }
        )
        
        rvContratos.layoutManager = LinearLayoutManager(context)
        rvContratos.adapter = contratosAdapter
    }
    
    private fun setupViewModel() {
        val repository = ContractRepository()
        val factory = ContratosViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ContratosViewModel::class.java]
        
        viewModel.contratosState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    LogUtils.debug("ContratosFragment", "Estado: Loading")
                    showLoading()
                }
                is UiState.Success -> {
                    LogUtils.debug("ContratosFragment", "Estado: Success - ${state.data.size} contratos")
                    showData(state.data)
                }
                is UiState.Empty -> {
                    LogUtils.debug("ContratosFragment", "Estado: Empty")
                    showEmpty()
                }
                is UiState.Error -> {
                    LogUtils.debug("ContratosFragment", "Estado: Error - ${state.message}")
                    // Não precisamos tratar o erro aqui, pois o BaseFragment já faz isso
                    // Apenas atualizamos a UI para esconder o loading
                    viewFlipper.displayedChild = STATE_LIST
                    if (contratosAdapter.itemCount == 0) {
                        viewFlipper.displayedChild = STATE_EMPTY
                    }
                }
            }
        }
    }
    
    private fun setupAddButton(view: View) {
        val fabAddContrato = view.findViewById<FloatingActionButton>(R.id.fabAddContrato)
        fabAddContrato.setOnClickListener {
            LogUtils.debug("ContratosFragment", "Botão adicionar contrato clicado")
            showDialogAdicionarContrato()
        }
    }
    
    private fun showDialogAdicionarContrato() {
        Snackbar.make(
            requireView(),
            "Adicionar novo contrato (funcionalidade em desenvolvimento)",
            Snackbar.LENGTH_LONG
        ).show()
    }
    
    private fun showContratoDetalhes(contrato: Contrato) {
        // No futuro, navegar para tela de detalhes
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contrato #${contrato.contractNumber}")
            .setMessage(
                "Cliente: ${contrato.client}\n" +
                "Valor: R$ ${String.format("%.2f", contrato.value)}\n" +
                "Data: ${contrato.startDate} - ${contrato.endDate}\n" +
                "Status: ${contrato.status}\n\n" +
                "${contrato.description ?: "Sem descrição"}"
            )
            .setPositiveButton("Fechar", null)
            .show()
    }
    
    private fun showLoading() {
        viewFlipper.displayedChild = STATE_LOADING
    }
    
    private fun showData(data: List<Contrato>) {
        viewFlipper.displayedChild = STATE_LIST
        contratosAdapter.updateData(data)
    }
    
    private fun showEmpty() {
        viewFlipper.displayedChild = STATE_EMPTY
    }
} 