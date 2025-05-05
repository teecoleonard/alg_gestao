package com.example.alg_gestao_02.ui.contract.repository

import com.example.alg_gestao_02.data.models.ContratoProjeto
import com.example.alg_gestao_02.data.repository.ContractRepository as DataContractRepository
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repositório para contratos no nível de UI
 */
class ContractRepository {
    
    // Usa o repositório de dados
    private val dataRepository = DataContractRepository()
    
    /**
     * Busca todos os contratos
     */
    suspend fun getAllContracts(): List<ContratoProjeto> = withContext(Dispatchers.IO) {
        // Delega para o repositório de dados
        return@withContext dataRepository.getContractsByProject("1") // ID fixo para demonstração
    }
    
    /**
     * Busca contratos por projeto
     */
    suspend fun getAllContractsByProject(projectId: String): List<ContratoProjeto> = withContext(Dispatchers.IO) {
        return@withContext dataRepository.getContractsByProject(projectId)
    }
    
    /**
     * Busca contratos por projeto e status
     */
    suspend fun getContractsByProjectAndStatus(projectId: String, status: String): List<ContratoProjeto> = withContext(Dispatchers.IO) {
        return@withContext dataRepository.getContractsByProjectAndStatus(projectId, status)
    }
    
    /**
     * Adiciona um novo contrato
     */
    suspend fun addContract(contract: ContratoProjeto): ContratoProjeto = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        return@withContext contract
    }
    
    /**
     * Atualiza um contrato existente
     */
    suspend fun updateContract(contract: ContratoProjeto): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        return@withContext true
    }
    
    /**
     * Exclui um contrato pelo ID
     */
    suspend fun deleteContract(contractId: String): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        return@withContext true
    }
} 