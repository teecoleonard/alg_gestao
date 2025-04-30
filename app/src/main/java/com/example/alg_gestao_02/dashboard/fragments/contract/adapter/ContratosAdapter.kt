package com.example.alg_gestao_02.dashboard.fragments.contract.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.contract.model.Contrato
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale

class ContratosAdapter(
    private var contratos: List<Contrato> = emptyList(),
    private val onItemClick: (Contrato) -> Unit
) : RecyclerView.Adapter<ContratosAdapter.ContratosViewHolder>() {

    fun updateData(newContratos: List<Contrato>) {
        this.contratos = newContratos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContratosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contract, parent, false)
        return ContratosViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ContratosViewHolder, position: Int) {
        holder.bind(contratos[position])
    }

    override fun getItemCount(): Int = contratos.size

    class ContratosViewHolder(
        itemView: View,
        private val onItemClick: (Contrato) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvCompanyName: TextView = itemView.findViewById(R.id.tvCompanyName)
        private val tvContractNumber: TextView = itemView.findViewById(R.id.tvContractNumber)
        private val tvContractValue: TextView = itemView.findViewById(R.id.tvContractValue)
        private val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        private val tvEndDate: TextView = itemView.findViewById(R.id.tvEndDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(contrato: Contrato) {
            tvCompanyName.text = contrato.client
            tvContractNumber.text = "Contrato #${contrato.contractNumber}"
            
            // Formatação do valor do contrato para moeda brasileira
            val formatter = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
            tvContractValue.text = formatter.format(contrato.value)
            
            tvStartDate.text = contrato.startDate
            tvEndDate.text = contrato.endDate
            
            // Configuração do texto e background do status
            tvStatus.text = getStatusText(contrato.status)
            tvStatus.setBackgroundResource(getStatusBackground(contrato.status))
            
            itemView.setOnClickListener {
                LogUtils.debug("ContratosAdapter", "Contrato clicado: ${contrato.id}")
                onItemClick(contrato)
            }
        }
        
        private fun getStatusText(status: String): String {
            return when (status) {
                "active" -> "Ativo"
                "pending" -> "Pendente"
                "completed" -> "Concluído"
                "cancelled" -> "Cancelado"
                else -> status.capitalize()
            }
        }
        
        private fun getStatusBackground(status: String): Int {
            return when (status) {
                "active" -> R.drawable.bg_status_active
                "pending" -> R.drawable.bg_status_pending
                "completed" -> R.drawable.bg_status_completed
                "cancelled" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_active
            }
        }
        
        private fun String.capitalize(): String {
            return this.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() 
            }
        }
    }
} 