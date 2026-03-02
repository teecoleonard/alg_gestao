package com.example.alg_gestao_02.ui.contrato

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import android.view.WindowManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.utils.LogUtils
import java.text.NumberFormat
import java.util.Locale
import com.example.alg_gestao_02.service.PdfService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.app.ProgressDialog
import android.widget.Toast
import com.example.alg_gestao_02.data.repository.ContratoRepository
import com.example.alg_gestao_02.utils.Resource
import java.util.Date
import androidx.navigation.Navigation
import androidx.appcompat.app.AlertDialog

class ContratoDetailsDialogFragment : DialogFragment() {

    private var contrato: Contrato? = null
    private var editRequestListener: OnEditRequestListener? = null
    private var contratoAtualizadoListener: OnContratoAtualizadoListener? = null
    private var pdfService: PdfService? = null

    // Interface para notificar o fragmento pai sobre o pedido de edição
    interface OnEditRequestListener {
        fun onEditRequested(contrato: Contrato)
    }
    
    // Interface para notificar quando o contrato é atualizado (ex: assinatura)
    interface OnContratoAtualizadoListener {
        fun onContratoAtualizado()
    }

    companion object {
        private const val ARG_CONTRATO = "arg_contrato"

        fun newInstance(contrato: Contrato): ContratoDetailsDialogFragment {
            val fragment = ContratoDetailsDialogFragment()
            val args = Bundle().apply {
                putParcelable(ARG_CONTRATO, contrato)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Definir um estilo para o diálogo (pode ser ajustado)
        // setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen) 
        // setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_MaterialComponents_Light_Dialog_Alert) // Estilo anterior com erro
        contrato = arguments?.getParcelable(ARG_CONTRATO)
        
        // Inicializar o serviço de PDF
        pdfService = PdfService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar o layout do diálogo
        return inflater.inflate(R.layout.dialog_contrato_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Encontrar as Views do layout
        val tvClienteNome: TextView = view.findViewById(R.id.tvDetalhesContratoClienteNome)
        val tvNumeroContrato: TextView = view.findViewById(R.id.tvDetalhesContratoNumero)
        val tvCpfCnpj: TextView = view.findViewById(R.id.tvDetalhesContratoCpfCnpj)
        val tvInscricaoEstadual: TextView = view.findViewById(R.id.tvDetalhesContratoInscricaoEstadual)
        val tvDataEmissao: TextView = view.findViewById(R.id.tvDetalhesContratoDataEmissao)
        val tvDataVenc: TextView = view.findViewById(R.id.tvDetalhesContratoDataVenc)
        val tvLocalObra: TextView = view.findViewById(R.id.tvDetalhesContratoLocalObra)
        val tvPeriodo: TextView = view.findViewById(R.id.tvDetalhesContratoPeriodo)
        val tvLocalEntrega: TextView = view.findViewById(R.id.tvDetalhesContratoLocalEntrega)
        val tvResponsavel: TextView = view.findViewById(R.id.tvDetalhesContratoResponsavel)
        val tvAssinatura: TextView = view.findViewById(R.id.tvDetalhesContratoAssinatura)
        val tvNumEquipamentos: TextView = view.findViewById(R.id.tvDetalhesContratoNumEquipamentos)
        val tvValorTotal: TextView = view.findViewById(R.id.tvDetalhesContratoValorTotal)
        val tvNomeEquipamento: TextView = view.findViewById(R.id.tvNomeEquipamento)
        val btnFechar: Button = view.findViewById(R.id.btnFecharDetalhesContrato)
        val btnEditar: Button = view.findViewById(R.id.btnEditarContrato)
        val btnAssinarContrato: Button = view.findViewById(R.id.btnAssinarContrato)
        val btnGerarPdf: Button = view.findViewById(R.id.btnGerarPdf)
        val btnDevolucao: Button = view.findViewById(R.id.btnDevolucao)
        val tvStatusContrato: TextView = view.findViewById(R.id.tvDetalhesContratoStatus)
        val btnAlterarStatus: Button = view.findViewById(R.id.btnAlterarStatus)

        // Formatter de moeda para o valor
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))

        // Preencher os dados do contrato nas Views
        contrato?.let { c -> // Renomeado para 'c' para evitar sombreamento
            LogUtils.debug("ContratoDetailsDialog", "Exibindo detalhes para contrato ID: ${c.id}")
            LogUtils.debug("ContratoDetailsDialog", "Objeto Contrato recebido: $c") // Log do objeto Contrato inteiro
            
            // Verificar e exibir nome do cliente, buscando o nome correto
            // Utilizar o método helper que já existe na classe Contrato
            tvClienteNome.text = c.resolverNomeCliente()
            
            tvNumeroContrato.text = "Contrato #${c.contratoNum}"
            
            // Exibir CPF/CNPJ e IE do cliente
            c.cliente?.let { cliente ->
                tvCpfCnpj.text = cliente.getDocumentoFormatado()
                
                // Exibir IE apenas para pessoa jurídica, RG para pessoa física
                if (!cliente.isPessoaFisica()) {
                    tvInscricaoEstadual.text = cliente.getDocumentoSecundarioFormatado()
                    tvInscricaoEstadual.visibility = View.VISIBLE
                } else {
                    // Para pessoa física, também podemos mostrar o RG
                    tvInscricaoEstadual.text = cliente.getDocumentoSecundarioFormatado()
                    tvInscricaoEstadual.visibility = View.VISIBLE
                }
                
                LogUtils.debug("ContratoDetailsDialog", "CPF/CNPJ: ${cliente.cpfCnpj}, RG/IE: ${cliente.rgIe}")
            } ?: run {
                tvCpfCnpj.visibility = View.GONE
                tvInscricaoEstadual.visibility = View.GONE
                LogUtils.debug("ContratoDetailsDialog", "Cliente não disponível no contrato")
            }
            
            // Exibir datas: emissão do modelo; vencimento calculado por período se aplicável
            tvDataEmissao.text = c.getDataEmissaoFormatada()
            tvDataVenc.text = calcularVencimentoParaExibicao(c)
            tvLocalObra.text = "Obra: ${c.obraLocal ?: "Não informado"}"
            tvPeriodo.text = "Período: ${c.contratoPeriodo ?: "Não informado"}"
            tvLocalEntrega.text = "Entrega: ${c.entregaLocal ?: "Não informado"}"
            tvResponsavel.text = "Responsável: ${c.respPedido ?: "Não informado"}"
            
            // Atualizar exibição do status da assinatura
            val statusAssinatura = when {
                c.status_assinatura == "ASSINADO" -> "Assinado em ${c.data_assinatura}"
                c.status_assinatura == "PENDENTE" -> "Aguardando assinatura"
                else -> "Não assinado"
            }
            tvAssinatura.text = statusAssinatura
            
            // Configurar visibilidade dos botões baseada no status de assinatura
            val jaAssinado = c.status_assinatura == "ASSINADO"
            configurarBotoesAssinatura(btnAssinarContrato, btnGerarPdf, jaAssinado)
            
            // Soma total das quantidades de todos os equipamentos
            val somaQuantidade = c.equipamentosParaExibicao.sumOf { it.quantidadeEquip }
            tvNumEquipamentos.text = "Quantidade total: $somaQuantidade"

            // Exibir todos os nomes dos equipamentos, separados por vírgula
            if (c.equipamentosParaExibicao.isNotEmpty()) {
                val nomesEquipamentos = c.equipamentosParaExibicao.joinToString(separator = ", ") { it.nomeEquipamentoExibicao }
                tvNomeEquipamento.text = nomesEquipamentos
            } else {
                tvNomeEquipamento.text = "Sem equipamentos cadastrados"
            }

            // Log detalhado da lista de equipamentos e seus valores totais
            if (c.equipamentosParaExibicao.isEmpty()) {
                LogUtils.debug("ContratoDetailsDialog", "Lista de equipamentos está nula ou vazia.")
            } else {
                LogUtils.debug("ContratoDetailsDialog", "Detalhes dos equipamentos no contrato:")
                c.equipamentosParaExibicao.forEachIndexed { index, equip ->
                    LogUtils.debug("ContratoDetailsDialog", 
                        "  Equipamento[[index]]: ID=${equip.id}, Nome=${equip.nomeEquipamentoExibicao}, ValorTotal=${equip.valorTotal}")
                }
            }
            
            // Calcular o valor total a partir dos equipamentos
            // Se houver equipamentos, somar seus valores totais, senão usar o contratoValor
            val valorTotalCalculado = if (c.equipamentosParaExibicao.isEmpty()) {
                c.contratoValor
            } else {
                c.equipamentosParaExibicao.sumOf { it.valorTotal ?: 0.0 }
            }

            LogUtils.debug("ContratoDetailsDialog", "Valor Total Calculado: $valorTotalCalculado, Valor no contrato: ${c.contratoValor}")
            tvValorTotal.text = currencyFormat.format(valorTotalCalculado)
            
            // Exibir status do contrato
            val statusContrato = c.getStatusContratoEnum()
            tvStatusContrato.text = "${statusContrato.getIcone()} ${statusContrato.descricao.uppercase()}"
            tvStatusContrato.setTextColor(android.graphics.Color.WHITE)
            tvStatusContrato.setBackgroundColor(statusContrato.getCor())

        } ?: run {
            // Se o objeto contrato for nulo, loga erro e fecha o diálogo
            LogUtils.error("ContratoDetailsDialog", "Contrato nulo recebido!")
            dismissAllowingStateLoss() // Usar dismissAllowingStateLoss se estiver em onViewCreated
        }

        // Configurar o botão Fechar
        btnFechar.setOnClickListener {
            // Limpar outros dialogs que possam estar empilhados
            parentFragmentManager.fragments.forEach { fragment ->
                if (fragment is DialogFragment && fragment != this@ContratoDetailsDialogFragment) {
                    fragment.dismissAllowingStateLoss()
                }
            }
            dismiss()
            LogUtils.debug("ContratoDetailsDialog", "Dialog fechado e navegação limpa")
        }

        // Configurar o botão Editar
        btnEditar.setOnClickListener {
            contrato?.let { contratoNaoNulo ->
                editRequestListener?.onEditRequested(contratoNaoNulo)
            }
            // Limpar a navegação antes de fechar
            parentFragmentManager.fragments.forEach { fragment ->
                if (fragment is DialogFragment && fragment != this@ContratoDetailsDialogFragment) {
                    fragment.dismissAllowingStateLoss()
                }
            }
            dismiss() 
        }
        
        // Configurar o botão Assinar Contrato
        btnAssinarContrato.setOnClickListener {
            contrato?.let { contratoNaoNulo ->
                abrirAssinatura(contratoNaoNulo)
            }
        }
        
        // Configurar o botão de gerar PDF
        btnGerarPdf.setOnClickListener {
            if (btnGerarPdf.isEnabled) {
                gerarPdfContrato()
            } else {
                Toast.makeText(requireContext(), "É necessário assinar o contrato primeiro!", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Configurar o botão Devolução
        btnDevolucao.setOnClickListener {
            contrato?.let { contratoNaoNulo ->
                mostrarDialogoConfirmacaoDevolucao(contratoNaoNulo.id)
            }
        }
        
        // Configurar o botão Alterar Status
        btnAlterarStatus.setOnClickListener {
            contrato?.let { contratoNaoNulo ->
                abrirBottomSheetAlterarStatus(contratoNaoNulo)
            }
        }
    }
    
    /**
     * Abre o BottomSheet para alterar o status do contrato
     */
    private fun abrirBottomSheetAlterarStatus(contrato: Contrato) {
        val bottomSheet = AtualizarStatusBottomSheet.newInstance(
            contratoId = contrato.id,
            statusAtual = contrato.statusContrato ?: "PENDENTE"
        ) { contratoAtualizado ->
            // Callback quando o status for atualizado
            Toast.makeText(
                requireContext(),
                "✅ Status atualizado para ${contratoAtualizado.getStatusContratoEnum().descricao}",
                Toast.LENGTH_SHORT
            ).show()
            
            // Atualizar a UI com o novo status
            view?.findViewById<TextView>(R.id.tvDetalhesContratoStatus)?.let { tvStatus ->
                val novoStatus = contratoAtualizado.getStatusContratoEnum()
                tvStatus.text = "${novoStatus.getIcone()} ${novoStatus.descricao.uppercase()}"
                tvStatus.setTextColor(android.graphics.Color.WHITE)
                tvStatus.setBackgroundColor(novoStatus.getCor())
            }
            
            // Notificar o listener
            contratoAtualizadoListener?.onContratoAtualizado()
            
            // Atualizar o objeto contrato local
            this.contrato = contratoAtualizado
        }
        
        bottomSheet.show(parentFragmentManager, "alterar_status")
    }

    private fun calcularVencimentoParaExibicao(c: Contrato): String {
        return try {
            val periodo = c.contratoPeriodo?.trim()?.uppercase(Locale.getDefault()) ?: ""
            val base = c.dataHoraEmissao ?: c.dataVenc ?: ""
            if (base.isBlank()) return c.getDataVencimentoFormatada()

            fun parseFlexible(value: String): Date? {
                val patterns = listOf(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss'Z'",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd"
                )
                for (p in patterns) {
                    try {
                        val sdf = java.text.SimpleDateFormat(p, java.util.Locale.getDefault())
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        return sdf.parse(value)
                    } catch (_: Exception) {}
                }
                return null
            }

            val baseDate = parseFlexible(base) ?: Date()
            val cal = java.util.Calendar.getInstance().apply { time = baseDate }
            when (java.text.Normalizer.normalize(periodo, java.text.Normalizer.Form.NFD).replace("[\\p{InCombiningDiacriticalMarks}]".toRegex(), "")) {
                "DIARIA", "DIARIO" -> cal.add(java.util.Calendar.DAY_OF_MONTH, 1)
                "QUINZENAL" -> cal.add(java.util.Calendar.DAY_OF_MONTH, 15)
                "MENSAL" -> cal.add(java.util.Calendar.MONTH, 1)
                "ANUAL" -> cal.add(java.util.Calendar.YEAR, 1)
                else -> {
                    // Sem período válido: usa a dataVenc existente formatada
                    return c.getDataVencimentoFormatada()
                }
            }
            java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(cal.time)
        } catch (_: Exception) {
            c.getDataVencimentoFormatada()
        }
    }

    // Método para o Fragment pai registrar o listener de edição
    fun setOnEditRequestListener(listener: OnEditRequestListener) {
        this.editRequestListener = listener
    }
    
    // Método para o Fragment pai registrar o listener de atualização
    fun setOnContratoAtualizadoListener(listener: OnContratoAtualizadoListener) {
        this.contratoAtualizadoListener = listener
    }

    override fun onStart() {
        super.onStart()
        // Ajustar o tamanho do diálogo
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, // Largura máxima
            WindowManager.LayoutParams.WRAP_CONTENT  // Altura baseada no conteúdo
        )
        // Opcional: Adicionar padding se necessário (ex: 16dp em cada lado)
        // val paddingDp = 16 
        // val density = requireContext().resources.displayMetrics.density
        // val paddingPx = (paddingDp * density).toInt()
        // dialog?.window?.decorView?.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }

    /**
     * Gera o PDF do contrato
     */
    private fun gerarPdfContrato() {
        contrato?.let { contratoNaoNulo ->
            // Mostrar dialog de progresso
            val progressDialog = ProgressDialog(requireContext()).apply {
                setMessage("Gerando PDF, aguarde...")
                setCancelable(false)
                show()
            }

            // Chamar o serviço em uma coroutine
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    LogUtils.debug("ContratoDetailsDialog", "Buscando dados atualizados do contrato #${contratoNaoNulo.id}")
                    
                    // Buscar dados atualizados do contrato antes de gerar o PDF
                    val contratoRepository = ContratoRepository()
                    val contratoAtualizado = when (val result = contratoRepository.getContratoById(contratoNaoNulo.id)) {
                        is Resource.Success -> {
                            LogUtils.debug("ContratoDetailsDialog", "Dados do contrato obtidos com sucesso. Gerando PDF...")
                            LogUtils.debug("ContratoDetailsDialog", "Dados do contrato antes de gerar PDF: id=${result.data?.id}, num=${result.data?.contratoNum}, assinado=${result.data?.isAssinado()}, cliente=${result.data?.cliente?.contratante}")
                            result.data
                        }
                        is Resource.Error -> {
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                                val errorMsg = "Erro ao buscar dados do contrato: ${result.message}"
                                LogUtils.error("ContratoDetailsDialog", errorMsg)
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                        else -> {
                            withContext(Dispatchers.Main) {
                                progressDialog.dismiss()
                                Toast.makeText(requireContext(), "Erro ao carregar dados do contrato", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                    }

                    // Verificar se o contrato foi obtido com sucesso
                    if (contratoAtualizado == null) {
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            val errorMsg = "Erro: Contrato não encontrado"
                            LogUtils.error("ContratoDetailsDialog", errorMsg)
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    // Verificar se o contrato tem cliente
                    if (contratoAtualizado.cliente == null) {
                        withContext(Dispatchers.Main) {
                            progressDialog.dismiss()
                            val errorMsg = "Erro: Dados do cliente não encontrados"
                            LogUtils.error("ContratoDetailsDialog", errorMsg)
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                        return@launch
                    }

                    // Verificar dados do cliente, incluindo Inscrição Estadual
                    LogUtils.debug("ContratoDetailsDialog", """
                        Dados do cliente para o PDF:
                        Nome: ${contratoAtualizado.cliente?.contratante}
                        CPF/CNPJ: ${contratoAtualizado.cliente?.cpfCnpj}
                        RG/IE: ${contratoAtualizado.cliente?.rgIe ?: "Não informado"}
                        Telefone: ${contratoAtualizado.cliente?.telefone ?: "Não informado"}
                        Endereço: ${contratoAtualizado.cliente?.getEnderecoCompleto()}
                        É Pessoa Física: ${contratoAtualizado.cliente?.isPessoaFisica()}
                    """.trimIndent())

                    LogUtils.debug("ContratoDetailsDialog", "Iniciando chamada para o serviço de PDF")
                    val result = pdfService?.gerarPdfContrato(contratoAtualizado, contratoAtualizado.cliente!!)
                    LogUtils.debug("ContratoDetailsDialog", "Resposta recebida do serviço de PDF")
                    
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        
                        if (result?.isSuccess == true) {
                            val pdfResponse = result.getOrNull()
                            if (pdfResponse != null && pdfResponse.success) {
                                LogUtils.debug("ContratoDetailsDialog", "PDF gerado com sucesso: ${pdfResponse.message}")
                                
                                // Fechar este dialog primeiro  
                                dismiss()
                                
                                // Limpar outros dialogs que possam estar abertos
                                parentFragmentManager.fragments.forEach { fragment ->
                                    if (fragment is DialogFragment && fragment != this@ContratoDetailsDialogFragment) {
                                        fragment.dismissAllowingStateLoss()
                                    }
                                }
                                
                                // Mostrar o PDF/HTML no visualizador
                                val pdfViewer = PdfViewerFragment.newInstance(
                                    pdfBase64 = pdfResponse.pdfBase64,
                                    contratoNumero = contratoAtualizado.getContratoNumOuVazio(),
                                    contratoId = contratoAtualizado.id,
                                    htmlUrl = pdfResponse.htmlUrl,
                                    htmlContent = pdfResponse.htmlContent
                                )
                                
                                // Configurar callback para atualizar lista quando contrato for assinado
                                LogUtils.debug("ContratoDetailsDialog", "🔧 Configurando callback do PdfViewer")
                                pdfViewer.setOnContratoAtualizadoCallback {
                                    LogUtils.debug("ContratoDetailsDialog", "🔔 Callback ContratoDetailsDialog recebido - contrato atualizado via assinatura")
                                    LogUtils.debug("ContratoDetailsDialog", "📞 Chamando contratoAtualizadoListener?.onContratoAtualizado()")
                                    // Notificar o fragmento pai para que atualize a lista
                                    contratoAtualizadoListener?.onContratoAtualizado()
                                    LogUtils.debug("ContratoDetailsDialog", "✅ Callback ContratoDetailsDialog concluído")
                                }
                                
                                // Adicionar logs para verificar o conteúdo
                                LogUtils.debug("ContratoDetailsDialogFragment", "htmlUrl recebido: ${pdfResponse.htmlUrl}")
                                LogUtils.debug("ContratoDetailsDialogFragment", "htmlContent recebido: ${pdfResponse.htmlContent?.substring(0, minOf(50, pdfResponse.htmlContent.length))}...")
                                
                                pdfViewer.show(parentFragmentManager, "pdf_viewer")
                                
                                // Feedback para o usuário baseado no status da assinatura
                                val mensagem = if (contratoAtualizado.isAssinado()) {
                                    "📄 PDF gerado com assinatura!"
                                } else {
                                    "📄 PDF gerado (aguardando assinatura)"
                                }
                                Toast.makeText(requireContext(), mensagem, Toast.LENGTH_SHORT).show()
                            } else {
                                val errorMsg = "Erro ao gerar PDF: ${pdfResponse?.message ?: "Resposta inválida"}"
                                LogUtils.error("ContratoDetailsDialog", errorMsg)
                                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                            }
                        } else {
                            val error = result?.exceptionOrNull()?.message ?: "Erro desconhecido"
                            val errorMsg = "Falha ao gerar PDF: $error"
                            LogUtils.error("ContratoDetailsDialog", errorMsg)
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    LogUtils.error("ContratoDetailsDialogFragment", "Erro ao gerar PDF", e)
                    withContext(Dispatchers.Main) {
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Erro ao gerar PDF: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } ?: run {
            Toast.makeText(
                requireContext(),
                "Erro: Dados do contrato não disponíveis",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    /**
     * Atualiza a UI do dialog após a assinatura ser feita
     */
    private fun atualizarUIAposAssinatura(contratoAtualizado: Contrato) {
        try {
            LogUtils.debug("ContratoDetailsDialog", "🔄 INICIANDO atualizarUIAposAssinatura")
            LogUtils.debug("ContratoDetailsDialog", "📋 Status do contrato: ${contratoAtualizado.status_assinatura}")
            LogUtils.debug("ContratoDetailsDialog", "📅 Data assinatura: ${contratoAtualizado.data_assinatura}")
            
            // Buscar as views novamente para garantir que ainda existem
            val view = this.view
            if (view == null) {
                LogUtils.error("ContratoDetailsDialog", "❌ View é null - não é possível atualizar UI")
                return
            }
            
            LogUtils.debug("ContratoDetailsDialog", "✅ View encontrada, buscando componentes...")
            
            val tvAssinatura: TextView? = view.findViewById(R.id.tvDetalhesContratoAssinatura)
            val btnAssinarContrato: Button? = view.findViewById(R.id.btnAssinarContrato) 
            val btnGerarPdf: Button? = view.findViewById(R.id.btnGerarPdf)
            
            LogUtils.debug("ContratoDetailsDialog", "📱 Componentes encontrados:")
            LogUtils.debug("ContratoDetailsDialog", "  - tvAssinatura: ${tvAssinatura != null}")
            LogUtils.debug("ContratoDetailsDialog", "  - btnAssinarContrato: ${btnAssinarContrato != null}")
            LogUtils.debug("ContratoDetailsDialog", "  - btnGerarPdf: ${btnGerarPdf != null}")
            
            // Atualizar status da assinatura
            if (tvAssinatura != null) {
                val textoAssinatura = "Assinado em ${contratoAtualizado.data_assinatura}"
                tvAssinatura.text = textoAssinatura
                LogUtils.debug("ContratoDetailsDialog", "📝 Texto da assinatura atualizado: $textoAssinatura")
            } else {
                LogUtils.error("ContratoDetailsDialog", "❌ tvAssinatura não encontrado!")
            }
            
            // Reconfigurar botões para o novo estado com animação sutil
            if (btnAssinarContrato != null && btnGerarPdf != null) {
                // Animação sutil para indicar mudança
                btnAssinarContrato.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(150)
                    .withEndAction {
                        btnAssinarContrato.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(150)
                            .start()
                    }
                    .start()
                
                configurarBotoesAssinatura(btnAssinarContrato, btnGerarPdf, true)
            }
            
            LogUtils.debug("ContratoDetailsDialog", "✅ UI atualizada após assinatura - dialog mantido aberto")
            
        } catch (e: Exception) {
            LogUtils.error("ContratoDetailsDialog", "Erro ao atualizar UI após assinatura", e)
        }
    }

    /**
     * Configura o estado visual dos botões baseado no status de assinatura
     */
    private fun configurarBotoesAssinatura(btnAssinar: Button, btnGerarPdf: Button, jaAssinado: Boolean) {
        if (jaAssinado) {
            // Estado: Já assinado - permitir alterar assinatura
            btnAssinar.isEnabled = true
            btnAssinar.text = "Já assinado"
            
            // Usar Material Button se possível para melhor aparência - cor verde para "sucesso"
            if (btnAssinar is com.google.android.material.button.MaterialButton) {
                btnAssinar.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.success)
                )
                btnAssinar.setTextColor(requireContext().getColor(R.color.white))
            } else {
                btnAssinar.setBackgroundColor(requireContext().getColor(R.color.success))
                btnAssinar.setTextColor(requireContext().getColor(R.color.white))
            }
            
            btnGerarPdf.isEnabled = true
            btnGerarPdf.text = "Gerar PDF"
            btnGerarPdf.alpha = 1.0f
        } else {
            // Estado: Não assinado - usar cor primária
            btnAssinar.isEnabled = true
            btnAssinar.text = "Assinar"
            
            // Usar Material Button se possível para melhor aparência
            if (btnAssinar is com.google.android.material.button.MaterialButton) {
                btnAssinar.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    requireContext().getColor(R.color.primary)
                )
                btnAssinar.setTextColor(requireContext().getColor(R.color.white))
            } else {
                btnAssinar.setBackgroundColor(requireContext().getColor(R.color.primary))
                btnAssinar.setTextColor(requireContext().getColor(R.color.white))
            }
            
            btnGerarPdf.isEnabled = false
            btnGerarPdf.text = "Gerar PDF"
            btnGerarPdf.alpha = 0.5f
        }
    }

    /**
     * Abre o fragmento de assinatura para o contrato selecionado
     */
    private fun abrirAssinatura(contrato: Contrato) {
        val jaAssinado = contrato.status_assinatura == "ASSINADO"
        val acao = if (jaAssinado) "Alterando" else "Criando"
        
        LogUtils.debug("ContratoDetailsDialog", "🖊️ $acao assinatura para contrato #${contrato.contratoNum}")
        
        val bundle = Bundle().apply {
            putString("contratoNumero", contrato.contratoNum)
            putInt("contratoId", contrato.id)
            putBoolean("isAlteracao", jaAssinado) // Passa se é uma alteração
        }
        
        val signatureFragment = SignatureCaptureFragment().apply {
            arguments = bundle
            setOnContratoAtualizadoListener {
                LogUtils.debug("ContratoDetailsDialog", "🔔 Callback recebido - contrato assinado")
                
                // Buscar dados atualizados da API para garantir informações corretas
                lifecycleScope.launch {
                    try {
                        val contratoRepository = ContratoRepository()
                        val result = contratoRepository.getContratoById(contrato.id)
                        
                        when (result) {
                            is Resource.Success -> {
                                val contratoAtualizado = result.data
                                LogUtils.debug("ContratoDetailsDialog", "✅ Contrato atualizado da API: status=${contratoAtualizado.status_assinatura}")
                                
                                // Atualizar o contrato local
                                this@ContratoDetailsDialogFragment.contrato = contratoAtualizado
                                
                                // Atualizar a UI do dialog em tempo real
                                atualizarUIAposAssinatura(contratoAtualizado)
                                
                                // Notificar que o contrato foi atualizado (para atualizar lista externa)
                                contratoAtualizadoListener?.onContratoAtualizado()
                                
                                // Mostrar feedback visual melhorado
                                val statusTexto = if (contratoAtualizado.isAssinado()) "ASSINADO" else "PENDENTE"
                                Toast.makeText(
                                    requireContext(), 
                                    "🔄 Status atualizado para: $statusTexto", 
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is Resource.Error -> {
                                LogUtils.error("ContratoDetailsDialog", "Erro ao buscar contrato atualizado: ${result.message}")
                                // Usar dados locais como fallback
                                val contratoAtualizado = contrato.copy(
                                    status_assinatura = "ASSINADO",
                                    data_assinatura = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                                )
                                this@ContratoDetailsDialogFragment.contrato = contratoAtualizado
                                atualizarUIAposAssinatura(contratoAtualizado)
                                contratoAtualizadoListener?.onContratoAtualizado()
                            }
                            is Resource.Loading -> {
                                LogUtils.debug("ContratoDetailsDialog", "⏳ Carregando dados atualizados do contrato...")
                            }
                        }
                    } catch (e: Exception) {
                        LogUtils.error("ContratoDetailsDialog", "Erro ao atualizar contrato", e)
                    }
                }
                
                // NÃO FECHAR o dialog - mantê-lo aberto com informações atualizadas
                
                val mensagem = if (jaAssinado) {
                    "🔄 Assinatura alterada com sucesso! Gere um novo PDF com a assinatura atualizada."
                } else {
                    "✅ Contrato assinado com sucesso! Agora você pode gerar o PDF."
                }
                
                Toast.makeText(
                    requireContext(),
                    mensagem,
                    Toast.LENGTH_LONG
                ).show()
                
                LogUtils.debug("ContratoDetailsDialog", "✅ Assinatura processada com sucesso")
            }
        }
        
        LogUtils.debug("ContratoDetailsDialog", "🔓 Abrindo SignatureCaptureFragment")
        signatureFragment.show(parentFragmentManager, "signature_fragment")
    }
    
    /**
     * Mostra diálogo de confirmação para gerar devolução
     */
    private fun mostrarDialogoConfirmacaoDevolucao(contratoId: Int) {
        LogUtils.info("ContratoDetailsDialog", "📦 Mostrando diálogo de confirmação para devolução do contrato ID: $contratoId")
        
        AlertDialog.Builder(requireContext())
            .setTitle("Devolução")
            .setMessage("Deseja gerar devolução?")
            .setPositiveButton("Sim") { _, _ ->
                LogUtils.debug("ContratoDetailsDialog", "✅ Usuário confirmou geração de devolução")
                navegarParaDevolucoes(contratoId)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                LogUtils.debug("ContratoDetailsDialog", "❌ Usuário cancelou geração de devolução")
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
    
    /**
     * Navega para a página de devoluções do contrato específico
     */
    private fun navegarParaDevolucoes(contratoId: Int) {
        LogUtils.info("ContratoDetailsDialog", "📦 Navegando para devoluções do contrato ID: $contratoId")
        
        // Fechar o diálogo atual
        dismiss()
        
        // Limpar outros dialogs que possam estar abertos
        parentFragmentManager.fragments.forEach { fragment ->
            if (fragment is DialogFragment && fragment != this@ContratoDetailsDialogFragment) {
                fragment.dismissAllowingStateLoss()
            }
        }
        
        // Navegar para o fragment de devoluções usando Navigation Component
        try {
            val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
            val bundle = Bundle().apply {
                putInt("contratoId", contratoId)
            }
            navController.navigate(R.id.devolucoesFragment, bundle)
            
            LogUtils.debug("ContratoDetailsDialog", "✅ Navegação para devoluções iniciada com sucesso")
            
            // Feedback para o usuário
            Toast.makeText(
                requireContext(),
                "Mostrando devoluções do contrato",
                Toast.LENGTH_SHORT
            ).show()
            
        } catch (e: Exception) {
            LogUtils.error("ContratoDetailsDialog", "❌ Erro ao navegar para devoluções", e)
            Toast.makeText(
                requireContext(),
                "Erro ao abrir devoluções: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
