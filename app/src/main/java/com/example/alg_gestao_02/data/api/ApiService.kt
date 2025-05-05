package com.example.alg_gestao_02.data.api

import com.example.alg_gestao_02.data.models.Equipamento
import com.example.alg_gestao_02.data.models.User
import com.example.alg_gestao_02.data.models.Cliente
import com.example.alg_gestao_02.data.models.Contrato
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

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
     * Criar um novo contrato
     */
    @POST("api/contratos")
    suspend fun createContrato(@Body contrato: Contrato): Response<Contrato>
    
    /**
     * Atualizar um contrato existente
     */
    @PUT("api/contratos/{id}")
    suspend fun updateContrato(
        @Path("id") id: Int,
        @Body contrato: Contrato
    ): Response<Contrato>
    
    /**
     * Excluir um contrato
     */
    @DELETE("api/contratos/{id}")
    suspend fun deleteContrato(@Path("id") id: Int): Response<Void>
    
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
} 