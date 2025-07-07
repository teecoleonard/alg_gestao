package com.example.alg_gestao_02.ui.financial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ReceitaCliente
import com.example.alg_gestao_02.utils.LogUtils

class ReceitaClienteAdapter(
    private var receitaClientes: List<ReceitaCliente> = emptyList(),
    private var mesReferencia: String? = null,
    private var onClienteClicked: ((ReceitaCliente) -> Unit)? = null
) : RecyclerView.Adapter<ReceitaClienteAdapter.ReceitaClienteViewHolder>() {

    class ReceitaClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvClienteNome: TextView = itemView.findViewById(R.id.tvClienteNome)
        val tvValorMensal: TextView = itemView.findViewById(R.id.tvValorMensal)
        val tvContratosAtivos: TextView = itemView.findViewById(R.id.tvContratosAtivos)
        val tvTicketMedio: TextView = itemView.findViewById(R.id.tvTicketMedio)
        val tvParticipacao: TextView = itemView.findViewById(R.id.tvParticipacao)
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
        
        // Click listener para o card inteiro - chama callback ou abre resumo
        holder.itemView.setOnClickListener {
            LogUtils.debug("ReceitaClienteAdapter", "Card clicado: ${receita.clienteNome} - verificando callback")
            
            if (onClienteClicked != null) {
                // Usar callback se disponível (novo fluxo com dialog)
                LogUtils.debug("ReceitaClienteAdapter", "Usando callback - dialog será mostrado")
                onClienteClicked?.invoke(receita)
            } else {
                // Fallback para comportamento antigo (compatibilidade)
                LogUtils.debug("ReceitaClienteAdapter", "Fallback - navegação direta")
                val context = holder.itemView.context
                val intent = ResumoMensalClienteActivity.newIntent(
                    context = context,
                    clienteId = receita.clienteId,
                    mesReferencia = null,
                    clienteNome = receita.clienteNome
                )
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = receitaClientes.size

    fun updateReceitas(newReceitas: List<ReceitaCliente>) {
        LogUtils.debug("ReceitaClienteAdapter", "Atualizando lista de receitas: ${newReceitas.size} itens")
        receitaClientes = newReceitas
        notifyDataSetChanged()
    }
    
    fun updatePeriodo(novoMesReferencia: String?) {
        LogUtils.debug("ReceitaClienteAdapter", "Atualizando período de referência: $novoMesReferencia")
        mesReferencia = novoMesReferencia
        // Não precisa notifyDataSetChanged aqui pois não afeta a UI visível
    }
    
    fun setOnClienteClickListener(listener: (ReceitaCliente) -> Unit) {
        LogUtils.debug("ReceitaClienteAdapter", "Configurando listener de clique do cliente")
        onClienteClicked = listener
    }
} 