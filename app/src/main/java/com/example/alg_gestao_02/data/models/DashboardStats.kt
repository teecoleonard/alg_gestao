package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para as estatísticas do dashboard
 */
data class DashboardStats(
    @SerializedName("contratos")
    val contratos: Int,
    
    @SerializedName("clientes")
    val clientes: Int,
    
    @SerializedName("equipamentos")
    val equipamentos: Int,
    
    @SerializedName("devolucoes")
    val devolucoes: Int,
    
    // Informações detalhadas
    @SerializedName("contratos_esta_semana")
    val contratosEstaSemana: Int = 0,
    
    @SerializedName("clientes_hoje")
    val clientesHoje: Int = 0,
    
    @SerializedName("equipamentos_disponiveis")
    val equipamentosDisponiveis: Int = 0,
    
    @SerializedName("devolucoes_pendentes")
    val devolucoesPendentes: Int = 0,
    
    // Atividades recentes
    @SerializedName("atividades_recentes")
    val atividadesRecentes: List<AtividadeRecente> = emptyList()
)

/**
 * Modelo para uma atividade recente
 */
data class AtividadeRecente(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("tipo")
    val tipo: String, // "contrato", "cliente", "equipamento", "devolucao"
    
    @SerializedName("titulo")
    val titulo: String,
    
    @SerializedName("descricao")
    val descricao: String,
    
    @SerializedName("data")
    val data: String,
    
    @SerializedName("tempo_relativo")
    val tempoRelativo: String // "Hoje", "Ontem", "2 dias atrás"
) 