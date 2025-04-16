package com.example.alg_gestao_02

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.alg_gestao_02.auth.LoginActivity
import com.example.alg_gestao_02.databinding.ActivitySplashBinding
import com.example.alg_gestao_02.utils.LogUtils

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.info("SplashActivity", "Iniciando aplicativo")
        
        // Exibe a tela de splash por 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            LogUtils.debug("SplashActivity", "Tempo de exibição concluído, navegando para LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000) // 3 segundos
    }
} 