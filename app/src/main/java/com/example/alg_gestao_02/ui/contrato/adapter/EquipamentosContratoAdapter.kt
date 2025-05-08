package com.example.alg_gestao_02.ui.contrato.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale

/**
 * Adapter para a lista de equipamentos de um contrato
 */
class EquipamentosContratoAdapter(
    equipamentos: List<EquipamentoContrato> = emptyList(),
    private val onEditClick: (EquipamentoContrato) -> Unit,
    private val onDeleteClick: (EquipamentoContrato) -> Unit
) : RecyclerView.Adapter<EquipamentosContratoAdapter.EquipamentoContratoViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    
    // Lista mutável para armazenar os equipamentos
    private val equipamentos = equipamentos.toMutableList()

    /**
     * Atualiza os dados do adapter usando DiffUtil para animações suaves
     */
    fun updateEquipamentos(newEquipamentos: List<EquipamentoContrato>) {
        LogUtils.debug("EquipamentosContratoAdapter", "Atualizando lista de equipamentos: ${newEquipamentos.size} itens")
        
        // Log detalhado de cada equipamento
        newEquipamentos.forEach { equip ->
            LogUtils.debug("EquipamentosContratoAdapter", """
                Equipamento recebido no adapter:
                ID: ${equip.id}
                Nome: ${equip.nomeEquipamentoExibicao}
                Quantidade: ${equip.quantidadeEquip}
                Valor Unitário: ${equip.valorUnitario}
                Valor Total: ${equip.valorTotal}
                Valor Frete: ${equip.valorFrete}
            """.trimIndent())
        }
        
        val diffCallback = EquipamentoContratoDiffCallback(equipamentos, newEquipamentos)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        equipamentos.clear()
        equipamentos.addAll(newEquipamentos)
        
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipamentoContratoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_equipamento_contrato, parent, false)
        return EquipamentoContratoViewHolder(view)
    }

    override fun onBindViewHolder(holder: EquipamentoContratoViewHolder, position: Int) {
        holder.bind(equipamentos[position])
    }

    override fun getItemCount(): Int = equipamentos.size

    inner class EquipamentoContratoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeEquipamento: TextView = itemView.findViewById(R.id.tvNomeEquipamento)
        private val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        private val tvValorUnitario: TextView = itemView.findViewById(R.id.tvValorUnitario)
        private val tvValorTotal: TextView = itemView.findViewById(R.id.tvValorTotal)
        private val tvValorFrete: TextView = itemView.findViewById(R.id.tvValorFrete)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: EquipamentoContrato) {
            LogUtils.debug("EquipamentosContratoAdapter", """
                Vinculando dados ao ViewHolder:
                ID: ${item.id}
                Nome: ${item.nomeEquipamentoExibicao}
                Quantidade: ${item.quantidadeEquip}
                Valor Unitário: ${item.valorUnitario}
                Valor Total: ${item.valorTotal}
                Valor Frete: ${item.valorFrete}
            """.trimIndent())
            
            tvNomeEquipamento.text = item.nomeEquipamentoExibicao
            tvQuantidade.text = "${item.quantidadeEquip} unid."
            tvValorUnitario.text = currencyFormat.format(item.valorUnitario)
            tvValorTotal.text = currencyFormat.format(item.valorTotal)
            tvValorFrete.text = currencyFormat.format(item.valorFrete)

            btnEdit.setOnClickListener { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    /**
     * DiffUtil callback para comparar listas de equipamentos de contrato
     */
    class EquipamentoContratoDiffCallback(
        private val oldList: List<EquipamentoContrato>,
        private val newList: List<EquipamentoContrato>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            return oldItem == newItem
        }
    }
} 