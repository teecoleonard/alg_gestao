package com.example.alg_gestao_02.service

import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.utils.LogUtils
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient
import com.example.alg_gestao_02.data.dto.AssinaturaRequestDTO

/**
 * Interface para comunicação com o gerador de PDF
 */
interface PdfApiService {
    @POST("api/contrato/gerar-pdf-direto")
    suspend fun gerarPdfContrato(@Body request: ContratoRequestDTO): Response<PdfResponse>
}

/**
 * Dados do cliente para o gerador de PDF
 */
data class ClientePdfDTO(
    val id: Int,
    val nome: String,
    val cpf: String? = null,
    val cnpj: String? = null,
    val inscricaoEstadual: String? = null,
    val email: String? = null,
    val telefone: String? = null,
    val endereco: String? = null
)

/**
 * Dados do produto (equipamento) para o gerador de PDF
 */
data class ProdutoPdfDTO(
    val id: Int,
    val nome: String,
    val descricao: String? = null,
    val valor: Double,
    val quantidade: Int
)

/**
 * Dados da assinatura para o gerador de PDF
 */
data class AssinaturaPdfDTO(
    val nome_arquivo: String
)

/**
 * Dados do contrato para o gerador de PDF
 */
data class ContratoPdfDTO(
    val id: Int,
    val numero: String,
    val data: String,
    val dataVencimento: String,
    val cliente: ClientePdfDTO,
    val produtos: List<ProdutoPdfDTO>,
    val valorTotal: Double,
    val observacoes: String,
    val status: String,
    val assinatura: AssinaturaPdfDTO? = null
)

/**
 * DTO para a requisição de geração de PDF
 */
data class ContratoRequestDTO(
    val contrato: ContratoPdfDTO,
    val incluirLogo: Boolean = true,
    val formatoPdf: String = "A4",
    val tipoContrato: String = "DETALHADO"
)

/**
 * DTO para a requisição de salvar assinatura
 */
data class AssinaturaRequestDTO(
    val base64Data: String,
    val contratoId: Int,
    val clienteId: Int
)

/**
 * Resposta da API de geração de PDF
 */
data class PdfResponse(
    val success: Boolean,
    val message: String,
    val pdfUrl: String? = null,
    @SerializedName("pdfBase64")
    val pdfBase64: String? = null,
    val htmlUrl: String? = null,
    val htmlContent: String? = null
)

/**
 * Resposta da API de salvar assinatura
 */
data class AssinaturaResponse(
    val success: Boolean,
    val message: String,
    val filePath: String? = null
)

/**
 * Serviço de geração de PDF
 */
class PdfService {
    private val pdfApiService: PdfApiService

    init {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.100.195:8080/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        pdfApiService = retrofit.create(PdfApiService::class.java)
    }

    /**
     * Converte um cliente do app para o formato esperado pelo gerador de PDF
     */
    private fun mapClienteToPdfDTO(cliente: Cliente): ClientePdfDTO {
        return ClientePdfDTO(
            id = cliente.id,
            nome = cliente.contratante,
            cpf = if (cliente.isPessoaFisica()) cliente.cpfCnpj else null,
            cnpj = if (!cliente.isPessoaFisica()) cliente.cpfCnpj else null,
            inscricaoEstadual = if (!cliente.isPessoaFisica()) cliente.rgIe else null,
            email = null, // O campo email não está disponível no modelo Cliente
            telefone = cliente.telefone,
            endereco = cliente.getEnderecoCompleto()
        )
    }

    /**
     * Converte um equipamento de contrato para o formato esperado pelo gerador de PDF
     */
    private fun mapEquipamentoToProdutoDTO(equipamento: EquipamentoContrato): ProdutoPdfDTO {
        return ProdutoPdfDTO(
            id = equipamento.id,
            nome = equipamento.nomeEquipamentoExibicao,
            descricao = "Quantidade: ${equipamento.quantidadeEquip}, Valor Unitário: R$ ${String.format("%.2f", equipamento.valorUnitario)}",
            valor = equipamento.valorUnitario,
            quantidade = equipamento.quantidadeEquip
        )
    }

