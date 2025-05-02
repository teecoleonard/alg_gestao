package com.example.alg_gestao_02.dashboard.fragments.client.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectInvoiceItem
import com.example.alg_gestao_02.databinding.ItemProjectInvoiceBinding
import java.text.NumberFormat
import java.util.Locale

class ProjectInvoicesAdapter(
    private val onItemClick: (ProjectInvoiceItem) -> Unit,
    private val onSeeDetailsClick: (ProjectInvoiceItem) -> Unit,
    private var invoices: List<ProjectInvoiceItem> = emptyList()
) : RecyclerView.Adapter<ProjectInvoicesAdapter.InvoiceViewHolder>() {

    fun updateData(newInvoices: List<ProjectInvoiceItem>) {
        this.invoices = newInvoices
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = ItemProjectInvoiceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.bind(invoice)
    }

    override fun getItemCount(): Int = invoices.size

    inner class InvoiceViewHolder(private val binding: ItemProjectInvoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(invoice: ProjectInvoiceItem) {
            val locale = Locale("pt", "BR")
            val format = NumberFormat.getCurrencyInstance(locale)

            binding.apply {
                tvInvoiceNumber.text = invoice.numero
                tvInvoiceValue.text = format.format(invoice.valor)
                tvIssueDate.text = invoice.dataEmissao
                tvDueDate.text = invoice.dataVencimento
                tvStatus.text = invoice.status

                // Definir cor baseada no status
                when (invoice.status) {
                    "Pago" -> tvStatus.setBackgroundResource(com.example.alg_gestao_02.R.drawable.status_background_success)
                    "Pendente" -> tvStatus.setBackgroundResource(com.example.alg_gestao_02.R.drawable.status_background_pending)
                    "Atrasado" -> tvStatus.setBackgroundResource(com.example.alg_gestao_02.R.drawable.status_background_error)
                    "Cancelado" -> tvStatus.setBackgroundResource(com.example.alg_gestao_02.R.drawable.status_background_neutral)
                    else -> tvStatus.setBackgroundResource(com.example.alg_gestao_02.R.drawable.status_background_neutral)
                }

                // Configurar clique no item
                root.setOnClickListener {
                    onItemClick(invoice)
                }

                // Configurar clique no botão "Ver detalhes"
                tvVerDetalhes.setOnClickListener {
                    onSeeDetailsClick(invoice)
                }
            }
        }
    }
} 