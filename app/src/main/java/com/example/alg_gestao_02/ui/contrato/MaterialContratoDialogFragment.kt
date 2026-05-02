package com.example.alg_gestao_02.ui.contrato

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.data.models.MaterialContrato
import com.example.alg_gestao_02.ui.material.viewmodel.MateriaisViewModel
import com.example.alg_gestao_02.ui.material.viewmodel.MateriaisViewModelFactory
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class MaterialContratoDialogFragment : DialogFragment() {

    private lateinit var materiaisViewModel: MateriaisViewModel
    private lateinit var tilMaterial: TextInputLayout
    private lateinit var actvMaterial: AutoCompleteTextView
    private lateinit var tilQuantidade: TextInputLayout
    private lateinit var etQuantidade: TextInputEditText
    private lateinit var tilValorUnitario: TextInputLayout
    private lateinit var etValorUnitario: TextInputEditText
    private lateinit var tvValorTotal: TextInputEditText
    private lateinit var btnCancelar: Button
    private lateinit var btnSalvar: Button

    private var materialContratoParaEdicao: MaterialContrato? = null
    private var contratoId: Int = 0
    private var materialSelecionado: Material? = null
    private var materiaisDisponiveis: List<Material> = emptyList()
    private var onMaterialSalvoListener: ((MaterialContrato) -> Unit)? = null

    companion object {
        private const val ARG_CONTRATO_ID = "arg_contrato_id"
        private const val ARG_MATERIAL_CONTRATO = "arg_material_contrato"

        fun newInstance(
            contratoId: Int,
            materialContrato: MaterialContrato? = null
        ): MaterialContratoDialogFragment {
            return MaterialContratoDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CONTRATO_ID, contratoId)
                    materialContrato?.let { putParcelable(ARG_MATERIAL_CONTRATO, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.CustomDialogStyle)
        contratoId = arguments?.getInt(ARG_CONTRATO_ID) ?: 0
        @Suppress("DEPRECATION")
        materialContratoParaEdicao = arguments?.getParcelable(ARG_MATERIAL_CONTRATO)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_material_contrato, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupViewModel()
        setupListeners()
        carregarMateriais()
        materialContratoParaEdicao?.let { preencherFormulario(it) }
    }

    private fun initViews(view: View) {
        tilMaterial = view.findViewById(R.id.tilMaterial)
        actvMaterial = view.findViewById(R.id.actvMaterial)
        tilQuantidade = view.findViewById(R.id.tilQuantidade)
        etQuantidade = view.findViewById(R.id.etQuantidade)
        tilValorUnitario = view.findViewById(R.id.tilValorUnitario)
        etValorUnitario = view.findViewById(R.id.etValorUnitario)
        tvValorTotal = view.findViewById(R.id.tvValorTotal)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
    }

    private fun setupViewModel() {
        val factory = MateriaisViewModelFactory()
        materiaisViewModel = ViewModelProvider(this, factory)[MateriaisViewModel::class.java]
    }

    private fun setupListeners() {
        actvMaterial.setOnItemClickListener { _, _, position, _ ->
            materialSelecionado = materiaisDisponiveis.getOrNull(position)
            materialSelecionado?.let { material ->
                etValorUnitario.setText(formatarMoedaInput(material.valorUnitario))
                if (materialContratoParaEdicao == null) {
                    etQuantidade.setText("1")
                }
                calcularValorTotal()
            }
        }

        etQuantidade.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorTotal()
            }
        })

        etValorUnitario.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularValorTotal()
            }
        })

        btnCancelar.setOnClickListener { dismiss() }
        btnSalvar.setOnClickListener {
            if (validarFormulario()) {
                salvarMaterialContrato()
            }
        }
    }

    private fun carregarMateriais() {
        materiaisViewModel.carregarMateriaisDisponiveis { materiais ->
            materiaisDisponiveis = materiais
            val labels = materiais.map { material ->
                "${material.nome} (${material.codigo ?: "-"}) - disp: ${material.getQuantidadeDisponivelAtual()}"
            }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, labels)
            actvMaterial.setAdapter(adapter)

            materialContratoParaEdicao?.let { item ->
                val encontrado = materiais.firstOrNull { it.id == item.materialId }
                materialSelecionado = encontrado
                val texto = encontrado?.let {
                    "${it.nome} (${it.codigo ?: "-"}) - disp: ${it.getQuantidadeDisponivelAtual()}"
                } ?: item.nomeMaterialExibicao
                actvMaterial.setText(texto, false)
            }
        }
    }

    private fun preencherFormulario(item: MaterialContrato) {
        etQuantidade.setText(item.quantidade.toString())
        etValorUnitario.setText(formatarMoedaInput(item.valorUnitario))
        tvValorTotal.setText(formatarMoedaInput(item.valorTotal))
    }

    private fun validarFormulario(): Boolean {
        var valido = true
        val material = materialSelecionado
        val quantidade = etQuantidade.text?.toString()?.toIntOrNull() ?: 0
        val valorUnitario = parseInputMoeda(etValorUnitario.text?.toString().orEmpty())

        if (material == null) {
            tilMaterial.error = "Selecione um material"
            valido = false
        } else {
            tilMaterial.error = null
        }

        if (quantidade <= 0) {
            tilQuantidade.error = "Quantidade deve ser maior que zero"
            valido = false
        } else {
            val quantidadeAnterior = materialContratoParaEdicao?.takeIf { it.materialId == material?.id }?.quantidade ?: 0
            val disponivel = (material?.getQuantidadeDisponivelAtual() ?: 0) + quantidadeAnterior
            if (quantidade > disponivel) {
                tilQuantidade.error = "Disponivel: $disponivel"
                valido = false
            } else {
                tilQuantidade.error = null
            }
        }

        if (valorUnitario <= 0) {
            tilValorUnitario.error = "Valor unitario invalido"
            valido = false
        } else {
            tilValorUnitario.error = null
        }

        return valido
    }

    private fun calcularValorTotal() {
        val quantidade = etQuantidade.text?.toString()?.toIntOrNull() ?: 0
        val valorUnitario = parseInputMoeda(etValorUnitario.text?.toString().orEmpty())
        val total = quantidade * valorUnitario
        tvValorTotal.setText(formatarMoedaInput(total))
    }

    private fun salvarMaterialContrato() {
        val material = materialSelecionado ?: return
        val quantidade = etQuantidade.text?.toString()?.toIntOrNull() ?: return
        val valorUnitario = parseInputMoeda(etValorUnitario.text?.toString().orEmpty())
        val valorTotal = quantidade * valorUnitario

        val item = MaterialContrato(
            id = materialContratoParaEdicao?.id ?: MaterialContrato.generateTempId(),
            contratoId = contratoId,
            materialId = material.id,
            quantidade = quantidade,
            valorUnitario = valorUnitario,
            valorTotal = valorTotal,
            material = material
        )

        onMaterialSalvoListener?.invoke(item)
        dismiss()
    }

    private fun parseInputMoeda(texto: String): Double {
        return texto.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    private fun formatarMoedaInput(valor: Double): String {
        return DecimalFormat("0.00", DecimalFormatSymbols(Locale.US)).format(valor)
    }

    fun setOnMaterialSalvoListener(listener: (MaterialContrato) -> Unit) {
        onMaterialSalvoListener = listener
    }
}
