package com.example.alg_gestao_02.dashboard.fragments.project.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.project.model.Invoice
import com.example.alg_gestao_02.dashboard.fragments.project.model.InvoiceStatus
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class InvoicesAdapter(
    private val invoices: List<Invoice>,
    private val onItemClick: (Invoice) -> Unit,
    private val onOptionsClick: (Invoice, View) -> Unit
) : RecyclerView.Adapter<InvoicesAdapter.InvoiceViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project_invoice, parent, false)
        return InvoiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {
        val invoice = invoices[position]
        holder.bind(invoice)
    }

    override fun getItemCount() = invoices.size

    inner class InvoiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvInvoiceNumber: TextView = itemView.findViewById(R.id.tvInvoiceNumber)
        private val tvValue: TextView = itemView.findViewById(R.id.tvInvoiceValue)
        private val tvIssueDate: TextView = itemView.findViewById(R.id.tvIssueDate)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val ivOptions: ImageView = itemView.findViewById(R.id.ivInvoiceOptions)

        fun bind(invoice: Invoice) {
            tvInvoiceNumber.text = invoice.invoiceNumber
            tvValue.text = currencyFormat.format(invoice.value)
            tvIssueDate.text = dateFormat.format(invoice.issueDate)
            tvDueDate.text = dateFormat.format(invoice.dueDate)

            // Configurar o status com o fundo e texto apropriados
            when (invoice.status) {
                InvoiceStatus.PAID -> {
                    tvStatus.text = itemView.context.getString(R.string.status_paid)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_active)
                }
                InvoiceStatus.PENDING -> {
                    tvStatus.text = itemView.context.getString(R.string.status_pending)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                }
                InvoiceStatus.OVERDUE -> {
                    tvStatus.text = itemView.context.getString(R.string.status_overdue)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_inactive)
                }
                InvoiceStatus.CANCELLED -> {
                    tvStatus.text = itemView.context.getString(R.string.status_cancelled)
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                }
            }

            itemView.setOnClickListener { onItemClick(invoice) }
            ivOptions.setOnClickListener { onOptionsClick(invoice, it) }
        }
    }
} 