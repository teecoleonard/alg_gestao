package com.example.alg_gestao_02.data.api

import android.content.Context
import com.example.alg_gestao_02.BuildConfig
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier

/**
 * Cliente para acessar a API
 */
object ApiClient {
    val BASE_URL: String = BuildConfig.API_BASE_URL
    private lateinit var sessionManager: SessionManager
    private lateinit var context: Context
    
    /**
     * Retorna a URL base configurada
     */
    fun getBaseUrl(): String = BASE_URL
    
    /**
     * Retorna o contexto da aplicação
     */
    fun getContext(): Context = context
    
    /**
     * Inicializa o cliente com contexto para o SessionManager
     */
    fun init(context: Context) {
        this.context = context
        sessionManager = SessionManager(context)
        LogUtils.info("ApiClient", "🚀 INICIALIZANDO ApiClient")
        LogUtils.info("ApiClient", "🌐 BASE_URL configurada: $BASE_URL")
        LogUtils.debug("ApiClient", "📱 Context inicializado: ${context.javaClass.simpleName}")
    }
    
    /**
     * Cria um TrustManager que aceita todos os certificados
     * ATENÇÃO: Usar apenas em desenvolvimento!
     */
    private fun getUnsafeOkHttpClient(): OkHttpClient.Builder {
        return try {
            // Criar um trust manager que aceita todos os certificados
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Instalar o trust manager que aceita todos
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())

            // Criar um SSL socket factory com nosso trust manager que aceita todos
            val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier(HostnameVerifier { _, _ -> true })

            LogUtils.warning("ApiClient", "⚠️ USANDO CONFIGURAÇÃO SSL NÃO SEGURA - APENAS PARA DESENVOLVIMENTO!")
            
            builder
        } catch (e: Exception) {
            LogUtils.error("ApiClient", "Erro ao configurar SSL não seguro: ${e.message}")
            OkHttpClient.Builder()
        }
    }
    
    // Interceptador para retry automático em caso de 502
    private val retryInterceptor = Interceptor { chain ->
        val request = chain.request()
        var response = chain.proceed(request)
        
        // Se receber 502, tenta novamente após um delay
        var retryCount = 0
        val maxRetries = 2
        
        while (response.code == 502 && retryCount < maxRetries) {
            retryCount++
            LogUtils.warning("ApiClient", "🔄 RETRY ${retryCount}/${maxRetries} devido ao erro 502")
            
            response.close() // Importante: fechar a resposta anterior
            
            // Delay progressivo: 2s, 5s
            Thread.sleep((retryCount * 2000 + 1000).toLong())
            
            try {
                response = chain.proceed(request)
                if (response.code != 502) {
                    LogUtils.info("ApiClient", "✅ RETRY ${retryCount} bem-sucedido: ${response.code}")
                }
            } catch (e: Exception) {
                LogUtils.error("ApiClient", "❌ RETRY ${retryCount} falhou: ${e.message}")
                if (retryCount >= maxRetries) {
                    throw e
                }
            }
        }
        
        if (response.code == 502 && retryCount >= maxRetries) {
            LogUtils.error("ApiClient", "💥 FALHA APÓS ${maxRetries} TENTATIVAS - Servidor Node.js provavelmente parado")
        }
        
        response
    }
    
    // Interceptador para monitorar todas as requisições feitas para o BASE_URL
    private val baseUrlMonitorInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Log da requisição sendo enviada
        LogUtils.info("ApiClient", "🌐 REQUISIÇÃO: ${request.method} ${request.url}")
        LogUtils.debug("ApiClient", "📡 URL COMPLETA: ${request.url}")
        LogUtils.debug("ApiClient", "🔗 BASE_URL: $BASE_URL")
        LogUtils.debug("ApiClient", "📋 HEADERS: ${request.headers}")
        
        // Verificar se o request body existe e loggar
        request.body?.let { body ->
            LogUtils.debug("ApiClient", "📦 BODY TYPE: ${body.contentType()}")
            LogUtils.debug("ApiClient", "📊 BODY SIZE: ${body.contentLength()} bytes")
        }
        
        try {
            val response = chain.proceed(request)
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            // Log da resposta recebida
            LogUtils.info("ApiClient", "✅ RESPOSTA: ${response.code} ${response.message} (${duration}ms)")
            LogUtils.debug("ApiClient", "📈 STATUS: ${response.code}")
            LogUtils.debug("ApiClient", "🏷️ HEADERS RESPOSTA: ${response.headers}")
            
            // Detectar requisições muito lentas (possível indicativo de problemas no servidor)
            if (duration > 10000) { // Mais de 10 segundos
                LogUtils.warning("ApiClient", "🐌 REQUISIÇÃO MUITO LENTA: ${duration}ms - Possível problema de performance no servidor")
            }
            
            // Log específico baseado no código de status
            when (response.code) {
                200 -> LogUtils.info("ApiClient", "🎉 SUCESSO: Dados recebidos com sucesso")
                201 -> LogUtils.info("ApiClient", "🆕 CRIADO: Recurso criado com sucesso")
                204 -> LogUtils.info("ApiClient", "✔️ SEM CONTEÚDO: Operação realizada com sucesso")
                400 -> LogUtils.warning("ApiClient", "❌ ERRO 400: Requisição inválida")
                401 -> LogUtils.error("ApiClient", "🔒 ERRO 401: Não autorizado - Token inválido?")
                403 -> LogUtils.error("ApiClient", "🚫 ERRO 403: Acesso negado")
                404 -> LogUtils.error("ApiClient", "🔍 ERRO 404: Endpoint não encontrado")
                500 -> LogUtils.error("ApiClient", "💥 ERRO 500: Erro interno do servidor")
                502 -> {
                    LogUtils.error("ApiClient", "🌐 ERRO 502 BAD GATEWAY: Servidor backend indisponível")
                    LogUtils.error("ApiClient", "🔧 POSSÍVEIS CAUSAS: Pool de conexões esgotado, Memory leak, ou aplicação Node.js travada")
                    LogUtils.error("ApiClient", "⏰ TEMPO DA REQUISIÇÃO: ${duration}ms")
                }
                503 -> LogUtils.error("ApiClient", "⏰ ERRO 503: Serviço temporariamente indisponível")
                504 -> LogUtils.error("ApiClient", "⏱️ ERRO 504: Gateway timeout - Servidor muito lento")
                else -> LogUtils.warning("ApiClient", "⚠️ STATUS INESPERADO: ${response.code}")
            }
            
            // Verificar se há conteúdo na resposta
            val contentLength = response.header("Content-Length")
            if (contentLength != null) {
                LogUtils.debug("ApiClient", "📏 TAMANHO RESPOSTA: $contentLength bytes")
                if (contentLength == "0") {
                    LogUtils.warning("ApiClient", "📭 RESPOSTA VAZIA: Servidor retornou conteúdo vazio")
                }
            }
            
            // Log específico para diferentes endpoints
            when {
                request.url.toString().contains("/login") -> {
                    LogUtils.info("ApiClient", "🔐 LOGIN: ${if (response.isSuccessful) "Sucesso" else "Falhou"}")
                }
                request.url.toString().contains("/dashboard/stats") -> {
                    LogUtils.info("ApiClient", "📊 ========== DASHBOARD STATS ==========")
                    if (response.isSuccessful) {
                        LogUtils.info("ApiClient", "✅ DASHBOARD: Estatísticas carregadas com sucesso!")
                        LogUtils.info("ApiClient", "⏱️ Tempo de resposta: ${duration}ms")
                        LogUtils.debug("ApiClient", "📄 Content-Type: ${response.header("Content-Type")}")
                        LogUtils.debug("ApiClient", "📏 Content-Length: ${response.header("Content-Length")} bytes")
                    } else {
                        LogUtils.error("ApiClient", "❌ DASHBOARD: Falha ao carregar estatísticas")
                        LogUtils.error("ApiClient", "💥 Status: ${response.code} - ${response.message}")
                        LogUtils.error("ApiClient", "⏱️ Tempo até falha: ${duration}ms")
                    }
                    LogUtils.info("ApiClient", "========================================")
                }
                request.url.toString().contains("/equipamentos") -> {
                    LogUtils.info("ApiClient", "🔧 EQUIPAMENTOS: ${if (response.isSuccessful) "Dados carregados" else "Falha ao carregar"}")
                }
                request.url.toString().contains("/contratos") -> {
                    LogUtils.info("ApiClient", "📋 CONTRATOS: ${if (response.isSuccessful) "Operação realizada" else "Falha na operação"}")
                }
                request.url.toString().contains("/devolucoes") -> {
                    LogUtils.info("ApiClient", "↩️ DEVOLUÇÕES: ${if (response.isSuccessful) "Processada" else "Falha no processamento"}")
                }
            }
            
            response
        } catch (e: Exception) {
            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            
            LogUtils.error("ApiClient", "💥 ERRO DE CONEXÃO: ${e.message} (${duration}ms)")
            LogUtils.error("ApiClient", "🔗 URL: ${request.url}")
            LogUtils.error("ApiClient", "📡 BASE_URL: $BASE_URL")
            
            // Verificar tipos específicos de erro
            when (e) {
                is java.net.UnknownHostException -> {
                    LogUtils.error("ApiClient", "🌐 ERRO DNS: Não foi possível resolver o hostname")
                }
                is java.net.ConnectException -> {
                    LogUtils.error("ApiClient", "🔌 ERRO CONEXÃO: Não foi possível conectar ao servidor")
                }
                is java.net.SocketTimeoutException -> {
                    LogUtils.error("ApiClient", "⏱️ TIMEOUT: Servidor não respondeu dentro do tempo limite")
                }
                else -> {
                    LogUtils.error("ApiClient", "❓ ERRO DESCONHECIDO: ${e.javaClass.simpleName}")
                }
            }
            
            throw e
        }
    }
    
    // Interceptador para verificar respostas específicas relacionadas a contratos
    private val contractResponseInterceptor = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)
        
        // Apenas para chamadas relacionadas a contratos
        if (request.url.toString().contains("/contratos") && 
            (request.method == "POST" || request.method == "PUT")) {
            
            // Apenas verificar metadados sem consumir o corpo
            LogUtils.debug("ApiClient", 
                "Resposta para operação de contrato: ${response.code} - ${response.message}")
            
            // Verificar se tivemos sucesso na operação
            if (response.isSuccessful) {
                LogUtils.info("ApiClient", 
                    "Operação de contrato bem-sucedida: ${request.method} ${request.url.encodedPath}")
            } else {
                LogUtils.warning("ApiClient", 
                    "Falha na operação de contrato: ${response.code} - ${response.message}")
            }
        }
        
        response
    }
    
    // Interceptador específico para monitorar operações de devolução
    private val devolucaoResponseInterceptor = Interceptor { chain ->
        val request = chain.request()
        val startTime = System.currentTimeMillis()
        
        // Log da requisição sendo enviada
        if (request.url.toString().contains("/devolucoes")) {
            LogUtils.debug("ApiClient", 
                ">>> REQUISIÇÃO DEVOLUÇÃO: ${request.method} ${request.url}")
            
            if (request.method == "PUT") {
                LogUtils.info("ApiClient", 
                    ">>> PROCESSANDO DEVOLUÇÃO: ID=${request.url.pathSegments.lastOrNull()}")
            }
        }
        
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        
        // Log da resposta recebida
        if (request.url.toString().contains("/devolucoes")) {
            LogUtils.debug("ApiClient", 
                "<<< RESPOSTA DEVOLUÇÃO: ${response.code} ${response.message} (${duration}ms)")
            
            if (request.method == "PUT") {
                if (response.isSuccessful) {
                    LogUtils.info("ApiClient", 
                        "<<< DEVOLUÇÃO PROCESSADA COM SUCESSO: ${response.code}")
                } else {
                    LogUtils.error("ApiClient", 
                        "<<< FALHA NO PROCESSAMENTO DA DEVOLUÇÃO: ${response.code} - ${response.message}")
                }
            }
        }
        
        response
    }
    
    /**
     * Cria um cliente OkHttp configurado com timeout, logger e interceptor de autenticação
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return getUnsafeOkHttpClient()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(retryInterceptor)
            .addInterceptor(baseUrlMonitorInterceptor)
            .addInterceptor(contractResponseInterceptor)
            .addInterceptor(devolucaoResponseInterceptor)
            .addInterceptor(AuthInterceptor(sessionManager))
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            // Configurações de pool de conexões para evitar problemas a longo prazo
            .connectionPool(okhttp3.ConnectionPool(5, 5, TimeUnit.MINUTES)) // Máx 5 conexões, keep-alive 5 min
            .build()
    }
    
    /**
     * Instância do Retrofit
     */
    private val retrofit by lazy {
        if (!::sessionManager.isInitialized) {
            throw IllegalStateException("ApiClient não foi inicializado. Chame ApiClient.init(context) antes de usar.")
        }
        
        LogUtils.info("ApiClient", "🔧 CRIANDO INSTÂNCIA RETROFIT")
        LogUtils.info("ApiClient", "🎯 URL BASE PARA RETROFIT: $BASE_URL")
        
        val retrofitInstance = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        LogUtils.info("ApiClient", "✅ RETROFIT CRIADO COM SUCESSO")
        retrofitInstance
    }
    
    /**
     * Cria e retorna a instância do serviço da API de autenticação
     */
    val apiService: ApiService by lazy {
        LogUtils.info("ApiClient", "🔐 CRIANDO ApiService")
        LogUtils.debug("ApiClient", "🌐 ApiService usando BASE_URL: $BASE_URL")
        val service = retrofit.create(ApiService::class.java)
        LogUtils.info("ApiClient", "✅ ApiService criado com sucesso")
        service
    }
    
    /**
     * Cria e retorna a instância do serviço de equipamentos
     */
    val equipamentoService: EquipamentoService by lazy {
        LogUtils.info("ApiClient", "🔧 CRIANDO EquipamentoService")
        LogUtils.debug("ApiClient", "🌐 EquipamentoService usando BASE_URL: $BASE_URL")
        val service = retrofit.create(EquipamentoService::class.java)
        LogUtils.info("ApiClient", "✅ EquipamentoService criado com sucesso")
        service
    }
} 
