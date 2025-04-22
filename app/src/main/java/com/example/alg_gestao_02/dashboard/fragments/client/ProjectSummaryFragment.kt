package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.adapter.ProjectContractsAdapter
import com.example.alg_gestao_02.dashboard.fragments.client.model.ProjectContractItem
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.button.MaterialButton

class ProjectSummaryFragment : Fragment() {
    
    private lateinit var contractsAdapter: ProjectContractsAdapter
    private val contractsList = mutableListOf<ProjectContractItem>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_project_summary, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ProjectSummaryFragment", "Inicializando fragmento de sumário do projeto")
        
        setupRecyclerView(view)
        setupListeners(view)
        loadMockData()
    }
    
    private fun setupRecyclerView(view: View) {
        val rvContracts = view.findViewById<RecyclerView>(R.id.rvContracts)
        
        contractsAdapter = ProjectContractsAdapter(emptyList()) { contract ->
            LogUtils.debug("ProjectSummaryFragment", "Contrato clicado: ${contract.name}")
            Toast.makeText(context, "Contrato selecionado: ${contract.name}", Toast.LENGTH_SHORT).show()
        }
        
        rvContracts.layoutManager = LinearLayoutManager(context)
        rvContracts.adapter = contractsAdapter
    }
    
    private fun setupListeners(view: View) {
        // Configurar busca
        view.findViewById<View>(R.id.cardSearch).setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Busca clicada")
            Toast.makeText(context, "Busca em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Configurar filtro
        view.findViewById<View>(R.id.cardFilter).setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Filtro clicado")
            Toast.makeText(context, "Filtro em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão de pagamento
        view.findViewById<MaterialButton>(R.id.btnPayment).setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Botão de pagamento clicado")
            Toast.makeText(context, "Função de pagamento em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        // Botão de devedor
        view.findViewById<MaterialButton>(R.id.btnDebt).setOnClickListener {
            LogUtils.debug("ProjectSummaryFragment", "Botão de devedor clicado")
            Toast.makeText(context, "Função de devedor em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun loadMockData() {
        // Dados simulados para testes
        contractsList.clear()
        contractsList.addAll(
            listOf(
                ProjectContractItem(
                    id = "1",
                    name = "Mohil Prajapati",
                    description = "Description here...",
                    value = 50000.0,
                    date = "30 Sep 2024, 07:23 PM",
                    status = "active",
                    type = "debt"
                ),
                ProjectContractItem(
                    id = "2",
                    name = "Freyja Hooper",
                    description = "Description here...",
                    value = 30000.0,
                    date = "30 Sep 2024, 07:23 PM",
                    status = "active",
                    type = "debt"
                ),
                ProjectContractItem(
                    id = "3",
                    name = "Alexander Gardner",
                    description = "Description here...",
                    value = 1150000.0,
                    date = "30 Sep 2024, 07:23 PM",
                    status = "active",
                    type = "payment"
                ),
                ProjectContractItem(
                    id = "4",
                    name = "Aiden Schneider",
                    description = "Description here...",
                    value = 10000.0,
                    date = "30 Sep 2024, 07:23 PM",
                    status = "active",
                    type = "debt"
                ),
                ProjectContractItem(
                    id = "5",
                    name = "Eliana Acosta",
                    description = "Description here...",
                    value = 1350000.0,
                    date = "30 Sep 2024, 07:23 PM",
                    status = "active",
                    type = "payment"
                )
            )
        )
        
        contractsAdapter.updateData(contractsList)
    }
    
    companion object {
        fun newInstance(): ProjectSummaryFragment {
            return ProjectSummaryFragment()
        }
    }
} 