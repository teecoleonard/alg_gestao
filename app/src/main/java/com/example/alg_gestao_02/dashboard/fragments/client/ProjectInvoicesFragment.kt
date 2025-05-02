package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.adapter.ProjectInvoicesAdapter
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.databinding.FragmentProjectInvoicesBinding
import com.example.alg_gestao_02.ui.invoice.repository.InvoiceRepository
import com.example.alg_gestao_02.ui.invoice.viewmodel.ProjectInvoicesViewModel
import com.example.alg_gestao_02.ui.invoice.viewmodel.ProjectInvoicesViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProjectInvoicesFragment : Fragment() {

    private var _binding: FragmentProjectInvoicesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProjectInvoicesViewModel
    private lateinit var invoicesAdapter: ProjectInvoicesAdapter

    private var projectId: String = ""
    private val calendar = Calendar.getInstance()
    private val monthYearFormatter = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            projectId = it.getString(ARG_PROJECT_ID, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectInvoicesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        updateMonthTitle()
        
        // Carregar invoices ao iniciar
        if (projectId.isNotEmpty()) {
            viewModel.loadInvoices(projectId)
        }
    }

    private fun setupViewModel() {
        val factory = ProjectInvoicesViewModelFactory(InvoiceRepository())
        viewModel = ViewModelProvider(this, factory)[ProjectInvoicesViewModel::class.java]
        
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> showLoading()
                is UiState.Success -> showInvoices(state.data)
                is UiState.Empty -> showEmpty()
                is UiState.Error -> showError(state.message)
            }
        }
    }

    private fun setupRecyclerView() {
        invoicesAdapter = ProjectInvoicesAdapter(
            onItemClick = { invoice -> handleInvoiceClick(invoice) },
            onSeeDetailsClick = { invoice -> handleSeeDetailsClick(invoice) }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = invoicesAdapter
        }
    }

    private fun setupListeners() {
        // Configurar navegação entre meses
        binding.btnPreviousMonth.setOnClickListener {
            viewModel.loadPreviousMonth()
            calendar.add(Calendar.MONTH, -1)
            updateMonthTitle()
        }

        binding.btnNextMonth.setOnClickListener {
            viewModel.loadNextMonth()
            calendar.add(Calendar.MONTH, 1)
            updateMonthTitle()
        }

        binding.tvViewAllInvoices.setOnClickListener {
            // Implementar navegação para visualizar todas as faturas
            Toast.makeText(requireContext(), "Ver todas as faturas", Toast.LENGTH_SHORT).show()
        }
        
        // Adicionar listener para botão de tentar novamente
        binding.btnTryAgain.setOnClickListener {
            if (projectId.isNotEmpty()) {
                viewModel.loadInvoices(projectId)
            }
        }
    }

    private fun updateMonthTitle() {
        binding.tvCurrentMonth.text = monthYearFormatter.format(calendar.time)
    }

    private fun showLoading() {
        binding.viewFlipper.displayedChild = STATE_LOADING
    }

    private fun showInvoices(invoices: List<ProjectInvoiceItem>) {
        binding.viewFlipper.displayedChild = STATE_LIST
        invoicesAdapter.updateData(invoices)
    }

    private fun showEmpty() {
        binding.viewFlipper.displayedChild = STATE_EMPTY
    }

    private fun showError(message: String) {
        binding.viewFlipper.displayedChild = STATE_ERROR
        binding.tvErrorMessage.text = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun handleInvoiceClick(invoice: ProjectInvoiceItem) {
        Toast.makeText(requireContext(), "Fatura selecionada: ${invoice.numero}", Toast.LENGTH_SHORT).show()
    }

    private fun handleSeeDetailsClick(invoice: ProjectInvoiceItem) {
        Toast.makeText(requireContext(), "Ver detalhes da fatura: ${invoice.numero}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_PROJECT_ID = "project_id"
        
        private const val STATE_LOADING = 0
        private const val STATE_EMPTY = 1
        private const val STATE_LIST = 2
        private const val STATE_ERROR = 3

        @JvmStatic
        fun newInstance(projectId: String) =
            ProjectInvoicesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PROJECT_ID, projectId)
                }
            }
    }
} 