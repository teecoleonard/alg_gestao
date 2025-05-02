package com.example.alg_gestao_02.ui.cliente.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Adapter para a lista de clientes
 */
class ClientesAdapter(
    private var clientes: List<Cliente>,
    private val onItemClick: (Cliente) -> Unit,
    private val onMenuClick: (Cliente, View) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    /**
     * ViewHolder para o item de cliente
     */
    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivTipoCliente: ImageView = itemView.findViewById(R.id.ivTipoCliente)
        private val tvNomeCliente: TextView = itemView.findViewById(R.id.tvNomeCliente)
        private val tvDocumentoCliente: TextView = itemView.findViewById(R.id.tvDocumentoCliente)
        private val tvRgIeCliente: TextView = itemView.findViewById(R.id.tvRgIeCliente)
        private val tvEnderecoCliente: TextView = itemView.findViewById(R.id.tvEnderecoCliente)
        private val tvTelefoneCliente: TextView = itemView.findViewById(R.id.tvTelefoneCliente)
        private val ivMenuCliente: ImageView = itemView.findViewById(R.id.ivMenuCliente)
        
        /**
         * Vincula os dados do cliente ao ViewHolder
         */
        fun bind(cliente: Cliente) {
            // Define o nome do cliente
            tvNomeCliente.text = cliente.contratante
            
            // Define o ícone de acordo com o tipo de pessoa (física ou jurídica)
            if (cliente.isPessoaFisica()) {
                ivTipoCliente.setImageResource(R.drawable.ic_person)
            } else {
                ivTipoCliente.setImageResource(R.drawable.ic_business)
            }
            
            // Define o documento principal (CPF/CNPJ)
            tvDocumentoCliente.text = cliente.getDocumentoFormatado()
            
            // Define o documento secundário (RG/IE)
            tvRgIeCliente.text = cliente.getDocumentoSecundarioFormatado()
            
            // Define o endereço completo
            tvEnderecoCliente.text = cliente.getEnderecoCompleto()
            
            // Define o telefone, se disponível
            if (!cliente.telefone.isNullOrBlank()) {
                tvTelefoneCliente.visibility = View.VISIBLE
                tvTelefoneCliente.text = "Telefone: ${cliente.telefone}"
            } else {
                tvTelefoneCliente.visibility = View.GONE
            }
            
            // Configura o clique no item
            itemView.setOnClickListener {
                LogUtils.debug("ClientesAdapter", "Item clicado: ${cliente.contratante}")
                onItemClick(cliente)
            }
            
            // Configura o clique no menu
            ivMenuCliente.setOnClickListener { view ->
                LogUtils.debug("ClientesAdapter", "Menu clicado: ${cliente.contratante}")
                onMenuClick(cliente, view)
            }
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(clientes[position])
    }
    
    override fun getItemCount(): Int = clientes.size
    
    /**
     * Atualiza a lista de clientes do adapter
     */
    fun updateClientes(newClientes: List<Cliente>) {
        this.clientes = newClientes
        notifyDataSetChanged()
    }
} 