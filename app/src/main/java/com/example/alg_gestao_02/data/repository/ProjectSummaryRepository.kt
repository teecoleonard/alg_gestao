package com.example.alg_gestao_02.data.repository

import android.content.Context
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.data.db.AppDatabase
import com.example.alg_gestao_02.data.db.mapper.ProjectContractMapper
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Repositório responsável por gerenciar os dados do sumário do projeto.
 * Esta classe implementa o padrão Repository do MVVM e centraliza
 * todas as operações relacionadas a dados do sumário do projeto.
 * 
 * Implementa uma estratégia offline-first, onde primeiro tenta buscar dados locais
 * e depois atualiza com dados remotos quando houver conexão.
 */
class ProjectSummaryRepository(private val context: Context) {
    
    private val database by lazy { AppDatabase.getInstance(context) }
    private val contractDao by lazy { database.projectContractDao() }
    
    /**
     * Busca todos os contratos de um projeto.
     * Utiliza uma estratégia offline-first:
     * 1. Busca dados locais do banco de dados
     * 2. Se houver conexão, busca dados remotos, atualiza o banco e retorna
     * 3. Se não houver conexão, usa apenas os dados locais
     */
    fun getContractsByProject(projectId: String): Flow<List<ProjectContractItem>> {
        // Busca os dados do Room DB e converte para o modelo de domínio
        val localData = contractDao.getContractsByProjectId(projectId)
            .map { entities -> ProjectContractMapper.fromEntityList(entities) }
        
        // Tenta atualizar os dados da API assincronamente usando uma coroutine
        GlobalScope.launch {
            refreshContractsFromApi(projectId)
        }
        
        // Retorna o fluxo de dados locais (que será atualizado quando os dados da API chegarem)
        return localData
    }
    
