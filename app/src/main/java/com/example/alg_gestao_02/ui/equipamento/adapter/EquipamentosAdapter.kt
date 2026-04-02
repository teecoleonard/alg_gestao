package com.example.alg_gestao_02.ui.equipamento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
     * Atualiza os dados do adapter usando DiffUtil para animacoes suaves.
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
        private val ivFotoEquipamento: ImageView = itemView.findViewById(R.id.ivFotoEquipamento)
        private val tvNomeEquipamento: TextView = itemView.findViewById(R.id.tvNomeEquipamento)
        private val tvCodigoEquipamento: TextView = itemView.findViewById(R.id.tvCodigoEquipamento)
        private val tvQuantidadeEquipamento: TextView = itemView.findViewById(R.id.tvQuantidadeEquipamento)
        private val tvBadgeDisponibilidade: TextView = itemView.findViewById(R.id.tvBadgeDisponibilidade)
        private val tvPrecoDiariaEquipamento: TextView = itemView.findViewById(R.id.tvPrecoDiariaEquipamento)
        private val ivMenuEquipamento: ImageView = itemView.findViewById(R.id.ivMenuEquipamento)

        fun bind(equipamento: Equipamento) {
            tvNomeEquipamento.text = equipamento.nomeEquip
            tvCodigoEquipamento.text = "Código: ${equipamento.codigoEquip}"

            val quantidadeTotal = equipamento.quantidadeTotal ?: equipamento.quantidadeDisp
            val quantidadeEmUso = equipamento.quantidadeEmUso ?: 0
            val quantidadeDisponivel = equipamento.getQuantidadeDisponivelAtual()

            tvQuantidadeEquipamento.text = if (quantidadeEmUso > 0) {
                "Total: $quantidadeTotal (${quantidadeEmUso} em uso)"
            } else {
                "Total: $quantidadeTotal"
            }

            tvBadgeDisponibilidade.text = "$quantidadeDisponivel Disp."

            val badgeColor = when {
                quantidadeDisponivel == 0 -> R.color.error
                quantidadeDisponivel < (quantidadeTotal * 0.3) -> R.color.warning
                else -> R.color.success
            }
            tvBadgeDisponibilidade.setBackgroundTintList(
                itemView.context.getColorStateList(badgeColor)
            )

            tvPrecoDiariaEquipamento.text = "Diária: ${currencyFormat.format(equipamento.precoDiaria)}"
            bindFotoEquipamento(equipamento)

            itemView.setOnClickListener {
                onItemClick(equipamento)
            }

            ivMenuEquipamento.setOnClickListener {
                onMenuClick(equipamento, it)
            }
        }

        private fun bindFotoEquipamento(equipamento: Equipamento) {
            val fotoUrl = equipamento.fotoUrl?.trim().orEmpty()

            if (fotoUrl.isBlank()) {
                Glide.with(itemView).clear(ivFotoEquipamento)
                ivFotoEquipamento.scaleType = ImageView.ScaleType.CENTER_INSIDE
                ivFotoEquipamento.setImageResource(R.drawable.ic_tools)
                return
            }

            ivFotoEquipamento.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(itemView)
                .load(fotoUrl)
                .placeholder(R.drawable.ic_tools)
                .error(R.drawable.ic_tools)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .centerCrop()
                .into(ivFotoEquipamento)
        }
    }

    /**
     * DiffUtil.Callback para calcular diferencas entre listas de equipamentos.
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
