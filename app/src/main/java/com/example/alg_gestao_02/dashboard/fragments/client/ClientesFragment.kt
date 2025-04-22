package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.adapter.ClientesAdapter
import com.example.alg_gestao_02.dashboard.fragments.client.model.Cliente
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.TextView

class ClientesFragment : Fragment() {
    
    private lateinit var clientesAdapter: ClientesAdapter
    private val clientesList = mutableListOf<Cliente>()
    
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerClientes: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var fabAddCliente: FloatingActionButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_clientes, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("ClientesFragment", "Inicializando fragmento de clientes")
        
        // Inicializar views
        initViews(view)
        setupRecyclerView()
        setupListeners()
        loadMockData()
    }
    
    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        recyclerClientes = view.findViewById(R.id.rvClientes)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        fabAddCliente = view.findViewById(R.id.fabAddCliente)
        
        // Inicialmente, ocultar o layout vazio e mostrar o progressBar
        layoutEmpty.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
    }
    
    private fun setupRecyclerView() {
        clientesAdapter = ClientesAdapter(
            emptyList(),
            onItemClick = { cliente ->
                LogUtils.debug("ClientesFragment", "Cliente clicado: ${cliente.nome}")
                Toast.makeText(context, "Cliente: ${cliente.nome}", Toast.LENGTH_SHORT).show()
            },
            onOptionsClick = { cliente, buttonView ->
                showClienteOptions(cliente, buttonView)
            }
        )
        
        recyclerClientes.layoutManager = LinearLayoutManager(context)
        recyclerClientes.adapter = clientesAdapter
    }
    
    private fun setupListeners() {
        fabAddCliente.setOnClickListener {
            LogUtils.debug("ClientesFragment", "Botão adicionar cliente clicado")
            Toast.makeText(context, "Adicionar novo cliente", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showClienteOptions(cliente: Cliente, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_cliente_options, popup.menu)
        
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    Toast.makeText(context, "Editar: ${cliente.nome}", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.action_delete -> {
                    Toast.makeText(context, "Excluir: ${cliente.nome}", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        
        popup.show()
    }
    
    private fun loadMockData() {
        // Simular carregamento
        progressBar.visibility = View.VISIBLE
        recyclerClientes.visibility = View.GONE
        layoutEmpty.visibility = View.GONE
        
        // Simulação de delay de rede
        view?.postDelayed({
            clientesList.clear()
            clientesList.addAll(
                listOf(
                    Cliente(
                        id = "1",
                        nome = "Empresa XYZ Ltda",
                        documento = "12.345.678/0001-90",
                        telefone = "(11) 3333-4444",
                        email = "contato@empresaxyz.com.br",
                        endereco = "Av. Paulista, 1000, São Paulo, SP",
                        tipo = "PJ"
                    ),
                    Cliente(
                        id = "2",
                        nome = "João Silva",
                        documento = "123.456.789-00",
                        telefone = "(11) 98765-4321",
                        email = "joao.silva@email.com",
                        endereco = "Rua das Flores, 123, São Paulo, SP",
                        tipo = "PF"
                    ),
                    Cliente(
                        id = "3",
                        nome = "Comércio ABC S.A.",
                        documento = "98.765.432/0001-10",
                        telefone = "(11) 2222-3333",
                        email = "comercial@abc.com.br",
                        endereco = "Av. Brasil, 500, Rio de Janeiro, RJ",
                        tipo = "PJ"
                    ),
                    Cliente(
                        id = "4",
                        nome = "Maria Oliveira",
                        documento = "987.654.321-00",
                        telefone = "(21) 97654-3210",
                        email = "maria.oliveira@email.com",
                        endereco = "Rua do Comércio, 45, Belo Horizonte, MG",
                        tipo = "PF"
                    )
                )
            )
            
            // Atualizar UI
            progressBar.visibility = View.GONE
            
            if (clientesList.isEmpty()) {
                showEmptyState("Nenhum cliente encontrado")
            } else {
                recyclerClientes.visibility = View.VISIBLE
                layoutEmpty.visibility = View.GONE
                clientesAdapter.updateData(clientesList)
            }
        }, 500) // Delay de 500ms para simular carregamento
    }
    
    private fun showEmptyState(message: String) {
        progressBar.visibility = View.GONE
        recyclerClientes.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        
        // Configurar a mensagem
        val tvMessage = layoutEmpty.findViewById<TextView>(R.id.tvEmptyMessage)
        tvMessage?.text = message
    }
} 