    /**
     * Atualiza os dados do banco local com dados da API (quando houver internet).
     */
    private suspend fun refreshContractsFromApi(projectId: String) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            try {
                withContext(Dispatchers.IO) {
                    LogUtils.debug("ProjectSummaryRepository", "Buscando contratos da API para o projeto: $projectId")
                    
                    // Simula um delay de rede
                    delay(500)
                    
                    // TODO: No futuro, substituir por chamada real à API
                    val apiData = getMockContracts(projectId)
                    
                    // Converte para entidades e salva no banco de dados
                    val entities = ProjectContractMapper.toEntityList(apiData, true)
                    contractDao.insertAll(entities)
                    
                    LogUtils.debug("ProjectSummaryRepository", "Dados da API salvos no banco de dados local: ${entities.size} contratos")
                }
            } catch (e: Exception) {
                LogUtils.error("ProjectSummaryRepository", "Erro ao atualizar dados da API: ${e.message}")
            }
        } else {
            LogUtils.debug("ProjectSummaryRepository", "Sem conexão de internet. Usando apenas dados locais.")
        }
    }
    
    /**
     * Filtra contratos por tipo (payment, debt).
     * Busca diretamente do banco de dados local.
     */
    fun getContractsByType(projectId: String, type: String): Flow<List<ProjectContractItem>> {
        return contractDao.getContractsByProjectIdAndType(projectId, type)
            .map { entities -> ProjectContractMapper.fromEntityList(entities) }
    }
    
    /**
     * Busca contratos por texto (busca em diferentes campos).
     * Busca diretamente do banco de dados local.
     */
    fun searchContracts(projectId: String, query: String): Flow<List<ProjectContractItem>> {
        return contractDao.searchContracts(projectId, query)
            .map { entities -> ProjectContractMapper.fromEntityList(entities) }
    }
    
    /**
     * Adiciona um novo contrato.
     * Salva localmente e envia para a API se houver conexão.
     */
    suspend fun addContract(contract: ProjectContractItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val entity = ProjectContractMapper.toEntity(contract, NetworkUtils.isNetworkAvailable(context))
            contractDao.insert(entity)
            
            if (NetworkUtils.isNetworkAvailable(context)) {
                // TODO: No futuro, enviar para a API
                delay(300) // Simula chamada de rede
                contractDao.markAsSynced(contract.id)
            }
            
            return@withContext true
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryRepository", "Erro ao adicionar contrato: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Atualiza um contrato existente.
     * Atualiza localmente e na API se houver conexão.
     */
    suspend fun updateContract(contract: ProjectContractItem): Boolean = withContext(Dispatchers.IO) {
        try {
            val entity = ProjectContractMapper.toEntity(contract, NetworkUtils.isNetworkAvailable(context))
            contractDao.update(entity)
            
            if (NetworkUtils.isNetworkAvailable(context)) {
                // TODO: No futuro, atualizar na API
                delay(300) // Simula chamada de rede
                contractDao.markAsSynced(contract.id)
            }
            
            return@withContext true
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryRepository", "Erro ao atualizar contrato: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Remove um contrato.
     * Remove localmente e na API se houver conexão.
     */
    suspend fun deleteContract(contractId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            contractDao.deleteById(contractId)
            
            if (NetworkUtils.isNetworkAvailable(context)) {
                // TODO: No futuro, excluir da API
                delay(300) // Simula chamada de rede
            }
            
            return@withContext true
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryRepository", "Erro ao excluir contrato: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Sincroniza dados locais não sincronizados com a API.
     * Útil para quando a conexão é restaurada.
     */
    suspend fun syncUnsyncedData(): Boolean = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isNetworkAvailable(context)) {
            return@withContext false
        }
        
        try {
            // Busca contratos não sincronizados
            val unsyncedContracts = contractDao.getUnsyncedContracts()
            
            for (contract in unsyncedContracts) {
                // TODO: No futuro, enviar para a API
                delay(100) // Simula chamada de rede
                contractDao.markAsSynced(contract.id)
            }
            
            return@withContext true
        } catch (e: Exception) {
            LogUtils.error("ProjectSummaryRepository", "Erro na sincronização: ${e.message}")
            return@withContext false
        }
    }
    
    /**
     * Carrega dados iniciais para teste, populando o banco de dados local.
     * Usado apenas para desenvolvimento/teste.
     */
    suspend fun loadInitialData(projectId: String) = withContext(Dispatchers.IO) {
        // Verifica primeiro se já temos dados no banco
        val existingData = contractDao.getContractsByProjectId(projectId).first()
        
        if (existingData.isEmpty()) {
            LogUtils.debug("ProjectSummaryRepository", "Carregando dados iniciais para o banco")
            
            // Dados mockados para popular o banco
            val mockData = getMockContracts(projectId)
            val entities = ProjectContractMapper.toEntityList(mockData, true)
            contractDao.insertAll(entities)
            
            LogUtils.debug("ProjectSummaryRepository", "Banco de dados populado com ${entities.size} contratos")
        }
    }
    
    /**
     * Dados mockados para testes enquanto não integra com a API
     */
    private fun getMockContracts(projectId: String): List<ProjectContractItem> {
        return listOf(
            ProjectContractItem(
                id = "1",
                projectId = projectId,
                name = "Mohil Prajapati",
                description = "Description here...",
                value = 50000.0,
                date = "30 Sep 2024, 07:23 PM",
                status = "active",
                type = "debt"
            ),
            ProjectContractItem(
                id = "2",
                projectId = projectId,
                name = "Freyja Hooper",
                description = "Description here...",
                value = 30000.0,
                date = "30 Sep 2024, 07:23 PM",
                status = "active",
                type = "debt"
            ),
            ProjectContractItem(
                id = "3",
                projectId = projectId,
                name = "Alexander Gardner",
                description = "Description here...",
                value = 1150000.0,
                date = "30 Sep 2024, 07:23 PM",
                status = "active",
                type = "payment"
            ),
            ProjectContractItem(
                id = "4",
                projectId = projectId,
                name = "Aiden Schneider",
                description = "Description here...",
                value = 10000.0,
                date = "30 Sep 2024, 07:23 PM",
                status = "active",
                type = "debt"
            ),
            ProjectContractItem(
                id = "5",
                projectId = projectId,
                name = "Eliana Acosta",
                description = "Description here...",
                value = 1350000.0,
                date = "30 Sep 2024, 07:23 PM",
                status = "active",
                type = "payment"
            )
        )
    }
} 