package com.example.alg_gestao_02.ui.contrato.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.MaterialContrato
import java.text.NumberFormat
import java.util.Locale

class MateriaisContratoAdapter(
    materiais: List<MaterialContrato> = emptyList(),
    private val onEditClick: (MaterialContrato) -> Unit,
    private val onDeleteClick: (MaterialContrato) -> Unit
) : RecyclerView.Adapter<MateriaisContratoAdapter.MaterialContratoViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    private val materiais = materiais.toMutableList()

    fun updateMateriais(newMateriais: List<MaterialContrato>) {
        val diffResult = DiffUtil.calculateDiff(MaterialContratoDiffCallback(materiais, newMateriais))
        materiais.clear()
        materiais.addAll(newMateriais)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialContratoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_material_contrato, parent, false)
        return MaterialContratoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialContratoViewHolder, position: Int) {
        holder.bind(materiais[position])
    }

    override fun getItemCount(): Int = materiais.size

    inner class MaterialContratoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeMaterial: TextView = itemView.findViewById(R.id.tvNomeMaterial)
        private val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        private val tvValorUnitario: TextView = itemView.findViewById(R.id.tvValorUnitario)
        private val tvValorTotal: TextView = itemView.findViewById(R.id.tvValorTotal)
        private val btnEdit: ImageButton = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: MaterialContrato) {
            tvNomeMaterial.text = item.nomeMaterialExibicao
            tvQuantidade.text = "${item.quantidade} unid."
            tvValorUnitario.text = currencyFormat.format(item.valorUnitario)
            tvValorTotal.text = currencyFormat.format(item.valorTotal)

            btnEdit.setOnClickListener { onEditClick(item) }
            btnDelete.setOnClickListener { onDeleteClick(item) }
        }
    }

    private class MaterialContratoDiffCallback(
        private val oldList: List<MaterialContrato>,
        private val newList: List<MaterialContrato>
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
