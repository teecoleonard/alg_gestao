package com.example.alg_gestao_02.ui.equipamento

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.text.NumberFormat
import java.text.ParseException
import java.util.Locale

/**
 * Dialog para cadastro e edição de equipamentos
 */
class CadastroEquipamentoDialogFragment : DialogFragment() {
    
    private lateinit var tilNomeEquipamento: TextInputLayout
    private lateinit var tilCodigoEquipamento: TextInputLayout
    private lateinit var tilPrecoDiaria: TextInputLayout
    private lateinit var tilPrecoSemanal: TextInputLayout
    private lateinit var tilPrecoQuinzenal: TextInputLayout
    private lateinit var tilPrecoMensal: TextInputLayout
    private lateinit var tilQuantidade: TextInputLayout
    private lateinit var tilValorPatrimonio: TextInputLayout
    
    private lateinit var etNomeEquipamento: TextInputEditText
    private lateinit var etCodigoEquipamento: TextInputEditText
    private lateinit var etPrecoDiaria: TextInputEditText
    private lateinit var etPrecoSemanal: TextInputEditText
    private lateinit var etPrecoQuinzenal: TextInputEditText
    private lateinit var etPrecoMensal: TextInputEditText
    private lateinit var etQuantidade: TextInputEditText
    private lateinit var etValorPatrimonio: TextInputEditText
    
    private lateinit var btnCancelar: Button
    private lateinit var btnSalvar: Button
    
    private var equipamento: Equipamento? = null
    private var onEquipamentoSavedListener: ((Equipamento) -> Unit)? = null
    
    companion object {
        private const val ARG_EQUIPAMENTO = "equipamento"
        
        fun newInstance(): CadastroEquipamentoDialogFragment {
            return CadastroEquipamentoDialogFragment()
        }
        
        fun newInstance(equipamento: Equipamento): CadastroEquipamentoDialogFragment {
            val fragment = CadastroEquipamentoDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_EQUIPAMENTO, equipamento)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
        
        @Suppress("DEPRECATION")
        equipamento = arguments?.getParcelable(ARG_EQUIPAMENTO)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_cadastro_equipamento, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupListeners()
        
        // Se for edição, preenche os campos
        equipamento?.let { preencherCampos(it) }
    }
    
    private fun initViews(view: View) {
        tilNomeEquipamento = view.findViewById(R.id.tilNomeEquipamento)
        tilCodigoEquipamento = view.findViewById(R.id.tilCodigoEquipamento)
        tilPrecoDiaria = view.findViewById(R.id.tilPrecoDiaria)
        tilPrecoSemanal = view.findViewById(R.id.tilPrecoSemanal)
        tilPrecoQuinzenal = view.findViewById(R.id.tilPrecoQuinzenal)
        tilPrecoMensal = view.findViewById(R.id.tilPrecoMensal)
        tilQuantidade = view.findViewById(R.id.tilQuantidade)
        tilValorPatrimonio = view.findViewById(R.id.tilValorPatrimonio)
        
        etNomeEquipamento = view.findViewById(R.id.etNomeEquipamento)
        etCodigoEquipamento = view.findViewById(R.id.etCodigoEquipamento)
        etPrecoDiaria = view.findViewById(R.id.etPrecoDiaria)
        etPrecoSemanal = view.findViewById(R.id.etPrecoSemanal)
        etPrecoQuinzenal = view.findViewById(R.id.etPrecoQuinzenal)
        etPrecoMensal = view.findViewById(R.id.etPrecoMensal)
        etQuantidade = view.findViewById(R.id.etQuantidade)
        etValorPatrimonio = view.findViewById(R.id.etValorPatrimonio)
        
        btnCancelar = view.findViewById(R.id.btnCancelar)
        btnSalvar = view.findViewById(R.id.btnSalvar)
        
        // Configurar o título conforme a operação
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.title = if (equipamento == null) "Cadastro de Equipamento" else "Editar Equipamento"
        toolbar.setNavigationOnClickListener {
            dismiss()
        }
    }
    
    private fun setupListeners() {
        // Aplicar formatação monetária para campos de preço
        setupCurrencyFormatting(etPrecoDiaria)
        setupCurrencyFormatting(etPrecoSemanal)
        setupCurrencyFormatting(etPrecoQuinzenal)
        setupCurrencyFormatting(etPrecoMensal)
        setupCurrencyFormatting(etValorPatrimonio)
        
        btnCancelar.setOnClickListener {
            dismiss()
        }
        
        btnSalvar.setOnClickListener {
            if (validarCampos()) {
                salvarEquipamento()
            }
        }
    }
    
