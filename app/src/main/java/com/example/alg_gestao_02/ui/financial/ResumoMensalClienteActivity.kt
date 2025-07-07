package com.example.alg_gestao_02.ui.financial

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ResumoMensalCliente
import com.example.alg_gestao_02.databinding.ActivityResumoMensalClienteBinding
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity para exibir resumo mensal detalhado do cliente
 */
class ResumoMensalClienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResumoMensalClienteBinding
    private lateinit var viewModel: FinancialViewModel
    
    private lateinit var contratosAdapter: ContratosResumoAdapter
    private lateinit var devolucoesAdapter: DevolucoesResumoAdapter
    private lateinit var loadingView: View
    
    private var clienteId: Int = 0
    private var mesReferencia: String = ""
    private var resumoAtual: ResumoMensalCliente? = null
    
    // Controle de filtros
    private var filtroExpandido: Boolean = false

    companion object {
        const val EXTRA_CLIENTE_ID = "cliente_id"
        const val EXTRA_MES_REFERENCIA = "mes_referencia"
        const val EXTRA_CLIENTE_NOME = "cliente_nome"

        fun newIntent(context: Context, clienteId: Int, mesReferencia: String? = null, clienteNome: String? = null): Intent {
            // Se mesReferencia é null, usar mês atual como padrão
            val mesDefault = mesReferencia ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
            return Intent(context, ResumoMensalClienteActivity::class.java).apply {
                putExtra(EXTRA_CLIENTE_ID, clienteId)
                putExtra(EXTRA_MES_REFERENCIA, mesDefault)
                putExtra(EXTRA_CLIENTE_NOME, clienteNome)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResumoMensalClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LogUtils.info("ResumoMensalClienteActivity", "📊 ========== INICIANDO RESUMO MENSAL ==========")
        
        // Inicializar views
        loadingView = findViewById(R.id.loadingView)
        
        setupExtras()
        setupViewModel()
        setupToolbar()
        setupRecyclerViews()
        setupPeriodSelectors()
        setupClickListeners()
        setupObservers()
        
        // Inicializar interface com dados básicos
        inicializarInterfaceBasica()
        
        // Carregar dados
        carregarResumoMensal()
    }

    private fun setupExtras() {
        clienteId = intent.getIntExtra(EXTRA_CLIENTE_ID, 0)
        mesReferencia = intent.getStringExtra(EXTRA_MES_REFERENCIA) ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        
        LogUtils.info("ResumoMensalClienteActivity", "👤 Cliente ID: $clienteId")
        LogUtils.info("ResumoMensalClienteActivity", "📅 Mês: $mesReferencia")
        
        if (clienteId == 0) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Cliente ID inválido!")
            finish()
            return
        }
    }

    private fun setupViewModel() {
        val factory = FinancialViewModelFactory()
        viewModel = ViewModelProvider(this, factory)[FinancialViewModel::class.java]
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // Manter apenas o título padrão "Resumo Mensal" definido no XML
        // Nome do cliente será exibido apenas no card principal, não na toolbar
        
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerViews() {
        // Adapter para contratos
        contratosAdapter = ContratosResumoAdapter()
        binding.rvContratos.apply {
            layoutManager = LinearLayoutManager(this@ResumoMensalClienteActivity)
            adapter = contratosAdapter
        }
        
        // Adapter para devoluções
        devolucoesAdapter = DevolucoesResumoAdapter()
        binding.rvDevolucoes.apply {
            layoutManager = LinearLayoutManager(this@ResumoMensalClienteActivity)
            adapter = devolucoesAdapter
        }
    }

    private fun setupClickListeners() {
        // Expandir/Contrair filtro
        binding.layoutHeaderFiltro.setOnClickListener {
            toggleFiltroExpansao()
        }
        
        // Aplicar filtro
        binding.btnAplicarFiltro.setOnClickListener {
            aplicarFiltroPeriodo()
        }
        
        // Mês atual
        binding.btnMesAtual.setOnClickListener {
            irParaMesAtual()
        }
        
        // Expandir/Contrair contratos
        binding.cardContratos.setOnClickListener {
            toggleVisibilidade(binding.rvContratos, binding.ivExpandContratos)
        }
        
        // Expandir/Contrair devoluções
        binding.cardDevolucoes.setOnClickListener {
            toggleVisibilidade(binding.rvDevolucoes, binding.ivExpandDevolucoes)
        }
        
        // Gerar PDF
        binding.btnGerarPdf.setOnClickListener {
            gerarPdfCliente()
        }
        
        // Compartilhar
        binding.btnCompartilhar.setOnClickListener {
            compartilharResumo()
        }
    }

    private fun setupObservers() {
        // Observer para UI State
        viewModel.uiState.observe(this) { state ->
            when (state) {
                is UiState.Loading -> {
                    loadingView.visibility = View.VISIBLE
                }
                is UiState.Success -> {
                    loadingView.visibility = View.GONE
                    // Success não tem message, apenas data
                    if (state.data is String) {
                        Toast.makeText(this, state.data as String, Toast.LENGTH_SHORT).show()
                    }
                }
                is UiState.Error -> {
                    loadingView.visibility = View.GONE
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
                else -> {
                    loadingView.visibility = View.GONE
                }
            }
        }
        
        // Observer para resumo mensal
        viewModel.resumoMensalCliente.observe(this) { resumo ->
            LogUtils.info("ResumoMensalClienteActivity", "✅ Resumo mensal carregado: ${resumo?.clienteNome}")
            resumoAtual = resumo
            atualizarInterface(resumo)
        }
        

        
        // Observer para PDF gerado
        viewModel.pdfGerado.observe(this) { pdfResponse ->
            LogUtils.info("ResumoMensalClienteActivity", "📄 PDF gerado: ${pdfResponse.nomeArquivo}")
            if (pdfResponse.sucesso && !pdfResponse.urlDownload.isNullOrEmpty()) {
                // Aqui você pode implementar o download ou visualização do PDF
                Toast.makeText(this, "PDF gerado com sucesso! ${pdfResponse.nomeArquivo}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun inicializarInterfaceBasica() {
        // Definir nome do cliente imediatamente com dados dos extras
        val nomeClienteExtra = intent.getStringExtra(EXTRA_CLIENTE_NOME)
        val nomeInicial = nomeClienteExtra ?: "Cliente ID: $clienteId"
        
        LogUtils.info("ResumoMensalClienteActivity", "🏷️ Definindo nome inicial: '$nomeInicial'")
        binding.tvNomeCliente.text = nomeInicial
        
        // Definir mês de referência formatado (usar formatação consistente)
        try {
            val localeBR = Locale("pt", "BR")
            val formato = SimpleDateFormat("yyyy-MM", localeBR)
            val data = formato.parse(mesReferencia)
            val formatoExibicao = SimpleDateFormat("MMMM/yyyy", localeBR)
            val mesFormatado = data?.let { formatoExibicao.format(it) } ?: mesReferencia
            binding.tvMesReferencia.text = mesFormatado
            LogUtils.debug("ResumoMensalClienteActivity", "🗓️ Mês inicial formatado: '$mesReferencia' → '$mesFormatado'")
        } catch (e: Exception) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao formatar mês inicial: ${e.message}")
            binding.tvMesReferencia.text = mesReferencia
        }
        
        // Definir valores padrão enquanto carrega (sem texto "Carregando")
        binding.tvValorMensal.text = "R$ --,--"
        binding.tvValorDevolucoes.text = "R$ --,--"
    }

    private fun carregarResumoMensal() {
        LogUtils.info("ResumoMensalClienteActivity", "🔄 Carregando resumo mensal...")
        viewModel.buscarResumoMensalCliente(clienteId, mesReferencia)
    }

    private fun atualizarInterface(resumo: ResumoMensalCliente?) {
        if (resumo == null) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Resumo é nulo!")
            return
        }

        LogUtils.info("ResumoMensalClienteActivity", "🔄 Atualizando interface...")
        LogUtils.info("ResumoMensalClienteActivity", "🔍 Filtro aplicado: '$mesReferencia' | API retornou: '${resumo.mesReferencia}'")

        // Header do cliente - usar nome dos extras como fallback
        val nomeClienteExtra = intent.getStringExtra(EXTRA_CLIENTE_NOME)
        val nomeCliente = if (!resumo.clienteNome.isNullOrEmpty()) {
            resumo.clienteNome
        } else {
            nomeClienteExtra ?: "Cliente ID: $clienteId"
        }
        
        LogUtils.info("ResumoMensalClienteActivity", "👤 Nome do cliente: API='${resumo.clienteNome}', Extra='$nomeClienteExtra', Final='$nomeCliente'")
        
        binding.tvNomeCliente.text = nomeCliente
        
        // Usar sempre o mês de referência aplicado pelo filtro (não o da API)
        val mesFormatadoFiltro = formatarMesReferencia(mesReferencia)
        LogUtils.info("ResumoMensalClienteActivity", "📅 Forçando exibição do mês filtrado: '$mesReferencia' → '$mesFormatadoFiltro'")
        binding.tvMesReferencia.text = mesFormatadoFiltro
        
        // VALIDAÇÃO: Verificar se os dados são realmente do período solicitado
        val dadosValidados = validarEFiltrarDadosPorPeriodo(resumo, mesReferencia)
        
        if (dadosValidados.isEmpty) {
            // Mostrar estado vazio para o período
            exibirEstadoVazio(mesFormatadoFiltro)
            return
        }
        
        // Exibir dados filtrados do período correto
        binding.tvValorMensal.text = dadosValidados.getValorMensalFormatado()
        binding.tvValorDevolucoes.text = dadosValidados.getValorDevolucoesFormatado()
        
        // Estatísticas do mês filtrado
        binding.tvContratosAtivos.text = dadosValidados.contratosAtivos.toString()
        binding.tvNovoContratos.text = dadosValidados.contratosMes.toString()
        binding.tvDevolucoesMes.text = dadosValidados.devolucoesMes.toString()
        binding.tvTicketMedio.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(dadosValidados.ticketMedio)
        
        // Listas filtradas
        contratosAdapter.submitList(dadosValidados.contratosDetalhes)
        devolucoesAdapter.submitList(dadosValidados.devolucoesDetalhes)
        
        // Visibilidade dos cards baseada no conteúdo filtrado
        binding.cardContratos.visibility = if (dadosValidados.contratosDetalhes.isNotEmpty()) View.VISIBLE else View.GONE
        binding.cardDevolucoes.visibility = if (dadosValidados.devolucoesDetalhes.isNotEmpty()) View.VISIBLE else View.GONE
        
        LogUtils.info("ResumoMensalClienteActivity", "✅ Interface atualizada com dados do período: $mesFormatadoFiltro")
    }
    
    /**
     * Formatar mês de referência localmente (independente da API)
     */
    private fun formatarMesReferencia(mesRef: String): String {
        return try {
            val localeBR = Locale("pt", "BR")
            val formato = SimpleDateFormat("yyyy-MM", localeBR)
            val formatoSaida = SimpleDateFormat("MMMM/yyyy", localeBR)
            val data = formato.parse(mesRef)
            data?.let { formatoSaida.format(it) } ?: mesRef
        } catch (e: Exception) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao formatar mês '$mesRef': ${e.message}")
            mesRef
        }
    }
    
    /**
     * Validar e filtrar dados para mostrar apenas do período solicitado
     */
    private fun validarEFiltrarDadosPorPeriodo(resumo: ResumoMensalCliente, periodoFiltro: String): ResumoMensalCliente {
        LogUtils.info("ResumoMensalClienteActivity", "🔍 Validando dados para período: $periodoFiltro")
        
        // Extrair ano e mês do filtro
        val (anoFiltro, mesFiltro) = try {
            val partes = periodoFiltro.split("-")
            Pair(partes[0].toInt(), partes[1].toInt())
        } catch (e: Exception) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao parsear período: $periodoFiltro")
            return resumo.copy(
                contratosDetalhes = emptyList(),
                devolucoesDetalhes = emptyList(),
                valorMensal = 0.0,
                valorDevolucoes = 0.0,
                contratosAtivos = 0,
                contratosMes = 0,
                devolucoesMes = 0,
                ticketMedio = 0.0,
                statusPagamento = resumo.statusPagamento ?: "PENDENTE"
            )
        }
        
        // Filtrar contratos que pertencem ao período
        val contratosDoMes = resumo.contratosDetalhes.filter { contrato ->
            contrato.dataAssinatura?.let { dataStr ->
                try {
                    val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val data = formato.parse(dataStr)
                    if (data != null) {
                        val calendar = Calendar.getInstance()
                        calendar.time = data
                        val anoContrato = calendar.get(Calendar.YEAR)
                        val mesContrato = calendar.get(Calendar.MONTH) + 1
                        val pertence = (anoContrato == anoFiltro && mesContrato == mesFiltro)
                        LogUtils.debug("ResumoMensalClienteActivity", "📋 Contrato ${contrato.contratoNum}: $anoContrato-$mesContrato → Filtro: $anoFiltro-$mesFiltro → Pertence: $pertence")
                        pertence
                    } else false
                } catch (e: Exception) {
                    LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao parsear data do contrato: $dataStr")
                    false
                }
            } ?: false
        }
        
        // Filtrar devoluções que pertencem ao período
        val devolucoesDoMes = resumo.devolucoesDetalhes.filter { devolucao ->
            try {
                val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val data = formato.parse(devolucao.dataDevolucao)
                if (data != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = data
                    val anoDevolucao = calendar.get(Calendar.YEAR)
                    val mesDevolucao = calendar.get(Calendar.MONTH) + 1
                    val pertence = (anoDevolucao == anoFiltro && mesDevolucao == mesFiltro)
                    LogUtils.debug("ResumoMensalClienteActivity", "📦 Devolução ${devolucao.numeroDevolucao}: $anoDevolucao-$mesDevolucao → Filtro: $anoFiltro-$mesFiltro → Pertence: $pertence")
                    pertence
                } else false
            } catch (e: Exception) {
                LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao parsear data da devolução: ${devolucao.dataDevolucao}")
                false
            }
        }
        
        // Calcular valores baseados apenas nos dados filtrados
        val valorMensalFiltrado = contratosDoMes.sumOf { it.valorMensal }
        val valorDevolucoesFiltrado = devolucoesDoMes.sumOf { it.valorMulta }
        val contratosAtivosFiltrado = contratosDoMes.size
        val contratosMesFiltrado = contratosDoMes.size // Todos os contratos filtrados são "do mês"
        val devolucoesMesFiltrado = devolucoesDoMes.size
        val ticketMedioFiltrado = if (contratosAtivosFiltrado > 0) valorMensalFiltrado / contratosAtivosFiltrado else 0.0
        
        LogUtils.info("ResumoMensalClienteActivity", "📊 Dados filtrados para $periodoFiltro:")
        LogUtils.info("ResumoMensalClienteActivity", "   💰 Valor mensal: R$ ${String.format("%.2f", valorMensalFiltrado)}")
        LogUtils.info("ResumoMensalClienteActivity", "   📋 Contratos: $contratosAtivosFiltrado")
        LogUtils.info("ResumoMensalClienteActivity", "   📦 Devoluções: $devolucoesMesFiltrado")
        
        return resumo.copy(
            mesReferencia = periodoFiltro,
            valorMensal = valorMensalFiltrado,
            valorDevolucoes = valorDevolucoesFiltrado,
            valorTotalPagar = valorMensalFiltrado + valorDevolucoesFiltrado,
            contratosAtivos = contratosAtivosFiltrado,
            contratosMes = contratosMesFiltrado,
            devolucoesMes = devolucoesMesFiltrado,
            ticketMedio = ticketMedioFiltrado,
            contratosDetalhes = contratosDoMes,
            devolucoesDetalhes = devolucoesDoMes,
            statusPagamento = resumo.statusPagamento ?: "PENDENTE"
        ).apply {
            // Adicionar propriedade para indicar se está vazio
            val isEmpty = contratosDoMes.isEmpty() && devolucoesDoMes.isEmpty() && valorMensalFiltrado == 0.0
        }
    }
    
    /**
     * Extensão para verificar se o resumo está vazio
     */
    private val ResumoMensalCliente.isEmpty: Boolean
        get() = contratosDetalhes.isEmpty() && devolucoesDetalhes.isEmpty() && valorMensal == 0.0
    
    /**
     * Exibir estado vazio quando não há dados para o período
     */
    private fun exibirEstadoVazio(mesFormatado: String) {
        LogUtils.info("ResumoMensalClienteActivity", "📭 Exibindo estado vazio para: $mesFormatado")
        
        // Valores zerados
        binding.tvValorMensal.text = "R$ 0,00"
        binding.tvValorDevolucoes.text = "R$ 0,00"
        
        // Estatísticas zeradas
        binding.tvContratosAtivos.text = "0"
        binding.tvNovoContratos.text = "0"
        binding.tvDevolucoesMes.text = "0"
        binding.tvTicketMedio.text = "R$ 0,00"
        
        // Listas vazias
        contratosAdapter.submitList(emptyList())
        devolucoesAdapter.submitList(emptyList())
        
        // Ocultar cards
        binding.cardContratos.visibility = View.GONE
        binding.cardDevolucoes.visibility = View.GONE
        
        // Mostrar toast informativo
        Toast.makeText(this, "Nenhum dado encontrado para $mesFormatado", Toast.LENGTH_LONG).show()
        
        LogUtils.info("ResumoMensalClienteActivity", "✅ Estado vazio configurado")
    }

    private fun toggleVisibilidade(view: View, iconView: View) {
        if (view.visibility == View.VISIBLE) {
            view.visibility = View.GONE
            iconView.rotation = 0f
        } else {
            view.visibility = View.VISIBLE
            iconView.rotation = 180f
        }
    }

    private fun gerarPdfCliente() {
        LogUtils.info("ResumoMensalClienteActivity", "📄 Gerando PDF para cliente: $clienteId")
        viewModel.gerarPdfResumoMensal(
            mesReferencia = mesReferencia,
            clienteIds = listOf(clienteId),
            tipoRelatorio = "COMPLETO"
        )
    }

    private fun compartilharResumo() {
        resumoAtual?.let { resumo ->
            val texto = """
                📊 Resumo Mensal - ${resumo.clienteNome}
                📅 ${resumo.getMesReferenciaFormatado()}
                
                💰 Valor Mensal: ${resumo.getValorMensalFormatado()}
                📦 Devoluções: ${resumo.getValorDevolucoesFormatado()}
                
                📋 Contratos Ativos: ${resumo.contratosAtivos}
                📈 Novos no Mês: ${resumo.contratosMes}
                📉 Devoluções: ${resumo.devolucoesMes}
            """.trimIndent()
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
                putExtra(Intent.EXTRA_SUBJECT, "Resumo Mensal - ${resumo.clienteNome}")
            }
            
            startActivity(Intent.createChooser(intent, "Compartilhar Resumo"))
        }
    }

    private fun setupPeriodSelectors() {
        // Configurar seletor de mês
        val meses = arrayOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )
        val mesAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, meses)
        binding.actvMes.setAdapter(mesAdapter)
        
        // Configurar seletor de ano
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        val anos = (2020..anoAtual + 1).map { it.toString() }.toTypedArray()
        val anoAdapter = ArrayAdapter(this, R.layout.dropdown_menu_popup_item, anos)
        binding.actvAno.setAdapter(anoAdapter)
        
        LogUtils.debug("ResumoMensalClienteActivity", "📅 Seletores de período configurados")
    }

    private fun toggleFiltroExpansao() {
        filtroExpandido = !filtroExpandido
        
        if (filtroExpandido) {
            // Expandir filtro
            binding.layoutConteudoFiltro.visibility = View.VISIBLE
            binding.ivExpandirFiltro.animate().rotation(180f).setDuration(200).start()
            LogUtils.debug("ResumoMensalClienteActivity", "📤 Filtro expandido")
        } else {
            // Contrair filtro
            binding.layoutConteudoFiltro.visibility = View.GONE
            binding.ivExpandirFiltro.animate().rotation(0f).setDuration(200).start()
            LogUtils.debug("ResumoMensalClienteActivity", "📥 Filtro contraído")
        }
    }

    private fun aplicarFiltroPeriodo() {
        val mesTexto = binding.actvMes.text.toString()
        val anoTexto = binding.actvAno.text.toString()
        
        if (mesTexto.isEmpty() || anoTexto.isEmpty()) {
            Toast.makeText(this, "Selecione mês e ano para filtrar", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val meses = arrayOf(
                "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
                "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
            )
            
            val mesIndex = meses.indexOf(mesTexto)
            if (mesIndex == -1) {
                Toast.makeText(this, "Mês inválido", Toast.LENGTH_SHORT).show()
                return
            }
            
            val mes = mesIndex + 1 // API espera 1-12
            val ano = anoTexto.toInt()
            
            // Atualizar mês de referência
            mesReferencia = String.format("%04d-%02d", ano, mes)
            
            LogUtils.info("ResumoMensalClienteActivity", "📅 Aplicando filtro: $mesReferencia")
            
            // Contrair filtro após aplicar
            if (filtroExpandido) {
                toggleFiltroExpansao()
            }
            
            // Recarregar dados com novo período
            carregarResumoMensal()
            
        } catch (e: Exception) {
            LogUtils.error("ResumoMensalClienteActivity", "❌ Erro ao aplicar filtro: ${e.message}")
            Toast.makeText(this, "Erro ao aplicar filtro", Toast.LENGTH_SHORT).show()
        }
    }

    private fun irParaMesAtual() {
        // Definir mês atual
        val calendar = Calendar.getInstance()
        val ano = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH) + 1 // Calendar usa 0-11
        
        mesReferencia = String.format("%04d-%02d", ano, mes)
        
        LogUtils.info("ResumoMensalClienteActivity", "📅 Indo para mês atual: $mesReferencia")
        
        // Limpar campos de filtro 
        binding.actvMes.setText("", false)
        binding.actvAno.setText("", false)
        
        // Contrair filtro
        if (filtroExpandido) {
            toggleFiltroExpansao()
        }
        
        // Recarregar dados
        carregarResumoMensal()
    }
} 