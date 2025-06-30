package com.example.alg_gestao_02.ui.financial

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ReceitaCliente
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.button.MaterialButton

class ReceitaClienteAdapter(
    private var receitaClientes: List<ReceitaCliente> = emptyList(),
    private val onItemClick: ((ReceitaCliente) -> Unit)? = null
) : RecyclerView.Adapter<ReceitaClienteAdapter.ReceitaClienteViewHolder>() {

    class ReceitaClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClienteNome: TextView = itemView.findViewById(R.id.tvClienteNome)
        val tvValorMensal: TextView = itemView.findViewById(R.id.tvValorMensal)
        val tvContratosAtivos: TextView = itemView.findViewById(R.id.tvContratosAtivos)
        val tvTicketMedio: TextView = itemView.findViewById(R.id.tvTicketMedio)
        val tvParticipacao: TextView = itemView.findViewById(R.id.tvParticipacao)
        val btnVerDetalhes: MaterialButton = itemView.findViewById(R.id.btnVerDetalhes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReceitaClienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receita_cliente, parent, false)
        return ReceitaClienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReceitaClienteViewHolder, position: Int) {
        val receita = receitaClientes[position]
        
        holder.tvClienteNome.text = receita.clienteNome
        holder.tvValorMensal.text = receita.getValorMensalFormatado()
        holder.tvContratosAtivos.text = "${receita.contratosAtivos} contrato(s) ativo(s)"
        holder.tvTicketMedio.text = "Ticket médio: ${receita.getTicketMedioFormatado()}"
        
        // Calcular participação (precisa do total geral que vem do response)
        val totalGeral = receitaClientes.sumOf { it.valorMensal }
        val participacao = receita.calcularParticipacao(totalGeral)
        holder.tvParticipacao.text = "${String.format("%.1f", participacao)}% do total"
        
        // Click listener para o card inteiro (dialog de detalhes)
        holder.itemView.setOnClickListener {
            LogUtils.debug("ReceitaClienteAdapter", "Card clicado: ${receita.clienteNome}")
            onItemClick?.invoke(receita)
        }
        
        // Click listener para o botão "Ver Resumo Detalhado"
        holder.btnVerDetalhes.setOnClickListener {
            LogUtils.debug("ReceitaClienteAdapter", "Botão Ver Detalhes clicado: ${receita.clienteNome}")
            
            // Navegar para ResumoMensalClienteActivity
            val context = holder.itemView.context
            val intent = ResumoMensalClienteActivity.newIntent(
                context = context,
                clienteId = receita.clienteId,
                mesReferencia = null, // Usar mês atual
                clienteNome = receita.clienteNome
            )
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = receitaClientes.size

    fun updateReceitas(newReceitas: List<ReceitaCliente>) {
        LogUtils.debug("ReceitaClienteAdapter", "Atualizando lista de receitas: ${newReceitas.size} itens")
        receitaClientes = newReceitas
        notifyDataSetChanged()
    }
} 