package com.example.alg_gestao_02.model

import java.util.Date

data class Notification(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val iconTint: Int,
    val iconBackground: Int,
    val timestamp: Date,
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.GENERAL
)

enum class NotificationType {
    CONTRACT_CREATED,
    CLIENT_ADDED,
    EQUIPMENT_AVAILABLE,
    RETURN_PENDING,
    RETURN_COMPLETED,
    GENERAL
} 