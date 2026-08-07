package com.example.alg_gestao_02.ui.orcamento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R

/**
 * Adapter de seleção múltipla (checkbox) para o catálogo de equipamentos/materiais.
 * A seleção é rastreada por id em [selecionados], então sobrevive à filtragem por busca.
 */
class CatalogoMultiAdapter(
    private val selecionados: MutableSet<Int>
) : RecyclerView.Adapter<CatalogoMultiAdapter.ItemViewHolder>() {

    /** Pares (id, rótulo) atualmente exibidos (já filtrados). */
    private var itens: List<Pair<Int, String>> = emptyList()

    fun update(novos: List<Pair<Int, String>>) {
        itens = novos
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_catalogo_check, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(itens[position])
    }

    override fun getItemCount(): Int = itens.size

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cb: CheckBox = itemView.findViewById(R.id.cbItem)

        fun bind(item: Pair<Int, String>) {
            val (id, label) = item
            cb.text = label
            cb.isChecked = selecionados.contains(id)
            cb.setOnClickListener {
                if (cb.isChecked) selecionados.add(id) else selecionados.remove(id)
            }
        }
    }
}
