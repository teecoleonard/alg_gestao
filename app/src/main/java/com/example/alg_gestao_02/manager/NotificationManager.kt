package com.example.alg_gestao_02.manager

import android.content.Context
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.model.Notification
import com.example.alg_gestao_02.model.NotificationType
import java.util.*

class NotificationManager {
    private val notifications = mutableListOf<Notification>()
    
    init {
        // Simular algumas notificações baseadas nas atividades recentes
        addSampleNotifications()
    }
    
    private fun addSampleNotifications() {
        val now = Date()
        val calendar = Calendar.getInstance()
        
        // Notificação de contrato criado (hoje)
        notifications.add(Notification(
            id = "1",
            title = "Novo contrato criado",
            description = "Contrato #2024-003 para Cliente ABC",
            iconRes = R.drawable.ic_add_circle,
            iconTint = R.color.success,
            iconBackground = R.drawable.circle_background_success_light,
            timestamp = now,
            isRead = false,
            type = NotificationType.CONTRACT_CREATED
        ))
        
        // Notificação de cliente adicionado (2 horas atrás)
        calendar.time = now
        calendar.add(Calendar.HOUR_OF_DAY, -2)
        notifications.add(Notification(
            id = "2",
            title = "Cliente cadastrado",
            description = "João Silva foi adicionado ao sistema",
            iconRes = R.drawable.ic_person_add,
            iconTint = R.color.primary,
            iconBackground = R.drawable.circle_background_primary_light,
            timestamp = calendar.time,
            isRead = false,
            type = NotificationType.CLIENT_ADDED
        ))
        
        // Notificação de equipamento disponível (ontem)
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        notifications.add(Notification(
            id = "3",
            title = "Equipamento disponível",
            description = "Furadeira #FUR-001 foi devolvida e está disponível",
            iconRes = R.drawable.ic_check_circle,
            iconTint = R.color.success,
            iconBackground = R.drawable.circle_background_success_light,
            timestamp = calendar.time,
            isRead = true,
            type = NotificationType.EQUIPMENT_AVAILABLE
        ))
        
        // Notificação de devolução pendente (3 dias atrás)
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, -3)
        notifications.add(Notification(
            id = "4",
            title = "Devolução pendente",
            description = "Equipamento #EQP-015 com atraso de 2 dias",
            iconRes = R.drawable.ic_warning,
            iconTint = R.color.warning,
            iconBackground = R.drawable.circle_background_warning_light,
            timestamp = calendar.time,
            isRead = true,
            type = NotificationType.RETURN_PENDING
        ))
        
        // Notificação de devolução concluída (1 semana atrás)
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        notifications.add(Notification(
            id = "5",
            title = "Devolução concluída",
            description = "Equipamento #EQP-008 foi devolvido com sucesso",
            iconRes = R.drawable.ic_assignment_return,
            iconTint = R.color.tertiary,
            iconBackground = R.drawable.circle_background_tertiary_light,
            timestamp = calendar.time,
            isRead = true,
            type = NotificationType.RETURN_COMPLETED
        ))
        
        // Ordenar por timestamp (mais recent primeiro)
        notifications.sortByDescending { it.timestamp }
    }
    
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