package com.example.alg_gestao_02.ui.devolucao.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Adapter para a lista de devoluções
 */
class DevolucoesAdapter(
    private var devolucoes: List<Devolucao>,
    private val onItemClick: (Devolucao) -> Unit,
    private val onMenuClick: (Devolucao, View) -> Unit
) : RecyclerView.Adapter<DevolucoesAdapter.DevolucaoViewHolder>() {

    /**
     * ViewHolder para o item de devolução
     */
    inner class DevolucaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClienteNome: TextView = itemView.findViewById(R.id.tvClienteNome)
        private val tvDevolucaoNumero: TextView = itemView.findViewById(R.id.tvDevolucaoNumero)
        private val tvEquipamentoNome: TextView = itemView.findViewById(R.id.tvEquipamentoNome)
        private val tvQuantidades: TextView = itemView.findViewById(R.id.tvQuantidades)
        private val tvDataPrevista: TextView = itemView.findViewById(R.id.tvDataPrevista)
        private val tvDataEfetiva: TextView = itemView.findViewById(R.id.tvDataEfetiva)
        private val ivMenuDevolucao: ImageView = itemView.findViewById(R.id.ivMenuDevolucao)
        private val ivStatusDevolucao: ImageView = itemView.findViewById(R.id.ivStatusDevolucao)
        
        /**
         * Vincula os dados da devolução ao ViewHolder
         */
        fun bind(devolucao: Devolucao) {
            // Define o nome do cliente
            tvClienteNome.text = devolucao.resolverNomeCliente()
            
            // Define o número da devolução
            tvDevolucaoNumero.text = "Devolução #${devolucao.devNum}"
            
            // Define o nome do equipamento
            tvEquipamentoNome.text = devolucao.resolverNomeEquipamento()
            
            // Define as quantidades
            val quantidadeDevolvida = devolucao.quantidadeDevolvida
            val quantidadeContratada = devolucao.quantidadeContratada
            val pendente = devolucao.getQuantidadePendente()
            tvQuantidades.text = "Devolvido: $quantidadeDevolvida de $quantidadeContratada (Pendente: $pendente)"
            
            // Define a data prevista
            tvDataPrevista.text = "Previsão: ${devolucao.getDataPrevistaFormatada()}"
            
            // Define a data efetiva ou status
            if (devolucao.dataDevolucaoEfetiva != null) {
                tvDataEfetiva.text = "Efetivado em: ${devolucao.getDataEfetivaFormatada()}"
                tvDataEfetiva.visibility = View.VISIBLE
            } else {
                tvDataEfetiva.visibility = View.GONE
            }
            
            // Define o status visual da devolução
            when (devolucao.statusItemDevolucao) {
                "Pendente" -> {
                    ivStatusDevolucao.setImageResource(R.drawable.ic_pending)
                    ivStatusDevolucao.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.warning)
                    )
                }
                "Devolvido" -> {
                    ivStatusDevolucao.setImageResource(R.drawable.ic_check_circle)
                    ivStatusDevolucao.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.success)
                    )
                }
                "Avariado" -> {
                    ivStatusDevolucao.setImageResource(R.drawable.ic_error)
                    ivStatusDevolucao.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.error)
                    )
                }
                "Faltante" -> {
                    ivStatusDevolucao.setImageResource(R.drawable.ic_error)
                    ivStatusDevolucao.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.error)
                    )
                }
                else -> {
                    ivStatusDevolucao.setImageResource(R.drawable.ic_info)
                    ivStatusDevolucao.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.text_secondary)
                    )
                }
            }
            
            // Configura o clique no item
            itemView.setOnClickListener {
                LogUtils.debug("DevolucoesAdapter", "Item clicado: Devolução #${devolucao.devNum}")
                onItemClick(devolucao)
            }
            
            // Configura o clique no menu
            ivMenuDevolucao.setOnClickListener { view ->
                LogUtils.debug("DevolucoesAdapter", "Menu clicado: Devolução #${devolucao.devNum}")
                onMenuClick(devolucao, view)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DevolucaoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_devolucao, parent, false)
        return DevolucaoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: DevolucaoViewHolder, position: Int) {
        holder.bind(devolucoes[position])
    }
    
    override fun getItemCount(): Int = devolucoes.size
    
    /**
     * Atualiza a lista de devoluções do adapter
     */
    fun updateDevolucoes(newDevolucoes: List<Devolucao>) {
        this.devolucoes = newDevolucoes
        notifyDataSetChanged()
    }
}
