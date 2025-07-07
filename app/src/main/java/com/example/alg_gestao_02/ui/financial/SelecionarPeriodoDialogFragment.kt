package com.example.alg_gestao_02.ui.financial

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.alg_gestao_02.databinding.DialogSelecionarPeriodoBinding
import com.example.alg_gestao_02.databinding.DialogPeriodoEspecificoBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * DialogFragment para seleção de período antes de abrir o resumo mensal do cliente
 */
class SelecionarPeriodoDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_CLIENTE_ID = "cliente_id"
        private const val ARG_CLIENTE_NOME = "cliente_nome"
        
        fun newInstance(clienteId: Int, clienteNome: String): SelecionarPeriodoDialogFragment {
            return SelecionarPeriodoDialogFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CLIENTE_ID, clienteId)
                    putString(ARG_CLIENTE_NOME, clienteNome)
                }
            }
        }
    }

    private var _binding: DialogSelecionarPeriodoBinding? = null
    private val binding get() = _binding!!
    
    private var clienteId: Int = 0
    private var clienteNome: String = ""
    
    interface OnPeriodoSelecionadoListener {
        fun onPeriodoSelecionado(clienteId: Int, clienteNome: String, mesReferencia: String)
    }
    
    private var listener: OnPeriodoSelecionadoListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when {
            parentFragment is OnPeriodoSelecionadoListener -> parentFragment as OnPeriodoSelecionadoListener
            activity is OnPeriodoSelecionadoListener -> activity as OnPeriodoSelecionadoListener
            else -> null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar estilo do dialog
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog_MinWidth)
        
        arguments?.let {
            clienteId = it.getInt(ARG_CLIENTE_ID)
            clienteNome = it.getString(ARG_CLIENTE_NOME) ?: ""
        }
        
        LogUtils.info("SelecionarPeriodoDialog", "📅 ========== DIALOG DE SELEÇÃO DE PERÍODO ==========")
        LogUtils.info("SelecionarPeriodoDialog", "👤 Cliente: $clienteNome (ID: $clienteId)")
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogSelecionarPeriodoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurações do dialog
        dialog?.window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        
        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Configurar nome do cliente
        binding.tvClienteNome.text = clienteNome
        
        // Configurar mês atual
        val calendar = Calendar.getInstance()
        val formatoMes = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
        val mesAtualFormatado = formatoMes.format(calendar.time)
        binding.tvMesAtual.text = mesAtualFormatado
        
        // Configurar último com dados (simulado - em produção viria da API)
        binding.tvUltimoComDados.text = "Junho/2025 (último período com movimento)"
        
        LogUtils.debug("SelecionarPeriodoDialog", "🗓️ Mês atual configurado: $mesAtualFormatado")
    }

    private fun setupClickListeners() {
        // Mês Atual
        binding.cardMesAtual.setOnClickListener {
            val calendar = Calendar.getInstance()
            val mesReferencia = String.format("%04d-%02d", 
                calendar.get(Calendar.YEAR), 
                calendar.get(Calendar.MONTH) + 1)
            
            LogUtils.info("SelecionarPeriodoDialog", "✅ Selecionado: Mês Atual ($mesReferencia)")
            selecionarPeriodo(mesReferencia)
        }
        
        // Período Específico
        binding.cardPeriodoEspecifico.setOnClickListener {
            LogUtils.info("SelecionarPeriodoDialog", "📊 Abrindo seletor de período específico")
            mostrarDialogPeriodoEspecifico()
        }
        
        // Último com Dados
        binding.cardUltimoComDados.setOnClickListener {
            // Por enquanto usar junho/2025 como exemplo
            // Em produção, isso viria da API
            val mesReferencia = "2025-06"
            
            LogUtils.info("SelecionarPeriodoDialog", "📈 Selecionado: Último com Dados ($mesReferencia)")
            selecionarPeriodo(mesReferencia)
        }
        
        // Cancelar
        binding.btnCancelar.setOnClickListener {
            LogUtils.info("SelecionarPeriodoDialog", "❌ Seleção cancelada pelo usuário")
            dismiss()
        }
    }

    private fun mostrarDialogPeriodoEspecifico() {
        val dialogBinding = DialogPeriodoEspecificoBinding.inflate(layoutInflater)
        
        // Configurar nome do cliente
        dialogBinding.tvClienteNome.text = clienteNome
        
        // Configurar seletores
        setupPeriodSelectors(dialogBinding)
        
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .create()
        
        // Configurar listeners
        dialogBinding.btnCancelar.setOnClickListener {
            LogUtils.debug("SelecionarPeriodoDialog", "❌ Período específico cancelado")
            dialog.dismiss()
        }
        
        dialogBinding.btnConfirmar.setOnClickListener {
            val mesTexto = dialogBinding.actvMes.text.toString()
            val anoTexto = dialogBinding.actvAno.text.toString()
            
            if (mesTexto.isEmpty() || anoTexto.isEmpty()) {
                Toast.makeText(requireContext(), "Selecione mês e ano", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                val meses = arrayOf(
                    "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                    "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
                )
                
                val mesIndex = meses.indexOf(mesTexto)
                if (mesIndex == -1) {
                    Toast.makeText(requireContext(), "Mês inválido", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                val mes = mesIndex + 1
                val ano = anoTexto.toInt()
                val mesReferencia = String.format("%04d-%02d", ano, mes)
                
                LogUtils.info("SelecionarPeriodoDialog", "✅ Período específico confirmado: $mesReferencia")
                dialog.dismiss()
                selecionarPeriodo(mesReferencia)
                
            } catch (e: Exception) {
                LogUtils.error("SelecionarPeriodoDialog", "❌ Erro ao processar período: ${e.message}")
                Toast.makeText(requireContext(), "Erro ao processar período", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }

    private fun setupPeriodSelectors(dialogBinding: DialogPeriodoEspecificoBinding) {
        // Configurar seletor de mês
        val meses = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val mesAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, meses)
        dialogBinding.actvMes.setAdapter(mesAdapter)
        
        // Forçar exibição do dropdown ao clicar
        dialogBinding.actvMes.setOnClickListener {
            dialogBinding.actvMes.showDropDown()
        }
        
        dialogBinding.actvMes.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialogBinding.actvMes.showDropDown()
            }
        }
        
        // Configurar seletor de ano
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (2020..anoAtual + 1).map { it.toString() }.toTypedArray()
        val anoAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, anos)
        dialogBinding.actvAno.setAdapter(anoAdapter)
        
        // Forçar exibição do dropdown ao clicar
        dialogBinding.actvAno.setOnClickListener {
            dialogBinding.actvAno.showDropDown()
        }
        
        dialogBinding.actvAno.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                dialogBinding.actvAno.showDropDown()
            }
        }
        
        LogUtils.debug("SelecionarPeriodoDialog", "📅 Seletores configurados: ${meses.size} meses, ${anos.size} anos")
        LogUtils.debug("SelecionarPeriodoDialog", "🔧 Listeners de clique configurados para forçar dropdown")
    }

    private fun selecionarPeriodo(mesReferencia: String) {
        LogUtils.info("SelecionarPeriodoDialog", "🎯 Período selecionado: $mesReferencia")
        LogUtils.info("SelecionarPeriodoDialog", "🔄 Redirecionando para resumo mensal...")
        
        listener?.onPeriodoSelecionado(clienteId, clienteNome, mesReferencia)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 