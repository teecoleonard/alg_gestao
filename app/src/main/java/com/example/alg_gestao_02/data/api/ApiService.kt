package com.example.alg_gestao_02.data.api

import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.models.User
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import com.example.alg_gestao_02.data.models.EquipamentoContrato
import com.example.alg_gestao_02.data.models.ContratoResponse
import com.example.alg_gestao_02.data.models.Devolucao
import com.example.alg_gestao_02.data.models.DashboardStats
import com.example.alg_gestao_02.data.models.FinancialMetrics
import com.example.alg_gestao_02.data.models.ProgressMetrics
import com.example.alg_gestao_02.data.models.TaskMetrics
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName
import com.example.alg_gestao_02.data.models.Assinatura
import com.example.alg_gestao_02.data.models.GerarPdfResumoRequest
import com.example.alg_gestao_02.data.models.PdfResumoResponse

data class AssinaturaApiRequest(
    val base64Data: String,
    val contratoId: Int
)

data class AssinaturaResponse(
    val success: Boolean,
    val message: String,
    val assinaturaId: Int? = null
)

interface ApiService {
    
    /**
     * Endpoint para login de usuário
     * @param loginRequest Objeto contendo CPF e senha
     * @return Response com token e dados do usuário
     */
    @POST("api/usuarios/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResponse>
    
    /**
     * Endpoint para registro de usuário
     * @param registerRequest Objeto contendo dados para registro
     * @return Response com token e dados do usuário
     */
    @POST("api/usuarios/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<LoginResponse>
    
    /**
     * Obter todos os equipamentos
     */
    @GET("api/equipamentos")
    suspend fun getEquipamentos(): Response<List<Equipamento>>
    
    /**
     * Obter equipamentos disponíveis
     */
    @GET("api/equipamentos/disponiveis")
    suspend fun getEquipamentosDisponiveis(): Response<List<Equipamento>>
    
    /**
     * Obter um equipamento específico por ID
     */
    @GET("api/equipamentos/{id}")
    suspend fun getEquipamentoById(@Path("id") id: Int): Response<Equipamento>
    
    /**
     * Criar um novo equipamento
     */
    @POST("api/equipamentos")
    suspend fun createEquipamento(@Body equipamento: Equipamento): Response<Equipamento>
    
    /**
     * Atualizar um equipamento existente
     */
    @PUT("api/equipamentos/{id}")
    suspend fun updateEquipamento(
        @Path("id") id: Int,
        @Body equipamento: Equipamento
    ): Response<Equipamento>
    
    /**
     * Excluir um equipamento
     */
    @DELETE("api/equipamentos/{id}")
    suspend fun deleteEquipamento(@Path("id") id: Int): Response<Void>
    
    /**
     * Obter todos os clientes
     */
    @GET("api/clientes")
    suspend fun getClientes(): Response<List<Cliente>>
    
    /**
     * Obter um cliente específico por ID
     */
    @GET("api/clientes/{id}")
    suspend fun getClienteById(@Path("id") id: Int): Response<Cliente>
    
    /**
     * Criar um novo cliente
     */
    @POST("api/clientes")
    suspend fun createCliente(@Body cliente: Cliente): Response<Cliente>
    
    /**
     * Atualizar um cliente existente
     */
    @PUT("api/clientes/{id}")
    suspend fun updateCliente(
        @Path("id") id: Int,
        @Body cliente: Cliente
    ): Response<Cliente>
    
    /**
     * Excluir um cliente
     */
    @DELETE("api/clientes/{id}")
    suspend fun deleteCliente(@Path("id") id: Int): Response<Void>

    /**
     * ENDPOINTS DE CONTRATOS 
     */
    
    /**
     * Obter todos os contratos
     */
    @GET("api/contratos")
    suspend fun getContratos(): Response<List<Contrato>>
    
    /**
     * Obter um contrato específico por ID
     */
    @GET("api/contratos/{id}")
    suspend fun getContratoById(@Path("id") id: Int): Response<Contrato>
    
    /**
     * Obter contratos de um cliente específico
     */
    @GET("api/contratos/cliente/{clienteId}")
    suspend fun getContratosByCliente(@Path("clienteId") clienteId: Int): Response<List<Contrato>>

    /**
     * Obter equipamentos de um contrato específico
     */
    @GET("api/contratos/{contratoId}/equipamentos")
    suspend fun getEquipamentosContrato(@Path("contratoId") contratoId: Int): Response<List<EquipamentoContrato>>

    
    /**
     * Criar um novo contrato com seus equipamentos
     */
    @POST("api/contratos")
    suspend fun createContrato(@Body contrato: Contrato): Response<ContratoResponse>
    
    /**
     * Atualizar um contrato existente com seus equipamentos
     */
    @PUT("api/contratos/{id}")
    suspend fun updateContrato(
        @Path("id") id: Int,
        @Body contrato: Contrato
    ): Response<ContratoResponse>
    
    /**
     * Excluir um contrato
     */
    @DELETE("api/contratos/{id}")
    suspend fun deleteContrato(
        @Path("id") id: Int,
        @Query("force") force: String? = null
    ): Response<Void>

    /**
     * ENDPOINTS DE DEVOLUÇÕES
     */

    /**
     * Obter todas as devoluções com filtros opcionais
     */
    @GET("api/devolucoes")
    suspend fun getDevolucoes(
        @Query("status") status: String? = null,
        @Query("clienteId") clienteId: Int? = null,
        @Query("contratoId") contratoId: Int? = null,
        @Query("devNum") devNum: String? = null,
        @Query("dataPrevistaInicio") dataPrevistaInicio: String? = null,
        @Query("dataPrevistaFim") dataPrevistaFim: String? = null
    ): Response<List<Devolucao>>

    /**
     * Obter uma devolução específica por ID
     */
    @GET("api/devolucoes/{id}")
    suspend fun getDevolucaoById(@Path("id") id: Int): Response<Devolucao>

    /**
     * Obter devoluções por número de devolução (dev_num)
     */
    @GET("api/devolucoes/dev-num/{devNum}")
    suspend fun getDevolucoesByDevNum(@Path("devNum") devNum: String): Response<List<Devolucao>>

    /**
     * Obter devoluções de um contrato específico
     */
    @GET("api/devolucoes/contrato/{contratoId}")
    suspend fun getDevolucoesByContratoId(@Path("contratoId") contratoId: Int): Response<List<Devolucao>>

    /**
     * Atualizar/processar um item de devolução
     */
    @PUT("api/devolucoes/{id}")
    suspend fun updateDevolucao(
        @Path("id") id: Int,
        @Body requestData: DevolucaoUpdateRequest
    ): Response<DevolucaoUpdateResponse>

    /**
     * Enviar assinatura para processamento
     */
    @POST("api/assinaturas")
    suspend fun enviarAssinatura(@Body request: AssinaturaApiRequest): Response<AssinaturaResponse>

    /**
     * Endpoint para obter estatísticas do dashboard
     * @return Response com contadores de contratos, clientes, equipamentos e devoluções
     */
    @GET("api/dashboard/stats")
    suspend fun getDashboardStats(): Response<DashboardStats>

    /**
     * Endpoint para obter métricas financeiras
     * @return Response com dados financeiros do sistema
     */
    @GET("api/dashboard/financial-metrics")
    suspend fun getFinancialMetrics(): Response<FinancialMetrics>

    /**
     * Endpoint para obter métricas de progresso/metas
     * @return Response com dados de metas e progresso
     */
    @GET("api/dashboard/progress-metrics")
    suspend fun getProgressMetrics(): Response<ProgressMetrics>

    /**
     * Endpoint para obter métricas de tarefas pendentes
     * @return Response com dados de tarefas pendentes
     */
    @GET("api/dashboard/task-metrics")
    suspend fun getTaskMetrics(): Response<TaskMetrics>

    /**
     * Endpoint para obter receita mensal por cliente
     * @return Response com lista de receita por cliente
     */
    @GET("api/dashboard/receita-por-cliente")
    suspend fun getReceitaPorCliente(): Response<com.example.alg_gestao_02.data.models.ReceitaClienteResponse>

    /**
     * Endpoint para obter receita mensal por cliente com filtro por período
     * @param mes Mês para filtrar (1-12)
     * @param ano Ano para filtrar
     * @return Response com lista de receita por cliente filtrada por período
     */
    @GET("api/dashboard/receita-por-cliente")
    suspend fun getReceitaPorClienteComFiltro(
        @Query("mes") mes: Int,
        @Query("ano") ano: Int
    ): Response<com.example.alg_gestao_02.data.models.ReceitaClienteResponse>

    /**
     * Endpoint para obter resumo mensal detalhado de um cliente específico
     * @param clienteId ID do cliente
     * @param mesReferencia Mês de referência (formato: yyyy-MM)
     * @return Response com resumo mensal detalhado
     */
    @GET("api/dashboard/resumo-mensal-cliente/{clienteId}")
    suspend fun getResumoMensalCliente(
        @Path("clienteId") clienteId: Int,
        @Query("mes") mesReferencia: String
    ): Response<com.example.alg_gestao_02.data.models.ResumoMensalCliente>

    /**
     * Endpoint para confirmar pagamento de um cliente
     * @param request Dados da confirmação de pagamento
     * @return Response com confirmação
     */
    @POST("api/dashboard/confirmar-pagamento")
    suspend fun confirmarPagamento(
        @Body request: com.example.alg_gestao_02.data.models.ConfirmarPagamentoRequest
    ): Response<com.example.alg_gestao_02.data.models.ConfirmarPagamentoResponse>

    /**
     * Endpoint para gerar PDF de relatório mensal por cliente
     * @param mesReferencia Mês de referência (formato: yyyy-MM)
     * @param clienteIds IDs dos clientes (opcional - se não fornecido, inclui todos)
     * @return Response com dados do PDF gerado
     */
    @POST("api/dashboard/gerar-pdf-resumo-mensal")
    suspend fun gerarPdfResumoMensal(
        @Body request: GerarPdfResumoRequest
    ): Response<PdfResumoResponse>

    /**
     * Classe para requisição de login
     */
    data class LoginRequest(
        val cpf: String,
        val senha: String
    )
    
    /**
     * Classe para requisição de registro
     */
    data class RegisterRequest(
        val cpf: String,
        val nome: String,
        val senha: String,
        val role: String
    )
    
    /**
     * Classe para resposta de login/registro
     */
    data class LoginResponse(
        val token: String,
        val user: User
    )

    /**
     * Classe para resposta de atualização de devolução
     */
    data class DevolucaoUpdateResponse(
        val message: String,
        val devolucao: Devolucao
    )

    /**
     * Classe para request de atualização de devolução
     */
    data class DevolucaoUpdateRequest(
        @SerializedName("quantidade_devolvida")
        val quantidadeDevolvida: Int? = null,
        
        @SerializedName("status_item_devolucao")
        val statusItemDevolucao: String? = null,
        
        @SerializedName("data_devolucao_efetiva")
        val dataDevolucaoEfetiva: String? = null,
        
        @SerializedName("observacao_item_devolucao")
        val observacaoItemDevolucao: String? = null
    )
}
