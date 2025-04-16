package com.example.alg_gestao_02.dashboard.fragments.contract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.databinding.FragmentContratosBinding
import com.example.alg_gestao_02.utils.LogUtils

class ContratosFragment : Fragment() {
    private var _binding: FragmentContratosBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContratosBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ContratosFragment", "Inicializando fragmento de contratos")
        
        setupUI()
    }
    
    private fun setupUI() {
        // Mostrar estado vazio para este exemplo
        binding.layoutEmpty.apply {
            root.visibility = View.VISIBLE
            tvEmptyMessage.text = "Módulo de Contratos em desenvolvimento"
            btnEmptyAction.visibility = View.GONE
        }
        
        // Esconder outras views
        binding.apply {
            progressBar.visibility = View.GONE
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 