package com.example.alg_gestao_02.ui.material

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

class CadastroMaterialDialogFragment : DialogFragment() {
    private lateinit var tilNomeMaterial: TextInputLayout
    private lateinit var tilCodigoMaterial: TextInputLayout
    private lateinit var tilValorUnitario: TextInputLayout
    private lateinit var tilQuantidade: TextInputLayout
    private lateinit var etNomeMaterial: TextInputEditText
    private lateinit var etCodigoMaterial: TextInputEditText
    private lateinit var etValorUnitario: TextInputEditText
    private lateinit var etQuantidade: TextInputEditText
    private lateinit var switchAtivo: SwitchMaterial
    private lateinit var btnCancelar: Button
    private lateinit var btnSalvar: Button

    private var material: Material? = null
    private var onMaterialSavedListener: ((Material) -> Unit)? = null

    companion object {
        private const val ARG_MATERIAL = "material"

        fun newInstance(material: Material? = null): CadastroMaterialDialogFragment {
            return CadastroMaterialDialogFragment().apply {
                arguments = Bundle().apply {
                    material?.let { putParcelable(ARG_MATERIAL, it) }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        @Suppress("DEPRECATION")
        material = arguments?.getParcelable(ARG_MATERIAL)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_cadastro_material, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setupListeners()
        material?.let { preencherCampos(it) }
    }

    private fun initViews(view: View) {
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = if (material == null) "Cadastro de Material" else "Editar Material"
        toolbar.setNavigationOnClickListener { dismiss() }

        tilNomeMaterial = view.findViewById(R.id.tilNomeMaterial)
        tilCodigoMaterial = view.findViewById(R.id.tilCodigoMaterial)
        tilValorUnitario = view.findViewById(R.id.tilValorUnitario)
        tilQuantidade = view.findViewById(R.id.tilQuantidade)
        etNomeMaterial = view.findViewById(R.id.etNomeMaterial)
        etCodigoMaterial = view.findViewById(R.id.etCodigoMaterial)
        etValorUnitario = view.findViewById(R.id.etValorUnitario)
        etQuantidade = view.findViewById(R.id.etQuantidade)
        switchAtivo = view.findViewById(R.id.switchAtivo)
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
    }

    private fun setupListeners() {
        setupCurrencyFormatting(etValorUnitario)

        btnCancelar.setOnClickListener { dismiss() }
        btnSalvar.setOnClickListener {
            if (validarCampos()) {
                salvarMaterial()
            }
        }
    }

    private fun setupCurrencyFormatting(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString() != current) {
                    editText.removeTextChangedListener(this)
                    val cleanString = s.toString().replace("[R$,.\\s]".toRegex(), "")
                    val parsed = if (cleanString.isNotEmpty()) cleanString.toDouble() / 100 else 0.0
                    val formatted = currencyFormat.format(parsed)
                    current = formatted
                    editText.setText(formatted)
                    editText.setSelection(formatted.length)
                    editText.addTextChangedListener(this)
                }
            }
        })
    }

    private fun parseCurrencyValue(text: String): Double {
        return try {
            val cleanString = text.replace("[R$,.\\s]".toRegex(), "")
            if (cleanString.isNotEmpty()) cleanString.toDouble() / 100 else 0.0
        } catch (_: ParseException) {
            0.0
        }
    }

    private fun preencherCampos(material: Material) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        etNomeMaterial.setText(material.nome)
        etCodigoMaterial.setText(material.codigo ?: "")
        etValorUnitario.setText(currencyFormat.format(material.valorUnitario))
        etQuantidade.setText(material.quantidadeDisponivel.toString())
        switchAtivo.isChecked = material.ativo
    }

    private fun validarCampos(): Boolean {
        var valid = true
        if (etNomeMaterial.text.isNullOrBlank()) {
            tilNomeMaterial.error = "Nome obrigatorio"
            valid = false
        } else {
            tilNomeMaterial.error = null
        }

        val valorUnitario = parseCurrencyValue(etValorUnitario.text?.toString().orEmpty())
        if (valorUnitario <= 0.0) {
            tilValorUnitario.error = "Valor unitario deve ser maior que zero"
            valid = false
        } else {
            tilValorUnitario.error = null
        }

        val quantidade = etQuantidade.text?.toString()?.toIntOrNull()
        if (quantidade == null || quantidade < 0) {
            tilQuantidade.error = "Quantidade invalida"
            valid = false
        } else {
            tilQuantidade.error = null
        }

        return valid
    }

    private fun salvarMaterial() {
        try {
            val novoMaterial = Material(
                id = material?.id ?: 0,
                nome = etNomeMaterial.text.toString().trim(),
                codigo = etCodigoMaterial.text?.toString()?.trim()?.takeIf { it.isNotBlank() },
                valorUnitario = parseCurrencyValue(etValorUnitario.text?.toString().orEmpty()),
                quantidadeDisponivel = etQuantidade.text?.toString()?.toIntOrNull() ?: 0,
                ativo = switchAtivo.isChecked
            )
            onMaterialSavedListener?.invoke(novoMaterial)
            dismiss()
        } catch (e: Exception) {
            LogUtils.error("CadastroMaterialDialog", "Erro ao salvar material", e)
        }
    }

    fun setOnMaterialSavedListener(listener: (Material) -> Unit) {
        onMaterialSavedListener = listener
    }
}
