package com.example.alg_gestao_02.ui.empresa

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Empresa
import com.example.alg_gestao_02.data.repository.EmpresaRepository
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.empresa.adapter.EmpresasAdapter
import com.example.alg_gestao_02.ui.empresa.viewmodel.EmpresaViewModel
import com.example.alg_gestao_02.ui.empresa.viewmodel.EmpresaViewModel.FiltroEmpresa
import com.example.alg_gestao_02.ui.empresa.viewmodel.EmpresaViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment que exibe a lista de empresas.
 * Implementa o padrão MVVM com ViewModel e observáveis.
 */
class EmpresasFragment : BaseFragment() {
    
    private lateinit var empresasAdapter: EmpresasAdapter
    private lateinit var viewModel: EmpresaViewModel
    
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerEmpresas: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var fabAddEmpresa: FloatingActionButton
    private lateinit var chipGroupFiltros: ChipGroup
    private lateinit var searchView: SearchView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Habilita opções de menu
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_empresas, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.debug("EmpresasFragment", "Inicializando fragmento de empresas")
        
        // Inicializar views
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        
        // Agora chamamos o método da classe pai depois de inicializar o viewModel
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        setupFiltros()
    }
    
    /**
     * Implementa o método abstrato getErrorViewModels para fornecer
     * a lista de ViewModels que tratam erros
     */
    override fun getErrorViewModels(): List<ErrorViewModel> {
        // Retorna uma lista contendo o empresaViewModel para observação de erros
        return listOf(viewModel.errorHandler)
    }
    
    /**
     * Implementa o método onErrorRetry para quando o usuário 
     * clica em "Tentar novamente" após um erro
     */
    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {
        // Recarrega as empresas ao clicar no botão de retry
        viewModel.loadEmpresas()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_empresas, menu)
        
        // Configurar SearchView
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { viewModel.setTextoBusca(it) }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.setTextoBusca(it) }
                return true
            }
        })
        
        super.onCreateOptionsMenu(menu, inflater)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadEmpresas()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        recyclerEmpresas = view.findViewById(R.id.rvEmpresas)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        fabAddEmpresa = view.findViewById(R.id.fabAddEmpresa)
        chipGroupFiltros = view.findViewById(R.id.chipGroupFiltros)
    }
    
    private fun setupRecyclerView() {
        empresasAdapter = EmpresasAdapter(
            emptyList(),
            onItemClick = { empresa ->
                LogUtils.debug("EmpresasFragment", "Empresa clicada: ${empresa.nome}")
                showEmpresaDetalhes(empresa)
            },
            onOptionsClick = { empresa, buttonView ->
                showEmpresaOptions(empresa, buttonView)
            }
        )
        
        recyclerEmpresas.layoutManager = LinearLayoutManager(context)
        recyclerEmpresas.adapter = empresasAdapter
    }
    
    private fun setupViewModel() {
        // Inicializar o ViewModel com seu Factory
        val factory = EmpresaViewModelFactory(EmpresaRepository())
        viewModel = ViewModelProvider(this, factory)[EmpresaViewModel::class.java]
        
        // Observar mudanças no estado da UI
        viewModel.empresasState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    showLoading()
                }
                is UiState.Success -> {
                    showEmpresas(state.data)
                }
                is UiState.Empty -> {
                    showEmptyState("Nenhuma empresa encontrada")
                }
                is UiState.Error -> {
                    // Não precisamos mais tratar erros aqui,
                    // o BaseFragment já faz isso por nós
                    // Mas ainda precisamos atualizar a UI para esconder o loading
                    progressBar.visibility = View.GONE
                    if (empresasAdapter.itemCount == 0) {
                        showEmptyState("Não foi possível carregar as empresas")
                    }
                }
            }
        }
    }
    
    private fun setupListeners() {
        fabAddEmpresa.setOnClickListener {
            LogUtils.debug("EmpresasFragment", "Botão adicionar empresa clicado")
            showDialogAdicionarEmpresa()
        }
    }
    
    private fun setupFiltros() {
        // Limpar filtros existentes
        chipGroupFiltros.removeAllViews()
        
        // Adicionar chips de filtro
        addFilterChip(getString(R.string.todos), FiltroEmpresa.TODOS, true)
        addFilterChip(getString(R.string.ativos), FiltroEmpresa.ATIVOS)
        addFilterChip(getString(R.string.inativos), FiltroEmpresa.INATIVOS)
        
        // Listener para mudança de seleção
        chipGroupFiltros.setOnCheckedChangeListener { group, checkedId ->
            val chip = group.findViewById<Chip>(checkedId)
            val filtro = chip?.tag as? FiltroEmpresa ?: FiltroEmpresa.TODOS
            
            LogUtils.debug("EmpresasFragment", "Filtro selecionado: $filtro")
            
            viewModel.setFiltroTipo(filtro)
        }
    }
    
    private fun addFilterChip(text: String, filtro: FiltroEmpresa, isChecked: Boolean = false) {
        // Criar chip com o estilo definido
        val chip = layoutInflater.inflate(
            R.layout.item_filter_chip, chipGroupFiltros, false
        ) as Chip
        
        // Configurar propriedades
        chip.text = text
        chip.tag = filtro
        chip.isChecked = isChecked
        
        // Adicionar ao grupo
        chipGroupFiltros.addView(chip)
    }
    
    private fun showEmpresaDetalhes(empresa: Empresa) {
        // No futuro, navegar para tela de detalhes
        val mensagem = "${empresa.getNomeExibicao()} - ${empresa.razaoSocial}"
        
        Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show()
    }
    
    private fun showDialogAdicionarEmpresa() {
        viewModel.adicionarEmpresa()
        Snackbar.make(
            requireView(),
            "Adicionar nova empresa",
            Snackbar.LENGTH_LONG
        ).show()
    }
    
    private fun showEmpresaOptions(empresa: Empresa, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_empresa_options, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    viewModel.editarEmpresa(empresa)
                    Toast.makeText(context, "Editar: ${empresa.nome}", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_delete -> {
                    viewModel.excluirEmpresa(empresa)
                    Toast.makeText(context, "Excluir: ${empresa.nome}", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    // Métodos para gerenciar o estado da UI
    
    private fun showLoading() {
        progressBar.visibility = View.VISIBLE
        recyclerEmpresas.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
    }
    
    private fun showEmpresas(empresas: List<Empresa>) {
        progressBar.visibility = View.GONE
        recyclerEmpresas.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
        
        empresasAdapter.updateData(empresas)
    }
    
    private fun showEmptyState(message: String) {
        progressBar.visibility = View.GONE
        recyclerEmpresas.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        
        // Configurar a mensagem
        val tvMessage = layoutEmpty.findViewById<TextView>(R.id.tvEmptyMessage)
        tvMessage?.text = message
    }
} 