    /**
     * Converte um contrato do app para o formato esperado pelo gerador de PDF
     */
    private fun mapContratoToPdfDTO(contrato: Contrato, cliente: Cliente? = null): ContratoPdfDTO {
        LogUtils.debug("PdfService", "Iniciando mapeamento do contrato #${contrato.id} para PDF")
        
        // Log detalhado sobre a assinatura
        contrato.assinatura?.let { assinatura ->
            LogUtils.debug("PdfService", "Assinatura encontrada no contrato: ID=${assinatura.id}, nome_arquivo=${assinatura.nome_arquivo}")
        } ?: run {
            LogUtils.debug("PdfService", "Nenhuma assinatura encontrada no contrato #${contrato.id}")
        }
        
        val clientePdf = cliente?.let { 
            LogUtils.debug("PdfService", "Mapeando dados do cliente #${cliente.id}")
            mapClienteToPdfDTO(it) 
        } ?: run {
            LogUtils.warning("PdfService", "Cliente não fornecido, usando dados básicos do contrato")
            ClientePdfDTO(
                id = contrato.clienteId,
                nome = contrato.resolverNomeCliente()
            )
        }
        
        // Verificar se a lista de equipamentos não é nula antes de fazer o map
        val produtosPdf = if (contrato.equipamentosParaExibicao.isEmpty()) {
            LogUtils.warning("PdfService", "Lista de equipamentos vazia para o contrato #${contrato.id}")
            emptyList()
        } else {
            LogUtils.debug("PdfService", "Mapeando ${contrato.equipamentosParaExibicao.size} equipamentos")
            contrato.equipamentosParaExibicao.map { mapEquipamentoToProdutoDTO(it) }
        }
        
        // Formatar as datas para o formato ISO
        val formatoOriginal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val dataEmissao = try {
            contrato.dataHoraEmissao ?: run {
                LogUtils.warning("PdfService", "Data de emissão não encontrada, usando data atual")
                formatoISO.format(Date())
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "Erro ao processar data de emissão", e)
            formatoISO.format(Date())
        }
        
        val dataVencimento = try {
            if (!contrato.dataVenc.isNullOrEmpty()) {
                val date = formatoOriginal.parse(contrato.getDataVencimentoFormatada())
                date?.let { formatoISO.format(it) } ?: run {
                    LogUtils.warning("PdfService", "Data de vencimento inválida, usando data atual")
                    formatoISO.format(Date())
                }
            } else {
                LogUtils.warning("PdfService", "Data de vencimento não encontrada, usando data atual")
                formatoISO.format(Date())
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "Erro ao converter data de vencimento", e)
            formatoISO.format(Date())
        }

        // Incluir a assinatura se estiver disponível
        val observacoes = StringBuilder().apply {
            append("Local da Obra: ${contrato.obraLocal ?: "Não especificado"}\n")
            append("Período do Contrato: ${contrato.contratoPeriodo ?: "Não especificado"}\n")
            append("Local de Entrega: ${contrato.entregaLocal ?: "Não especificado"}")
            
            // Adicionar informações da assinatura se disponível
            contrato.assinatura?.let { assinatura ->
                LogUtils.debug("PdfService", "Adicionando informações da assinatura ao PDF")
                append("\n\nAssinatura: ${assinatura.nome_arquivo}")
            } ?: run {
                LogUtils.warning("PdfService", "Nenhuma assinatura encontrada para o contrato #${contrato.id}")
            }
        }.toString()
        
        val assinaturaPdfDTO = contrato.assinatura?.let { assinatura ->
            if (!assinatura.nome_arquivo.isNullOrEmpty()) {
                LogUtils.debug("PdfService", "Incluindo assinatura no PDF: ${assinatura.nome_arquivo}")
                AssinaturaPdfDTO(assinatura.nome_arquivo)
            } else {
                LogUtils.warning("PdfService", "Nome do arquivo de assinatura está vazio ou nulo")
                null
            }
        }
        
        val contratoPdfDTO = ContratoPdfDTO(
            id = contrato.id,
            numero = contrato.getContratoNumOuVazio(),
            data = dataEmissao,
            dataVencimento = dataVencimento,
            cliente = clientePdf,
            produtos = produtosPdf,
            valorTotal = contrato.getValorEfetivo(),
            observacoes = observacoes,
            status = if (contrato.isAssinado()) "FINALIZADO" else "ATIVO",
            assinatura = assinaturaPdfDTO
        )
        
        LogUtils.debug("PdfService", "Mapeamento do contrato concluído com sucesso")
        return contratoPdfDTO
    }

    /**
     * Gera um PDF para um contrato
     */
    suspend fun gerarPdfContrato(contrato: Contrato, cliente: Cliente? = null): Result<PdfResponse> {
        return try {
            LogUtils.debug("PdfService", "Iniciando geração de PDF para contrato #${contrato.contratoNum}")
            LogUtils.debug("PdfService", "Iniciando chamada para gerar PDF na porta 8080")
            
            // Validar dados do contrato
            if (contrato.id <= 0) {
                val error = "ID do contrato inválido: ${contrato.id}"
                LogUtils.error("PdfService", error)
                return Result.failure(Exception(error))
            }
            
            if (contrato.getContratoNumOuVazio().isBlank()) {
                val error = "Número do contrato inválido"
                LogUtils.error("PdfService", error)
                return Result.failure(Exception(error))
            }
            
            val contratoPdfDTO = mapContratoToPdfDTO(contrato, cliente)
            LogUtils.debug("PdfService", "Dados do contrato mapeados com sucesso")
            
            val request = ContratoRequestDTO(
                contrato = contratoPdfDTO,
                incluirLogo = true,
                formatoPdf = "A4",
                tipoContrato = "DETALHADO"
            )
            
            LogUtils.debug("PdfService", "Enviando requisição para gerar PDF")
            val response = pdfApiService.gerarPdfContrato(request)
            LogUtils.debug("PdfService", "Resposta recebida do serviço de PDF: ${response.code()}")
            
            if (response.isSuccessful) {
                val pdfResponse = response.body()
                if (pdfResponse != null) {
                    if (pdfResponse.success) {
                        LogUtils.debug("PdfService", "PDF gerado com sucesso: ${pdfResponse.message}")
                        Result.success(pdfResponse)
                    } else {
                        val error = "Erro na geração do PDF: ${pdfResponse.message}"
                        LogUtils.error("PdfService", error)
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Resposta vazia do serviço de PDF"
                    LogUtils.error("PdfService", error)
                    Result.failure(Exception(error))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                val error = "Erro ao gerar PDF: $errorBody"
                LogUtils.error("PdfService", error)
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "Erro ao gerar PDF", e)
            Result.failure(e)
        }
    }
} 