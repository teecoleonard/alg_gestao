package com.example.alg_gestao_02.ui.equipamento

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModel
import com.example.alg_gestao_02.ui.equipamento.viewmodel.EquipamentosViewModelFactory
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale

/**
 * Dialog para exibição dos detalhes de um equipamento
 */
class EquipamentoDetailDialog : DialogFragment() {
    
    private lateinit var viewModel: EquipamentosViewModel
    private var equipamentoId: Int = 0
    private var equipamento: Equipamento? = null
    
    private lateinit var tvDetalhesNomeEquipamento: TextView
    private lateinit var tvDetalhesCodigoEquipamento: TextView
    private lateinit var tvDetalhesQuantidadeEquipamento: TextView
    private lateinit var tvDetalhesPrecoDiaria: TextView
    private lateinit var tvDetalhesPrecoSemanal: TextView
    private lateinit var tvDetalhesPrecoQuinzenal: TextView
    private lateinit var tvDetalhesPrecoMensal: TextView
    private lateinit var tvDetalhesValorPatrimonio: TextView
    private lateinit var btnFecharDetalhes: Button
    private lateinit var btnEditarEquipamento: Button
    
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    companion object {
        private const val ARG_EQUIPAMENTO_ID = "equipamento_id"
        
        fun newInstance(equipamentoId: Int): EquipamentoDetailDialog {
            val fragment = EquipamentoDetailDialog()
            val args = Bundle()
            args.putInt(ARG_EQUIPAMENTO_ID, equipamentoId)
            fragment.arguments = args
            return fragment
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.EquipamentoDetailDialogStyle)
        
        equipamentoId = arguments?.getInt(ARG_EQUIPAMENTO_ID) ?: 0
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_equipamento_details, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews(view)
        setupViewModel()
        setupListeners()
        
        carregarDadosEquipamento()
    }
    
    private fun initViews(view: View) {
        tvDetalhesNomeEquipamento = view.findViewById(R.id.tvDetalhesNomeEquipamento)
        tvDetalhesCodigoEquipamento = view.findViewById(R.id.tvDetalhesCodigoEquipamento)
        tvDetalhesQuantidadeEquipamento = view.findViewById(R.id.tvDetalhesQuantidadeEquipamento)
        tvDetalhesPrecoDiaria = view.findViewById(R.id.tvDetalhesPrecoDiaria)
        tvDetalhesPrecoSemanal = view.findViewById(R.id.tvDetalhesPrecoSemanal)
        tvDetalhesPrecoQuinzenal = view.findViewById(R.id.tvDetalhesPrecoQuinzenal)
        tvDetalhesPrecoMensal = view.findViewById(R.id.tvDetalhesPrecoMensal)
        tvDetalhesValorPatrimonio = view.findViewById(R.id.tvDetalhesValorPatrimonio)
        btnFecharDetalhes = view.findViewById(R.id.btnFecharDetalhes)
        btnEditarEquipamento = view.findViewById(R.id.btnEditarEquipamento)
    }
    
    private fun setupViewModel() {
        val factory = EquipamentosViewModelFactory()
        viewModel = ViewModelProvider(requireActivity(), factory)[EquipamentosViewModel::class.java]
    }
    
    private fun setupListeners() {
        btnFecharDetalhes.setOnClickListener {
            dismiss()
        }
        
        btnEditarEquipamento.setOnClickListener {
            equipamento?.let { equip ->
                val dialog = CadastroEquipamentoDialogFragment.newInstance(equip)
                dialog.setOnEquipamentoSavedListener { equipamentoAtualizado ->
                    viewModel.atualizarEquipamento(equip.id, equipamentoAtualizado)
                    dismiss()
                }
                dialog.show(parentFragmentManager, "EditEquipamentoDialog")
            }
        }
    }
    
    private fun carregarDadosEquipamento() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            if (state is com.example.alg_gestao_02.ui.state.UiState.Success) {
                val equipamentoEncontrado = state.data.find { it.id == equipamentoId }
                if (equipamentoEncontrado != null) {
                    equipamento = equipamentoEncontrado
                    exibirDetalhesEquipamento(equipamentoEncontrado)
                } else {
                    Toast.makeText(context, "Equipamento não encontrado", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
        }
    }
    
    private fun exibirDetalhesEquipamento(equipamento: Equipamento) {
        tvDetalhesNomeEquipamento.text = equipamento.nomeEquip
        tvDetalhesCodigoEquipamento.text = "Código: ${equipamento.codigoEquip}"
        tvDetalhesQuantidadeEquipamento.text = "Quantidade disponível: ${equipamento.quantidadeEquip}"
        
        tvDetalhesPrecoDiaria.text = currencyFormat.format(equipamento.precoDiaria)
        tvDetalhesPrecoSemanal.text = currencyFormat.format(equipamento.precoSemanal)
        tvDetalhesPrecoQuinzenal.text = currencyFormat.format(equipamento.precoQuinzenal)
        tvDetalhesPrecoMensal.text = currencyFormat.format(equipamento.precoMensal)
        
        if (equipamento.valorPatrimonio != null && equipamento.valorPatrimonio > 0) {
            tvDetalhesValorPatrimonio.text = currencyFormat.format(equipamento.valorPatrimonio)
        } else {
            tvDetalhesValorPatrimonio.text = "Não informado"
        }
    }
} 