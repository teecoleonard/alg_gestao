package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import android.widget.ViewFlipper
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.adapter.ProjectContractsAdapter
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.repository.ContractRepository
import com.example.alg_gestao_02.ui.contract.viewmodel.ProjectContractsViewModel
import com.example.alg_gestao_02.ui.contract.viewmodel.ProjectContractsViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import java.time.format.TextStyle
import java.util.Locale

class ProjectContractsFragment : Fragment() {
    
    private lateinit var contractsAdapter: ProjectContractsAdapter
    private lateinit var viewFlipper: ViewFlipper
    private lateinit var viewModel: ProjectContractsViewModel
    
    private var projectId: String = ""
    
    // Constantes para os índices dos estados do ViewFlipper
    companion object {
        private const val VIEW_LIST = 0
        private const val VIEW_EMPTY = 1
        private const val VIEW_LOADING = 2
        private const val ARG_PROJECT_ID = "project_id"
        
        fun newInstance(projectId: String): ProjectContractsFragment {
            return ProjectContractsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID, "")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_project_contracts, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ProjectContractsFragment", "Inicializando fragmento de contratos do projeto")
        
        // Inicializar ViewFlipper antes de qualquer outra operação que o utilize
        viewFlipper = view.findViewById(R.id.viewFlipper)
        
        setupViewModel()
        setupRecyclerView()
        setupListeners(view)
        
        if (projectId.isNotEmpty()) {
            viewModel.loadContracts(projectId)
        }
    }
    
    private fun setupViewModel() {
        val factory = ProjectContractsViewModelFactory(ContractRepository())
        viewModel = ViewModelProvider(this, factory)[ProjectContractsViewModel::class.java]
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> showContracts(state.data)
                is UiState.Empty -> showEmptyState()
                is UiState.Error -> showError(state.message)
            }
        }
    }
    
    private fun setupRecyclerView() {
        // Certifique-se de que o RecyclerView existe no layout
        val rvContracts = view?.findViewById<RecyclerView>(R.id.rvContracts)
        if (rvContracts == null) {
            LogUtils.debug("ProjectContractsFragment", "RecyclerView não encontrado no layout")
            return
        }
        
        contractsAdapter = ProjectContractsAdapter(emptyList()) { contract ->
            LogUtils.debug("ProjectContractsFragment", "Contrato clicado: ${contract.name}")
            Toast.makeText(requireContext(), "Contrato selecionado: ${contract.name}", Toast.LENGTH_SHORT).show()
        }
        
        rvContracts.layoutManager = LinearLayoutManager(requireContext())
        rvContracts.adapter = contractsAdapter
    }
    
    private fun setupListeners(view: View) {
        // Configurar o botão "Criar contato"
        view.findViewById<MaterialButton>(R.id.btnCriarContato)?.setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Botão criar contato clicado")
            Toast.makeText(requireContext(), "Criar contato clicado", Toast.LENGTH_SHORT).show()
        }
        
        // Navegação de data
        view.findViewById<View>(R.id.ivPrevDate).setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Data anterior clicada")
            viewModel.loadPreviousMonth()
        }
        
        view.findViewById<View>(R.id.ivNextDate).setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Próxima data clicada")
            viewModel.loadNextMonth()
        }
        
        // Ver mais fotos
        view.findViewById<View>(R.id.tvViewAllPhotos)?.setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Ver todas as fotos clicado")
            Toast.makeText(requireContext(), "Ver todas as fotos", Toast.LENGTH_SHORT).show()
        }
        
        // Adicionar foto
        view.findViewById<View>(R.id.cardAddPhoto)?.setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Adicionar foto clicado")
            Toast.makeText(requireContext(), "Adicionar foto", Toast.LENGTH_SHORT).show()
        }
        
        // Ver todos os contratos
        view.findViewById<View>(R.id.tvViewAllContracts)?.setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "Ver todos os contratos clicado")
            viewModel.loadAllContracts()
        }
        
        // Outros elementos interativos
        view.findViewById<View>(R.id.cardMoreItems)?.setOnClickListener {
            LogUtils.debug("ProjectContractsFragment", "+9 more clicado")
            Toast.makeText(requireContext(), "Ver mais itens do contrato", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showContractOptions(view: View, contract: ProjectContractItem) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_contract_options, popupMenu.menu)
        
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_edit -> {
                    Toast.makeText(requireContext(), "Editar contrato ${contract.name}", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.menu_delete -> {
                    viewModel.deleteContract(contract.id)
                    true
                }
                else -> false
            }
        }
        
        popupMenu.show()
    }
    
    private fun showEmptyState() {
        viewFlipper.displayedChild = VIEW_EMPTY
        updateCurrentMonth()
    }
    
    private fun showLoading() {
        viewFlipper.displayedChild = VIEW_LOADING
    }
    
    private fun showContracts(contracts: List<ProjectContractItem>) {
        contractsAdapter.updateData(contracts)
        viewFlipper.displayedChild = VIEW_LIST
        updateCurrentMonth()
        
        // Configurar menu do contrato para o primeiro item (se existir)
        if (contracts.isNotEmpty()) {
            view?.findViewById<View>(R.id.ivMenuContrato)?.setOnClickListener {
                showContractOptions(it, contracts[0])
            }
        }
    }
    
    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        LogUtils.error("ProjectContractsFragment", "Erro: $message")
        showEmptyState()
    }
    
    private fun updateCurrentMonth() {
        val currentMonth = viewModel.getCurrentMonth()
        val monthName = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR")).capitalize()
        view?.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvCurrentMonth)?.text = 
            "$monthName ${currentMonth.year}"
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
        }
    }
} 