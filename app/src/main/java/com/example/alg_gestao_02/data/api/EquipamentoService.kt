package com.example.alg_gestao_02.data.api

import com.example.alg_gestao_02.data.models.Equipamento
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface para acessar endpoints de equipamentos
 */
interface EquipamentoService {
    
    /**
     * Obtém todos os equipamentos
     */
    @GET("api/equipamentos")
    suspend fun getEquipamentos(): Response<List<Equipamento>>
    
    /**
     * Obtém equipamentos disponíveis
     */
    @GET("api/equipamentos/disponiveis")
    suspend fun getEquipamentosDisponiveis(): Response<List<Equipamento>>
    
    /**
     * Obtém um equipamento pelo ID
     */
    @GET("api/equipamentos/{id}")
    suspend fun getEquipamentoById(@Path("id") id: Int): Response<Equipamento>
    
    /**
     * Cria um novo equipamento
     */
    @POST("api/equipamentos")
    suspend fun createEquipamento(@Body equipamento: Equipamento): Response<Equipamento>
    
    /**
     * Atualiza um equipamento existente
     */
    @PUT("api/equipamentos/{id}")
    suspend fun updateEquipamento(
        @Path("id") id: Int,
        @Body equipamento: Equipamento
    ): Response<Equipamento>
    
    /**
     * Exclui um equipamento
     */
    @DELETE("api/equipamentos/{id}")
    suspend fun deleteEquipamento(@Path("id") id: Int): Response<Void>
} 