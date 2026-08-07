package com.example.alg_gestao_02.ui.orcamento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.ui.orcamento.viewmodel.OrcamentoMaterialLinha
import java.text.NumberFormat
import java.util.Locale

class OrcamentoMaterialAdapter(
    private val onEditar: (OrcamentoMaterialLinha) -> Unit,
    private val onRemover: (OrcamentoMaterialLinha) -> Unit
) : RecyclerView.Adapter<OrcamentoMaterialAdapter.LinhaViewHolder>() {

    private var itens: List<OrcamentoMaterialLinha> = emptyList()
    private val currency = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    fun update(novos: List<OrcamentoMaterialLinha>) {
        itens = novos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinhaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_orcamento_equip, parent, false)
        return LinhaViewHolder(view)
    }

    override fun onBindViewHolder(holder: LinhaViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    override fun getItemCount(): Int = itens.size

    inner class LinhaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomeItem)
        private val tvDetalhe: TextView = itemView.findViewById(R.id.tvDetalheItem)
        private val tvTotal: TextView = itemView.findViewById(R.id.tvTotalItem)
        private val ivRemover: ImageView = itemView.findViewById(R.id.ivRemoverItem)

        fun bind(linha: OrcamentoMaterialLinha) {
            tvNome.text = linha.nome
            val prefixoCodigo = linha.codigo?.takeIf { it.isNotBlank() }?.let { "$it • " } ?: ""
            tvDetalhe.text =
                "$prefixoCodigo${linha.quantidade} x ${currency.format(linha.valorUnitario)}"
            tvTotal.text = currency.format(linha.valorTotal)
            itemView.setOnClickListener { onEditar(linha) }
            ivRemover.setOnClickListener { onRemover(linha) }
        }
    }
}
