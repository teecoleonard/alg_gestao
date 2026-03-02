package com.example.alg_gestao_02.ui.contrato

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.api.ApiService
import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.StatusContrato
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

/**
 * BottomSheet para atualizar o status do contrato
 */
class AtualizarStatusBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tvTitulo: TextView
    private lateinit var tvStatusAtual: TextView
    private lateinit var radioGroup: RadioGroup
    private lateinit var etMotivo: EditText
    private lateinit var btnAtualizar: Button
    private lateinit var btnCancelar: Button

    private var contratoId: Int = 0
    private var statusAtual: String = "PENDENTE"
    private var onStatusAtualizado: ((Contrato) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contratoId = arguments?.getInt(ARG_CONTRATO_ID) ?: 0
        statusAtual = arguments?.getString(ARG_STATUS_ATUAL) ?: "PENDENTE"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_atualizar_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar views
        tvTitulo = view.findViewById(R.id.tvTituloStatus)
        tvStatusAtual = view.findViewById(R.id.tvStatusAtual)
        radioGroup = view.findViewById(R.id.radioGroupStatus)
        etMotivo = view.findViewById(R.id.etMotivo)
        btnAtualizar = view.findViewById(R.id.btnAtualizar)
        btnCancelar = view.findViewById(R.id.btnCancelar)

        // Configurar status atual
        val status = StatusContrato.fromString(statusAtual)
        tvStatusAtual.text = "${status.getIcone()} ${status.descricao}"
        tvStatusAtual.setTextColor(status.getCor())

        // Configurar opções disponíveis baseado no status atual
        configurarOpcoesDisponiveis()

        // Configurar botões
        btnAtualizar.setOnClickListener {
            atualizarStatus()
        }

        btnCancelar.setOnClickListener {
            dismiss()
        }
    }

    /**
     * Configura quais status estão disponíveis baseado no status atual
     */
    private fun configurarOpcoesDisponiveis() {
        radioGroup.removeAllViews()

        val opcoesDisponiveis = when (statusAtual) {
            "PENDENTE" -> listOf(
                StatusContrato.ASSINADO,
                StatusContrato.CANCELADO
            )
            "ASSINADO" -> listOf(
                StatusContrato.EM_ANDAMENTO,
                StatusContrato.CANCELADO
            )
            "EM_ANDAMENTO" -> listOf(
                StatusContrato.FINALIZADO,
                StatusContrato.CANCELADO
            )
            "FINALIZADO" -> listOf(
                StatusContrato.FATURADO,
                StatusContrato.CANCELADO
            )
            else -> emptyList()
        }

        if (opcoesDisponiveis.isEmpty()) {
            val tvNenhumaOpcao = TextView(requireContext()).apply {
                text = "⚠️ Status ${StatusContrato.fromString(statusAtual).descricao} não permite alterações"
                textSize = 14f
                setPadding(16, 16, 16, 16)
            }
            radioGroup.addView(tvNenhumaOpcao)
            btnAtualizar.isEnabled = false
            return
        }

        // Criar radio buttons para cada opção
        opcoesDisponiveis.forEach { status ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = "${status.getIcone()} ${status.descricao}"
                textSize = 16f
                setPadding(16, 16, 16, 16)
                setTextColor(status.getCor())
                tag = status.valor
            }
            radioGroup.addView(radioButton)
        }
    }

    /**
     * Atualiza o status do contrato via API
     */
    private fun atualizarStatus() {
        val selectedId = radioGroup.checkedRadioButtonId
        if (selectedId == -1) {
            Toast.makeText(requireContext(), "Selecione um status", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedRadio = view?.findViewById<RadioButton>(selectedId)
        val novoStatus = selectedRadio?.tag as? String ?: return
        val motivo = etMotivo.text.toString().trim()

        lifecycleScope.launch {
            try {
                btnAtualizar.isEnabled = false
                btnAtualizar.text = "Atualizando..."

                val apiService = ApiClient.apiService
                val request = ApiService.AtualizarStatusRequest(
                    statusContrato = novoStatus,
                    motivo = if (motivo.isNotEmpty()) motivo else null
                )

                val response = apiService.atualizarStatusContrato(contratoId, request)

                if (response.isSuccessful && response.body() != null) {
                    val resultado = response.body()!!
                    
                    Toast.makeText(
                        requireContext(),
                        "✅ ${resultado.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    onStatusAtualizado?.invoke(resultado.contrato)
                    dismiss()
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Erro desconhecido"
                    Toast.makeText(
                        requireContext(),
                        "❌ Erro: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "❌ Erro: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnAtualizar.isEnabled = true
                btnAtualizar.text = "Atualizar Status"
            }
        }
    }

    /**
     * Define callback para quando status for atualizado
     */
    fun setOnStatusAtualizadoListener(listener: (Contrato) -> Unit) {
        onStatusAtualizado = listener
    }

    companion object {
        private const val ARG_CONTRATO_ID = "contrato_id"
        private const val ARG_STATUS_ATUAL = "status_atual"

        /**
         * Cria nova instância do BottomSheet
         */
        fun newInstance(
            contratoId: Int,
            statusAtual: String,
            onStatusAtualizado: (Contrato) -> Unit
        ): AtualizarStatusBottomSheet {
            return AtualizarStatusBottomSheet().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CONTRATO_ID, contratoId)
                    putString(ARG_STATUS_ATUAL, statusAtual)
                }
                setOnStatusAtualizadoListener(onStatusAtualizado)
            }
        }
    }
}

