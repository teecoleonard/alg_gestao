package com.example.alg_gestao_02.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.utils.LogUtils

class DashboardFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        LogUtils.debug("DashboardFragment", "Inicializando fragmento do dashboard")
        
        setupListeners(view)
    }
    
    private fun setupListeners(view: View) {
        // Configurar botão de busca
        view.findViewById<View>(R.id.cardSearch)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Busca clicada")
            Toast.makeText(context, "Função de busca em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão "ver todos" dos insights
        view.findViewById<View>(R.id.tvViewAll)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Ver todos os insights clicado")
            Toast.makeText(context, "Ver todos os insights em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Card de Equipamentos
        view.findViewById<View>(R.id.cardEquipamentos)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de equipamentos clicado")
            
            // Navegar para a página de equipamentos usando o NavController
            findNavController().navigate(R.id.equipamentosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_equipamentos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Contratos
        view.findViewById<View>(R.id.cardWorkers)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de contratos clicado")
            
            // Navegar para a página de contratos usando o NavController
            findNavController().navigate(R.id.contratosFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_contratos)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Clientes
        view.findViewById<View>(R.id.cardTasks)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de clientes clicado")
            
            // Navegar para a página de clientes usando o NavController
            findNavController().navigate(R.id.clientesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_clientes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
        
        // Card de Devoluções
        view.findViewById<View>(R.id.cardDevolucoes)?.setOnClickListener {
            LogUtils.debug("DashboardFragment", "Card de devoluções clicado")
            
            // Navegar para a página de devoluções usando o NavController
            findNavController().navigate(R.id.devolucoesFragment)
            
            // Atualizar item selecionado no menu de navegação
            try {
                requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                    .setCheckedItem(R.id.nav_devolucoes)
            } catch (e: Exception) {
                LogUtils.error("DashboardFragment", "Erro ao atualizar menu: ${e.message}")
            }
        }
    }
} 