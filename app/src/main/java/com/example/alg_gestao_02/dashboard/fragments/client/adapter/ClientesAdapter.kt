package com.example.alg_gestao_02.dashboard.fragments.client.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.model.Cliente
import com.example.alg_gestao_02.utils.LogUtils

class ClientesAdapter(
    private var clientes: List<Cliente> = emptyList(),
    private val onItemClick: (Cliente) -> Unit,
    private val onOptionsClick: (Cliente, View) -> Unit
) : RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    fun updateData(newClientes: List<Cliente>) {
        this.clientes = newClientes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cliente, parent, false)
        return ClienteViewHolder(view, onItemClick, onOptionsClick)
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        holder.bind(clientes[position])
    }

    override fun getItemCount(): Int = clientes.size

    class ClienteViewHolder(
        itemView: View,
        private val onItemClick: (Cliente) -> Unit,
        private val onOptionsClick: (Cliente, View) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNome)
        private val tvDocumento: TextView = itemView.findViewById(R.id.tvDocumento)
        private val tvTelefone: TextView = itemView.findViewById(R.id.tvTelefone)
        private val btnMore: ImageButton = itemView.findViewById(R.id.btnMore)

        fun bind(cliente: Cliente) {
            tvNome.text = cliente.nome
            tvDocumento.text = formatarDocumento(cliente.documento, cliente.tipo)
            tvTelefone.text = "Tel: ${cliente.telefone}"

            itemView.setOnClickListener {
                LogUtils.debug("ClientesAdapter", "Cliente clicado: ${cliente.nome}")
                onItemClick(cliente)
            }

            btnMore.setOnClickListener {
                LogUtils.debug("ClientesAdapter", "Botão de opções clicado para: ${cliente.nome}")
                onOptionsClick(cliente, it)
            }
        }

        private fun formatarDocumento(documento: String, tipo: String): String {
            return when (tipo) {
                "PF" -> "CPF: $documento"
                "PJ" -> "CNPJ: $documento"
                else -> "Doc: $documento"
            }
        }
    }
} 