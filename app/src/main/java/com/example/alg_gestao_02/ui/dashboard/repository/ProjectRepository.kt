package com.example.alg_gestao_02.ui.dashboard.repository

import com.example.alg_gestao_02.dashboard.fragments.dashboard.model.Project
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Repositório responsável por gerenciar dados de projetos.
 * Nota: Esta implementação simula acesso a dados. Em produção, isto se conectaria a uma API ou banco de dados.
 */
class ProjectRepository {
    
    // Lista simulada de projetos para testes
    private val mockProjects = mutableListOf<Project>().apply {
        add(
            Project(
                id = "1",
                name = "Monte Carlo Casino",
                location = "Surat, Gujarat, Índia",
                status = "in_progress",
                budget = "R$ 5.481.245,59",
                expenses = "R$ 1.842.195,40",
                startDate = "30/09/2024",
                endDate = "01/01/2026",
                imageUrl = ""
            )
        )
        add(
            Project(
                id = "2",
                name = "Resort Paradise",
                location = "Rio de Janeiro, Brasil",
                status = "in_progress",
                budget = "R$ 3.781.245,59",
                expenses = "R$ 1.342.195,40",
                startDate = "15/08/2024",
                endDate = "25/12/2025",
                imageUrl = ""
            )
        )
        add(
            Project(
                id = "3",
                name = "Torre Empresarial",
                location = "São Paulo, Brasil",
                status = "planning",
                budget = "R$ 8.721.845,00",
                expenses = "R$ 345.921,15",
                startDate = "10/10/2024",
                endDate = "30/06/2026",
                imageUrl = ""
            )
        )
        add(
            Project(
                id = "4",
                name = "Shopping Center Norte",
                location = "Porto Alegre, Brasil",
                status = "in_progress",
                budget = "R$ 12.845.654,20",
                expenses = "R$ 4.571.542,90",
                startDate = "02/05/2024",
                endDate = "15/11/2025",
                imageUrl = ""
            )
        )
    }
    
    /**
     * Obtém todos os projetos
     * @return Lista de projetos
     */
    suspend fun getAllProjects(): List<Project> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(800)
        return@withContext mockProjects
    }
    
    /**
     * Obtém projetos filtrados por status
     * @param status Status para filtrar (in_progress, planning, completed)
     * @return Lista de projetos filtrados pelo status
     */
    suspend fun getProjectsByStatus(status: String): List<Project> = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(600)
        return@withContext mockProjects.filter { it.status == status }
    }
    
    /**
     * Obtém um projeto pelo ID
     * @param projectId ID do projeto
     * @return O projeto encontrado ou null se não existir
     */
    suspend fun getProjectById(projectId: String): Project? = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(400)
        return@withContext mockProjects.find { it.id == projectId }
    }
    
    /**
     * Adiciona um novo projeto
     * @param project Projeto a ser adicionado
     * @return O projeto adicionado com ID gerado
     */
    suspend fun addProject(project: Project): Project = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(600)
        
        val newId = (mockProjects.maxOfOrNull { it.id.toIntOrNull() ?: 0 } ?: 0) + 1
        val newProject = project.copy(id = newId.toString())
        mockProjects.add(newProject)
        return@withContext newProject
    }
    
    /**
     * Atualiza um projeto existente
     * @param project Projeto com informações atualizadas
     * @return true se a operação foi bem sucedida
     */
    suspend fun updateProject(project: Project): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(600)
        
        val index = mockProjects.indexOfFirst { it.id == project.id }
        if (index >= 0) {
            mockProjects[index] = project
            return@withContext true
        }
        return@withContext false
    }
    
    /**
     * Exclui um projeto pelo ID
     * @param projectId ID do projeto
     * @return true se a operação foi bem sucedida
     */
    suspend fun deleteProject(projectId: String): Boolean = withContext(Dispatchers.IO) {
        // Simula uma operação de rede
        delay(500)
        
        val initialSize = mockProjects.size
        mockProjects.removeIf { it.id == projectId }
        
        return@withContext initialSize > mockProjects.size
    }
} 