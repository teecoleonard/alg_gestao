package com.example.alg_gestao_02.ui.contrato.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.*

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
        private val tvStatusContratoCard: TextView = itemView.findViewById(R.id.tvStatusContratoCard)
        private val llEquipamentos: LinearLayout = itemView.findViewById(R.id.llEquipamentos)
        
        /**
         * Vincula os dados do contrato ao ViewHolder
         */
        fun bind(contrato: Contrato) {
            // Define o nome do cliente (limitado a 1 linha)
            val nomeCliente = contrato.resolverNomeCliente()
            tvClienteNome.text = nomeCliente
            
            // Define o número do contrato
            tvContratoNumero.text = "Contrato #${contrato.contratoNum}"
            
            // Define o valor do contrato usando a função getValorEfetivo() que considera todas as opções
            val valorExibido = contrato.getValorEfetivo()
            LogUtils.debug("ContratosAdapter", "Contrato #${contrato.contratoNum}: Valor efetivo: $valorExibido")
            tvValorContrato.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valorExibido)
            
            // Define a data de emissão
            tvDataEmissao.text = "Emissão: ${contrato.getDataEmissaoFormatada()}"
            
            // Define a data de vencimento
            tvDataVencimento.text = "Vencimento: ${contrato.getDataVencimentoFormatada()}"
            
            // Define o local da obra
            tvLocalObra.text = "Local: ${contrato.obraLocal}"
            
            // Define o período do contrato
            tvPeriodoContrato.text = "Período: ${contrato.contratoPeriodo}"
            
            // Popular equipamentos
            populateEquipamentos(contrato)
            
            // Define o status visual do contrato (assinado/não assinado) - ícone
            if (contrato.isAssinado()) {
                ivStatusContrato.setImageResource(R.drawable.ic_check_circle)
                ivStatusContrato.setColorFilter(itemView.context.getColor(R.color.success))
            } else {
                ivStatusContrato.setImageResource(R.drawable.ic_pending)
                ivStatusContrato.setColorFilter(itemView.context.getColor(R.color.warning))
            }
            
            // Define o status do contrato - Badge com texto
            val statusContrato = contrato.getStatusContratoEnum()
            tvStatusContratoCard.text = "${statusContrato.getIcone()} ${statusContrato.descricao.uppercase()}"
            tvStatusContratoCard.setTextColor(android.graphics.Color.WHITE)
            tvStatusContratoCard.setBackgroundColor(statusContrato.getCor())
            
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
        
        /**
         * Popula a lista de equipamentos do contrato
         */
        private fun populateEquipamentos(contrato: Contrato) {
            // Limpar equipamentos existentes (exceto os exemplos)
            llEquipamentos.removeAllViews()
            
            val equipamentos = contrato.equipamentos
            if (equipamentos != null && equipamentos.isNotEmpty()) {
                // Mostrar apenas os primeiros 2 equipamentos para manter o layout compacto
                val equipamentosParaMostrar = equipamentos.take(2)
                
                equipamentosParaMostrar.forEach { equipamento ->
                    val equipamentoView = createEquipamentoView(equipamento)
                    llEquipamentos.addView(equipamentoView)
                }
                
                // Se há mais de 2 equipamentos, mostrar indicador
                if (equipamentos.size > 2) {
                    val maisEquipamentosView = createMaisEquipamentosView(equipamentos.size - 2)
                    llEquipamentos.addView(maisEquipamentosView)
                }
            } else {
                // Se não há equipamentos ou é null, mostrar mensagem
                val semEquipamentosView = createSemEquipamentosView()
                llEquipamentos.addView(semEquipamentosView)
                
                // Log para debug
                LogUtils.debug("ContratosAdapter", "Contrato ${contrato.contratoNum}: equipamentos é ${if (equipamentos == null) "null" else "vazio"}")
            }
        }
        
        /**
         * Cria uma view para um equipamento
         */
        private fun createEquipamentoView(equipamento: com.example.alg_gestao_02.data.models.EquipamentoContrato): View {
            val layout = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 6 // 2dp em pixels
                }
            }
            
            // Bullet point
            val bullet = View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(9, 9).apply {
                    marginEnd = 18 // 6dp em pixels
                }
                setBackgroundResource(R.drawable.circle_background_primary)
                alpha = 0.6f
            }
            
            // Nome do equipamento
            val nomeEquipamento = TextView(itemView.context).apply {
                text = equipamento.equipamentoNome ?: "Equipamento"
                textSize = 10f
                setTextColor(itemView.context.getColor(R.color.text_primary))
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // layout_weight = 1
                )
            }
            
            // Quantidade
            val quantidade = TextView(itemView.context).apply {
                text = "x${equipamento.quantidadeEquip}"
                textSize = 9f
                setTextColor(itemView.context.getColor(R.color.text_secondary))
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = 24 // 8dp em pixels
                }
            }
            
            layout.addView(bullet)
            layout.addView(nomeEquipamento)
            layout.addView(quantidade)
            
            return layout
        }
        
        /**
         * Cria uma view para indicar mais equipamentos
         */
        private fun createMaisEquipamentosView(quantidade: Int): View {
            val layout = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 6 // 2dp em pixels
                }
            }
            
            // Bullet point
            val bullet = View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(9, 9).apply {
                    marginEnd = 18 // 6dp em pixels
                }
                setBackgroundResource(R.drawable.circle_background_primary)
                alpha = 0.6f
            }
            
            val maisEquipamentos = TextView(itemView.context).apply {
                text = "+ $quantidade equipamentos"
                textSize = 9f
                setTextColor(itemView.context.getColor(R.color.primary_color))
                setTypeface(null, android.graphics.Typeface.ITALIC)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // layout_weight = 1
                )
            }
            
            layout.addView(bullet)
            layout.addView(maisEquipamentos)
            return layout
        }
        
        /**
         * Cria uma view para quando não há equipamentos
         */
        private fun createSemEquipamentosView(): View {
            val layout = LinearLayout(itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 6 // 2dp em pixels
                }
            }
            
            // Bullet point
            val bullet = View(itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(9, 9).apply {
                    marginEnd = 18 // 6dp em pixels
                }
                setBackgroundResource(R.drawable.circle_background_primary)
                alpha = 0.6f
            }
            
            val semEquipamentos = TextView(itemView.context).apply {
                text = "Nenhum equipamento"
                textSize = 9f
                setTextColor(itemView.context.getColor(R.color.text_secondary))
                setTypeface(null, android.graphics.Typeface.ITALIC)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // layout_weight = 1
                )
            }
            
            layout.addView(bullet)
            layout.addView(semEquipamentos)
            return layout
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContratoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contrato, parent, false)
        return ContratoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ContratoViewHolder, position: Int) {
        LogUtils.debug("ContratosAdapter", "onBindViewHolder: posição $position, total contratos: ${contratos.size}")
        if (position < contratos.size) {
            holder.bind(contratos[position])
            LogUtils.debug("ContratosAdapter", "ViewHolder bind executado para posição $position")
        } else {
            LogUtils.error("ContratosAdapter", "Posição $position fora do range (${contratos.size})")
        }
    }
    
    override fun getItemCount(): Int = contratos.size
    
    /**
     * Atualiza a lista de contratos do adapter
     */
    fun updateContratos(newContratos: List<Contrato>) {
        LogUtils.debug("ContratosAdapter", "updateContratos chamado com ${newContratos.size} contratos")
        this.contratos = newContratos
        LogUtils.debug("ContratosAdapter", "Lista de contratos atualizada, chamando notifyDataSetChanged()")
        notifyDataSetChanged()
        LogUtils.debug("ContratosAdapter", "notifyDataSetChanged() executado")
    }
    
    /**
     * Alias para updateContratos para compatibilidade com ClientDetailsFragment
     */
    fun updateData(newContratos: List<Contrato>) {
        updateContratos(newContratos)
    }
} 