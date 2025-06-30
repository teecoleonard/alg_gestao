package com.example.alg_gestao_02.ui.financial

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.ResumoMensalCliente
import com.example.alg_gestao_02.databinding.ActivityResumoMensalClienteBinding
import com.example.alg_gestao_02.databinding.DialogConfirmarPagamentoBinding
import com.example.alg_gestao_02.ui.state.UiState
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    companion object {
        const val EXTRA_CLIENTE_ID = "cliente_id"
        const val EXTRA_MES_REFERENCIA = "mes_referencia"
        const val EXTRA_CLIENTE_NOME = "cliente_nome"

        fun newIntent(context: Context, clienteId: Int, mesReferencia: String? = null, clienteNome: String? = null): Intent {
            return Intent(context, ResumoMensalClienteActivity::class.java).apply {
                putExtra(EXTRA_CLIENTE_ID, clienteId)
                putExtra(EXTRA_MES_REFERENCIA, mesReferencia ?: SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
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
        // Expandir/Contrair contratos
        binding.cardContratos.setOnClickListener {
            toggleVisibilidade(binding.rvContratos, binding.ivExpandContratos)
        }
        
        // Expandir/Contrair devoluções
        binding.cardDevolucoes.setOnClickListener {
            toggleVisibilidade(binding.rvDevolucoes, binding.ivExpandDevolucoes)
        }
        
        // Confirmar pagamento
        binding.btnConfirmarPagamento.setOnClickListener {
            resumoAtual?.let { resumo ->
                mostrarDialogConfirmarPagamento(resumo)
            }
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
        
        // Observer para confirmação de pagamento
        viewModel.confirmacaoPagamento.observe(this) { confirmacao ->
            LogUtils.info("ResumoMensalClienteActivity", "💳 Pagamento confirmado: ${confirmacao.sucesso}")
            if (confirmacao.sucesso) {
                // Recarregar dados
                carregarResumoMensal()
            }
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
        
        // Definir mês de referência formatado
        try {
            val formato = SimpleDateFormat("yyyy-MM", Locale.getDefault())
            val data = formato.parse(mesReferencia)
            val formatoExibicao = SimpleDateFormat("MMMM/yyyy", Locale("pt", "BR"))
            binding.tvMesReferencia.text = data?.let { formatoExibicao.format(it) } ?: mesReferencia
        } catch (e: Exception) {
            binding.tvMesReferencia.text = mesReferencia
        }
        
        // Definir valores padrão enquanto carrega (sem texto "Carregando")
        binding.tvValorMensal.text = "R$ --,--"
        binding.tvValorDevolucoes.text = "R$ --,--"
        binding.tvValorTotal.text = "R$ --,--"
        
        // Status padrão
        binding.chipStatusPagamento.text = "AGUARDE"
        binding.chipStatusPagamento.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.text_secondary)
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

        // Header do cliente - usar nome dos extras como fallback
        val nomeClienteExtra = intent.getStringExtra(EXTRA_CLIENTE_NOME)
        val nomeCliente = if (!resumo.clienteNome.isNullOrEmpty()) {
            resumo.clienteNome
        } else {
            nomeClienteExtra ?: "Cliente ID: $clienteId"
        }
        
        LogUtils.info("ResumoMensalClienteActivity", "👤 Nome do cliente: API='${resumo.clienteNome}', Extra='$nomeClienteExtra', Final='$nomeCliente'")
        
        binding.tvNomeCliente.text = nomeCliente
        binding.tvMesReferencia.text = resumo.getMesReferenciaFormatado()
        
        // Valores principais
        binding.tvValorMensal.text = resumo.getValorMensalFormatado()
        binding.tvValorDevolucoes.text = resumo.getValorDevolucoesFormatado()
        binding.tvValorTotal.text = resumo.getValorTotalPagarFormatado()
        
        // Status de pagamento
        setupStatusPagamento(resumo)
        
        // Estatísticas do mês
        binding.tvContratosAtivos.text = resumo.contratosAtivos.toString()
        binding.tvNovoContratos.text = resumo.contratosMes.toString()
        binding.tvDevolucoesMes.text = resumo.devolucoesMes.toString()
        binding.tvTicketMedio.text = NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(resumo.ticketMedio)
        
        // Listas
        contratosAdapter.submitList(resumo.contratosDetalhes)
        devolucoesAdapter.submitList(resumo.devolucoesDetalhes)
        
        // Visibilidade dos cards baseada no conteúdo
        binding.cardContratos.visibility = if (resumo.contratosDetalhes.isNotEmpty()) View.VISIBLE else View.GONE
        binding.cardDevolucoes.visibility = if (resumo.devolucoesDetalhes.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun setupStatusPagamento(resumo: ResumoMensalCliente) {
        val chip = binding.chipStatusPagamento
        
        when (resumo.statusPagamento) {
            "PAGO" -> {
                chip.text = "✅ PAGO"
                chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.success)
                binding.btnConfirmarPagamento.visibility = View.GONE
            }
            "ATRASADO" -> {
                chip.text = "❌ ATRASADO"
                chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.error)
                binding.btnConfirmarPagamento.visibility = View.VISIBLE
            }
            else -> {
                chip.text = "⏰ PENDENTE"
                chip.chipBackgroundColor = ContextCompat.getColorStateList(this, R.color.warning)
                binding.btnConfirmarPagamento.visibility = View.VISIBLE
            }
        }
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

    private fun mostrarDialogConfirmarPagamento(resumo: ResumoMensalCliente) {
        val dialogBinding = DialogConfirmarPagamentoBinding.inflate(layoutInflater)
        
        // Preencher dados
        dialogBinding.tvClienteNomeDialog.text = resumo.clienteNome
        dialogBinding.tvMesReferenciaDialog.text = resumo.getMesReferenciaFormatado()
        dialogBinding.tvValorMensalDialog.text = resumo.getValorMensalFormatado()
        dialogBinding.tvValorDevolucoesDialog.text = resumo.getValorDevolucoesFormatado()
        dialogBinding.tvValorTotalDialog.text = resumo.getValorTotalPagarFormatado()
        
        // Valor padrão
        dialogBinding.etValorPago.setText(resumo.valorTotalPagar.toString())
        
        val dialog = MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        
        dialogBinding.btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        
        dialogBinding.btnConfirmar.setOnClickListener {
            val valorPagoStr = dialogBinding.etValorPago.text.toString().trim()
            val observacoes = dialogBinding.etObservacoes.text.toString().trim()
            
            if (valorPagoStr.isEmpty()) {
                dialogBinding.etValorPago.error = "Informe o valor pago"
                return@setOnClickListener
            }
            
            try {
                val valorPago = valorPagoStr.toDouble()
                if (valorPago <= 0) {
                    dialogBinding.etValorPago.error = "Valor deve ser maior que zero"
                    return@setOnClickListener
                }
                
                viewModel.confirmarPagamento(
                    clienteId = resumo.clienteId,
                    mesReferencia = resumo.mesReferencia,
                    valorPago = valorPago,
                    observacoes = observacoes.ifEmpty { null }
                )
                
                dialog.dismiss()
                
            } catch (e: NumberFormatException) {
                dialogBinding.etValorPago.error = "Valor inválido"
            }
        }
        
        dialog.show()
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
                💳 Total a Pagar: ${resumo.getValorTotalPagarFormatado()}
                
                📋 Contratos Ativos: ${resumo.contratosAtivos}
                📈 Novos no Mês: ${resumo.contratosMes}
                📉 Devoluções: ${resumo.devolucoesMes}
                
                Status: ${resumo.statusPagamento}
            """.trimIndent()
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, texto)
                putExtra(Intent.EXTRA_SUBJECT, "Resumo Mensal - ${resumo.clienteNome}")
            }
            
            startActivity(Intent.createChooser(intent, "Compartilhar Resumo"))
        }
    }
} 