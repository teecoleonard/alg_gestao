package com.example.alg_gestao_02.data.models

import com.google.gson.annotations.SerializedName

/**
 * Request para gerar PDF de resumo mensal
 */
data class GerarPdfResumoRequest(
    @SerializedName("mes_referencia")
    val mesReferencia: String, // formato: "2024-01"
    
    @SerializedName("cliente_ids")
    val clienteIds: List<Int>? = null, // Se null, inclui todos os clientes
    
    @SerializedName("incluir_detalhes")
    val incluirDetalhes: Boolean = true, // Se deve incluir detalhes de contratos e devoluções
    
    @SerializedName("incluir_graficos")
    val incluirGraficos: Boolean = false, // Se deve incluir gráficos (futuro)
    
    @SerializedName("tipo_relatorio")
    val tipoRelatorio: String = "COMPLETO" // "COMPLETO", "RESUMIDO", "FINANCEIRO"
)

/**
 * Response da geração de PDF
 */
data class PdfResumoResponse(
    @SerializedName("sucesso")
    val sucesso: Boolean,
    
    @SerializedName("mensagem")
    val mensagem: String,
    
    @SerializedName("url_download")
    val urlDownload: String?,
    
    @SerializedName("nome_arquivo")
    val nomeArquivo: String?,
    
    @SerializedName("tamanho_arquivo")
    val tamanhoArquivo: Long?,
    
    @SerializedName("data_geracao")
    val dataGeracao: String,
    
    @SerializedName("resumo_estatisticas")
    val resumoEstatisticas: EstatisticasPdf?
)

/**
 * Estatísticas incluídas no PDF
 */
data class EstatisticasPdf(
    @SerializedName("total_clientes")
    val totalClientes: Int,
    
    @SerializedName("valor_total_mensal")
    val valorTotalMensal: Double,
    
    @SerializedName("total_contratos")
    val totalContratos: Int,
    
    @SerializedName("total_devolucoes")
    val totalDevolucoes: Int,
    
    @SerializedName("clientes_pagos")
    val clientesPagos: Int,
    
    @SerializedName("clientes_pendentes")
    val clientesPendentes: Int,
    
    @SerializedName("clientes_atrasados")
    val clientesAtrasados: Int
) 