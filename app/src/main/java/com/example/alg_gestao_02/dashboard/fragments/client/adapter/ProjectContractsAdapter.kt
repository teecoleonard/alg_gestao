package com.example.alg_gestao_02.dashboard.fragments.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale

class ProjectContractsAdapter(
    private var contracts: List<ProjectContractItem> = emptyList(),
    private val onItemClick: (ProjectContractItem) -> Unit
) : RecyclerView.Adapter<ProjectContractsAdapter.ContractViewHolder>() {

    fun updateData(newContracts: List<ProjectContractItem>) {
        this.contracts = newContracts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project_contract, parent, false)
        return ContractViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        holder.bind(contracts[position])
    }

    override fun getItemCount(): Int = contracts.size

    class ContractViewHolder(
        itemView: View,
        private val onItemClick: (ProjectContractItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val cardStatus: CardView = itemView.findViewById(R.id.cardStatus)
        private val ivStatus: ImageView = itemView.findViewById(R.id.ivStatus)
        private val tvContractName: TextView = itemView.findViewById(R.id.tvContractName)
        private val tvContractDescription: TextView = itemView.findViewById(R.id.tvContractDescription)
        private val tvContractValue: TextView = itemView.findViewById(R.id.tvContractValue)
        private val tvContractDate: TextView = itemView.findViewById(R.id.tvContractDate)

        fun bind(contract: ProjectContractItem) {
            tvContractName.text = contract.name
            tvContractDescription.text = contract.description
            
            // Formata o valor para o formato indiano com símbolo ₹
            val formattedValue = "₹" + formatNumberWithCommas(contract.value.toInt().toString())
            tvContractValue.text = formattedValue
            
            // Formata a data
            tvContractDate.text = contract.date
            
            // Configura o estilo baseado no tipo (pagamento ou débito)
            when (contract.type) {
                "payment" -> {
                    // Pagamento: círculo verde com seta para direita
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.success))
                    ivStatus.setImageResource(R.drawable.ic_arrow_right)
                    tvContractValue.setTextColor(itemView.context.getColor(R.color.success))
                }
                "debt" -> {
                    // Débito: círculo vermelho com seta para esquerda
                    cardStatus.setCardBackgroundColor(itemView.context.getColor(R.color.error))
                    ivStatus.setImageResource(R.drawable.ic_arrow_left)
                    tvContractValue.setTextColor(itemView.context.getColor(R.color.error))
                }
            }
            
            // Configura o clique no item
            itemView.setOnClickListener {
                LogUtils.debug("ProjectContractsAdapter", "Contrato clicado: ${contract.id}")
                onItemClick(contract)
            }
        }
        
        // Função para formatar números com vírgulas no estilo indiano: 1,00,000
        private fun formatNumberWithCommas(number: String): String {
            val length = number.length
            if (length <= 3) return number
            
            var result = ""
            var count = 0
            
            for (i in length - 1 downTo 0) {
                result = number[i] + result
                count++
                
                if (count == 3 && i > 0) {
                    result = "," + result
                    count = 0
                } else if (count == 2 && i > 0 && result.contains(",")) {
                    result = "," + result
                    count = 0
                }
            }
            
            return result
        }
    }
} 