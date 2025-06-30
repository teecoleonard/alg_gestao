package com.example.alg_gestao_02.ui.financial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.DevolucaoResumo
import com.example.alg_gestao_02.databinding.ItemDevolucaoResumoBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adapter para lista de devoluções no resumo mensal
 */
class DevolucoesResumoAdapter : ListAdapter<DevolucaoResumo, DevolucoesResumoAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDevolucaoResumoBinding.inflate(
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
        private val binding: ItemDevolucaoResumoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(devolucao: DevolucaoResumo) {
            // Número da devolução
            binding.tvNumeroDevolucao.text = "Devolução ${devolucao.numeroDevolucao}"
            
            // Equipamento
            binding.tvEquipamentoDevolucao.text = devolucao.equipamentoNome ?: "Equipamento não informado"
            
            // Valor da multa formatado
            val valorFormatado = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                .format(devolucao.valorMulta)
            binding.tvValorMulta.text = valorFormatado
            
            // Data da devolução
            binding.tvDataDevolucao.text = formatarData(devolucao.dataDevolucao)
            
            // Status da devolução
            setupStatusChip(devolucao.status)
        }

        private fun setupStatusChip(status: String?) {
            val chip = binding.chipStatusDevolucao
            val context = binding.root.context
            
            when (status?.uppercase()) {
                "PROCESSADA" -> {
                    chip.text = "PROCESSADA"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.success)
                }
                "PENDENTE" -> {
                    chip.text = "PENDENTE"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.warning)
                }
                "CANCELADA" -> {
                    chip.text = "CANCELADA"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.error)
                }
                "EM_ANALISE" -> {
                    chip.text = "EM ANÁLISE"
                    chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.primary)
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

    class DiffCallback : DiffUtil.ItemCallback<DevolucaoResumo>() {
        override fun areItemsTheSame(oldItem: DevolucaoResumo, newItem: DevolucaoResumo): Boolean {
            return oldItem.devolucaoId == newItem.devolucaoId
        }

        override fun areContentsTheSame(oldItem: DevolucaoResumo, newItem: DevolucaoResumo): Boolean {
            return oldItem == newItem
        }
    }
} 