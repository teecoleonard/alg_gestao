package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.databinding.FragmentClientesBinding
import com.example.alg_gestao_02.utils.LogUtils

class ClientesFragment : Fragment() {
    private var _binding: FragmentClientesBinding? = null
    private val binding get() = _binding!!
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ClientesFragment", "Inicializando fragmento de clientes")
        
        setupUI()
    }
    
    private fun setupUI() {
        // Configurar o botão de adicionar
        binding.fabAddCliente.setOnClickListener {
            LogUtils.debug("ClientesFragment", "Botão adicionar cliente pressionado")
            Toast.makeText(requireContext(), "Adicionar cliente (em desenvolvimento)", Toast.LENGTH_SHORT).show()
        }
        
        // Exibir estado vazio (empty state) por enquanto
        showEmptyState()
    }
    
    private fun showEmptyState() {
        binding.apply {
            progressBar.visibility = View.GONE
            recyclerClientes.visibility = View.GONE
            layoutEmpty.root.visibility = View.VISIBLE
            layoutEmpty.tvEmptyMessage.text = "Não há clientes cadastrados"
            layoutEmpty.btnEmptyAction.text = "Adicionar Cliente"
            layoutEmpty.btnEmptyAction.setOnClickListener {
                LogUtils.debug("ClientesFragment", "Botão adicionar cliente do empty state pressionado")
                Toast.makeText(requireContext(), "Adicionar cliente (em desenvolvimento)", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 