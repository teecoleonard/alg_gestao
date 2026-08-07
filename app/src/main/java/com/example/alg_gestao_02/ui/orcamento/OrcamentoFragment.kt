package com.example.alg_gestao_02.ui.orcamento

import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.service.PdfResponse
import com.example.alg_gestao_02.ui.common.BaseFragment
import com.example.alg_gestao_02.ui.common.ErrorViewModel
import com.example.alg_gestao_02.ui.orcamento.adapter.OrcamentoEquipAdapter
import com.example.alg_gestao_02.ui.orcamento.adapter.OrcamentoMaterialAdapter
import com.example.alg_gestao_02.ui.orcamento.viewmodel.OrcamentoEquipLinha
import com.example.alg_gestao_02.ui.orcamento.viewmodel.OrcamentoMaterialLinha
import com.example.alg_gestao_02.ui.orcamento.viewmodel.OrcamentoViewModel
import com.example.alg_gestao_02.ui.orcamento.viewmodel.OrcamentoViewModelFactory
import com.example.alg_gestao_02.ui.orcamento.viewmodel.PeriodoOrcamento
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.PdfUtils
import com.example.alg_gestao_02.utils.ShareUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class OrcamentoFragment : BaseFragment() {

    private lateinit var viewModel: OrcamentoViewModel
    private lateinit var equipAdapter: OrcamentoEquipAdapter
    private lateinit var materialAdapter: OrcamentoMaterialAdapter

    private lateinit var recyclerEquip: RecyclerView
    private lateinit var recyclerMat: RecyclerView
    private lateinit var tvEquipVazio: TextView
    private lateinit var tvMaterialVazio: TextView
    private lateinit var tvTotalGeral: TextView
    private lateinit var etDestinatario: EditText
    private lateinit var etValidade: EditText
    private lateinit var etObservacoes: EditText
    private lateinit var btnAddEquip: Button
    private lateinit var btnAddMaterial: Button
    private lateinit var btnGerar: Button
    private lateinit var btnVisualizar: Button
    private lateinit var btnCompartilhar: Button

    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private var arquivoPdfGerado: File? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_orcamento, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initViews(view)
        setupRecyclers()
        setupViewModel()
        setupListeners()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun getErrorViewModels(): List<ErrorViewModel> = emptyList()

    override fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent) {}

    private fun initViews(view: View) {
        recyclerEquip = view.findViewById(R.id.recyclerEquipamentos)
        recyclerMat = view.findViewById(R.id.recyclerMateriais)
        tvEquipVazio = view.findViewById(R.id.tvEquipVazio)
        tvMaterialVazio = view.findViewById(R.id.tvMaterialVazio)
        tvTotalGeral = view.findViewById(R.id.tvTotalGeral)
        etDestinatario = view.findViewById(R.id.etDestinatario)
        etValidade = view.findViewById(R.id.etValidade)
        etObservacoes = view.findViewById(R.id.etObservacoes)
        btnAddEquip = view.findViewById(R.id.btnAddEquipamento)
        btnAddMaterial = view.findViewById(R.id.btnAddMaterial)
        btnGerar = view.findViewById(R.id.btnGerarPdf)
        btnVisualizar = view.findViewById(R.id.btnVisualizar)
        btnCompartilhar = view.findViewById(R.id.btnCompartilhar)
    }

    private fun setupRecyclers() {
        equipAdapter = OrcamentoEquipAdapter(
            onEditar = { linha -> mostrarDialogEquip(null, linha) },
            onRemover = { linha -> viewModel.removerEquipamento(linha.uid) }
        )
        recyclerEquip.layoutManager = LinearLayoutManager(requireContext())
        recyclerEquip.adapter = equipAdapter

        materialAdapter = OrcamentoMaterialAdapter(
            onEditar = { linha -> mostrarDialogMaterial(null, linha) },
            onRemover = { linha -> viewModel.removerMaterial(linha.uid) }
        )
        recyclerMat.layoutManager = LinearLayoutManager(requireContext())
        recyclerMat.adapter = materialAdapter
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, OrcamentoViewModelFactory())[OrcamentoViewModel::class.java]

        viewModel.equipItens.observe(viewLifecycleOwner) { lista ->
            equipAdapter.update(lista)
            tvEquipVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            atualizarTotais()
        }
        viewModel.materialItens.observe(viewLifecycleOwner) { lista ->
            materialAdapter.update(lista)
            tvMaterialVazio.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
            atualizarTotais()
        }
    }

    private fun setupListeners() {
        btnAddEquip.setOnClickListener { abrirCatalogoEquipamentos() }
        btnAddMaterial.setOnClickListener { abrirCatalogoMateriais() }
        btnGerar.setOnClickListener { gerarPdf() }
        btnVisualizar.setOnClickListener {
            arquivoPdfGerado?.let { ShareUtils.openPdfFile(requireContext(), it) }
        }
        btnCompartilhar.setOnClickListener {
            arquivoPdfGerado?.let {
                PdfUtils.compartilharPdf(
                    requireContext(),
                    it,
                    "Compartilhar orçamento",
                    "Segue o orçamento - ALG Gestão"
                )
            }
        }
    }

    private fun atualizarTotais() {
        tvTotalGeral.text = currency.format(viewModel.totalGeral())
    }

    // ---------- Catálogo ----------

    private fun abrirCatalogoEquipamentos() {
        viewModel.carregarCatalogoEquipamentos(
            onResult = { lista ->
                if (!isAdded) return@carregarCatalogoEquipamentos
                if (lista.isEmpty()) {
                    toast("Nenhum equipamento no catálogo")
                    return@carregarCatalogoEquipamentos
                }
                mostrarPicker("Selecionar equipamento", lista.map { it.nomeEquip }) { indice ->
                    mostrarDialogEquip(lista[indice], null)
                }
            },
            onError = { toast(it) }
        )
    }

    private fun abrirCatalogoMateriais() {
        viewModel.carregarCatalogoMateriais(
            onResult = { lista ->
                if (!isAdded) return@carregarCatalogoMateriais
                if (lista.isEmpty()) {
                    toast("Nenhum material no catálogo")
                    return@carregarCatalogoMateriais
                }
                mostrarPicker("Selecionar material", lista.map { it.nome }) { indice ->
                    mostrarDialogMaterial(lista[indice], null)
                }
            },
            onError = { toast(it) }
        )
    }

    private fun mostrarPicker(titulo: String, nomes: List<String>, onPick: (Int) -> Unit) {
        val view = layoutInflater.inflate(R.layout.dialog_orcamento_picker, null)
        val etBusca = view.findViewById<EditText>(R.id.etBuscaCatalogo)
        val lv = view.findViewById<ListView>(R.id.lvCatalogo)

        var indicesFiltrados: List<Int> = nomes.indices.toList()
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            nomes.toMutableList()
        )
        lv.adapter = adapter

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setView(view)
            .setNegativeButton("Cancelar", null)
            .create()

        etBusca.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val termo = s?.toString().orEmpty().trim()
                indicesFiltrados = nomes.indices.filter { nomes[it].contains(termo, ignoreCase = true) }
                adapter.clear()
                adapter.addAll(indicesFiltrados.map { nomes[it] })
                adapter.notifyDataSetChanged()
            }
        })

        lv.setOnItemClickListener { _, _, pos, _ ->
            val indiceReal = indicesFiltrados.getOrNull(pos)
            dialog.dismiss()
            if (indiceReal != null) onPick(indiceReal)
        }

        dialog.show()
    }

    // ---------- Diálogos de item ----------

    private fun mostrarDialogEquip(equipamento: Equipamento?, existente: OrcamentoEquipLinha?) {
        val view = layoutInflater.inflate(R.layout.dialog_orcamento_equip_item, null)
        val tvNome = view.findViewById<TextView>(R.id.tvItemNome)
        val spPeriodo = view.findViewById<Spinner>(R.id.spPeriodo)
        val etQtd = view.findViewById<EditText>(R.id.etQuantidade)
        val etValor = view.findViewById<EditText>(R.id.etValorUnitario)

        val nome = equipamento?.nomeEquip ?: existente?.nome ?: "Equipamento"
        tvNome.text = nome

        val periodos = PeriodoOrcamento.values()
        spPeriodo.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            periodos.map { it.label }
        )
        val periodoInicial = existente?.periodo ?: PeriodoOrcamento.MENSAL
        spPeriodo.setSelection(periodoInicial.ordinal)

        etQtd.setText((existente?.quantidade ?: 1).toString())
        val valorInicial = existente?.valorUnitario
            ?: equipamento?.let { viewModel.valorPorPeriodo(it, periodoInicial) }
            ?: 0.0
        etValor.setText(formatarValorEdit(valorInicial))

        // Ao adicionar (com catálogo), atualiza o valor conforme o período escolhido.
        if (equipamento != null) {
            spPeriodo.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    v: View?,
                    position: Int,
                    id: Long
                ) {
                    val periodo = PeriodoOrcamento.fromOrdinalSafe(position)
                    etValor.setText(formatarValorEdit(viewModel.valorPorPeriodo(equipamento, periodo)))
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existente == null) "Adicionar equipamento" else "Editar equipamento")
            .setView(view)
            .setPositiveButton(if (existente == null) "Adicionar" else "Salvar") { _, _ ->
                val periodo = PeriodoOrcamento.fromOrdinalSafe(spPeriodo.selectedItemPosition)
                val qtd = etQtd.text.toString().toIntOrNull() ?: 1
                val valor = parseValor(etValor.text.toString())
                if (existente == null && equipamento != null) {
                    viewModel.adicionarEquipamento(equipamento.id, nome, periodo, qtd, valor)
                } else if (existente != null) {
                    viewModel.atualizarEquipamento(existente.uid, periodo, qtd, valor)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogMaterial(material: Material?, existente: OrcamentoMaterialLinha?) {
        val view = layoutInflater.inflate(R.layout.dialog_orcamento_material_item, null)
        val tvNome = view.findViewById<TextView>(R.id.tvItemNome)
        val etQtd = view.findViewById<EditText>(R.id.etQuantidade)
        val etValor = view.findViewById<EditText>(R.id.etValorUnitario)

        val nome = material?.nome ?: existente?.nome ?: "Material"
        tvNome.text = nome

        etQtd.setText((existente?.quantidade ?: 1).toString())
        val valorInicial = existente?.valorUnitario ?: material?.valorUnitario ?: 0.0
        etValor.setText(formatarValorEdit(valorInicial))

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (existente == null) "Adicionar material" else "Editar material")
            .setView(view)
            .setPositiveButton(if (existente == null) "Adicionar" else "Salvar") { _, _ ->
                val qtd = etQtd.text.toString().toIntOrNull() ?: 1
                val valor = parseValor(etValor.text.toString())
                if (existente == null && material != null) {
                    viewModel.adicionarMaterial(material.id, nome, material.codigo, qtd, valor)
                } else if (existente != null) {
                    viewModel.atualizarMaterial(existente.uid, qtd, valor)
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ---------- Geração / envio ----------

    private fun gerarPdf() {
        val destinatario = etDestinatario.text.toString()
        val validade = etValidade.text.toString().toIntOrNull()
        val observacoes = etObservacoes.text.toString()

        btnGerar.isEnabled = false
        toast("Gerando orçamento...")
        viewModel.gerarOrcamento(
            destinatario = destinatario,
            validadeDias = validade,
            observacoes = observacoes,
            onSuccess = { resp ->
                if (!isAdded) return@gerarOrcamento
                btnGerar.isEnabled = true
                onPdfGerado(resp)
            },
            onError = {
                if (!isAdded) return@gerarOrcamento
                btnGerar.isEnabled = true
                toast(it)
            }
        )
    }

    private fun onPdfGerado(resp: PdfResponse) {
        val base64 = resp.pdfBase64
        if (base64.isNullOrBlank()) {
            toast("Resposta sem PDF")
            return
        }
        try {
            val bytes = Base64.decode(base64, Base64.DEFAULT)
            PdfUtils.criarArquivoTemporario(requireContext(), bytes, "orcamento").fold(
                onSuccess = { file ->
                    arquivoPdfGerado = file
                    btnVisualizar.isEnabled = true
                    btnCompartilhar.isEnabled = true
                    toast("Orçamento gerado. Use Visualizar ou Compartilhar.")
                },
                onFailure = {
                    LogUtils.error("OrcamentoFragment", "Erro ao salvar PDF", it)
                    toast("Erro ao salvar o PDF")
                }
            )
        } catch (e: Exception) {
            LogUtils.error("OrcamentoFragment", "Erro ao decodificar PDF", e)
            toast("Erro ao processar o PDF")
        }
    }

    // ---------- Helpers ----------

    private fun parseValor(texto: String): Double {
        val t = texto.trim()
        if (t.isEmpty()) return 0.0
        return if (t.contains(",")) {
            t.replace(".", "").replace(",", ".").toDoubleOrNull() ?: 0.0
        } else {
            t.toDoubleOrNull() ?: 0.0
        }
    }

    private fun formatarValorEdit(valor: Double): String =
        String.format(Locale("pt", "BR"), "%.2f", valor)

    private fun toast(msg: String) {
        if (isAdded) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
