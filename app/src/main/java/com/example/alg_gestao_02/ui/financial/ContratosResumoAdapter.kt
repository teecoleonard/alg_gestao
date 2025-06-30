package com.example.alg_gestao_02.ui.financial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ContratoResumo
import com.example.alg_gestao_02.databinding.ItemContratoResumoBinding
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para lista de contratos no resumo mensal
 */
class ContratosResumoAdapter : ListAdapter<ContratoResumo, ContratosResumoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContratoResumoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(
        private val binding: ItemContratoResumoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contrato: ContratoResumo) {
            // Debug: Log dos dados do contrato
            LogUtils.debug("ContratosResumoAdapter", "Contrato recebido: ID=${contrato.contratoId}, Num='${contrato.contratoNum}', Período='${contrato.periodo}', Status='${contrato.status}'")
            
            // Número do contrato
            val numeroContrato = contrato.contratoNum?.takeIf { it.isNotBlank() } ?: contrato.contratoId.toString()
            binding.tvNumeroContrato.text = "Contrato $numeroContrato"
            
            // Valor mensal formatado
            val valorFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                .format(contrato.valorMensal)
            binding.tvValorMensalContrato.text = valorFormatado
            
            // Período - com mais variações e fallback
            binding.tvPeriodoContrato.text = when (contrato.periodo?.uppercase()?.trim()) {
                "DIARIO", "DIÁRIA", "DAILY" -> "Diário"
                "SEMANAL", "WEEKLY" -> "Semanal"
                "QUINZENAL", "BIWEEKLY" -> "Quinzenal"
                "MENSAL", "MONTHLY" -> "Mensal"
                "ANUAL", "YEARLY" -> "Anual"
                "30", "30_DIAS" -> "Mensal"
                "7", "7_DIAS" -> "Semanal"
                "1", "1_DIA" -> "Diário"
                null, "", " " -> "Mensal" // Fallback padrão se período vier vazio
                else -> {
                    // Se não reconhecer, usar o valor original ou um padrão
                    val periodoOriginal = contrato.periodo
                    if (!periodoOriginal.isNullOrBlank()) {
                        periodoOriginal.lowercase().replaceFirstChar { it.uppercase() }
                    } else {
                        "Mensal" // Padrão mais comum
                    }
                }
            }
            
            // Data de assinatura
            binding.tvDataAssinatura.text = formatarData(contrato.dataAssinatura)
            
            // Status do contrato
            setupStatusChip(contrato.status)
        }

        private fun setupStatusChip(status: String?) {
            val chip = binding.chipStatusContrato
            val context = binding.root.context
            
            when (status?.uppercase()) {
                "ASSINADO" -> {
                    chip.text = "ASSINADO"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.success)
                }
                "PENDENTE" -> {
                    chip.text = "PENDENTE"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.warning)
                }
                "CANCELADO" -> {
                    chip.text = "CANCELADO"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.error)
                }
                else -> {
                    chip.text = status ?: "N/A"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.text_secondary)
                }
            }
        }

        private fun formatarData(dataString: String?): String {
            if (dataString.isNullOrEmpty()) return "N/A"
            
            return try {
                val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val formatoSaida = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val data = formatoEntrada.parse(dataString)
                formatoSaida.format(data ?: Date())
            } catch (e: Exception) {
                dataString
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ContratoResumo>() {
        override fun areItemsTheSame(oldItem: ContratoResumo, newItem: ContratoResumo): Boolean {
            return oldItem.contratoId == newItem.contratoId
        }

        override fun areContentsTheSame(oldItem: ContratoResumo, newItem: ContratoResumo): Boolean {
            return oldItem == newItem
        }
    }
} 