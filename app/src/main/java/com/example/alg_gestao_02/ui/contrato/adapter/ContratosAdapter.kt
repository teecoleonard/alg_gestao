package com.example.alg_gestao_02.ui.contrato.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Adapter para a lista de contratos
 */
class ContratosAdapter(
    private var contratos: List<Contrato>,
    private val onItemClick: (Contrato) -> Unit,
    private val onMenuClick: (Contrato, View) -> Unit
) : RecyclerView.Adapter<ContratosAdapter.ContratoViewHolder>() {

    /**
     * ViewHolder para o item de contrato
     */
    inner class ContratoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClienteNome: TextView = itemView.findViewById(R.id.tvClienteNome)
        private val tvContratoNumero: TextView = itemView.findViewById(R.id.tvContratoNumero)
        private val tvValorContrato: TextView = itemView.findViewById(R.id.tvValorContrato)
        private val tvDataEmissao: TextView = itemView.findViewById(R.id.tvDataEmissao)
        private val tvDataVencimento: TextView = itemView.findViewById(R.id.tvDataVencimento)
        private val tvLocalObra: TextView = itemView.findViewById(R.id.tvLocalObra)
        private val tvPeriodoContrato: TextView = itemView.findViewById(R.id.tvPeriodoContrato)
        private val ivMenuContrato: ImageView = itemView.findViewById(R.id.ivMenuContrato)
        private val ivStatusContrato: ImageView = itemView.findViewById(R.id.ivStatusContrato)
        
        /**
         * Vincula os dados do contrato ao ViewHolder
         */
        fun bind(contrato: Contrato) {
            // Define o nome do cliente
            tvClienteNome.text = contrato.resolverNomeCliente()
            
            // Define o número do contrato
            tvContratoNumero.text = "Contrato #${contrato.contratoNum}"
            
            // Define o valor do contrato
            tvValorContrato.text = contrato.getValorFormatado()
            
            // Define a data de emissão
            tvDataEmissao.text = "Emissão: ${contrato.getDataEmissaoFormatada()}"
            
            // Define a data de vencimento
            tvDataVencimento.text = "Vencimento: ${contrato.getDataVencimentoFormatada()}"
            
            // Define o local da obra
            tvLocalObra.text = "Local: ${contrato.obraLocal}"
            
            // Define o período do contrato
            tvPeriodoContrato.text = "Período: ${contrato.contratoPeriodo}"
            
            // Define o status visual do contrato (assinado/não assinado)
            if (contrato.isAssinado()) {
                ivStatusContrato.setImageResource(R.drawable.ic_check_circle)
                ivStatusContrato.setColorFilter(itemView.context.getColor(R.color.success))
            } else {
                ivStatusContrato.setImageResource(R.drawable.ic_pending)
                ivStatusContrato.setColorFilter(itemView.context.getColor(R.color.warning))
            }
            
            // Configura o clique no item
            itemView.setOnClickListener {
                LogUtils.debug("ContratosAdapter", "Item clicado: Contrato #${contrato.contratoNum}")
                onItemClick(contrato)
            }
            
            // Configura o clique no menu
            ivMenuContrato.setOnClickListener { view ->
                LogUtils.debug("ContratosAdapter", "Menu clicado: Contrato #${contrato.contratoNum}")
                onMenuClick(contrato, view)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContratoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contrato, parent, false)
        return ContratoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContratoViewHolder, position: Int) {
        holder.bind(contratos[position])
    }
    
    override fun getItemCount(): Int = contratos.size
    
    /**
     * Atualiza a lista de contratos do adapter
     */
    fun updateContratos(newContratos: List<Contrato>) {
        this.contratos = newContratos
        notifyDataSetChanged()
    }
} 