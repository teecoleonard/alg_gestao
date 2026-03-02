package com.example.alg_gestao_02.ui.client

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.repository.ClienteRepository
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.data.repository.DevolucaoRepository
import com.example.alg_gestao_02.data.repository.EquipamentoContratoRepository
import com.example.alg_gestao_02.databinding.FragmentClientDetailsBinding
import com.example.alg_gestao_02.ui.client.viewmodel.ClientDetailsViewModel
import com.example.alg_gestao_02.ui.contrato.CadastroContratoDialogFragment
import com.example.alg_gestao_02.ui.contrato.ContratoDetailsDialogFragment
import com.example.alg_gestao_02.ui.contrato.adapter.ContratosAdapter
import com.example.alg_gestao_02.ui.devolucao.DevolucaoDetailsDialogFragment
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import com.example.alg_gestao_02.utils.SessionManager
import com.example.alg_gestao_02.utils.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Fragment para exibir os detalhes do cliente
 */
class ClientDetailsFragment : Fragment(), ContratoDetailsDialogFragment.OnEditRequestListener {

    private var _binding: FragmentClientDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ClientDetailsViewModel
    private lateinit var contratosAdapter: ContratosAdapter
    
    private var clienteId: Int = 0

    companion object {
        private const val ARG_CLIENTE_ID = "cliente_id"
        
        fun newInstance(clienteId: Int): ClientDetailsFragment {
            return ClientDetailsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CLIENTE_ID, clienteId)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        clienteId = arguments?.getInt(ARG_CLIENTE_ID, 0) ?: 0
        if (clienteId == 0) {
            LogUtils.error("ClientDetailsFragment", "ID do cliente não fornecido")
            findNavController().navigateUp()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClientDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        // Carrega os dados do cliente
        viewModel.carregarDetalhesCliente(clienteId)
    }

    private fun setupViewModel() {
        val clienteRepository = ClienteRepository()
        val contratoRepository = ContratoRepository()
        val devolucaoRepository = DevolucaoRepository()
        val equipamentoContratoRepository = EquipamentoContratoRepository()

        val factory = ViewModelFactory(
            clienteRepository = clienteRepository,
            contratoRepository = contratoRepository,
            devolucaoRepository = devolucaoRepository,
            equipamentoContratoRepository = equipamentoContratoRepository
        )

        viewModel = ViewModelProvider(this, factory)[ClientDetailsViewModel::class.java]
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.tvToolbarTitle.text = getString(R.string.cliente_detalhes)
    }

    private fun setupRecyclerView() {
        contratosAdapter = ContratosAdapter(
            contratos = emptyList(),
            onItemClick = { contrato ->
                showContratoDetails(contrato)
            },
            onMenuClick = { contrato, view ->
                // Usando menu_contract_options.xml
                val popup = PopupMenu(requireContext(), view)
                popup.menuInflater.inflate(R.menu.menu_contract_options, popup.menu)
                
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.menu_edit -> {
                            LogUtils.debug("ClientDetailsFragment", "Editar contrato selecionado: ${contrato.contratoNum}")
                            onEditRequested(contrato)
                            true
                        }
                        R.id.menu_delete -> {
                            LogUtils.debug("ClientDetailsFragment", "Excluir contrato selecionado: ${contrato.contratoNum}")
                            confirmarExclusao(contrato)
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        )

        binding.rvContracts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = contratosAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observa dados do cliente
        viewModel.cliente.observe(viewLifecycleOwner) { cliente ->
            updateClienteInfo(cliente)
        }

        // Observa contratos do cliente
        viewModel.contratos.observe(viewLifecycleOwner) { contratos ->
            updateContratosInfo(contratos)
        }

        // Observa devoluções relacionadas aos contratos do cliente
        viewModel.devolucoes.observe(viewLifecycleOwner) { _ ->
            updateDevolucoesInfo()
        }

        // Observa estado de carregamento
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observa erros
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotBlank()) {
                showError(errorMessage)
            }
        }
    }

    private fun setupListeners() {
        // Configura botão de ligar para o cliente
        binding.btnCall.setOnClickListener {
            val cliente = viewModel.cliente.value ?: return@setOnClickListener
            cliente.telefone?.let { telefone ->
                if (telefone.isNotBlank()) {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$telefone")
                    }
                    startActivity(intent)
                }
            }
        }

        // Configura botão de editar cliente
        binding.btnEdit.setOnClickListener {
            val cliente = viewModel.cliente.value ?: return@setOnClickListener
            val dialog = com.example.alg_gestao_02.ui.cliente.CadastroClienteDialogFragment.newInstance(cliente)
            dialog.setOnClienteSavedListener {
                // Recarrega os dados do cliente após edição
                viewModel.carregarDetalhesCliente(clienteId)
            }
            dialog.show(parentFragmentManager, "editar_cliente")
        }

        // Configura botão de tentar novamente em caso de erro
        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            viewModel.carregarDetalhesCliente(clienteId)
        }
        
