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

class ContratoDetailsDialogFragment : DialogFragment() {

    private var contrato: Contrato? = null
    private var editRequestListener: OnEditRequestListener? = null
    private var pdfService: PdfService? = null

    // Interface para notificar o fragmento pai sobre o pedido de edição
    interface OnEditRequestListener {
        fun onEditRequested(contrato: Contrato)
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
        val btnGerarPdf: Button = view.findViewById(R.id.btnGerarPdf)

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
            
            // Usar os métodos de formatação da classe Contrato (sem parâmetros)
            tvDataEmissao.text = c.getDataEmissaoFormatada()
            tvDataVenc.text = c.getDataVencimentoFormatada()
            tvLocalObra.text = "Obra: ${c.obraLocal ?: "Não informado"}"
            tvPeriodo.text = "Período: ${c.contratoPeriodo ?: "Não informado"}"
            tvLocalEntrega.text = "Entrega: ${c.entregaLocal ?: "Não informado"}"
            tvResponsavel.text = "Responsável: ${c.respPedido ?: "Não informado"}"
            tvAssinatura.text = c.contratoAss ?: "Não assinado"
            
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

        } ?: run {
            // Se o objeto contrato for nulo, loga erro e fecha o diálogo
            LogUtils.error("ContratoDetailsDialog", "Contrato nulo recebido!")
            dismissAllowingStateLoss() // Usar dismissAllowingStateLoss se estiver em onViewCreated
        }

        // Configurar o botão Fechar
        btnFechar.setOnClickListener {
            dismiss()
        }

        // Configurar o botão Editar
        btnEditar.setOnClickListener {
            contrato?.let { contratoNaoNulo ->
                editRequestListener?.onEditRequested(contratoNaoNulo)
            }
                dismiss() 
            }
        
        // Configurar o botão de gerar PDF
        btnGerarPdf.setOnClickListener {
            gerarPdfContrato()
        }
    }

    // Método para o Fragment pai registrar o listener
    fun setOnEditRequestListener(listener: OnEditRequestListener) {
        this.editRequestListener = listener
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
        val contrato = arguments?.getParcelable<Contrato>(ARG_CONTRATO)
        if (contrato == null) {
            Toast.makeText(
                requireContext(),
                "Erro: Dados do contrato não disponíveis",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        
        // Verificar dados do cliente, incluindo Inscrição Estadual
        contrato.cliente?.let { cliente ->
            LogUtils.debug("ContratoDetailsDialog", """
                Dados do cliente para o PDF:
                Nome: ${cliente.contratante}
                CPF/CNPJ: ${cliente.cpfCnpj}
                RG/IE: ${cliente.rgIe ?: "Não informado"}
                Telefone: ${cliente.telefone ?: "Não informado"}
                Endereço: ${cliente.endereco}, ${cliente.bairro}, ${cliente.cidade}/${cliente.estado}
                É Pessoa Física: ${cliente.isPessoaFisica()}
            """.trimIndent())
        }
        
        // Mostrar dialog de progresso
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Gerando PDF, aguarde...")
            setCancelable(false)
            show()
        }
        
        // Verificar se o cliente completo já está no contrato
        val cliente = contrato.cliente
        
        // Chamar o serviço em uma coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val result = pdfService?.gerarPdfContrato(contrato, cliente)
                
                withContext(Dispatchers.Main) {
                    progressDialog.dismiss()
                    
                    if (result?.isSuccess == true) {
                        val pdfResponse = result.getOrNull()
                        if (pdfResponse != null && pdfResponse.success) {
                            // Mostrar o PDF/HTML no visualizador
                            if (pdfResponse.pdfBase64 != null) {
                                val pdfViewer = PdfViewerFragment.newInstance(
                                    pdfBase64 = pdfResponse.pdfBase64,
                                    contratoNumero = contrato.getContratoNumOuVazio(),
                                    htmlUrl = pdfResponse.htmlUrl,
                                    htmlContent = pdfResponse.htmlContent
                                )
                                
                                // Adicionar logs para verificar o conteúdo
                                LogUtils.debug("ContratoDetailsDialogFragment", "htmlUrl recebido: ${pdfResponse.htmlUrl}")
                                LogUtils.debug("ContratoDetailsDialogFragment", "htmlContent recebido: ${pdfResponse.htmlContent?.substring(0, minOf(50, pdfResponse.htmlContent.length))}...")
                                
                                pdfViewer.show(parentFragmentManager, "pdf_viewer")
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Erro: PDF não gerado corretamente",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Erro ao gerar PDF: ${pdfResponse?.message ?: "Resposta inválida"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        val error = result?.exceptionOrNull()?.message ?: "Erro desconhecido"
                        Toast.makeText(
                            requireContext(),
                            "Falha ao gerar PDF: $error",
                            Toast.LENGTH_LONG
                        ).show()
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
    }
}
