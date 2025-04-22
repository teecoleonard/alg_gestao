package com.example.alg_gestao_02.dashboard.fragments.contract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.contract.adapter.ContratosAdapter
import com.example.alg_gestao_02.dashboard.fragments.contract.model.Contrato
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ContratosFragment : Fragment() {
    
    private lateinit var contratosAdapter: ContratosAdapter
    private val contratosList = mutableListOf<Contrato>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_contratos, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ContratosFragment", "Inicializando fragmento de contratos")
        
        setupRecyclerView(view)
        setupListeners(view)
        loadMockData()
    }
    
    private fun setupRecyclerView(view: View) {
        val rvContratos = view.findViewById<RecyclerView>(R.id.rvContratos)
        
        contratosAdapter = ContratosAdapter(
            emptyList(),
            onItemClick = { contrato ->
                LogUtils.debug("ContratosFragment", "Contrato clicado: ${contrato.contractNumber}")
                Toast.makeText(context, "Contrato #${contrato.contractNumber}", Toast.LENGTH_SHORT).show()
            }
        )
        
        rvContratos.layoutManager = LinearLayoutManager(context)
        rvContratos.adapter = contratosAdapter
    }
    
    private fun setupListeners(view: View) {
        val fabAddContrato = view.findViewById<FloatingActionButton>(R.id.fabAddContrato)
        fabAddContrato.setOnClickListener {
            LogUtils.debug("ContratosFragment", "Botão adicionar contrato clicado")
            Toast.makeText(context, "Adicionar novo contrato", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadMockData() {
        contratosList.clear()
        contratosList.addAll(
            listOf(
                Contrato(
                    id = "1",
                    contractNumber = "2023001",
                    companyName = "Empresa XYZ Ltda",
                    value = 50000.0,
                    startDate = "01/02/2023",
                    endDate = "01/02/2024",
                    status = "active"
                ),
                Contrato(
                    id = "2",
                    contractNumber = "2023002",
                    companyName = "Tecnologia ABC S.A.",
                    value = 75000.0,
                    startDate = "15/03/2023",
                    endDate = "15/03/2025",
                    status = "pending"
                ),
                Contrato(
                    id = "3",
                    contractNumber = "2022056",
                    companyName = "Comércio Rápido Ltda",
                    value = 35000.0,
                    startDate = "10/12/2022",
                    endDate = "10/12/2023",
                    status = "completed"
                ),
                Contrato(
                    id = "4",
                    contractNumber = "2022045",
                    companyName = "Indústria Nacional S.A.",
                    value = 120000.0,
                    startDate = "05/09/2022",
                    endDate = "05/09/2024",
                    status = "cancelled"
                )
            )
        )
        
        contratosAdapter.updateData(contratosList)
    }
} 