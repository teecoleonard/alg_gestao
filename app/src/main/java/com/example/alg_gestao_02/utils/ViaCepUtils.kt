package com.example.alg_gestao_02.utils

import com.example.alg_gestao_02.data.api.EnderecoCep
import com.example.alg_gestao_02.data.api.ViaCepService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Classe utilitária para consultar endereços via CEP
 */
object ViaCepUtils {
    private const val BASE_URL = "https://viacep.com.br/"
    
    /**
     * Cliente Retrofit para a API ViaCEP
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    /**
     * Serviço para consulta de CEP
     */
    private val service = retrofit.create(ViaCepService::class.java)
    
    /**
     * Consulta um endereço pelo CEP
     * @param cep CEP a ser consultado (apenas números)
     * @return Objeto EnderecoCep com as informações do endereço
     * @throws Exception se houver erro na consulta
     */
    suspend fun consultarCep(cep: String): EnderecoCep {
        // Remove qualquer caractere não numérico
        val cepLimpo = cep.replace("\\D".toRegex(), "")
        
        // Validar o formato do CEP (8 dígitos)
        if (cepLimpo.length != 8) {
            throw IllegalArgumentException("CEP deve conter 8 dígitos")
        }
        
        return suspendCoroutine { continuation ->
            service.buscarEnderecoPorCep(cepLimpo).enqueue(object : Callback<EnderecoCep> {
                override fun onResponse(call: Call<EnderecoCep>, response: Response<EnderecoCep>) {
                    if (response.isSuccessful) {
                        val enderecoCep = response.body()
                        if (enderecoCep != null) {
                            if (enderecoCep.erro) {
                                continuation.resumeWithException(Exception("CEP não encontrado"))
                            } else {
                                continuation.resume(enderecoCep)
                            }
                        } else {
                            continuation.resumeWithException(Exception("Resposta vazia"))
                        }
                    } else {
                        continuation.resumeWithException(Exception("Erro na requisição: ${response.code()}"))
                    }
                }
                
                override fun onFailure(call: Call<EnderecoCep>, t: Throwable) {
                    LogUtils.error("ViaCepUtils", "Erro ao consultar CEP: ${t.message}")
                    continuation.resumeWithException(t)
                }
            })
        }
    }
} 