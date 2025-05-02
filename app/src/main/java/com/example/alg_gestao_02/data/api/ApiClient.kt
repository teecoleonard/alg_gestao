package com.example.alg_gestao_02.data.api

import android.content.Context
import com.example.alg_gestao_02.utils.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente para acessar a API
 */
object ApiClient {
    private const val BASE_URL = "http://192.168.100.195:3000/"
    private lateinit var sessionManager: SessionManager
    
    /**
     * Inicializa o cliente com contexto para o SessionManager
     */
    fun init(context: Context) {
        sessionManager = SessionManager(context)
    }
    
    /**
     * Cria um cliente OkHttp configurado com timeout, logger e interceptor de autenticação
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Instância do Retrofit
     */
    private val retrofit by lazy {
        if (!::sessionManager.isInitialized) {
            throw IllegalStateException("ApiClient não foi inicializado. Chame ApiClient.init(context) antes de usar.")
        }
        
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    /**
     * Cria e retorna a instância do serviço da API de autenticação
     */
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
    
    /**
     * Cria e retorna a instância do serviço de equipamentos
     */
    val equipamentoService: EquipamentoService by lazy {
        retrofit.create(EquipamentoService::class.java)
    }
} 