package com.example.alg_gestao_02

import android.app.Application
import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Classe Application personalizada para inicialização de componentes globais
 */
class AlgGestaoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializa o cliente da API com contexto para o SessionManager
        ApiClient.init(applicationContext)
        
        LogUtils.info("AlgGestaoApplication", "Aplicativo inicializado")
    }
} 