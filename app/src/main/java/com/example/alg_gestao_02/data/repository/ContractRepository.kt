package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.dashboard.fragments.contract.model.Contrato
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Repositório responsável por gerenciar os dados de contratos.
 * Esta classe implementa o padrão Repository do MVVM e centraliza
 * todas as operações relacionadas a dados de contratos.
 */
class ContractRepository {
    
    /**
     * Busca todos os contratos disponíveis.
     * Por enquanto usa dados mockados, mas no futuro chamará a API.
     */
    suspend fun getAllContratos(): List<Contrato> = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Buscando todos os contratos")
        
        // Simula um delay de rede
        delay(500)
        
        // TODO: No futuro, chamar a API real aqui
        return@withContext getMockContratos()
    }
    
    /**
     * Busca contratos por status (ativos, pendentes, etc).
     */
    suspend fun getContratosByStatus(status: String): List<Contrato> = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Buscando contratos com status: $status")
        
        delay(300)
        
        // Filtra os contratos pelo status
        return@withContext getMockContratos().filter { it.status == status }
    }
    
    /**
     * Busca contratos por texto (busca em diferentes campos)
     */
    suspend fun searchContratos(query: String): List<Contrato> = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Buscando contratos com query: $query")
        
        delay(300)
        
        // Simula busca em vários campos
        val termoBusca = query.lowercase()
        return@withContext getMockContratos().filter {
            it.title.lowercase().contains(termoBusca) ||
            it.contractNumber.lowercase().contains(termoBusca) ||
            it.client.lowercase().contains(termoBusca) ||
            it.description?.lowercase()?.contains(termoBusca) == true
        }
    }
    
    /**
     * Adiciona um novo contrato
     */
    suspend fun addContrato(contrato: Contrato): Contrato = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Adicionando contrato: ${contrato.contractNumber}")
        
        delay(500)
        
        // Simula adição de contrato
        return@withContext contrato
    }
    
    /**
     * Atualiza um contrato existente
     */
    suspend fun updateContrato(contrato: Contrato): Boolean = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Atualizando contrato: ${contrato.contractNumber}")
        
        delay(500)
        
        // Simula atualização de contrato
        return@withContext true
    }
    
    /**
     * Remove um contrato
     */
    suspend fun deleteContrato(id: String): Boolean = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Removendo contrato com ID: $id")
        
        delay(500)
        
        // Simula remoção de contrato
        return@withContext true
    }
    
    /**
     * Busca contratos por projeto
     */
    suspend fun getContractsByProject(projectId: String): List<ProjectContractItem> = withContext(Dispatchers.IO) {
        LogUtils.debug("ContractRepository", "Buscando contratos do projeto: $projectId")
        
        delay(300)
        
        // Filtra os contratos pelo projeto
        val contratos = getMockContratos().filter { it.projectId == projectId }
        
        // Converte para ProjectContractItem
        return@withContext contratos.map {
            ProjectContractItem(
                id = it.id,
                projectId = it.projectId,
                name = it.title,
                description = it.description ?: "",
                value = it.value,
                date = it.startDate,
                status = it.status,
                type = "contract"
            )
        }
    }
    
    /**
     * Busca contratos por projeto e status
     */
    suspend fun getContractsByProjectAndStatus(projectId: String, status: String): List<ProjectContractItem> = 
        withContext(Dispatchers.IO) {
            LogUtils.debug("ContractRepository", "Buscando contratos do projeto $projectId com status $status")
            
            delay(300)
            
            // Filtra os contratos pelo projeto e status
            val contratos = getMockContratos().filter { it.projectId == projectId && it.status == status }
            
            // Converte para ProjectContractItem
            return@withContext contratos.map {
                ProjectContractItem(
                    id = it.id,
                    projectId = it.projectId,
                    name = it.title,
                    description = it.description ?: "",
                    value = it.value,
                    date = it.startDate,
                    status = it.status,
                    type = "contract"
                )
            }
        }
    
    /**
     * Dados mockados para testes enquanto não integra com a API
     */
    private fun getMockContratos(): List<Contrato> {
        return listOf(
            Contrato(
                id = "1",
                projectId = "PROJ-001",
                contractNumber = "CT-2023-001",
                title = "Desenvolvimento de Website Corporativo",
                client = "Empresa ABC Ltda",
                startDate = "01/03/2023",
                endDate = "30/06/2023",
                value = 25000.0,
                status = "active",
                description = "Desenvolvimento completo de website responsivo com área administrativa"
            ),
            Contrato(
                id = "2",
                projectId = "PROJ-002",
                contractNumber = "CT-2023-002",
                title = "Manutenção de Sistema Legado",
                client = "Tech Solutions",
                startDate = "15/01/2023",
                endDate = "15/01/2024",
                value = 36000.0,
                status = "active",
                description = "Contrato anual para manutenção e suporte de sistema ERP legado"
            ),
            Contrato(
                id = "3",
                projectId = "PROJ-003",
                contractNumber = "CT-2023-003",
                title = "Desenvolvimento de Aplicativo Mobile",
                client = "StartupX",
                startDate = "10/04/2023",
                endDate = "10/08/2023",
                value = 45000.0,
                status = "pending",
                description = "Aplicativo para Android e iOS para gestão de tarefas"
            ),
            Contrato(
                id = "4",
                projectId = "PROJ-004",
                contractNumber = "CT-2023-004",
                title = "Consultoria em Segurança da Informação",
                client = "Banco Financial",
                startDate = "05/02/2023",
                endDate = "30/04/2023",
                value = 18500.0,
                status = "completed",
                description = "Auditoria de segurança e implementação de melhorias"
            ),
            Contrato(
                id = "5",
                projectId = "PROJ-005",
                contractNumber = "CT-2023-005",
                title = "Automação de Processos",
                client = "Indústria Mecânica Ltda",
                startDate = "20/03/2023",
                endDate = "20/05/2023",
                value = 15000.0,
                status = "cancelled",
                description = "Projeto cancelado devido a mudanças nas prioridades do cliente"
            )
        )
    }
} 