        // Navegação para todos os contratos do cliente
        binding.btnViewAllContracts.setOnClickListener {
            navigateToContractsByClient()
        }
        
        // Navegação para todas as devoluções do cliente
        binding.btnViewAllReturns.setOnClickListener {
            navigateToReturnsByClient()
        }
        
        // Configura cliques nos cards de status de devolução
        binding.cardPendingReturns.setOnClickListener {
            val pendentes = viewModel.getDevolucoesByStatus("Pendente")
            showDevolucoesByStatus(pendentes, "Devoluções Pendentes")
        }
        
        binding.cardCompletedReturns.setOnClickListener {
            val concluidas = viewModel.getDevolucoesByStatus("Devolvido")
            showDevolucoesByStatus(concluidas, "Devoluções Concluídas")
        }
        
        binding.cardIssuesReturns.setOnClickListener {
            val problemas = viewModel.getDevolucoesByStatus("Problemas")
            showDevolucoesByStatus(problemas, "Devoluções com Problemas")
        }
    }

    private fun updateClienteInfo(cliente: Cliente) {
        binding.tvClientName.text = cliente.contratante
        binding.tvClientDocument.text = cliente.getDocumentoFormatado()
        binding.tvClientSecondaryDocument.text = cliente.getDocumentoSecundarioFormatado()
        binding.tvClientPhone.text = if (!cliente.telefone.isNullOrBlank()) "Telefone: ${cliente.telefone}" else "Telefone não cadastrado"
        binding.tvClientAddress.text = cliente.getEnderecoCompleto()
    }

    private fun updateContratosInfo(contratos: List<Contrato>) {
        val hasContratos = contratos.isNotEmpty()
        
        // Atualiza o adaptador com a lista de contratos
        contratosAdapter.updateData(contratos)
        
        // Atualiza o contador de contratos
        binding.tvContractsCount.text = contratos.size.toString()
        
        // Controla a visibilidade da lista vs estado vazio
        if (hasContratos) {
            binding.rvContracts.visibility = View.VISIBLE
            binding.tvEmptyContracts.visibility = View.GONE
        } else {
            binding.rvContracts.visibility = View.GONE
            binding.tvEmptyContracts.visibility = View.VISIBLE
        }
    }

    private fun updateDevolucoesInfo() {
        // Obtém as contagens de devoluções por status
        val (pendentes, concluidas, problemas) = viewModel.getStatusDevolucoesCount()
        
        // Atualiza os textos de contagem
        binding.tvPendingReturnsCount.text = pendentes.toString()
        binding.tvCompletedReturnsCount.text = concluidas.toString()
        binding.tvIssuesReturnsCount.text = problemas.toString()
        
        // Atualiza o contador total de devoluções
        val totalDevolucoes = pendentes + concluidas + problemas
        binding.tvReturnsCount.text = totalDevolucoes.toString()
    }

    private fun showContratoDetails(contrato: Contrato) {
        LogUtils.debug("ClientDetailsFragment", "Mostrando detalhes do contrato: ${contrato.id}")
        // Usar o mesmo método que o ContratosFragment usa
        viewLifecycleOwner.lifecycleScope.launch {
            // val contratoRepository = ContratoRepository() // Não utilizado
            val contratoViewModelFactory = com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory()
            val contratoViewModel = ViewModelProvider(this@ClientDetailsFragment, contratoViewModelFactory)[com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel::class.java]
            
            // Observar o resultado do carregamento do contrato detalhado
            contratoViewModel.contratoDetalhado.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is com.example.alg_gestao_02.ui.state.UiState.Success -> {
                        if (state.data != null) {
                            LogUtils.debug("ClientDetailsFragment", "Contrato com detalhes carregado: ${state.data.contratoNum}")
                            val dialog = ContratoDetailsDialogFragment.newInstance(state.data)
                            dialog.setOnEditRequestListener(this@ClientDetailsFragment)
                            dialog.show(parentFragmentManager, "detalhes_contrato")
                            
                            // Limpar observer após uso
                            contratoViewModel.contratoDetalhado.removeObservers(viewLifecycleOwner)
                        } else {
                            Toast.makeText(requireContext(), "Contrato não encontrado.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    is com.example.alg_gestao_02.ui.state.UiState.Error -> {
                        LogUtils.error("ClientDetailsFragment", "Erro ao carregar contrato: ${state.message}")
                        Toast.makeText(requireContext(), "Erro ao carregar detalhes do contrato", Toast.LENGTH_SHORT).show()
                        
                        // Limpar observer após uso
                        contratoViewModel.contratoDetalhado.removeObservers(viewLifecycleOwner)
                    }
                    else -> {
                        // Loading ou outros estados
                    }
                }
            }
            
            // Carregar o contrato usando o mesmo método do ContratosFragment
            contratoViewModel.carregarContratoComDetalhes(contrato.id)
        }
    }
    
    override fun onEditRequested(contrato: Contrato) {
        LogUtils.debug("ClientDetailsFragment", "Pedido de edição recebido para o contrato: ${contrato.contratoNum}")
        val dialog = CadastroContratoDialogFragment.newInstance(contrato)
        dialog.setOnContratoSavedListener {
            // Recarrega os dados do cliente e seus contratos após edição
            viewModel.carregarDetalhesCliente(clienteId)
        }
        dialog.show(parentFragmentManager, "editar_contrato_${contrato.id}")
    }

    private fun showDevolucoesByStatus(devolucoes: List<Devolucao>, titulo: String) {
        if (devolucoes.isEmpty()) {
            // Se não houver devoluções desse status, mostra uma mensagem mais informativa
            val mensagem = when {
                titulo.contains("Pendentes") -> "Não há devoluções pendentes para este cliente."
                titulo.contains("Concluídas") -> "Não há devoluções concluídas para este cliente."
                titulo.contains("Problemas") -> "Não há devoluções com problemas para este cliente."
                else -> "Não há devoluções com esse status para este cliente."
            }
            
            Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
            return
        }
        
        // Se houver apenas uma devolução, abre diretamente a tela de detalhes
        if (devolucoes.size == 1) {
            showDevolucaoDetails(devolucoes[0])
            return
        }
        
        // Mostrar diálogo com lista de devoluções para seleção
        val itemsArray = devolucoes.map {
            val equipName = it.resolverNomeEquipamento()
            val quantidadeInfo = "${it.quantidadeDevolvida}/${it.quantidadeContratada} unidades"
            val dataInfo = it.getDataPrevistaFormatada()
            "$equipName - $quantidadeInfo - Prev: $dataInfo"
        }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setItems(itemsArray) { _, position ->
                // Abrir o detalhamento da devolução selecionada
                showDevolucaoDetails(devolucoes[position])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun showDevolucaoDetails(devolucao: Devolucao) {
        LogUtils.debug("ClientDetailsFragment", "Mostrando detalhes da devolução: ${devolucao.id}")
        val dialog = DevolucaoDetailsDialogFragment.newInstance(devolucao)
        
        // Configurar o listener para processamento de devolução
        dialog.setOnProcessarRequestListener(object : DevolucaoDetailsDialogFragment.OnProcessarRequestListener {
            override fun onProcessarRequested(devolucao: Devolucao, quantidade: Int, status: String?, observacao: String?) {
                LogUtils.info("ClientDetailsFragment", "🚀 PROCESSAMENTO DE DEVOLUÇÃO SOLICITADO VIA CLIENTE")
                LogUtils.debug("ClientDetailsFragment", "Devolução ID: ${devolucao.id}, Quantidade: $quantidade, Status: $status")
                
                // Criar ViewModel de devoluções para processar a devolução
                val apiService = com.example.alg_gestao_02.data.api.ApiClient.apiService
                val devolucaoRepository = DevolucaoRepository(apiService)
                val devolucaoViewModelFactory = com.example.alg_gestao_02.ui.devolucao.viewmodel.DevolucoesViewModelFactory(devolucaoRepository)
                val devolucaoViewModel = ViewModelProvider(requireActivity(), devolucaoViewModelFactory)[com.example.alg_gestao_02.ui.devolucao.viewmodel.DevolucoesViewModel::class.java]
                
                // Observar o resultado do processamento
                devolucaoViewModel.processamentoState.observe(viewLifecycleOwner) { state ->
                    when (state) {
                        is com.example.alg_gestao_02.ui.state.UiState.Loading -> {
                            LogUtils.debug("ClientDetailsFragment", "🔄 Processando devolução...")
                        }
                        
                        is com.example.alg_gestao_02.ui.state.UiState.Success -> {
                            LogUtils.info("ClientDetailsFragment", "✅ Devolução processada com sucesso!")
                            Toast.makeText(requireContext(), "Devolução processada com sucesso", Toast.LENGTH_SHORT).show()
                            
                            // Limpar observer e recarregar dados do cliente
                            devolucaoViewModel.processamentoState.removeObservers(viewLifecycleOwner)
                            devolucaoViewModel.clearProcessamentoState()
                            
                            // Recarregar dados do cliente para atualizar contadores
                            viewModel.carregarDetalhesCliente(clienteId)
                        }
                        
                        is com.example.alg_gestao_02.ui.state.UiState.Error -> {
                            LogUtils.error("ClientDetailsFragment", "❌ Erro no processamento: ${state.message}")
                            Toast.makeText(requireContext(), "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                            
                            // Limpar observer
                            devolucaoViewModel.processamentoState.removeObservers(viewLifecycleOwner)
                            devolucaoViewModel.clearProcessamentoState()
                        }
                        
                        else -> {
                            // Estado null ou outros
                        }
                    }
                }
                
                // Processar a devolução
                LogUtils.info("ClientDetailsFragment", "Chamando devolucaoViewModel.processarDevolucao...")
                devolucaoViewModel.processarDevolucao(
                    devolucaoId = devolucao.id,
                    quantidadeDevolvida = quantidade,
                    statusItemDevolucao = status,
                    observacaoItemDevolucao = observacao
                )
            }
        })
        
        dialog.show(parentFragmentManager, "detalhes_devolucao_${devolucao.id}")
    }

    private fun showError(message: String) {
        binding.layoutError.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    /**
     * Navega para a tela de contratos filtrada pelo cliente atual
     */
    private fun navigateToContractsByClient() {
        LogUtils.debug("ClientDetailsFragment", "Navegando para contratos do cliente: $clienteId")
        
        val cliente = viewModel.cliente.value
        if (cliente == null) {
            Toast.makeText(requireContext(), "Dados do cliente não disponíveis", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Definir filtro pendente para contratos
        com.example.alg_gestao_02.utils.FilterManager.setPendingClientFilter(
            clienteId = clienteId,
            clienteNome = cliente.contratante,
            filterType = com.example.alg_gestao_02.utils.FilterManager.FilterType.CONTRATOS
        )
        
        // Navegar para contratos usando Navigation Component
        findNavController().navigate(R.id.contratosFragment)
        
        // Atualizar item selecionado no menu de navegação
        try {
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                .setCheckedItem(R.id.nav_contratos)
        } catch (e: Exception) {
            LogUtils.error("ClientDetailsFragment", "Erro ao atualizar menu de navegação: ${e.message}")
        }
        
        LogUtils.info("ClientDetailsFragment", "Filtro definido para contratos de: ${cliente.contratante}")
    }
    
    /**
     * Navega para a tela de devoluções filtrada pelo cliente atual
     */
    private fun navigateToReturnsByClient() {
        LogUtils.debug("ClientDetailsFragment", "Navegando para devoluções do cliente: $clienteId")
        
        val cliente = viewModel.cliente.value
        if (cliente == null) {
            Toast.makeText(requireContext(), "Dados do cliente não disponíveis", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Definir filtro pendente para devoluções
        com.example.alg_gestao_02.utils.FilterManager.setPendingClientFilter(
            clienteId = clienteId,
            clienteNome = cliente.contratante,
            filterType = com.example.alg_gestao_02.utils.FilterManager.FilterType.DEVOLUCOES
        )
        
        // Navegar para devoluções usando Navigation Component
        findNavController().navigate(R.id.devolucoesFragment)
        
        // Atualizar item selecionado no menu de navegação
        try {
            requireActivity().findViewById<com.google.android.material.navigation.NavigationView>(R.id.navView)
                .setCheckedItem(R.id.nav_devolucoes)
        } catch (e: Exception) {
            LogUtils.error("ClientDetailsFragment", "Erro ao atualizar menu de navegação: ${e.message}")
        }
        
        LogUtils.info("ClientDetailsFragment", "Filtro definido para devoluções de: ${cliente.contratante}")
    }

    private fun confirmarExclusao(contrato: Contrato) {
        // Verificar role do usuário
        val sessionManager = SessionManager(requireContext())
        val userRole = sessionManager.getUserRole()
        val isAdmin = userRole == "admin"
        
        LogUtils.debug("ClientDetailsFragment", "👤 Usuário role: $userRole")
        LogUtils.debug("ClientDetailsFragment", "🔐 É admin: $isAdmin")
        LogUtils.debug("ClientDetailsFragment", "📋 Status contrato: ${contrato.status_assinatura}")
        
        // Verificar se pode excluir
        val (podeExcluir, mensagem) = contrato.podeExcluir(isAdmin, forcar = isAdmin)
        
        if (!podeExcluir && !isAdmin) {
            LogUtils.warning("ClientDetailsFragment", "❌ Exclusão negada: $mensagem")
            
            // Mostrar diálogo informativo para não-admins
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("❌ Exclusão Não Permitida")
                .setMessage(mensagem)
                .setPositiveButton("Entendi") { dialog, _ ->
                    dialog.dismiss()
                }
                .setIcon(R.drawable.ic_error)
                .show()
            return
        }
        
        // Configurar mensagem do diálogo baseada no status do contrato
        val titulo: String
        val mensagemDialog: String
        val icone: Int
        
        when (contrato.status_assinatura) {
            "ASSINADO" -> {
                titulo = "Excluir Contrato Assinado"
                mensagemDialog = if (isAdmin) {
                    "Este contrato possui assinatura digital!\n\n" +
                    "📋 Contrato: #${contrato.contratoNum}\n" +
                    "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                    "📅 Status: ASSINADO ✍️\n\n" +
                    "Deseja realmente excluir este contrato assinado?"
                } else {
                    "❌ Este contrato possui assinatura digital e não pode ser excluído.\n\n" +
                    "📞 Entre em contato com o administrador do sistema."
                }
                icone = R.drawable.ic_warning
            }
            
            "PENDENTE" -> {
                titulo = "🔄 Excluir Contrato Pendente"
                mensagemDialog = "📋 Contrato: #${contrato.contratoNum}\n" +
                        "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                        "📅 Status: PENDENTE ⏳\n\n" +
                        "ℹ️ Este contrato está aguardando assinatura.\n\n" +
                        "❓ Deseja realmente excluir este contrato?"
                icone = R.drawable.ic_info
            }
            
            else -> {
                titulo = "🗑️ Excluir Contrato"
                mensagemDialog = "📋 Contrato: #${contrato.contratoNum}\n" +
                        "👤 Cliente: ${contrato.clienteNome ?: contrato.cliente?.contratante ?: "N/A"}\n" +
                        "📅 Status: NÃO ASSINADO 📝\n\n" +
                        "❓ Deseja realmente excluir este contrato?"
                icone = R.drawable.ic_delete
            }
        }
        
        // Mostrar diálogo de confirmação
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(titulo)
            .setMessage(mensagemDialog)
            .setPositiveButton(if (contrato.status_assinatura == "ASSINADO") "⚠️ Forçar Exclusão" else "🗑️ Sim, Excluir") { dialog, _ ->
                dialog.dismiss()
                
                // Mostrar mensagem informativa para admin
                if (isAdmin && contrato.status_assinatura == "ASSINADO") {
                    LogUtils.info("ClientDetailsFragment", "⚠️ Admin confirmou exclusão de contrato assinado")
                    Toast.makeText(context, "⚠️ Forçando exclusão de contrato assinado...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "🗑️ Excluindo contrato...", Toast.LENGTH_SHORT).show()
                }
                
                LogUtils.debug("ClientDetailsFragment", "▶️ Usuário confirmou exclusão do contrato ${contrato.contratoNum}")
                excluirContrato(contrato.id)
            }
            .setNegativeButton("🚫 Cancelar") { dialog, _ ->
                LogUtils.debug("ClientDetailsFragment", "🚫 Usuário cancelou exclusão do contrato ${contrato.contratoNum}")
                dialog.dismiss()
            }
            .setIcon(icone)
            .setCancelable(true)
            .show()
    }
    
    private fun excluirContrato(contratoId: Int) {
        // Criar ViewModel de contratos para processar a exclusão
        viewLifecycleOwner.lifecycleScope.launch {
            // val contratoRepository = ContratoRepository() // Não utilizado
            val contratoViewModelFactory = com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModelFactory()
            val contratoViewModel = ViewModelProvider(this@ClientDetailsFragment, contratoViewModelFactory)[com.example.alg_gestao_02.ui.contrato.viewmodel.ContratosViewModel::class.java]
            
            // Observar o resultado da exclusão
            contratoViewModel.operationState.observe(viewLifecycleOwner) { state ->
                when (state) {
                    is com.example.alg_gestao_02.ui.state.UiState.Loading -> {
                        LogUtils.debug("ClientDetailsFragment", "🔄 Excluindo contrato...")
                    }
                    
                    is com.example.alg_gestao_02.ui.state.UiState.Success -> {
                        LogUtils.info("ClientDetailsFragment", "✅ Contrato excluído com sucesso!")
                        Toast.makeText(requireContext(), "Contrato excluído com sucesso", Toast.LENGTH_SHORT).show()
                        
                        // Limpar observer e recarregar dados do cliente
                        contratoViewModel.operationState.removeObservers(viewLifecycleOwner)
                        contratoViewModel.resetOperationState()
                        
                        // Recarregar dados do cliente para atualizar lista de contratos
                        viewModel.carregarDetalhesCliente(clienteId)
                    }
                    
                    is com.example.alg_gestao_02.ui.state.UiState.Error -> {
                        LogUtils.error("ClientDetailsFragment", "❌ Erro na exclusão: ${state.message}")
                        Toast.makeText(requireContext(), "Erro: ${state.message}", Toast.LENGTH_LONG).show()
                        
                        // Limpar observer
                        contratoViewModel.operationState.removeObservers(viewLifecycleOwner)
                        contratoViewModel.resetOperationState()
                    }
                    
                    else -> {
                        // Estado null ou outros
                    }
                }
            }
            
            // Executar a exclusão
            LogUtils.info("ClientDetailsFragment", "Chamando contratoViewModel.excluirContrato...")
            contratoViewModel.excluirContrato(contratoId)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
