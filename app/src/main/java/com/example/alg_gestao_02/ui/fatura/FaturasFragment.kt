package com.example.alg_gestao_02.ui.fatura

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Fatura
import com.example.alg_gestao_02.ui.contrato.PdfViewerFragment
import com.example.alg_gestao_02.ui.fatura.adapter.FaturasAdapter
import com.example.alg_gestao_02.ui.fatura.viewmodel.FaturaPdfUiResult
import com.example.alg_gestao_02.ui.fatura.viewmodel.FaturasViewModel
import com.example.alg_gestao_02.ui.fatura.viewmodel.FaturasViewModelFactory
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.PdfUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class FaturasFragment : Fragment() {
    private lateinit var viewModel: FaturasViewModel
    private lateinit var adapter: FaturasAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var viewLoading: View
    private lateinit var viewEmpty: View
    private lateinit var viewError: View
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabFilter: FloatingActionButton
    private var hasLoadedData: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_faturas, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        viewModel.loadFaturas()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.rvFaturas)
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        viewLoading = view.findViewById(R.id.viewLoading)
        viewEmpty = view.findViewById(R.id.viewEmpty)
        viewError = view.findViewById(R.id.viewError)
        etSearch = view.findViewById(R.id.etSearch)
        fabFilter = view.findViewById(R.id.fabFilter)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, FaturasViewModelFactory())[FaturasViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FaturasAdapter(
            faturas = emptyList(),
            onItemClick = { fatura -> showDetalhesFatura(fatura) },
            onMenuClick = { fatura, view -> showMenuFatura(fatura, view) },
        )
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupListeners() {
        swipeRefresh.setOnRefreshListener {
            swipeRefresh.isRefreshing = true
            viewModel.loadFaturas()
        }
        fabFilter.setOnClickListener { showStatusFilterDialog() }

        etSearch.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                viewModel.setSearchTerm(etSearch.text.toString().trim())
                true
            } else {
                false
            }
        }

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchTerm(s?.toString().orEmpty())
            }
        })
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    val keepContentVisible = hasLoadedData && adapter.itemCount > 0
                    if (keepContentVisible) {
                        viewLoading.visibility = View.GONE
                        viewEmpty.visibility = View.GONE
                        viewError.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                        swipeRefresh.isRefreshing = true
                    } else {
                        viewLoading.visibility = View.VISIBLE
                        viewEmpty.visibility = View.GONE
                        viewError.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                        swipeRefresh.isRefreshing = false
                    }
                }
                is UiState.Success -> {
                    hasLoadedData = true
                    swipeRefresh.isRefreshing = false
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(state.data)
                }
                is UiState.Empty -> {
                    hasLoadedData = true
                    swipeRefresh.isRefreshing = false
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.VISIBLE
                    viewError.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                }
                is UiState.Error -> {
                    swipeRefresh.isRefreshing = false
                    val keepContentVisible = hasLoadedData && adapter.itemCount > 0
                    viewLoading.visibility = View.GONE
                    viewEmpty.visibility = View.GONE
                    viewError.visibility = if (keepContentVisible) View.GONE else View.VISIBLE
                    recyclerView.visibility = if (keepContentVisible) View.VISIBLE else View.GONE
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.pdfState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    Toast.makeText(context, "Gerando PDF da fatura...", Toast.LENGTH_SHORT).show()
                }
                is UiState.Success -> {
                    handlePdfSuccess(state.data)
                    viewModel.clearPdfState()
                }
                is UiState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.clearPdfState()
                }
                else -> Unit
            }
        }
    }

    private fun showDetalhesFatura(fatura: Fatura) {
        val mensagem = buildString {
            append("Cliente: ${fatura.cliente?.nome ?: "Nao informado"}\n")
            append("Periodo: ${fatura.periodo ?: "-"}\n")
            append("Vencimento: ${fatura.dataVencimento ?: "-"}\n")
            append("Status: ${fatura.status ?: "PENDENTE"}\n")
            append("Valor total: R$ ${"%.2f".format(fatura.valorTotal)}")
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Fatura #${fatura.numero}")
            .setMessage(mensagem)
            .setPositiveButton("Visualizar PDF") { _, _ -> viewModel.gerarPdfFatura(fatura.id, compartilharDireto = false) }
            .setNeutralButton("Compartilhar PDF") { _, _ -> viewModel.gerarPdfFatura(fatura.id, compartilharDireto = true) }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun showMenuFatura(fatura: Fatura, anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.menu_fatura_item, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_fatura_detalhes -> {
                    showDetalhesFatura(fatura)
                    true
                }
                R.id.menu_fatura_visualizar_pdf -> {
                    viewModel.gerarPdfFatura(fatura.id, compartilharDireto = false)
                    true
                }
                R.id.menu_fatura_compartilhar_pdf -> {
                    viewModel.gerarPdfFatura(fatura.id, compartilharDireto = true)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showStatusFilterDialog() {
        val itens = arrayOf("Todos", "PENDENTE", "ENVIADA", "PAGA", "CANCELADA")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar status da fatura")
            .setItems(itens) { _, which ->
                if (which == 0) {
                    viewModel.setStatusFilter(null)
                } else {
                    viewModel.setStatusFilter(itens[which])
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handlePdfSuccess(result: FaturaPdfUiResult) {
        if (result.compartilharDireto) {
            val pdfBase64 = result.pdf.pdfBase64
            if (pdfBase64.isNullOrBlank()) {
                Toast.makeText(context, "PDF indisponivel para compartilhamento", Toast.LENGTH_LONG).show()
                return
            }

            val bytes = android.util.Base64.decode(pdfBase64, android.util.Base64.DEFAULT)
            val arquivo = PdfUtils.criarArquivoTemporario(requireContext(), bytes, "fatura_${result.fatura.numero}")
            arquivo.onSuccess { file ->
                PdfUtils.compartilharPdf(
                    requireContext(),
                    file,
                    "Compartilhar Fatura",
                    "Fatura #${result.fatura.numero}",
                ).onFailure {
                    Toast.makeText(context, "Erro ao compartilhar PDF: ${it.message}", Toast.LENGTH_LONG).show()
                }
            }.onFailure {
                Toast.makeText(context, "Erro ao preparar PDF: ${it.message}", Toast.LENGTH_LONG).show()
            }
            return
        }

        val viewer = PdfViewerFragment.newInstance(
            pdfBase64 = result.pdf.pdfBase64,
            htmlUrl = result.pdf.htmlUrl,
            htmlContent = result.pdf.htmlContent,
            contratoNumero = result.fatura.numero,
            contratoId = result.fatura.id,
            documentType = PdfViewerFragment.DOC_TYPE_FATURA,
        )
        viewer.show(parentFragmentManager, "FaturaPdfViewer")
    }
}
