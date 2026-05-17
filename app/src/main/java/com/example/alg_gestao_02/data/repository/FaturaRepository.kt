package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.BuildConfig
import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.models.Fatura
import com.example.alg_gestao_02.data.models.FaturaPdfCliente
import com.example.alg_gestao_02.data.models.FaturaPdfEquipamento
import com.example.alg_gestao_02.data.models.FaturaPdfInfo
import com.example.alg_gestao_02.data.models.FaturaPdfMaterial
import com.example.alg_gestao_02.data.models.FaturaPdfRequest
import com.example.alg_gestao_02.data.models.FaturaPdfResponse
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.Locale

data class FaturasListResult(
    val data: List<Fatura>,
    val page: Int,
    val limit: Int,
    val total: Int,
    val totalPages: Int,
)

class FaturaRepository {
    private val apiService = ApiClient.faturasApiService
    private val httpClient = OkHttpClient.Builder().build()

    suspend fun getFaturas(
        page: Int = 1,
        limit: Int = 50,
        search: String? = null,
        status: String? = null,
    ): Resource<FaturasListResult> {
        return try {
            val response = apiService.getFaturas(page = page, limit = limit, search = search, status = status)
            if (!response.isSuccessful) {
                return Resource.Error("Erro ao carregar faturas: ${response.message()}")
            }

            val body = response.body()
            if (body == null) {
                Resource.Error("Resposta vazia ao carregar faturas")
            } else {
                Resource.Success(
                    FaturasListResult(
                        data = body.data,
                        page = body.page,
                        limit = body.limit,
                        total = body.total,
                        totalPages = body.totalPages,
                    ),
                )
            }
        } catch (e: Exception) {
            LogUtils.error("FaturaRepository", "Erro ao carregar faturas", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun getFaturaById(id: Int): Resource<Fatura> {
        return try {
            val response = apiService.getFaturaById(id)
            if (!response.isSuccessful) {
                return Resource.Error("Erro ao carregar fatura: ${response.message()}")
            }

            response.body()?.let { Resource.Success(it) }
                ?: Resource.Error("Fatura não encontrada")
        } catch (e: Exception) {
            LogUtils.error("FaturaRepository", "Erro ao carregar fatura", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun gerarPdfFatura(faturaId: Int): Resource<Pair<Fatura, FaturaPdfResponse>> {
        val faturaResult = getFaturaById(faturaId)
        if (faturaResult !is Resource.Success) {
            return Resource.Error(faturaResult.message ?: "Não foi possivel carregar a fatura")
        }

        val fatura = faturaResult.data
        return try {
            val request = mapearRequestPdf(fatura)
            val response = apiService.gerarPdfFatura(request)

            if (!response.isSuccessful) {
                return Resource.Error("Erro ao gerar PDF da fatura: ${response.message()}")
            }

            val body = response.body() ?: return Resource.Error("Resposta vazia ao gerar PDF da fatura")
            if (!body.success) {
                return Resource.Error(body.message ?: "Falha ao gerar PDF da fatura")
            }

            var respostaFinal = body
            val pdfUrl = respostaFinal.pdfUrl
            if (respostaFinal.pdfBase64.isNullOrBlank() && !pdfUrl.isNullOrBlank()) {
                val pdfBase64Baixado = baixarPdfComoBase64(pdfUrl)
                if (!pdfBase64Baixado.isNullOrBlank()) {
                    respostaFinal = respostaFinal.copy(pdfBase64 = pdfBase64Baixado)
                }
            }

            Resource.Success(fatura to respostaFinal)
        } catch (e: Exception) {
            LogUtils.error("FaturaRepository", "Erro ao gerar PDF da fatura", e)
            Resource.Error("Erro ao gerar PDF da fatura: ${e.message}")
        }
    }

    private fun mapearRequestPdf(fatura: Fatura): FaturaPdfRequest {
        val periodo = fatura.periodo.orEmpty()
        val mesReferencia = formatarMesReferencia(periodo)
        val cpfCnpj = fatura.cliente?.cpfCnpj.orEmpty()
        val somenteDigitos = cpfCnpj.filter { it.isDigit() }

        val cliente = FaturaPdfCliente(
            nome = fatura.cliente?.nome ?: "Cliente não informado",
            cpf = if (somenteDigitos.length == 11) cpfCnpj else null,
            cnpj = if (somenteDigitos.length == 14) cpfCnpj else null,
            inscricaoEstadual = fatura.cliente?.rgIe,
            rg = fatura.cliente?.rgIe,
            telefone = fatura.cliente?.telefone,
            endereco = montarEnderecoCliente(fatura),
        )

        val equipamentos = fatura.equipamentosDetalhados.map { eq ->
            FaturaPdfEquipamento(
                contrato = eq.contratoNumero ?: "-",
                quantidade = eq.quantidadeEquip,
                descricao = eq.equipamentoNome ?: "Equipamento",
                obra = eq.obraLocal ?: "-",
                recebidoPor = eq.recebidoPor ?: "",
                entregueCpf = eq.entregueCpf ?: "",
                dataDevolucao = eq.dataDevolucao ?: "",
                periodo = eq.contratoPeriodo?.uppercase(Locale.getDefault()) ?: "",
                dataEmissaoContrato = eq.dataEmissaoContrato ?: "",
                dataVencimentoContrato = eq.dataVencimentoContrato ?: "",
                valorUnitario = eq.valorUnitario,
            )
        }

        val materiais = fatura.materiaisDetalhados.map { mat ->
            FaturaPdfMaterial(
                contrato = mat.contratoNumero ?: "-",
                quantidade = mat.quantidadeMaterial,
                descricao = mat.materialNome ?: "Material",
                codigo = mat.materialCodigo,
                valorUnitario = mat.valorUnitario,
                valorTotal = mat.valorTotal,
            )
        }
        val materiaisTotal = materiais.sumOf { it.valorTotal }

        val info = FaturaPdfInfo(
            numero = fatura.numero,
            mesReferencia = mesReferencia,
            periodo = "Mensal",
            observacoes = fatura.observacoes.orEmpty(),
            subtotal = fatura.valorContratos,
            frete = fatura.valorAdicional,
            desconto = fatura.desconto,
            valorTotal = fatura.valorTotal,
        )

        return FaturaPdfRequest(
            fatura = info,
            cliente = cliente,
            equipamentos = equipamentos,
            materiais = materiais,
            materiaisTotal = materiaisTotal,
            totalGeral = fatura.valorTotal,
            dataEmissao = fatura.dataEmissao,
            dataVencimento = fatura.dataVencimento,
        )
    }

    private fun montarEnderecoCliente(fatura: Fatura): String {
        val cliente = fatura.cliente ?: return ""
        val partes = listOfNotNull(
            cliente.endereco?.takeIf { it.isNotBlank() },
            cliente.bairro?.takeIf { it.isNotBlank() },
            cliente.cidade?.takeIf { it.isNotBlank() },
        ).toMutableList()

        val ufCep = listOfNotNull(
            cliente.estado?.takeIf { it.isNotBlank() },
            cliente.cep?.takeIf { it.isNotBlank() },
        ).joinToString(" - ")

        if (ufCep.isNotBlank()) partes.add(ufCep)
        return partes.joinToString(", ")
    }

    private fun formatarMesReferencia(periodo: String): String {
        val regex = Regex("""^(\d{4})-(\d{2})$""")
        val match = regex.find(periodo) ?: return periodo
        val ano = match.groupValues[1]
        val mes = match.groupValues[2].toIntOrNull() ?: return periodo
        val nomesMes = listOf(
            "Janeiro", "Fevereiro", "Marco", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro",
        )
        val nome = nomesMes.getOrNull(mes - 1) ?: return periodo
        return "$nome/$ano"
    }

    private fun baixarPdfComoBase64(urlRecebida: String): String? {
        return try {
            val url = resolverPdfUrl(urlRecebida)
            val request = Request.Builder().url(url).get().build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    LogUtils.warning("FaturaRepository", "Falha ao baixar PDF para compartilhamento: ${response.code}")
                    return null
                }

                val bytes = response.body?.bytes() ?: return null
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            LogUtils.error("FaturaRepository", "Erro ao baixar PDF para compartilhamento", e)
            null
        }
    }

    private fun resolverPdfUrl(urlRecebida: String): String {
        if (urlRecebida.startsWith("http://") || urlRecebida.startsWith("https://")) {
            return urlRecebida
        }

        val base = BuildConfig.PDF_BASE_URL.removeSuffix("/")
        return if (urlRecebida.startsWith("/")) "$base$urlRecebida" else "$base/$urlRecebida"
    }
}
