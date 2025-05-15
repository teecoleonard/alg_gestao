package com.example.alg_gestao_02.ui.contrato

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale

class EquipamentoContratoAdapter(
    private val context: Context,
    private var equipamentos: MutableList<EquipamentoContrato> = mutableListOf(),
    private val onItemClick: ((EquipamentoContrato) -> Unit)? = null,
    private val onItemRemove: ((EquipamentoContrato) -> Unit)? = null
) : RecyclerView.Adapter<EquipamentoContratoAdapter.EquipamentoViewHolder>() {

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipamentoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_equipamento_contrato, parent, false)
        return EquipamentoViewHolder(view)
    }

    override fun getItemCount(): Int = equipamentos.size

    override fun onBindViewHolder(holder: EquipamentoViewHolder, position: Int) {
        val equipamento = equipamentos[position]
        
        // Log detalhado para depuração
        LogUtils.debug("EquipamentoContratoAdapter", "Vinculando dados ao ViewHolder:\n" +
                "ID: ${equipamento.id}\n" +
                "Nome: ${equipamento.nomeEquipamentoExibicao}\n" +  // Usar a propriedade computada para exibição
                "Quantidade: ${equipamento.quantidadeEquip}\n" +
                "Valor Unitário: ${equipamento.valorUnitario}\n" +
                "Valor Total: ${equipamento.valorTotal}\n" +
                "Valor Frete: ${equipamento.valorFrete}")
        
        // Usar a propriedade nomeEquipamentoExibicao que já contém a lógica para obter o nome correto
        holder.tvNomeEquipamento.text = equipamento.nomeEquipamentoExibicao
        
        // Mostrar quantidade
        holder.tvQuantidade.text = "Qtd: ${equipamento.quantidadeEquip}"
        
        // Mostrar valor unitário formatado
        holder.tvValorUnitario.text = "Unit: ${currencyFormat.format(equipamento.valorUnitario)}"
        
        // Mostrar valor total formatado
        holder.tvValorTotal.text = currencyFormat.format(equipamento.valorTotal)
        
        // Configurar clique no item para edição
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(equipamento)
        }
        
        // Configurar clique no botão de remoção
        holder.btnRemover.setOnClickListener {
            confirmarRemocao(equipamento, position)
        }
    }
    
    /**
     * Atualiza a lista de equipamentos e notifica o adaptador
     */
    fun updateEquipamentos(newEquipamentos: List<EquipamentoContrato>) {
        LogUtils.debug("EquipamentoContratoAdapter", "Atualizando lista de equipamentos: ${newEquipamentos.size} itens")
        
        // Log do primeiro item para depuração
        if (newEquipamentos.isNotEmpty()) {
            val primeiro = newEquipamentos.first()
            LogUtils.debug("EquipamentoContratoAdapter", "Equipamento recebido no adapter:\n" +
                    "ID: ${primeiro.id}\n" +
                    "Nome: ${primeiro.nomeEquipamentoExibicao}\n" +  // Usar a propriedade computada 
                    "Quantidade: ${primeiro.quantidadeEquip}\n" +
                    "Valor Unitário: ${primeiro.valorUnitario}\n" +
                    "Valor Total: ${primeiro.valorTotal}\n" +
                    "Valor Frete: ${primeiro.valorFrete}")
        }
        
        this.equipamentos.clear()
        this.equipamentos.addAll(newEquipamentos)
        notifyDataSetChanged()
    }
    
    private fun confirmarRemocao(equipamento: EquipamentoContrato, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Remover Equipamento")
            .setMessage("Deseja remover ${equipamento.nomeEquipamentoExibicao} do contrato?")
            .setPositiveButton("Remover") { _, _ ->
                onItemRemove?.invoke(equipamento)
                equipamentos.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemCount)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    class EquipamentoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNomeEquipamento: TextView = itemView.findViewById(R.id.tvNomeEquipamento)
        val tvQuantidade: TextView = itemView.findViewById(R.id.tvQuantidade)
        val tvValorUnitario: TextView = itemView.findViewById(R.id.tvValorUnitario)
        val tvValorTotal: TextView = itemView.findViewById(R.id.tvValorTotal)
        val btnRemover: ImageButton = itemView.findViewById(R.id.btnDelete)
    }
} 