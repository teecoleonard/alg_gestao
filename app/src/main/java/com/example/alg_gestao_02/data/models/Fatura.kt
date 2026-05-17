package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FaturaCliente(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName(value = "nome", alternate = ["contratante"])
    val nome: String? = null,
    @SerializedName(value = "cpfCnpj", alternate = ["cpf_cnpj"])
    val cpfCnpj: String? = null,
    @SerializedName("telefone")
    val telefone: String? = null,
    @SerializedName("endereco")
    val endereco: String? = null,
    @SerializedName(value = "rgIe", alternate = ["rg_ie"])
    val rgIe: String? = null,
    @SerializedName("bairro")
    val bairro: String? = null,
    @SerializedName("cidade")
    val cidade: String? = null,
    @SerializedName("estado")
    val estado: String? = null,
    @SerializedName("cep")
    val cep: String? = null,
) : Parcelable

@Parcelize
data class FaturaEquipamentoDetalhe(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName(value = "contratoId", alternate = ["contrato_id"])
    val contratoId: Int = 0,
    @SerializedName(value = "cicloId", alternate = ["contratoCicloId", "contrato_ciclo_id"])
    val cicloId: Int? = null,
    @SerializedName(value = "referenciaCiclo", alternate = ["referencia_ciclo"])
    val referenciaCiclo: String? = null,
    @SerializedName(value = "equipamentoNome", alternate = ["nomeEquip"])
    val equipamentoNome: String? = null,
    @SerializedName("quantidadeEquip")
    val quantidadeEquip: Int = 0,
    @SerializedName("valorUnitario")
    val valorUnitario: Double = 0.0,
    @SerializedName("contratoNumero")
    val contratoNumero: String? = null,
    @SerializedName("obraLocal")
    val obraLocal: String? = null,
    @SerializedName(value = "recebidoPor", alternate = ["recebido_por"])
    val recebidoPor: String? = null,
    @SerializedName(value = "entregueCpf", alternate = ["entregue_cpf"])
    val entregueCpf: String? = null,
    @SerializedName("dataDevolucao")
    val dataDevolucao: String? = null,
    @SerializedName("dataEmissaoContrato")
    val dataEmissaoContrato: String? = null,
    @SerializedName("dataVencimentoContrato")
    val dataVencimentoContrato: String? = null,
    @SerializedName("contratoPeriodo")
    val contratoPeriodo: String? = null,
) : Parcelable

@Parcelize
data class FaturaMaterialDetalhe(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName(value = "contratoId", alternate = ["contrato_id"])
    val contratoId: Int = 0,
    @SerializedName(value = "cicloId", alternate = ["contratoCicloId", "contrato_ciclo_id"])
    val cicloId: Int? = null,
    @SerializedName(value = "referenciaCiclo", alternate = ["referencia_ciclo"])
    val referenciaCiclo: String? = null,
    @SerializedName("materialNome")
    val materialNome: String? = null,
    @SerializedName("materialCodigo")
    val materialCodigo: String? = null,
    @SerializedName("quantidadeMaterial")
    val quantidadeMaterial: Int = 0,
    @SerializedName("valorUnitario")
    val valorUnitario: Double = 0.0,
    @SerializedName("valorTotal")
    val valorTotal: Double = 0.0,
    @SerializedName("contratoNumero")
    val contratoNumero: String? = null,
) : Parcelable

@Parcelize
data class Fatura(
    @SerializedName("id")
    val id: Int = 0,
    @SerializedName("clienteId")
    val clienteId: Int = 0,
    @SerializedName("numero")
    val numero: String = "",
    @SerializedName("periodo")
    val periodo: String? = null,
    @SerializedName("dataEmissao")
    val dataEmissao: String? = null,
    @SerializedName("dataVencimento")
    val dataVencimento: String? = null,
    @SerializedName("valorContratos")
    val valorContratos: Double = 0.0,
    @SerializedName("desconto")
    val desconto: Double = 0.0,
    @SerializedName("valorAdicional")
    val valorAdicional: Double = 0.0,
    @SerializedName("valorTotal")
    val valorTotal: Double = 0.0,
    @SerializedName("status")
    val status: String? = null,
    @SerializedName(value = "statusAssinatura", alternate = ["status_assinatura"])
    val statusAssinatura: String? = null,
    @SerializedName(value = "dataAssinatura", alternate = ["data_assinatura"])
    val dataAssinatura: String? = null,
    @SerializedName("observacoes")
    val observacoes: String? = null,
    @SerializedName("caminhoArquivo")
    val caminhoArquivo: String? = null,
    @SerializedName("cliente")
    val cliente: FaturaCliente? = null,
    @SerializedName("equipamentosDetalhados")
    val equipamentosDetalhados: List<FaturaEquipamentoDetalhe> = emptyList(),
    @SerializedName("materiaisDetalhados")
    val materiaisDetalhados: List<FaturaMaterialDetalhe> = emptyList(),
) : Parcelable

