package com.example.alg_gestao_02.ui.material

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.material.adapter.MateriaisAdapter
import com.example.alg_gestao_02.ui.material.viewmodel.MateriaisViewModel
import com.example.alg_gestao_02.ui.material.viewmodel.MateriaisViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MateriaisFragment : BaseFragment() {
    private lateinit var viewModel: MateriaisViewModel
    private lateinit var materiaisAdapter: MateriaisAdapter

    private lateinit var recyclerMateriais: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var fabAddMaterial: FloatingActionButton
    private lateinit var etSearch: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_materiais, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews(view)
        setupRecyclerView()
        setupViewModel()
        setupListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getErrorViewModels(): List<ErrorViewModel> {
        return listOf(viewModel.errorHandler)
    }

    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {
        viewModel.loadMateriais()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_materiais, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_refresh -> {
                viewModel.loadMateriais()
                true
            }
            R.id.action_filter_todos -> {
                viewModel.setFiltro(MateriaisViewModel.FiltroMaterial.TODOS)
                item.isChecked = true
                true
            }
            R.id.action_filter_disponiveis -> {
                viewModel.setFiltro(MateriaisViewModel.FiltroMaterial.DISPONIVEIS)
                item.isChecked = true
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initViews(view: View) {
        recyclerMateriais = view.findViewById(R.id.recyclerMateriais)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        fabAddMaterial = view.findViewById(R.id.fabAddMaterial)
        etSearch = view.findViewById(R.id.etSearch)
    }

    private fun setupRecyclerView() {
        materiaisAdapter = MateriaisAdapter(
            onItemClick = { material -> showEditDialog(material) },
            onMenuClick = { material, anchorView -> showOptionsMenu(material, anchorView) }
        )
        recyclerMateriais.adapter = materiaisAdapter
    }

    private fun setupViewModel() {
        val factory = MateriaisViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[MateriaisViewModel::class.java]

        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> {
                    hideLoading()
                    materiaisAdapter.updateMateriais(state.data)
                    showContent()
                }
                is UiState.Empty -> {
                    hideLoading()
                    showEmpty()
                }
                is UiState.Error -> hideLoading()
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        fabAddMaterial.setOnClickListener { showCreateDialog() }
        swipeRefresh.setOnRefreshListener { viewModel.loadMateriais() }
        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                viewModel.setTextoBusca(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }
        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setTextoBusca(s?.toString().orEmpty())
            }
        })
    }

    private fun showOptionsMenu(material: Material, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menuInflater.inflate(R.menu.menu_material_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    showEditDialog(material)
                    true
                }
                R.id.action_delete -> {
                    confirmDelete(material)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCreateDialog() {
        val dialog = CadastroMaterialDialogFragment.newInstance()
        dialog.setOnMaterialSavedListener { material ->
            viewModel.criarMaterial(material)
        }
        dialog.show(parentFragmentManager, "CadastroMaterialDialog")
    }

    private fun showEditDialog(material: Material) {
        val dialog = CadastroMaterialDialogFragment.newInstance(material)
        dialog.setOnMaterialSavedListener { materialAtualizado ->
            viewModel.atualizarMaterial(material.id, materialAtualizado)
        }
        dialog.show(parentFragmentManager, "EditMaterialDialog")
    }

    private fun confirmDelete(material: Material) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Excluir Material")
            .setMessage("Deseja realmente excluir o material '${material.nome}'?")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.excluirMaterial(material.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLoading() {
        swipeRefresh.isRefreshing = true
    }

    private fun hideLoading() {
        swipeRefresh.isRefreshing = false
    }

    private fun showContent() {
        recyclerMateriais.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
    }

    private fun showEmpty() {
        recyclerMateriais.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
    }
}
