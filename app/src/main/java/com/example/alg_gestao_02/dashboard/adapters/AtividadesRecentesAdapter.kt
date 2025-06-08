package com.example.alg_gestao_02.dashboard.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.AtividadeRecente
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Adapter para exibir atividades recentes no dashboard
 */
class AtividadesRecentesAdapter(
    private var atividades: List<AtividadeRecente> = emptyList()
) : RecyclerView.Adapter<AtividadesRecentesAdapter.AtividadeViewHolder>() {

    inner class AtividadeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivIcon: ImageView = itemView.findViewById(R.id.ivAtividadeIcon)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvAtividadeTitulo)
        val tvDescricao: TextView = itemView.findViewById(R.id.tvAtividadeDescricao)
        val tvTempo: TextView = itemView.findViewById(R.id.tvAtividadeTempo)
        val vDivisor: View = itemView.findViewById(R.id.vDivisor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AtividadeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_atividade_recente, parent, false)
        return AtividadeViewHolder(view)
    }

    override fun onBindViewHolder(holder: AtividadeViewHolder, position: Int) {
        val atividade = atividades[position]
        
        LogUtils.debug("AtividadesRecentesAdapter", "🎯 Bind atividade ${position + 1}: [${atividade.tipo}] ${atividade.titulo}")
        
        // Configurar textos
        holder.tvTitulo.text = atividade.titulo
        holder.tvDescricao.text = atividade.descricao
        holder.tvTempo.text = atividade.tempoRelativo
        
        // Configurar ícone e cor baseado no tipo
        when (atividade.tipo.lowercase()) {
            "contrato" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_add_circle)
                holder.ivIcon.setBackgroundResource(R.drawable.circle_background_success_light)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.success))
                LogUtils.debug("AtividadesRecentesAdapter", "   🎨 Aplicado estilo CONTRATO (verde)")
            }
            "cliente" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_person_add)
                holder.ivIcon.setBackgroundResource(R.drawable.circle_background_tertiary_light)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.tertiary))
                LogUtils.debug("AtividadesRecentesAdapter", "   🎨 Aplicado estilo CLIENTE (tertiary)")
            }
            "equipamento" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_settings)
                holder.ivIcon.setBackgroundResource(R.drawable.circle_background_primary_light)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.primary))
                LogUtils.debug("AtividadesRecentesAdapter", "   🎨 Aplicado estilo EQUIPAMENTO (primary)")
            }
            "devolucao" -> {
                holder.ivIcon.setImageResource(R.drawable.ic_assignment_return)
                holder.ivIcon.setBackgroundResource(R.drawable.circle_background_primary_light)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.primary))
                LogUtils.debug("AtividadesRecentesAdapter", "   🎨 Aplicado estilo DEVOLUÇÃO (primary)")
            }
            else -> {
                holder.ivIcon.setImageResource(R.drawable.ic_add_circle)
                holder.ivIcon.setBackgroundResource(R.drawable.circle_background_success_light)
                holder.ivIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.success))
                LogUtils.warning("AtividadesRecentesAdapter", "   ⚠️ Tipo desconhecido: ${atividade.tipo}, usando estilo padrão")
            }
        }
        
        // Ocultar divisor no último item
        holder.vDivisor.visibility = if (position == atividades.size - 1) View.GONE else View.VISIBLE
        
        LogUtils.debug("AtividadesRecentesAdapter", "   ✅ Item configurado: ${atividade.titulo} - ${atividade.tempoRelativo}")
    }

    override fun getItemCount(): Int = atividades.size

    /**
     * Atualiza a lista de atividades
     */
    fun updateAtividades(novasAtividades: List<AtividadeRecente>) {
        LogUtils.info("AtividadesRecentesAdapter", "🔄 ========== ATUALIZANDO ATIVIDADES ==========")
        LogUtils.info("AtividadesRecentesAdapter", "📊 Atividades anteriores: ${atividades.size}")
        LogUtils.info("AtividadesRecentesAdapter", "📊 Novas atividades: ${novasAtividades.size}")
        
        atividades = novasAtividades
        notifyDataSetChanged()
        
        LogUtils.info("AtividadesRecentesAdapter", "✅ Lista atualizada com ${atividades.size} atividades")
        
        // Log detalhado das atividades
        atividades.forEachIndexed { index, atividade ->
            LogUtils.debug("AtividadesRecentesAdapter", "   ${index + 1}. [${atividade.tipo.uppercase()}] ${atividade.titulo}")
        }
    }
} 