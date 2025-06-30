package com.example.alg_gestao_02.ui.financial

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.ReceitaClienteResponse
import com.example.alg_gestao_02.data.models.ReceitaCliente
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.service.ReportService
import com.example.alg_gestao_02.utils.ShareUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class FinancialFragment : Fragment() {

    private lateinit var viewModel: FinancialViewModel
    private lateinit var swipeRefresh: SwipeRefreshLayout
    
    // Views principais
    private lateinit var tvValorTotalAtivo: TextView
    private lateinit var tvReceitaMensal: TextView
    private lateinit var tvTicketMedio: TextView
    private lateinit var tvMetaReceita: TextView
    private lateinit var tvProgressoMeta: TextView
    private lateinit var progressBarMeta: ProgressBar
    
    // Cards interativos
    private lateinit var cardValorTotal: CardView
    private lateinit var cardReceitaMensal: CardView
    private lateinit var cardTicketMedio: CardView
    private lateinit var cardMeta: CardView
    
    // Botões de ação
    private lateinit var btnDefinirMeta: Button
    private lateinit var btnFiltrarPeriodo: Button
    private lateinit var btnExportarRelatorio: Button
    private lateinit var btnVerReceitaCliente: Button
    
    // Notificação explicativa
    private lateinit var cardNotificacaoFinancial: CardView
    private lateinit var btnFecharNotificacaoFinancial: ImageButton
    

    
    // Período selecionado
    private var dataInicio: Date? = null
    private var dataFim: Date? = null
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_financial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.info("FinancialFragment", "🚀 ========== FINANCIAL FRAGMENT INICIADO ==========")
        
        initViews(view)
        setupViewModel()
        setupListeners()
        observeViewModel()
        
        LogUtils.info("FinancialFragment", "✅ Financial Fragment configurado com sucesso!")
    }

    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipeRefresh)
        
        // TextViews de métricas
        tvValorTotalAtivo = view.findViewById(R.id.tvValorTotalAtivo)
        tvReceitaMensal = view.findViewById(R.id.tvReceitaMensal)
        tvTicketMedio = view.findViewById(R.id.tvTicketMedio)
        tvMetaReceita = view.findViewById(R.id.tvMetaReceita)
        tvProgressoMeta = view.findViewById(R.id.tvProgressoMeta)
        progressBarMeta = view.findViewById(R.id.progressBarMeta)
        
        // Cards interativos
        cardValorTotal = view.findViewById(R.id.cardValorTotal)
        cardReceitaMensal = view.findViewById(R.id.cardReceitaMensal)
        cardTicketMedio = view.findViewById(R.id.cardTicketMedio)
        cardMeta = view.findViewById(R.id.cardMeta)
        
        // Botões
        btnDefinirMeta = view.findViewById(R.id.btnDefinirMeta)
        btnFiltrarPeriodo = view.findViewById(R.id.btnFiltrarPeriodo)
        btnExportarRelatorio = view.findViewById(R.id.btnExportarRelatorio)
        btnVerReceitaCliente = view.findViewById(R.id.btnVerReceitaCliente)
        
        // Notificação explicativa
        cardNotificacaoFinancial = view.findViewById(R.id.cardNotificacaoFinancial)
        btnFecharNotificacaoFinancial = view.findViewById(R.id.btnFecharNotificacaoFinancial)
        
        // Verificar se deve mostrar a notificação
        verificarNotificacaoFinancial()
    }

    private fun setupViewModel() {
        val factory = FinancialViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[FinancialViewModel::class.java]
    }

    private fun setupListeners() {
        // SwipeRefresh
        swipeRefresh.setOnRefreshListener {
            LogUtils.debug("FinancialFragment", "Atualizando dados financeiros via swipe refresh")
            viewModel.refreshFinancialData()
        }
        
        // Cards clicáveis com detalhes
        cardValorTotal.setOnClickListener {
            showDetailDialog("Valor Total Ativo", 
                "Soma de todos os contratos ativos (assinados) no sistema.\n\n" +
                "Este valor representa o portfólio total da empresa.")
        }
        
        cardReceitaMensal.setOnClickListener {
            showDetailDialog("Receita Mensal", 
                "Valor total dos contratos assinados no mês atual.\n\n" +
                "Usado para acompanhar a performance mensal.")
        }
        
        cardTicketMedio.setOnClickListener {
            showDetailDialog("Ticket Médio", 
                "Valor médio por contrato assinado.\n\n" +
                "Calculado como: Valor Total ÷ Número de Contratos")
        }
        
        cardMeta.setOnClickListener {
            showMetaDetailDialog()
        }
        
        // Botão definir meta
        btnDefinirMeta.setOnClickListener {
            showDefinirMetaDialog()
        }
        
        // Botão filtrar período
        btnFiltrarPeriodo.setOnClickListener {
            showFiltrarPeriodoDialog()
        }
        
        // Botão exportar relatório
        btnExportarRelatorio.setOnClickListener {
            exportarRelatorio()
        }
        
        // Botão ver receita por clientes
        btnVerReceitaCliente.setOnClickListener {
            navegarParaReceitaClientes()
        }
        
        // Botão fechar notificação
        btnFecharNotificacaoFinancial.setOnClickListener {
            fecharNotificacaoFinancial()
        }
    }

    private fun observeViewModel() {
        // Observar estado da UI
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    LogUtils.info("FinancialFragment", "⏳ Carregando dados financeiros...")
                }
                is UiState.Success -> {
                    LogUtils.info("FinancialFragment", "✅ Dados financeiros carregados!")
                    swipeRefresh.isRefreshing = false
                }
                is UiState.Error -> {
                    LogUtils.error("FinancialFragment", "❌ Erro: ${state.message}")
                    swipeRefresh.isRefreshing = false
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    LogUtils.debug("FinancialFragment", "Estado desconhecido: $state")
                    swipeRefresh.isRefreshing = false
                }
            }
        }
        
        // Observar métricas financeiras
        viewModel.financialMetrics.observe(viewLifecycleOwner) { metrics ->
            if (metrics != null) {
                updateFinancialMetrics(metrics)
            }
        }
        
        // Observar métricas de progresso
        viewModel.progressMetrics.observe(viewLifecycleOwner) { progress ->
            if (progress != null) {
                updateProgressMetrics(progress)
            }
        }
        
        // Observar receita por cliente (dados disponíveis para o novo fragmento)
        viewModel.receitaPorCliente.observe(viewLifecycleOwner) { receitaResponse ->
            if (receitaResponse != null) {
                LogUtils.info("FinancialFragment", "💰 Receita por cliente atualizada: ${receitaResponse.clientes.size} clientes")
            }
        }

        // Observer para PDF gerado
        viewModel.pdfGerado.observe(viewLifecycleOwner) { pdfResponse ->
            if (pdfResponse.sucesso && !pdfResponse.urlDownload.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "PDF gerado: ${pdfResponse.nomeArquivo}", Toast.LENGTH_LONG).show()
                // Aqui você pode implementar o download ou compartilhamento do PDF
            }
        }
    }

    private fun updateFinancialMetrics(metrics: FinancialMetrics) {
        LogUtils.info("FinancialFragment", "💰 Atualizando métricas financeiras")
        
        tvValorTotalAtivo.text = currencyFormat.format(metrics.valorTotalAtivo)
        tvReceitaMensal.text = currencyFormat.format(metrics.receitaMensal)
        tvTicketMedio.text = currencyFormat.format(metrics.ticketMedio)
    }

    private fun updateProgressMetrics(progress: ProgressMetrics) {
        LogUtils.info("FinancialFragment", "📊 Atualizando métricas de progresso")
        
        tvMetaReceita.text = currencyFormat.format(progress.receitaMeta)
        
        val percentual = progress.receitaPercentual
        tvProgressoMeta.text = "$percentual%"
        progressBarMeta.progress = percentual
        
        // Alterar cor da progress bar baseado no percentual
        val progressDrawable = when {
            percentual >= 90 -> R.drawable.progress_bar_success
            percentual >= 70 -> R.drawable.progress_bar_warning
            else -> R.drawable.progress_bar_error
        }
        progressBarMeta.progressDrawable = resources.getDrawable(progressDrawable, null)
    }

    private fun showDetailDialog(titulo: String, descricao: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(titulo)
            .setMessage(descricao)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showMetaDetailDialog() {
        val progressMetrics = viewModel.progressMetrics.value
        if (progressMetrics != null) {
            val atual = currencyFormat.format(progressMetrics.receitaAtual)
            val meta = currencyFormat.format(progressMetrics.receitaMeta)
            val percentual = progressMetrics.receitaPercentual
            val restante = currencyFormat.format(progressMetrics.receitaMeta - progressMetrics.receitaAtual)
            
            val mensagem = """
                Meta Mensal: $meta
                Realizado: $atual
                Progresso: $percentual%
                Restante: $restante
                
                ${if (percentual >= 100) "🎉 Meta atingida!" else "💪 Continue assim!"}
            """.trimIndent()
            
            showDetailDialog("Detalhes da Meta", mensagem)
        }
    }

    private fun showDefinirMetaDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_definir_meta, null)
        val editMeta = dialogView.findViewById<TextInputEditText>(R.id.editMeta)
        
        // Pré-preencher com meta atual se existir
        viewModel.progressMetrics.value?.let { progress ->
            editMeta.setText(progress.receitaMeta.toString())
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Definir Meta de Receita")
            .setView(dialogView)
            .setPositiveButton("Salvar") { dialog, _ ->
                val novaMetaText = editMeta.text.toString()
                if (novaMetaText.isNotEmpty()) {
                    try {
                        val novaMeta = novaMetaText.toDouble()
                        viewModel.definirMeta(novaMeta)
                        Toast.makeText(context, "Meta definida: ${currencyFormat.format(novaMeta)}", Toast.LENGTH_SHORT).show()
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Valor inválido", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showFiltrarPeriodoDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_filtrar_periodo, null)
        val btnDataInicio = dialogView.findViewById<Button>(R.id.btnDataInicio)
        val btnDataFim = dialogView.findViewById<Button>(R.id.btnDataFim)
        val tvDataInicio = dialogView.findViewById<TextView>(R.id.tvDataInicio)
        val tvDataFim = dialogView.findViewById<TextView>(R.id.tvDataFim)
        
        var dataInicioSelecionada: Date? = null
        var dataFimSelecionada: Date? = null
        
        btnDataInicio.setOnClickListener {
            showDatePicker { data ->
                dataInicioSelecionada = data
                tvDataInicio.text = dateFormat.format(data)
            }
        }
        
        btnDataFim.setOnClickListener {
            showDatePicker { data ->
                dataFimSelecionada = data
                tvDataFim.text = dateFormat.format(data)
            }
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Filtrar por Período")
            .setView(dialogView)
            .setPositiveButton("Aplicar") { dialog, _ ->
                if (dataInicioSelecionada != null && dataFimSelecionada != null) {
                    dataInicio = dataInicioSelecionada
                    dataFim = dataFimSelecionada
                    viewModel.filtrarPorPeriodo(dataInicio!!, dataFim!!)
                    Toast.makeText(context, "Filtro aplicado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Selecione ambas as datas", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNeutralButton("Limpar Filtro") { dialog, _ ->
                dataInicio = null
                dataFim = null
                viewModel.limparFiltro()
                Toast.makeText(context, "Filtro removido", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun exportarRelatorio() {
        val financialMetrics = viewModel.financialMetrics.value
        val progressMetrics = viewModel.progressMetrics.value
        
        if (financialMetrics != null && progressMetrics != null) {
            LogUtils.info("FinancialFragment", "🚀 ========== INICIANDO EXPORTAÇÃO DE RELATÓRIO ==========")
            
            // Mostrar dialog de opções
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("📊 Exportar Relatório")
                .setMessage("Escolha como deseja exportar o relatório:")
                .setPositiveButton("📄 Gerar PDF") { dialog, _ ->
                    gerarRelatorioPdf(financialMetrics, progressMetrics)
                    dialog.dismiss()
                }
                .setNeutralButton("👀 Visualizar") { dialog, _ ->
                    visualizarRelatorio(financialMetrics, progressMetrics)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            Toast.makeText(context, "Dados não disponíveis para exportação", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun gerarRelatorioPdf(
        financialMetrics: FinancialMetrics,
        progressMetrics: ProgressMetrics
    ) {
        LogUtils.info("FinancialFragment", "📄 ========== GERANDO RELATÓRIO PDF ==========")
        
        // Mostrar loading
        val loadingDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Gerando Relatório")
            .setMessage("Por favor, aguarde...")
            .setCancelable(false)
            .create()
        
        loadingDialog.show()
        
        // Gerar PDF em background
        lifecycleScope.launch {
            try {
                val reportService = ReportService(requireContext())
                
                val pdfFile = withContext(Dispatchers.IO) {
                    reportService.generateFinancialReport(
                        financialMetrics = financialMetrics,
                        progressMetrics = progressMetrics,
                        dataInicio = dataInicio,
                        dataFim = dataFim
                    )
                }
                
                loadingDialog.dismiss()
                
                if (pdfFile != null && ShareUtils.isFileValid(pdfFile)) {
                    LogUtils.info("FinancialFragment", "✅ PDF gerado com sucesso!")
                    
                    // Mostrar opções do que fazer com o PDF
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("✅ Relatório Gerado!")
                        .setMessage("PDF criado com sucesso!\nTamanho: ${ShareUtils.formatFileSize(pdfFile.length())}")
                        .setPositiveButton("📤 Compartilhar") { dialog, _ ->
                            try {
                                ShareUtils.sharePdfFile(requireContext(), pdfFile)
                                Toast.makeText(context, "Compartilhamento iniciado!", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                LogUtils.error("FinancialFragment", "Erro ao compartilhar: ${e.message}")
                                Toast.makeText(context, "Erro ao compartilhar: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }
                        .setNeutralButton("👀 Abrir") { dialog, _ ->
                            try {
                                ShareUtils.openPdfFile(requireContext(), pdfFile)
                            } catch (e: Exception) {
                                LogUtils.error("FinancialFragment", "Erro ao abrir: ${e.message}")
                                Toast.makeText(context, "Erro ao abrir: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                            dialog.dismiss()
                        }
                        .setNegativeButton("OK") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                } else {
                    LogUtils.error("FinancialFragment", "❌ Falha ao gerar PDF")
                    Toast.makeText(context, "Erro ao gerar relatório PDF", Toast.LENGTH_SHORT).show()
                }
                
            } catch (e: Exception) {
                loadingDialog.dismiss()
                LogUtils.error("FinancialFragment", "❌ Erro na geração do PDF: ${e.message}", e)
                Toast.makeText(context, "Erro ao gerar relatório: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun visualizarRelatorio(
        financialMetrics: FinancialMetrics,
        progressMetrics: ProgressMetrics
    ) {
        val periodo = if (dataInicio != null && dataFim != null) {
            "Período: ${dateFormat.format(dataInicio!!)} a ${dateFormat.format(dataFim!!)}"
        } else {
            "Período: Todos os dados"
        }
        
        val relatorio = """
            📊 RELATÓRIO FINANCEIRO
            ========================
            
            $periodo
            Data: ${dateFormat.format(Date())}
            
            💰 MÉTRICAS PRINCIPAIS
            ----------------------
            Valor Total Ativo: ${currencyFormat.format(financialMetrics.valorTotalAtivo)}
            Receita Mensal: ${currencyFormat.format(financialMetrics.receitaMensal)}
            Ticket Médio: ${currencyFormat.format(financialMetrics.ticketMedio)}
            
            🎯 METAS E PROGRESSO
            --------------------
            Meta de Receita: ${currencyFormat.format(progressMetrics.receitaMeta)}
            Receita Atual: ${currencyFormat.format(progressMetrics.receitaAtual)}
            Progresso: ${progressMetrics.receitaPercentual}%
            Restante: ${currencyFormat.format(progressMetrics.receitaMeta - progressMetrics.receitaAtual)}
            
            📈 ANÁLISE
            ----------
            ${if (progressMetrics.receitaPercentual >= 100) "✅ Meta atingida!" else "⚠️ Meta ainda não atingida"}
            ${if (financialMetrics.ticketMedio > 3000) "💎 Ticket médio alto" else "📊 Ticket médio normal"}
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📊 Relatório Financeiro")
            .setMessage(relatorio)
            .setPositiveButton("📄 Gerar PDF") { dialog, _ ->
                gerarRelatorioPdf(financialMetrics, progressMetrics)
                dialog.dismiss()
            }
            .setNegativeButton("Fechar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    

    
    /**
     * Navega para o fragmento de receita por cliente
     */
    private fun navegarParaReceitaClientes() {
        LogUtils.info("FinancialFragment", "📊 Navegando para receita por cliente...")
        
        try {
            // Usar NavController para navegação
            val navController = findNavController()
            navController.navigate(R.id.action_financialFragment_to_receitaClientesFragment)
                
            LogUtils.debug("FinancialFragment", "✅ Navegação iniciada para ReceitaClientesFragment")
        } catch (e: Exception) {
            LogUtils.error("FinancialFragment", "❌ Erro na navegação: ${e.message}", e)
            Toast.makeText(context, "Erro ao navegar para receita por cliente", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Verifica se deve mostrar a notificação explicativa
     */
    private fun verificarNotificacaoFinancial() {
        val sharedPrefs = requireContext().getSharedPreferences("alg_gestao_notifications", Context.MODE_PRIVATE)
        val notificacaoFechada = sharedPrefs.getBoolean("financial_explanation_closed", false)
        
        if (!notificacaoFechada) {
            LogUtils.debug("FinancialFragment", "📢 Mostrando notificação explicativa")
            cardNotificacaoFinancial.visibility = View.VISIBLE
        } else {
            LogUtils.debug("FinancialFragment", "🔕 Notificação já foi fechada pelo usuário")
            cardNotificacaoFinancial.visibility = View.GONE
        }
    }
    
    /**
     * Fecha a notificação explicativa com animação
     */
    private fun fecharNotificacaoFinancial() {
        LogUtils.info("FinancialFragment", "🔕 Fechando notificação explicativa")
        
        // Animação de fade out
        val fadeOut = android.view.animation.AnimationUtils.loadAnimation(requireContext(), android.R.anim.fade_out)
        fadeOut.setAnimationListener(object : android.view.animation.Animation.AnimationListener {
            override fun onAnimationStart(animation: android.view.animation.Animation?) {}
            override fun onAnimationRepeat(animation: android.view.animation.Animation?) {}
            override fun onAnimationEnd(animation: android.view.animation.Animation?) {
                cardNotificacaoFinancial.visibility = View.GONE
            }
        })
        
        cardNotificacaoFinancial.startAnimation(fadeOut)
        
        // Salvar no SharedPreferences que foi fechada
        val sharedPrefs = requireContext().getSharedPreferences("alg_gestao_notifications", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("financial_explanation_closed", true).apply()
        
        LogUtils.info("FinancialFragment", "✅ Notificação marcada como fechada permanentemente")
    }

} 