    private fun setupCurrencyFormatting(editText: TextInputEditText) {
        editText.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
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
            
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    
    private fun parseCurrencyValue(text: String): Double {
        try {
            val cleanString = text.replace("[R$,.\\s]".toRegex(), "")
            return if (cleanString.isNotEmpty()) cleanString.toDouble() / 100 else 0.0
        } catch (e: ParseException) {
            LogUtils.error("CadastroEquipamentoDialogFragment", "Erro ao converter valor monetário", e)
            return 0.0
        }
    }
    
    private fun preencherCampos(equipamento: Equipamento) {
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        
        etNomeEquipamento.setText(equipamento.nomeEquip)
        etCodigoEquipamento.setText(equipamento.codigoEquip)
        etPrecoDiaria.setText(currencyFormat.format(equipamento.precoDiaria))
        etPrecoSemanal.setText(currencyFormat.format(equipamento.precoSemanal))
        etPrecoQuinzenal.setText(currencyFormat.format(equipamento.precoQuinzenal))
        etPrecoMensal.setText(currencyFormat.format(equipamento.precoMensal))
        etQuantidade.setText(equipamento.quantidadeDisp.toString())
        etValorPatrimonio.setText(equipamento.valorPatrimonio?.let { currencyFormat.format(it) } ?: "")
    }
    
    private fun validarCampos(): Boolean {
        var isValid = true
        
        // Validar nome
        if (etNomeEquipamento.text.isNullOrBlank()) {
            tilNomeEquipamento.error = "Nome é obrigatório"
            isValid = false
        } else {
            tilNomeEquipamento.error = null
        }
        
        // Validar código
        if (etCodigoEquipamento.text.isNullOrBlank()) {
            tilCodigoEquipamento.error = "Código é obrigatório"
            isValid = false
        } else {
            tilCodigoEquipamento.error = null
        }
        
        // Validar quantidade
        if (etQuantidade.text.isNullOrBlank()) {
            tilQuantidade.error = "Quantidade é obrigatória"
            isValid = false
        } else {
            try {
                val quantidade = etQuantidade.text.toString().toInt()
                if (quantidade <= 0) {
                    tilQuantidade.error = "Quantidade deve ser maior que zero"
                    isValid = false
                } else {
                    tilQuantidade.error = null
                }
            } catch (e: NumberFormatException) {
                tilQuantidade.error = "Quantidade inválida"
                isValid = false
            }
        }
        
        // Validar preços (pelo menos o preço da diária é obrigatório)
        if (etPrecoDiaria.text.isNullOrBlank() || parseCurrencyValue(etPrecoDiaria.text.toString()) <= 0) {
            tilPrecoDiaria.error = "Preço da diária é obrigatório"
            isValid = false
        } else {
            tilPrecoDiaria.error = null
        }
        
        return isValid
    }
    
    private fun salvarEquipamento() {
        try {
            val nome = etNomeEquipamento.text.toString().trim()
            val codigo = etCodigoEquipamento.text.toString().trim()
            val precoDiaria = parseCurrencyValue(etPrecoDiaria.text.toString())
            val precoSemanal = parseCurrencyValue(etPrecoSemanal.text.toString())
            val precoQuinzenal = parseCurrencyValue(etPrecoQuinzenal.text.toString())
            val precoMensal = parseCurrencyValue(etPrecoMensal.text.toString())
            val quantidade = etQuantidade.text.toString().toInt()
            val valorPatrimonio = if (etValorPatrimonio.text.isNullOrBlank()) null else parseCurrencyValue(etValorPatrimonio.text.toString())
            
            val novoEquipamento = Equipamento(
                id = equipamento?.id ?: 0,
                nomeEquip = nome,
                codigoEquip = codigo,
                precoDiaria = precoDiaria,
                precoSemanal = precoSemanal,
                precoQuinzenal = precoQuinzenal,
                precoMensal = precoMensal,
                quantidadeDisp = quantidade,
                valorPatrimonio = valorPatrimonio
            )
            
            onEquipamentoSavedListener?.invoke(novoEquipamento)
            dismiss()
            
        } catch (e: Exception) {
            LogUtils.error("CadastroEquipamentoDialogFragment", "Erro ao salvar equipamento", e)
            Toast.makeText(context, "Erro ao salvar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    fun setOnEquipamentoSavedListener(listener: (Equipamento) -> Unit) {
        onEquipamentoSavedListener = listener
    }
} 