data class FaturasPageResponse(
    @SerializedName("data")
    val data: List<Fatura> = emptyList(),
    @SerializedName("total")
    val total: Int = 0,
    @SerializedName("page")
    val page: Int = 1,
    @SerializedName("limit")
    val limit: Int = 20,
    @SerializedName("totalPages")
    val totalPages: Int = 1,
)

data class FaturaPdfInfo(
    @SerializedName("numero")
    val numero: String,
    @SerializedName("mesReferencia")
    val mesReferencia: String,
    @SerializedName("periodo")
    val periodo: String = "Mensal",
    @SerializedName("observacoes")
    val observacoes: String = "",
    @SerializedName("subtotal")
    val subtotal: Double = 0.0,
    @SerializedName("frete")
    val frete: Double = 0.0,
    @SerializedName("desconto")
    val desconto: Double = 0.0,
    @SerializedName("valorTotal")
    val valorTotal: Double = 0.0,
)

data class FaturaPdfCliente(
    @SerializedName("nome")
    val nome: String,
    @SerializedName("cpf")
    val cpf: String? = null,
    @SerializedName("cnpj")
    val cnpj: String? = null,
    @SerializedName("inscricaoEstadual")
    val inscricaoEstadual: String? = null,
    @SerializedName("rg")
    val rg: String? = null,
    @SerializedName("telefone")
    val telefone: String? = null,
    @SerializedName("endereco")
    val endereco: String? = null,
)

data class FaturaPdfEquipamento(
    @SerializedName("contrato")
    val contrato: String,
    @SerializedName("quantidade")
    val quantidade: Int,
    @SerializedName("descricao")
    val descricao: String,
    @SerializedName("obra")
    val obra: String = "-",
    @SerializedName("recebidoPor")
    val recebidoPor: String = "",
    @SerializedName("entregueCpf")
    val entregueCpf: String = "",
    @SerializedName("dataDevolucao")
    val dataDevolucao: String = "",
    @SerializedName("periodo")
    val periodo: String = "",
    @SerializedName("dataEmissaoContrato")
    val dataEmissaoContrato: String = "",
    @SerializedName("dataVencimentoContrato")
    val dataVencimentoContrato: String = "",
    @SerializedName("valorUnitario")
    val valorUnitario: Double = 0.0,
)

data class FaturaPdfMaterial(
    @SerializedName("contrato")
    val contrato: String,
    @SerializedName("quantidade")
    val quantidade: Int,
    @SerializedName("descricao")
    val descricao: String,
    @SerializedName("codigo")
    val codigo: String? = null,
    @SerializedName("valorUnitario")
    val valorUnitario: Double = 0.0,
    @SerializedName("valorTotal")
    val valorTotal: Double = 0.0,
)

data class FaturaPdfRequest(
    @SerializedName("fatura")
    val fatura: FaturaPdfInfo,
    @SerializedName("cliente")
    val cliente: FaturaPdfCliente,
    @SerializedName("equipamentos")
    val equipamentos: List<FaturaPdfEquipamento> = emptyList(),
    @SerializedName("materiais")
    val materiais: List<FaturaPdfMaterial> = emptyList(),
    @SerializedName("materiaisTotal")
    val materiaisTotal: Double = 0.0,
    @SerializedName("totalGeral")
    val totalGeral: Double = 0.0,
    @SerializedName("dataEmissao")
    val dataEmissao: String? = null,
    @SerializedName("dataVencimento")
    val dataVencimento: String? = null,
)

data class FaturaPdfResponse(
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("pdfUrl")
    val pdfUrl: String? = null,
    @SerializedName("htmlUrl")
    val htmlUrl: String? = null,
    @SerializedName("pdfBase64")
    val pdfBase64: String? = null,
    @SerializedName("htmlContent")
    val htmlContent: String? = null,
)
