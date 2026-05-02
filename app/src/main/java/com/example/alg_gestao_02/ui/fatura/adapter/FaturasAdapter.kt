package com.example.alg_gestao_02.ui.fatura.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Fatura
import java.text.NumberFormat
import java.util.Locale

class FaturasAdapter(
    private var faturas: List<Fatura>,
    private val onItemClick: (Fatura) -> Unit,
    private val onMenuClick: (Fatura, View) -> Unit,
) : RecyclerView.Adapter<FaturasAdapter.FaturaViewHolder>() {

    private val money = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    inner class FaturaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNumero: TextView = itemView.findViewById(R.id.tvNumeroFatura)
        private val tvCliente: TextView = itemView.findViewById(R.id.tvClienteFatura)
        private val tvPeriodo: TextView = itemView.findViewById(R.id.tvPeriodoFatura)
        private val tvVencimento: TextView = itemView.findViewById(R.id.tvVencimentoFatura)
        private val tvValor: TextView = itemView.findViewById(R.id.tvValorFatura)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatusFatura)
        private val ivMenu: ImageView = itemView.findViewById(R.id.ivMenuFatura)

        fun bind(fatura: Fatura) {
            tvNumero.text = "Fatura #${fatura.numero}"
            tvCliente.text = fatura.cliente?.nome ?: "Cliente não informado"
            tvPeriodo.text = "Periodo: ${fatura.periodo ?: "-"}"
            tvVencimento.text = "Vencimento: ${fatura.dataVencimento ?: "-"}"
            tvValor.text = money.format(fatura.valorTotal)
            tvStatus.text = fatura.status ?: "PENDENTE"

            val color = when (fatura.status?.uppercase()) {
                "PAGA" -> R.color.success
                "CANCELADA" -> R.color.error
                "ENVIADA" -> R.color.info
                else -> R.color.warning
            }
            tvStatus.backgroundTintList = itemView.context.getColorStateList(color)

            itemView.setOnClickListener { onItemClick(fatura) }
            ivMenu.setOnClickListener { onMenuClick(fatura, it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaturaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fatura, parent, false)
        return FaturaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FaturaViewHolder, position: Int) {
        holder.bind(faturas[position])
    }

    override fun getItemCount(): Int = faturas.size

    fun updateData(novaLista: List<Fatura>) {
        faturas = novaLista
        notifyDataSetChanged()
    }
}
