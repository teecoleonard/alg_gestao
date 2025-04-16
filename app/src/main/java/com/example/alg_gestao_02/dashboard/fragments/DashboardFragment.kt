package com.example.alg_gestao_02.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.databinding.FragmentDashboardBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var sessionManager: SessionManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("DashboardFragment", "Inicializando fragmento do dashboard")
        
        sessionManager = SessionManager(requireContext())
        
        // Configurações iniciais
        setupDashboard()
    }
    
    private fun setupDashboard() {
        // Nome do usuário logado
        val userName = sessionManager.getUserName() ?: "Usuário"
        binding.tvWelcome.text = "Bem-vindo, $userName!"
        
        // Para a demonstração, usaremos dados simulados
        setupCards()
    }
    
    private fun setupCards() {
        // Dados simulados para demonstração
        binding.apply {
            cardClientes.tvCardCount.text = "42"
            cardClientes.tvCardTitle.text = "Clientes"
            
            cardEmpresas.tvCardCount.text = "15"
            cardEmpresas.tvCardTitle.text = "Empresas"
            
            cardContratos.tvCardCount.text = "28"
            cardContratos.tvCardTitle.text = "Contratos"
            
            cardEquipamentos.tvCardCount.text = "112"
            cardEquipamentos.tvCardTitle.text = "Equipamentos"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 