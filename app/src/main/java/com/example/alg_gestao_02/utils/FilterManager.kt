package com.example.alg_gestao_02.utils

/**
 * Gerenciador de filtros compartilhados entre fragments
 * Permite aplicar filtros quando navegando de um fragment para outro
 */
object FilterManager {
    
    private var pendingClientFilter: ClientFilter? = null
    
    data class ClientFilter(
        val clienteId: Int,
        val clienteNome: String,
        val targetType: FilterType
    )
    
    enum class FilterType {
        CONTRATOS,
        DEVOLUCOES
    }
    
    /**
     * Define um filtro de cliente pendente para ser aplicado
     */
    fun setPendingClientFilter(clienteId: Int, clienteNome: String, filterType: FilterType) {
        pendingClientFilter = ClientFilter(clienteId, clienteNome, filterType)
        LogUtils.debug("FilterManager", "Filtro pendente definido: $clienteNome (ID: $clienteId) para $filterType")
    }
    
    /**
     * Obtém e consome o filtro pendente para contratos
     */
    fun consumePendingContractsFilter(): ClientFilter? {
        val filter = pendingClientFilter?.takeIf { it.targetType == FilterType.CONTRATOS }
        if (filter != null) {
            pendingClientFilter = null
            LogUtils.debug("FilterManager", "Filtro de contratos consumido: ${filter.clienteNome}")
        }
        return filter
    }
    
    /**
     * Obtém e consome o filtro pendente para devoluções
     */
    fun consumePendingReturnsFilter(): ClientFilter? {
        val filter = pendingClientFilter?.takeIf { it.targetType == FilterType.DEVOLUCOES }
        if (filter != null) {
            pendingClientFilter = null
            LogUtils.debug("FilterManager", "Filtro de devoluções consumido: ${filter.clienteNome}")
        }
        return filter
    }
    
    /**
     * Limpa qualquer filtro pendente
     */
    fun clearPendingFilter() {
        if (pendingClientFilter != null) {
            LogUtils.debug("FilterManager", "Filtro pendente limpo")
            pendingClientFilter = null
        }
    }
    
    /**
     * Verifica se há um filtro pendente
     */
    fun hasPendingFilter(): Boolean {
        return pendingClientFilter != null
    }
} 