package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.models.Empresa
import com.example.alg_gestao_02.utils.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * Repositório responsável por operações relacionadas a empresas
 */
class EmpresaRepository {
    
    /**
     * Obtém todas as empresas do sistema
     */
    suspend fun getAllEmpresas(): List<Empresa> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Buscando todas as empresas")
            // Simular chamada de rede
            delay(800)
            return@withContext getMockEmpresas()
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro ao buscar empresas: ${e.message}")
            throw e
        }
    }
    
    /**
     * Busca empresas pelo nome, razão social ou nome fantasia
     */
    suspend fun searchEmpresas(query: String): List<Empresa> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Buscando empresas com query: $query")
            // Simular chamada de rede
            delay(500)
            
            val termoLowerCase = query.lowercase()
            return@withContext getMockEmpresas().filter { empresa ->
                empresa.nome.lowercase().contains(termoLowerCase) ||
                empresa.razaoSocial.lowercase().contains(termoLowerCase) ||
                empresa.nomeFantasia.lowercase().contains(termoLowerCase) ||
                empresa.cnpj.replace(Regex("[^0-9]"), "").contains(query.replace(Regex("[^0-9]"), ""))
            }
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro na busca por empresas: ${e.message}")
            throw e
        }
    }
    
    /**
     * Obtém empresas por status
     */
    suspend fun getEmpresasByStatus(status: String): List<Empresa> = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Buscando empresas com status: $status")
            // Simular chamada de rede
            delay(500)
            
            return@withContext getMockEmpresas().filter { 
                it.status.equals(status, ignoreCase = true)
            }
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro ao filtrar empresas por status: ${e.message}")
            throw e
        }
    }
    
    /**
     * Cria uma nova empresa
     */
    suspend fun criarEmpresa(empresa: Empresa): Empresa = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Criando empresa: ${empresa.nome}")
            // Simular chamada de rede
            delay(1000)
            
            // Aqui apenas simulamos que criamos a empresa e retornamos a mesma com ID
            return@withContext empresa.copy(id = UUID.randomUUID().toString())
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro ao criar empresa: ${e.message}")
            throw e
        }
    }
    
    /**
     * Atualiza uma empresa existente
     */
    suspend fun atualizarEmpresa(empresa: Empresa): Empresa = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Atualizando empresa: ${empresa.id}")
            // Simular chamada de rede
            delay(1000)
            
            // Aqui apenas simulamos que atualizamos a empresa e retornamos a mesma
            return@withContext empresa
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro ao atualizar empresa: ${e.message}")
            throw e
        }
    }
    
    /**
     * Exclui uma empresa
     */
    suspend fun excluirEmpresa(id: String): Boolean = withContext(Dispatchers.IO) {
        try {
            LogUtils.debug("EmpresaRepository", "Excluindo empresa: $id")
            // Simular chamada de rede
            delay(800)
            
            // Aqui apenas simulamos que excluímos a empresa
            return@withContext true
        } catch (e: Exception) {
            LogUtils.error("EmpresaRepository", "Erro ao excluir empresa: ${e.message}")
            throw e
        }
    }
    
    /**
     * Retorna dados mock para testes e desenvolvimento
     */
    private fun getMockEmpresas(): List<Empresa> {
        return listOf(
            Empresa(
                id = "1",
                nome = "Tech Solutions Brasil",
                nomeFantasia = "Tech Solutions",
                razaoSocial = "Tech Solutions Tecnologia Brasil Ltda",
                cnpj = "45.678.901/0001-23",
                inscricaoEstadual = "456789012",
                telefone = "(31) 4444-5555",
                email = "contato@techsolutions.com.br",
                endereco = "Rua dos Programadores, 123, Belo Horizonte, MG",
                ramoAtividade = "Tecnologia",
                observacoes = "Parceiro estratégico",
                status = "ativo"
            ),
            Empresa(
                id = "2",
                nome = "Construções Seguras",
                nomeFantasia = "Seguras Construções",
                razaoSocial = "Construções Seguras Engenharia Ltda",
                cnpj = "78.912.345/0001-67",
                inscricaoEstadual = "789123456",
                telefone = "(11) 7777-8888",
                email = "contato@segurasconstrucoes.com.br",
                endereco = "Av. das Obras, 500, São Paulo, SP",
                ramoAtividade = "Construção Civil",
                observacoes = "Especializada em obras residenciais",
                status = "ativo"
            ),
            Empresa(
                id = "3",
                nome = "Distribuidora Rápida",
                nomeFantasia = "Distri Rápida",
                razaoSocial = "Distribuidora Rápida de Alimentos Ltda",
                cnpj = "23.456.789/0001-01",
                inscricaoEstadual = "234567890",
                telefone = "(21) 9999-0000",
                email = "contato@distrirapida.com.br",
                endereco = "Rua do Comércio, 321, Rio de Janeiro, RJ",
                ramoAtividade = "Distribuição de Alimentos",
                status = "ativo"
            ),
            Empresa(
                id = "4",
                nome = "Consultoria Financeira Expert",
                nomeFantasia = "Expert Finance",
                razaoSocial = "Expert Consultoria Financeira Ltda",
                cnpj = "34.567.890/0001-12",
                inscricaoEstadual = "345678901",
                telefone = "(51) 3333-2222",
                email = "contato@expertfinance.com.br",
                endereco = "Av. dos Bancos, 100, Porto Alegre, RS",
                ramoAtividade = "Consultoria Financeira",
                status = "inativo"
            ),
            Empresa(
                id = "5",
                nome = "Transportes Eficientes",
                nomeFantasia = "Trans Eficiente",
                razaoSocial = "Transportes Eficientes Logística Ltda",
                cnpj = "56.789.012/0001-34",
                inscricaoEstadual = "567890123",
                telefone = "(41) 5555-6666",
                email = "contato@transeficiente.com.br",
                endereco = "Rodovia Principal, km 10, Curitiba, PR",
                ramoAtividade = "Transporte e Logística",
                observacoes = "Frota própria de 50 caminhões",
                status = "ativo"
            )
        )
    }
} 