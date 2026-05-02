package com.example.alg_gestao_02.ui.material.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Material
import java.text.NumberFormat
import java.util.Locale

class MateriaisAdapter(
    private var materiais: List<Material> = emptyList(),
    private val onItemClick: (Material) -> Unit,
    private val onMenuClick: (Material, View) -> Unit
) : RecyclerView.Adapter<MateriaisAdapter.MaterialViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun updateMateriais(newMateriais: List<Material>) {
        val diffCallback = MaterialDiffCallback(materiais, newMateriais)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        materiais = newMateriais
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_material, parent, false)
        return MaterialViewHolder(view)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(materiais[position])
    }

    override fun getItemCount(): Int = materiais.size

    inner class MaterialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNomeMaterial: TextView = itemView.findViewById(R.id.tvNomeMaterial)
        private val tvCodigoMaterial: TextView = itemView.findViewById(R.id.tvCodigoMaterial)
        private val tvQuantidadeMaterial: TextView = itemView.findViewById(R.id.tvQuantidadeMaterial)
        private val tvBadgeDisponibilidade: TextView = itemView.findViewById(R.id.tvBadgeDisponibilidade)
        private val tvValorUnitarioMaterial: TextView = itemView.findViewById(R.id.tvValorUnitarioMaterial)
        private val ivMenuMaterial: ImageView = itemView.findViewById(R.id.ivMenuMaterial)

        fun bind(material: Material) {
            tvNomeMaterial.text = material.nome
            tvCodigoMaterial.text = "Codigo: ${material.codigo ?: "-"}"

            val quantidadeDisponivel = material.getQuantidadeDisponivelAtual()
            val quantidadeTotal = material.quantidadeTotal ?: material.quantidadeDisponivel
            val quantidadeEmUso = material.quantidadeEmUso ?: 0

            tvQuantidadeMaterial.text = if (quantidadeEmUso > 0) {
                "Total: $quantidadeTotal ($quantidadeEmUso em uso)"
            } else {
                "Total: $quantidadeTotal"
            }

            tvBadgeDisponibilidade.text = "$quantidadeDisponivel disp."
            val badgeColor = when {
                quantidadeDisponivel == 0 -> R.color.error
                quantidadeDisponivel < (quantidadeTotal * 0.3) -> R.color.warning
                else -> R.color.success
            }
            tvBadgeDisponibilidade.setBackgroundTintList(itemView.context.getColorStateList(badgeColor))

            tvValorUnitarioMaterial.text = "Unitario: ${currencyFormat.format(material.valorUnitario)}"

            itemView.setOnClickListener { onItemClick(material) }
            ivMenuMaterial.setOnClickListener { onMenuClick(material, it) }
        }
    }

    private class MaterialDiffCallback(
        private val oldList: List<Material>,
        private val newList: List<Material>
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
