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
import java.text.SimpleDateFormat
import java.util.*
import okhttp3.OkHttpClient

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
    val observacoes: String? = null,
    val status: String = "ATIVO"
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
 * Serviço de geração de PDF
 */
class PdfService {
    // Configurando um cliente OkHttp com timeout
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()
    
    // URL do servidor de PDF - deve ser atualizada conforme o ambiente
    private val pdfServerUrl = "http://192.168.100.195:8080/"
    
    private val retrofit = Retrofit.Builder()
        .baseUrl(pdfServerUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()

    private val pdfApiService = retrofit.create(PdfApiService::class.java)

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
        val clientePdf = cliente?.let { mapClienteToPdfDTO(it) } ?: 
            ClientePdfDTO(
                id = contrato.clienteId,
                nome = contrato.resolverNomeCliente()
            )
        
        // Verificar se a lista de equipamentos não é nula antes de fazer o map
        val produtosPdf = if (contrato.equipamentosParaExibicao.isEmpty()) {
            emptyList()
        } else {
            contrato.equipamentosParaExibicao.map { mapEquipamentoToProdutoDTO(it) }
        }
        
        // Formatar as datas para o formato ISO
        val formatoOriginal = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formatoISO = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val dataEmissao = try {
            contrato.dataHoraEmissao ?: formatoISO.format(Date())
        } catch (e: Exception) {
            formatoISO.format(Date())
        }
        
        val dataVencimento = try {
            if (!contrato.dataVenc.isNullOrEmpty()) {
                val date = formatoOriginal.parse(contrato.getDataVencimentoFormatada())
                date?.let { formatoISO.format(it) } ?: formatoISO.format(Date())
            } else {
                formatoISO.format(Date())
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "Erro ao converter data de vencimento", e)
            formatoISO.format(Date())
        }
        
        return ContratoPdfDTO(
            id = contrato.id,
            numero = contrato.getContratoNumOuVazio(),
            data = dataEmissao,
            dataVencimento = dataVencimento,
            cliente = clientePdf,
            produtos = produtosPdf,
            valorTotal = contrato.getValorEfetivo(),
            observacoes = "Local da Obra: ${contrato.obraLocal ?: "Não especificado"}\n" +
                         "Período do Contrato: ${contrato.contratoPeriodo ?: "Não especificado"}\n" +
                         "Local de Entrega: ${contrato.entregaLocal ?: "Não especificado"}",
            status = if (contrato.isAssinado()) "FINALIZADO" else "ATIVO"
        )
    }

    /**
     * Gera um PDF para um contrato
     */
    suspend fun gerarPdfContrato(contrato: Contrato, cliente: Cliente? = null): Result<PdfResponse> {
        return try {
            LogUtils.debug("PdfService", "Iniciando geração de PDF para contrato #${contrato.contratoNum}")
            
            val contratoPdfDTO = mapContratoToPdfDTO(contrato, cliente)
            val request = ContratoRequestDTO(
                contrato = contratoPdfDTO,
                incluirLogo = true,
                formatoPdf = "A4",
                tipoContrato = "DETALHADO"
            )
            
            val response = pdfApiService.gerarPdfContrato(request)
            
            if (response.isSuccessful) {
                val pdfResponse = response.body()
                if (pdfResponse != null) {
                    LogUtils.debug("PdfService", "PDF gerado com sucesso: ${pdfResponse.message}")
                    Result.success(pdfResponse)
                } else {
                    LogUtils.error("PdfService", "Resposta vazia do serviço de PDF")
                    Result.failure(Exception("Resposta vazia do serviço de PDF"))
                }
            } else {
                val errorMessage = "Erro ao gerar PDF: ${response.errorBody()?.string() ?: response.message()}"
                LogUtils.error("PdfService", errorMessage)
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "Erro ao gerar PDF", e)
            Result.failure(e)
        }
    }
} 