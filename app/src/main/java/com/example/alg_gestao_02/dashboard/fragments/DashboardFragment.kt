package com.example.alg_gestao_02.dashboard.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.ProjectDetailActivity
import com.example.alg_gestao_02.dashboard.fragments.contract.ContratosFragment
import com.example.alg_gestao_02.dashboard.fragments.dashboard.adapter.ProjectsAdapter
import com.example.alg_gestao_02.dashboard.fragments.dashboard.model.Project
import com.example.alg_gestao_02.utils.LogUtils

class DashboardFragment : Fragment() {
    
    private lateinit var projectsAdapter: ProjectsAdapter
    private val projectsList = mutableListOf<Project>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("DashboardFragment", "Inicializando fragmento do dashboard")
        
        setupRecyclerView(view)
        setupListeners(view)
        loadMockData()
    }
    
    private fun setupRecyclerView(view: View) {
        val rvProjects = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvProjects)
        
        projectsAdapter = ProjectsAdapter(emptyList()) { project ->
            LogUtils.debug("DashboardFragment", "Projeto clicado: ${project.name}")
            // Toast será exibido pela ação no adapter
        }
        
        rvProjects.layoutManager = LinearLayoutManager(context)
        rvProjects.adapter = projectsAdapter
    }
    
    private fun setupListeners(view: View) {
        // Configurar botão de busca
        view.findViewById<View>(R.id.cardSearch).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Busca clicada")
            Toast.makeText(context, "Função de busca em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão "ver todos" dos insights
        view.findViewById<View>(R.id.tvViewAll).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Ver todos os insights clicado")
            Toast.makeText(context, "Ver todos os insights em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Cards de insights
        view.findViewById<View>(R.id.cardMaterials).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de materiais clicado")
            Toast.makeText(context, "Lista de materiais em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.cardWorkers).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de funcionários clicado")
            Toast.makeText(context, "Lista de funcionários em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.cardTasks).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de tarefas clicado")
            Toast.makeText(context, "Lista de tarefas em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão para contratos
        view.findViewById<View>(R.id.fabContratos).setOnClickListener {
            LogUtils.debug("DashboardFragment", "Botão contratos clicado")
            
            // Navegar para a página de contratos
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, ContratosFragment())
            transaction.addToBackStack(null)
            transaction.commit()
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
    }
    
    private fun loadMockData() {
        // Dados simulados para testes
        projectsList.clear()
        projectsList.addAll(
            listOf(
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
                ),
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
        )
        
        projectsAdapter.updateData(projectsList)
    }
} 