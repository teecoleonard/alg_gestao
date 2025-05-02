package com.example.alg_gestao_02.ui.empresa.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Empresa

/**
 * Adapter para a lista de empresas.
 * Responsável por gerenciar a exibição dos itens na RecyclerView.
 */
class EmpresasAdapter(
    private var empresasList: List<Empresa>,
    private val onItemClick: (Empresa) -> Unit,
    private val onOptionsClick: (Empresa, View) -> Unit
) : RecyclerView.Adapter<EmpresasAdapter.EmpresaViewHolder>() {
    
    /**
     * Atualiza os dados do adapter usando DiffUtil para animações suaves.
     */
    fun updateData(newList: List<Empresa>) {
        val diffCallback = EmpresaDiffCallback(empresasList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        
        this.empresasList = newList
        diffResult.dispatchUpdatesTo(this)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmpresaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empresa, parent, false)
        return EmpresaViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: EmpresaViewHolder, position: Int) {
        holder.bind(empresasList[position])
    }
    
    override fun getItemCount(): Int = empresasList.size
    
    /**
     * ViewHolder para um item de empresa na lista.
     */
    inner class EmpresaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomeEmpresa)
        private val tvCnpj: TextView = itemView.findViewById(R.id.tvCnpjEmpresa) 
        private val tvRamoAtividade: TextView = itemView.findViewById(R.id.tvRamoAtividade)
        private val tvTelefone: TextView = itemView.findViewById(R.id.tvTelefoneEmpresa)
        private val imgStatus: ImageView = itemView.findViewById(R.id.imgStatus)
        private val btnOptions: ImageButton = itemView.findViewById(R.id.btnOptions)
        
        fun bind(empresa: Empresa) {
            tvNome.text = empresa.getNomeExibicao()
            tvCnpj.text = empresa.getCnpjFormatado()
            tvRamoAtividade.text = empresa.ramoAtividade ?: "N/A"
            tvTelefone.text = empresa.telefone
            
            // Configurar status com cor adequada
            if (empresa.status == "ativo") {
                imgStatus.setImageResource(R.drawable.ic_status_active)
            } else {
                imgStatus.setImageResource(R.drawable.ic_status_inactive)
            }
            
            // Configurar listeners
            itemView.setOnClickListener { onItemClick(empresa) }
            btnOptions.setOnClickListener { onOptionsClick(empresa, it) }
        }
    }
    
    /**
     * Implementação de DiffUtil.Callback para cálculo eficiente de diferenças.
     */
    class EmpresaDiffCallback(
        private val oldList: List<Empresa>,
        private val newList: List<Empresa>
    ) : DiffUtil.Callback() {
        
        override fun getOldListSize(): Int = oldList.size
        
        override fun getNewListSize(): Int = newList.size
        
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }
        
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldList[oldItemPosition]
            val newItem = newList[newItemPosition]
            
            return oldItem.nome == newItem.nome &&
                    oldItem.cnpj == newItem.cnpj &&
                    oldItem.ramoAtividade == newItem.ramoAtividade &&
                    oldItem.status == newItem.status
        }
    }
} 