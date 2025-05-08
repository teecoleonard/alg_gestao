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

class ContratoDetailsDialogFragment : DialogFragment() {

    private var contrato: Contrato? = null
    private var editRequestListener: OnEditRequestListener? = null

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
        val tvDataEmissao: TextView = view.findViewById(R.id.tvDetalhesContratoDataEmissao)
        val tvDataVenc: TextView = view.findViewById(R.id.tvDetalhesContratoDataVenc)
        val tvLocalObra: TextView = view.findViewById(R.id.tvDetalhesContratoLocalObra)
        val tvPeriodo: TextView = view.findViewById(R.id.tvDetalhesContratoPeriodo)
        val tvLocalEntrega: TextView = view.findViewById(R.id.tvDetalhesContratoLocalEntrega)
        val tvResponsavel: TextView = view.findViewById(R.id.tvDetalhesContratoResponsavel)
        val tvAssinatura: TextView = view.findViewById(R.id.tvDetalhesContratoAssinatura)
        val tvNumEquipamentos: TextView = view.findViewById(R.id.tvDetalhesContratoNumEquipamentos)
        val tvValorTotal: TextView = view.findViewById(R.id.tvDetalhesContratoValorTotal)
        val btnFechar: Button = view.findViewById(R.id.btnFecharDetalhesContrato)
        val btnEditar: Button = view.findViewById(R.id.btnEditarContrato)

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
            // Usar os métodos de formatação da classe Contrato (sem parâmetros)
            tvDataEmissao.text = c.getDataEmissaoFormatada()
            tvDataVenc.text = c.getDataVencimentoFormatada()
            tvLocalObra.text = "Obra: ${c.obraLocal ?: "Não informado"}"
            tvPeriodo.text = "Período: ${c.contratoPeriodo ?: "Não informado"}"
            tvLocalEntrega.text = "Entrega: ${c.entregaLocal ?: "Não informado"}"
            tvResponsavel.text = "Responsável: ${c.respPedido ?: "Não informado"}"
            tvAssinatura.text = c.contratoAss ?: "Não assinado"
            
            val numEquip = c.equipamentos?.size ?: 0 
            tvNumEquipamentos.text = "Quantidade: $numEquip"

            // Log detalhado da lista de equipamentos e seus valores totais
            if (c.equipamentos.isNullOrEmpty()) {
                LogUtils.debug("ContratoDetailsDialog", "Lista de equipamentos está nula ou vazia.")
            } else {
                LogUtils.debug("ContratoDetailsDialog", "Detalhes dos equipamentos no contrato:")
                c.equipamentos!!.forEachIndexed { index, equip ->
                    LogUtils.debug("ContratoDetailsDialog", 
                        "  Equipamento[$index]: ID=${equip.id}, Nome=${equip.nomeEquipamentoExibicao}, ValorTotal=${equip.valorTotal}")
                }
            }
            
            // Calcular o valor total a partir dos equipamentos
            // Se houver equipamentos, somar seus valores totais, senão usar o contratoValor
            val valorTotalCalculado = if (c.equipamentos.isNullOrEmpty()) {
                c.contratoValor
            } else {
                c.equipamentos.sumOf { it.valorTotal ?: 0.0 }
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
            contrato?.let { c -> // Usar a variável local 'c'
                LogUtils.debug("ContratoDetailsDialog", "Botão Editar clicado para contrato ID: ${c.id}")
                // Notifica o listener (o Fragment pai) que a edição foi solicitada
                editRequestListener?.onEditRequested(c)
                // Fecha este diálogo de detalhes
                dismiss() 
            }
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
}
