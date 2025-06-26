package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para tarefas pendentes do dashboard
 */
data class TaskMetrics(
    @SerializedName("contratos_aguardando_assinatura")
    val contratosAguardandoAssinatura: Int,
    
    @SerializedName("devolucoes_em_atraso")
    val devolucoesEmAtraso: Int,
    
    @SerializedName("equipamentos_manutencao")
    val equipamentosManutencao: Int
) {
    // Total de tarefas pendentes
    val totalTarefas: Int
        get() = contratosAguardandoAssinatura + devolucoesEmAtraso + equipamentosManutencao
} 