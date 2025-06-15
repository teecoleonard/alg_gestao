package com.example.alg_gestao_02.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.databinding.ItemNotificationBinding
import com.example.alg_gestao_02.model.Notification
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private var notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit,
    private val onMarkAsRead: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM", Locale("pt", "BR"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("pt", "BR"))

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(notification: Notification, position: Int) {
            binding.apply {
                // Configurar textos
                tvNotificationTitle.text = notification.title
                tvNotificationDescription.text = notification.description
                
                // Configurar ícone
                ivNotificationIcon.setImageResource(notification.iconRes)
                ivNotificationIcon.setColorFilter(notification.iconTint)
                ivNotificationIcon.setBackgroundResource(notification.iconBackground)
                
                // Configurar tempo
                tvNotificationTime.text = formatTime(notification.timestamp)
                
                // Configurar indicador de leitura
                viewUnreadIndicator.visibility = if (!notification.isRead) View.VISIBLE else View.GONE
                
                // Configurar divisor (ocultar no último item)
                vNotificationDivisor.visibility = if (position == notifications.size - 1) View.GONE else View.VISIBLE
                
                // Configurar cliques
                root.setOnClickListener {
                    onNotificationClick(notification)
                    if (!notification.isRead) {
                        onMarkAsRead(notification)
                    }
                }
                
                // Aplicar estilo para lida/não lida
                val alpha = if (notification.isRead) 0.6f else 1.0f
                tvNotificationTitle.alpha = alpha
                tvNotificationDescription.alpha = alpha
                ivNotificationIcon.alpha = alpha
            }
        }
        
        private fun formatTime(date: Date): String {
            val now = Date()
            val diffMillis = now.time - date.time
            val diffHours = diffMillis / (1000 * 60 * 60)
            val diffDays = diffMillis / (1000 * 60 * 60 * 24)
            
            return when {
                diffHours < 1 -> "Agora"
                diffHours < 24 -> "${diffHours}h"
                diffDays == 1L -> "Ontem"
                diffDays < 7 -> "${diffDays}d"
                else -> dateFormat.format(date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position], position)
    }

    override fun getItemCount(): Int = notifications.size

    fun updateNotifications(newNotifications: List<Notification>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
} 