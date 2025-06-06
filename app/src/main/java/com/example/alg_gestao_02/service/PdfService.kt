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
    private val baseUrl = "http://45.10.160.10:8080/"

    init {
        LogUtils.debug("PdfService", "🔧 INICIALIZANDO SERVIÇO DE PDF")
        LogUtils.debug("PdfService", "📡 URL do servidor: $baseUrl")
        LogUtils.debug("PdfService", "⏱️ Timeout de conexão: 30 segundos")
        LogUtils.debug("PdfService", "⏱️ Timeout de leitura: 30 segundos")
        LogUtils.debug("PdfService", "⏱️ Timeout de escrita: 30 segundos")
        
        val client = OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                val startTime = System.currentTimeMillis()
                
                LogUtils.debug("PdfService", "🌐 REQUISIÇÃO HTTP INICIADA")
                LogUtils.debug("PdfService", "  📍 URL: ${request.url}")
                LogUtils.debug("PdfService", "  🎯 Método: ${request.method}")
                LogUtils.debug("PdfService", "  📦 Content-Type: ${request.header("Content-Type")}")
                
                try {
                    val response = chain.proceed(request)
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    LogUtils.debug("PdfService", "✅ RESPOSTA HTTP RECEBIDA")
                    LogUtils.debug("PdfService", "  🎯 Status Code: ${response.code}")
                    LogUtils.debug("PdfService", "  📊 Status Message: ${response.message}")
                    LogUtils.debug("PdfService", "  ⏱️ Tempo de resposta: ${duration}ms")
                    LogUtils.debug("PdfService", "  📄 Content-Type: ${response.header("Content-Type")}")
                    LogUtils.debug("PdfService", "  📐 Content-Length: ${response.header("Content-Length") ?: "Não informado"}")
                    
                    if (response.isSuccessful) {
                        LogUtils.debug("PdfService", "🎉 CONEXÃO COM SERVIDOR ESTABELECIDA COM SUCESSO!")
                    } else {
                        LogUtils.warning("PdfService", "⚠️ RESPOSTA COM ERRO DO SERVIDOR")
                        LogUtils.warning("PdfService", "  ❌ Status: ${response.code} - ${response.message}")
                    }
                    
                    response
                } catch (e: Exception) {
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime
                    
                    LogUtils.error("PdfService", "🚨 ERRO DE CONECTIVIDADE")
                    LogUtils.error("PdfService", "  ⏱️ Tempo até erro: ${duration}ms")
                    LogUtils.error("PdfService", "  📍 URL tentada: ${request.url}")
                    LogUtils.error("PdfService", "  💥 Tipo do erro: ${e.javaClass.simpleName}")
                    LogUtils.error("PdfService", "  📝 Mensagem: ${e.message}")
                    
                    when (e) {
                        is java.net.ConnectException -> {
                            LogUtils.error("PdfService", "🔌 FALHA DE CONEXÃO: Servidor pode estar offline ou inacessível")
                        }
                        is java.net.SocketTimeoutException -> {
                            LogUtils.error("PdfService", "⏰ TIMEOUT: Servidor não respondeu a tempo")
                        }
                        is java.net.UnknownHostException -> {
                            LogUtils.error("PdfService", "🌐 DNS/HOST: Não foi possível resolver o endereço")
                        }
                        else -> {
                            LogUtils.error("PdfService", "❓ ERRO DESCONHECIDO de conectividade")
                        }
                    }
                    
                    throw e
                }
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        pdfApiService = retrofit.create(PdfApiService::class.java)
        LogUtils.debug("PdfService", "✅ SERVIÇO DE PDF INICIALIZADO COM SUCESSO")
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
        LogUtils.debug("PdfService", "=== INÍCIO DO MAPEAMENTO DO CONTRATO ===")
        LogUtils.debug("PdfService", "Contrato ID: ${contrato.id}")
        LogUtils.debug("PdfService", "Contrato Número: ${contrato.contratoNum}")
        LogUtils.debug("PdfService", "Status Assinatura: ${contrato.status_assinatura}")
        
        // Log detalhado sobre a assinatura
        contrato.assinatura?.let { assinatura ->
            LogUtils.debug("PdfService", "✅ ASSINATURA ENCONTRADA NO CONTRATO:")
            LogUtils.debug("PdfService", "  - ID da assinatura: ${assinatura.id}")
            LogUtils.debug("PdfService", "  - Nome do arquivo: '${assinatura.nome_arquivo}'")
            LogUtils.debug("PdfService", "  - Contrato ID da assinatura: ${assinatura.contrato_id}")
            LogUtils.debug("PdfService", "  - Cliente ID da assinatura: ${assinatura.cliente_id}")
        } ?: run {
            LogUtils.warning("PdfService", "❌ NENHUMA ASSINATURA ENCONTRADA NO CONTRATO #${contrato.id}")
            LogUtils.debug("PdfService", "Status da assinatura do contrato: ${contrato.status_assinatura}")
            LogUtils.debug("PdfService", "Data da assinatura do contrato: ${contrato.data_assinatura}")
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
                LogUtils.warning("PdfService", "Nenhuma assinatura encontrada para adicionar às observações")
            }
        }.toString()
        
        val assinaturaPdfDTO = contrato.assinatura?.let { assinatura ->
            if (!assinatura.nome_arquivo.isNullOrEmpty()) {
                LogUtils.debug("PdfService", "✅ CRIANDO AssinaturaPdfDTO com nome: '${assinatura.nome_arquivo}'")
                AssinaturaPdfDTO(assinatura.nome_arquivo)
            } else {
                LogUtils.warning("PdfService", "❌ Nome do arquivo de assinatura está vazio ou nulo")
                null
            }
        } ?: run {
            LogUtils.warning("PdfService", "❌ Contrato não possui assinatura associada")
            null
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
        
        LogUtils.debug("PdfService", "✅ RESULTADO DO MAPEAMENTO:")
        LogUtils.debug("PdfService", "  - Assinatura no DTO: ${if (assinaturaPdfDTO != null) "SIM (${assinaturaPdfDTO.nome_arquivo})" else "NÃO"}")
        LogUtils.debug("PdfService", "=== FIM DO MAPEAMENTO DO CONTRATO ===")
        
        return contratoPdfDTO
    }

    /**
     * Gera um PDF para um contrato
     */
    suspend fun gerarPdfContrato(contrato: Contrato, cliente: Cliente? = null): Result<PdfResponse> {
        return try {
            LogUtils.debug("PdfService", "🚀 INICIANDO GERAÇÃO DE PDF")
            LogUtils.debug("PdfService", "📊 INFORMAÇÕES DA REQUISIÇÃO:")
            LogUtils.debug("PdfService", "  🎯 Servidor destino: $baseUrl")
            LogUtils.debug("PdfService", "  📋 Contrato #${contrato.contratoNum} (ID: ${contrato.id})")
            LogUtils.debug("PdfService", "  🏢 Cliente: ${contrato.resolverNomeCliente()}")
            LogUtils.debug("PdfService", "  💰 Valor: R$ ${String.format("%.2f", contrato.getValorEfetivo())}")
            LogUtils.debug("PdfService", "  📝 Status: ${if (contrato.isAssinado()) "ASSINADO" else "PENDENTE"}")
            
            // Testar conectividade básica
            LogUtils.debug("PdfService", "🔍 TESTANDO CONECTIVIDADE COM SERVIDOR...")
            
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
            
            LogUtils.debug("PdfService", "✅ VALIDAÇÃO DOS DADOS CONCLUÍDA")
            LogUtils.debug("PdfService", "🔄 INICIANDO MAPEAMENTO DOS DADOS...")
            
            val contratoPdfDTO = mapContratoToPdfDTO(contrato, cliente)
            LogUtils.debug("PdfService", "✅ Dados do contrato mapeados com sucesso")
            
            val request = ContratoRequestDTO(
                contrato = contratoPdfDTO,
                incluirLogo = true,
                formatoPdf = "A4",
                tipoContrato = "DETALHADO"
            )
            
            // Log do payload que será enviado (resumido)
            LogUtils.debug("PdfService", "📦 PAYLOAD DA REQUISIÇÃO:")
            LogUtils.debug("PdfService", "  📄 Contrato ID: ${request.contrato.id}")
            LogUtils.debug("PdfService", "  📄 Número: ${request.contrato.numero}")
            LogUtils.debug("PdfService", "  👤 Cliente: ${request.contrato.cliente.nome}")
            LogUtils.debug("PdfService", "  🛠️ Produtos: ${request.contrato.produtos.size} itens")
            LogUtils.debug("PdfService", "  💰 Valor Total: R$ ${String.format("%.2f", request.contrato.valorTotal)}")
            LogUtils.debug("PdfService", "  ✍️ Tem Assinatura: ${request.contrato.assinatura != null}")
            LogUtils.debug("PdfService", "  🎨 Formato: ${request.formatoPdf}")
            LogUtils.debug("PdfService", "  🖼️ Incluir Logo: ${request.incluirLogo}")
            
            val startTime = System.currentTimeMillis()
            LogUtils.debug("PdfService", "📡 ENVIANDO REQUISIÇÃO HTTP...")
            LogUtils.debug("PdfService", "  🌐 URL: ${baseUrl}api/contrato/gerar-pdf-direto")
            LogUtils.debug("PdfService", "  ⏰ Iniciado em: ${SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())}")
            
            val response = pdfApiService.gerarPdfContrato(request)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            LogUtils.debug("PdfService", "📥 RESPOSTA RECEBIDA DO SERVIDOR")
            LogUtils.debug("PdfService", "  ⏱️ Tempo total da requisição: ${duration}ms")
            LogUtils.debug("PdfService", "  📊 Status HTTP: ${response.code()}")
            LogUtils.debug("PdfService", "  📝 Mensagem HTTP: ${response.message()}")
            LogUtils.debug("PdfService", "  🔍 Headers relevantes:")
            LogUtils.debug("PdfService", "    - Content-Type: ${response.headers()["Content-Type"]}")
            LogUtils.debug("PdfService", "    - Content-Length: ${response.headers()["Content-Length"]}")
            LogUtils.debug("PdfService", "    - Server: ${response.headers()["Server"] ?: "Não informado"}")
            
            if (response.isSuccessful) {
                LogUtils.debug("PdfService", "🎉 RESPOSTA HTTP BEM-SUCEDIDA!")
                
                val pdfResponse = response.body()
                if (pdfResponse != null) {
                    LogUtils.debug("PdfService", "📄 ANALISANDO RESPOSTA DO PDF:")
                    LogUtils.debug("PdfService", "  ✅ Success: ${pdfResponse.success}")
                    LogUtils.debug("PdfService", "  📝 Message: ${pdfResponse.message}")
                    LogUtils.debug("PdfService", "  🔗 PDF URL: ${pdfResponse.pdfUrl ?: "Não fornecida"}")
                    LogUtils.debug("PdfService", "  🌐 HTML URL: ${pdfResponse.htmlUrl ?: "Não fornecida"}")
                    LogUtils.debug("PdfService", "  📊 PDF Base64: ${if (pdfResponse.pdfBase64 != null) "Fornecido (${pdfResponse.pdfBase64.length} chars)" else "Não fornecido"}")
                    LogUtils.debug("PdfService", "  📰 HTML Content: ${if (pdfResponse.htmlContent != null) "Fornecido (${pdfResponse.htmlContent.length} chars)" else "Não fornecido"}")
                    
                    if (pdfResponse.success) {
                        LogUtils.debug("PdfService", "🏆 PDF GERADO COM SUCESSO TOTAL!")
                        LogUtils.debug("PdfService", "  📈 Resumo da operação:")
                        LogUtils.debug("PdfService", "    - Servidor: ONLINE e FUNCIONAL ✅")
                        LogUtils.debug("PdfService", "    - Conectividade: PERFEITA ✅")
                        LogUtils.debug("PdfService", "    - Tempo resposta: ${duration}ms ✅")
                        LogUtils.debug("PdfService", "    - PDF gerado: SIM ✅")
                        Result.success(pdfResponse)
                    } else {
                        val error = "Erro na geração do PDF: ${pdfResponse.message}"
                        LogUtils.error("PdfService", "❌ FALHA NA GERAÇÃO DO PDF:")
                        LogUtils.error("PdfService", "  🔴 Servidor: ONLINE mas com erro no processamento")
                        LogUtils.error("PdfService", "  📝 Erro: ${pdfResponse.message}")
                        Result.failure(Exception(error))
                    }
                } else {
                    val error = "Resposta vazia do serviço de PDF"
                    LogUtils.error("PdfService", "❌ RESPOSTA VAZIA:")
                    LogUtils.error("PdfService", "  🔴 Servidor: ONLINE mas retornou body vazio")
                    LogUtils.error("PdfService", "  📊 Status Code: ${response.code()}")
                    Result.failure(Exception(error))
                }
            } else {
                val errorBody = response.errorBody()?.string() ?: response.message()
                val error = "Erro HTTP ${response.code()}: $errorBody"
                LogUtils.error("PdfService", "🚨 ERRO HTTP DO SERVIDOR:")
                LogUtils.error("PdfService", "  🔴 Status Code: ${response.code()}")
                LogUtils.error("PdfService", "  📝 Status Message: ${response.message()}")
                LogUtils.error("PdfService", "  📄 Error Body: $errorBody")
                LogUtils.error("PdfService", "  ⏱️ Tempo até erro: ${duration}ms")
                
                when (response.code()) {
                    404 -> LogUtils.error("PdfService", "  🎯 Diagnóstico: Endpoint não encontrado - Verificar se o servidor está rodando corretamente")
                    500 -> LogUtils.error("PdfService", "  💥 Diagnóstico: Erro interno do servidor - Verificar logs do servidor PDF")
                    503 -> LogUtils.error("PdfService", "  🚫 Diagnóstico: Serviço indisponível - Servidor pode estar sobrecarregado")
                    else -> LogUtils.error("PdfService", "  ❓ Diagnóstico: Erro HTTP genérico")
                }
                
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            LogUtils.error("PdfService", "💥 EXCEÇÃO DURANTE GERAÇÃO DE PDF:")
            LogUtils.error("PdfService", "  🔴 Tipo: ${e.javaClass.simpleName}")
            LogUtils.error("PdfService", "  📝 Mensagem: ${e.message}")
            LogUtils.error("PdfService", "  🌐 Servidor testado: $baseUrl")
            
            when (e) {
                is java.net.ConnectException -> {
                    LogUtils.error("PdfService", "  🔌 DIAGNÓSTICO: SERVIDOR OFFLINE ou INACESSÍVEL")
                    LogUtils.error("PdfService", "    - Verificar se o servidor PDF está rodando")
                    LogUtils.error("PdfService", "    - Verificar se a porta 8080 está aberta")
                    LogUtils.error("PdfService", "    - Verificar conectividade de rede")
                }
                is java.net.SocketTimeoutException -> {
                    LogUtils.error("PdfService", "  ⏰ DIAGNÓSTICO: TIMEOUT - Servidor muito lento")
                    LogUtils.error("PdfService", "    - Servidor pode estar sobrecarregado")
                    LogUtils.error("PdfService", "    - Conexão de rede pode estar lenta")
                }
                is java.net.UnknownHostException -> {
                    LogUtils.error("PdfService", "  🌐 DIAGNÓSTICO: ERRO DE DNS/HOST")
                    LogUtils.error("PdfService", "    - IP 45.10.160.10 não está acessível")
                    LogUtils.error("PdfService", "    - Verificar configuração de rede")
                }
                else -> {
                    LogUtils.error("PdfService", "  ❓ DIAGNÓSTICO: Erro desconhecido")
                }
            }
            
            Result.failure(e)
        }
    }
} 