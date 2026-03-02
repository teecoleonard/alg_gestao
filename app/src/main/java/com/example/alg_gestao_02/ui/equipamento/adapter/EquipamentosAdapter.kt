package com.example.alg_gestao_02.ui.equipamento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Equipamento
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter para a lista de equipamentos.
 */
class EquipamentosAdapter(
    private var equipamentos: List<Equipamento> = emptyList(),
    private val onItemClick: (Equipamento) -> Unit,
    private val onMenuClick: (Equipamento, View) -> Unit
) : RecyclerView.Adapter<EquipamentosAdapter.EquipamentoViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    /**
     * Atualiza os dados do adapter usando DiffUtil para animações suaves
     */
    fun updateEquipamentos(newEquipamentos: List<Equipamento>) {
        val diffCallback = EquipamentoDiffCallback(equipamentos, newEquipamentos)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.equipamentos = newEquipamentos
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipamentoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipamento, parent, false)
        return EquipamentoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipamentoViewHolder, position: Int) {
        holder.bind(equipamentos[position])
    }

    override fun getItemCount(): Int = equipamentos.size

    inner class EquipamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeEquipamento: TextView = itemView.findViewById(R.id.tvNomeEquipamento)
        private val tvCodigoEquipamento: TextView = itemView.findViewById(R.id.tvCodigoEquipamento)
        private val tvQuantidadeEquipamento: TextView = itemView.findViewById(R.id.tvQuantidadeEquipamento)
        private val tvBadgeDisponibilidade: TextView = itemView.findViewById(R.id.tvBadgeDisponibilidade)
        private val tvPrecoDiariaEquipamento: TextView = itemView.findViewById(R.id.tvPrecoDiariaEquipamento)
        private val ivMenuEquipamento: ImageView = itemView.findViewById(R.id.ivMenuEquipamento)

        fun bind(equipamento: Equipamento) {
            tvNomeEquipamento.text = equipamento.nomeEquip
            tvCodigoEquipamento.text = "Código: ${equipamento.codigoEquip}"
            
            // Mostrar quantidade total e em uso
            val quantidadeTotal = equipamento.quantidadeTotal ?: equipamento.quantidadeDisp
            val quantidadeEmUso = equipamento.quantidadeEmUso ?: 0
            val quantidadeDisponivel = equipamento.getQuantidadeDisponivelAtual()
            
            tvQuantidadeEquipamento.text = if (quantidadeEmUso > 0) {
                "Total: $quantidadeTotal (${quantidadeEmUso} em uso)"
            } else {
                "Total: $quantidadeTotal"
            }
            
            // Badge de disponibilidade com cores
            tvBadgeDisponibilidade.text = "$quantidadeDisponivel Disp."
            
            // Cor do badge baseado na disponibilidade
            val badgeColor = when {
                quantidadeDisponivel == 0 -> R.color.error // Vermelho
                quantidadeDisponivel < (quantidadeTotal * 0.3) -> R.color.warning // Laranja
                else -> R.color.success // Verde
            }
            tvBadgeDisponibilidade.setBackgroundTintList(
                itemView.context.getColorStateList(badgeColor)
            )
            
            tvPrecoDiariaEquipamento.text = "Diária: ${currencyFormat.format(equipamento.precoDiaria)}"

            // Configura o clique no item
            itemView.setOnClickListener {
                onItemClick(equipamento)
            }

            // Configura o clique no botão de menu
            ivMenuEquipamento.setOnClickListener {
                onMenuClick(equipamento, it)
            }
        }
    }

    /**
     * DiffUtil.Callback para calcular diferenças entre listas de equipamentos
     */
    private class EquipamentoDiffCallback(
        private val oldList: List<Equipamento>,
        private val newList: List<Equipamento>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
} 