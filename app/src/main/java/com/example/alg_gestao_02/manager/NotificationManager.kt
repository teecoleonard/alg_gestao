package com.example.alg_gestao_02.manager

import android.content.Context
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.model.Notification
import com.example.alg_gestao_02.model.NotificationType
import com.example.alg_gestao_02.data.models.Contrato
import java.util.*

class NotificationManager {
    private val notifications = mutableListOf<Notification>()
    
    init {
        // Sistema de notificações inicializado sem notificações fake
        // As notificações reais serão adicionadas conforme eventos ocorrem
    }
    
    /**
     * Adiciona notificações para contratos vencidos
     */
    fun addContratosVencidosNotifications(contratos: List<Contrato>) {
        contratos.forEach { contrato ->
            val diasVencidos = -contrato.getDiasAteVencimento()
            val notification = Notification(
                id = "vencido_${contrato.id}",
                title = "Contrato Vencido",
                description = "Contrato #${contrato.contratoNum} de ${contrato.resolverNomeCliente()} venceu há $diasVencidos dia(s)",
                iconRes = R.drawable.ic_warning,
                iconTint = R.color.error,
                iconBackground = R.drawable.circle_background_warning_light,
                timestamp = Date(),
                isRead = false,
                type = NotificationType.CONTRACT_EXPIRED
            )
            addNotification(notification)
        }
    }
    
    /**
     * Adiciona notificações para contratos próximos do vencimento
     */
    fun addContratosProximosVencimentoNotifications(contratos: List<Contrato>) {
        contratos.forEach { contrato ->
            val diasRestantes = contrato.getDiasAteVencimento()
            val notification = Notification(
                id = "proximo_venc_${contrato.id}",
                title = "Contrato Próximo do Vencimento",
                description = "Contrato #${contrato.contratoNum} de ${contrato.resolverNomeCliente()} vence em $diasRestantes dia(s)",
                iconRes = R.drawable.ic_warning,
                iconTint = R.color.warning,
                iconBackground = R.drawable.circle_background_warning_light,
                timestamp = Date(),
                isRead = false,
                type = NotificationType.CONTRACT_NEAR_EXPIRATION
            )
            addNotification(notification)
        }
    }
    
    /**
     * Método removido - não usamos mais notificações fake
     * As notificações reais são criadas através dos métodos:
     * - addContratosVencidosNotifications()
     * - addContratosProximosVencimentoNotifications()
     */
    
    fun getAllNotifications(): List<Notification> {
        return notifications.toList()
    }
    
    fun getUnreadCount(): Int {
        return notifications.count { !it.isRead }
    }
    
    fun markAsRead(notificationId: String) {
        val index = notifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            notifications[index] = notifications[index].copy(isRead = true)
        }
    }
    
    fun markAllAsRead() {
        for (i in notifications.indices) {
            notifications[i] = notifications[i].copy(isRead = true)
        }
    }
    
    fun addNotification(notification: Notification) {
        notifications.add(0, notification) // Adicionar no início da lista
        notifications.sortByDescending { it.timestamp }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: NotificationManager? = null
        
        fun getInstance(): NotificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NotificationManager().also { INSTANCE = it }
            }
        }
    }
} 