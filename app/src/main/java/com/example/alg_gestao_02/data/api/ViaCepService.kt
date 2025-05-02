package com.example.alg_gestao_02.data.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Interface para o serviço ViaCEP
 * Documentação: https://viacep.com.br
 */
interface ViaCepService {
    /**
     * Busca endereço pelo CEP
     * @param cep CEP no formato 00000000 (apenas números)
     * @return Dados do endereço
     */
    @GET("ws/{cep}/json/")
    fun buscarEnderecoPorCep(@Path("cep") cep: String): Call<EnderecoCep>
}

/**
 * Modelo de dados de retorno da API ViaCEP
 */
data class EnderecoCep(
    val cep: String,
    val logradouro: String,
    val complemento: String,
    val bairro: String,
    val localidade: String,
    val uf: String,
    val ibge: String,
    val gia: String,
    val ddd: String,
    val siafi: String,
    val erro: Boolean = false
) 