package com.example.alg_gestao_02.dashboard.fragments.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.databinding.FragmentEmpresasBinding
import com.example.alg_gestao_02.utils.LogUtils

class EmpresasFragment : Fragment() {
    private var _binding: FragmentEmpresasBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmpresasBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("EmpresasFragment", "Inicializando fragmento de empresas")
        
        setupUI()
    }
    
    private fun setupUI() {
        // Configurar o botão de adicionar
        binding.fabAddEmpresa.setOnClickListener {
            LogUtils.debug("EmpresasFragment", "Botão adicionar empresa pressionado")
            Toast.makeText(requireContext(), "Adicionar empresa (em desenvolvimento)", Toast.LENGTH_SHORT).show()
        }
        
        // Exibir estado vazio (empty state) por enquanto
        showEmptyState()
    }
    
    private fun showEmptyState() {
        binding.apply {
            progressBar.visibility = View.GONE
            recyclerEmpresas.visibility = View.GONE
            layoutEmpty.root.visibility = View.VISIBLE
            layoutEmpty.tvEmptyMessage.text = "Não há empresas cadastradas"
            layoutEmpty.btnEmptyAction.text = "Adicionar Empresa"
            layoutEmpty.btnEmptyAction.setOnClickListener {
                LogUtils.debug("EmpresasFragment", "Botão adicionar empresa do empty state pressionado")
                Toast.makeText(requireContext(), "Adicionar empresa (em desenvolvimento)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 