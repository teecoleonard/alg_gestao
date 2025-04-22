package com.example.alg_gestao_02

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.alg_gestao_02.auth.LoginActivity
import com.example.alg_gestao_02.databinding.ActivitySplashBinding
import com.example.alg_gestao_02.utils.LogUtils

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isNavigating = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.info("SplashActivity", "Iniciando aplicativo")
        
        setupAnimations()
    }
    
    private fun setupAnimations() {
        try {
            // Carregar animação de fade in para o texto e loading
            val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            
            // Após 1 segundo, mostrar o nome do app e o loading com fade in
            handler.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    binding.tvAppName.alpha = 1f
                    binding.tvAppName.startAnimation(fadeIn)
                    
                    binding.loadingAnimation.alpha = 1f
                    binding.loadingAnimation.startAnimation(fadeIn)
                }
            }, 1000)
            
            // Navegar para o Login após 2.5 segundos para garantir que as animações terminem
            handler.postDelayed({
                if (!isFinishing && !isDestroyed && !isNavigating) {
                    navigateToLogin()
                }
            }, 2500)
        } catch (e: Exception) {
            LogUtils.error("SplashActivity", "Erro ao configurar animações: ${e.message}")
            // Em caso de erro, navegar diretamente para o login após um curto delay
            handler.postDelayed({
                if (!isFinishing && !isDestroyed && !isNavigating) {
                    navigateToLogin()
                }
            }, 1000)
        }
    }
    
    private fun navigateToLogin() {
        if (isNavigating || isFinishing || isDestroyed) {
            return
        }
        
        try {
            // Marca que já estamos navegando para evitar chamadas duplicadas
            isNavigating = true
            LogUtils.debug("SplashActivity", "Navegando para LoginActivity")
            
            // Cancela as animações Lottie antes de navegar
            try {
                binding.lottieAnimationView.cancelAnimation()
                binding.loadingAnimation.cancelAnimation()
            } catch (e: Exception) {
                LogUtils.error("SplashActivity", "Erro ao cancelar animações: ${e.message}")
            }
            
            // Remove todos os callbacks pendentes
            handler.removeCallbacksAndMessages(null)
            
            // Cria a intent para a próxima tela
            val intent = Intent(this, LoginActivity::class.java)
            
            // Inicia a atividade com flags para limpar a pilha
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            
            // Finaliza esta atividade de forma limpa e sem animações adicionais
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } catch (e: Exception) {
            isNavigating = false
            LogUtils.error("SplashActivity", "Erro na navegação: ${e.message}")
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Cancela as animações ao pausar para evitar vazamentos
        try {
            binding.lottieAnimationView.pauseAnimation()
            binding.loadingAnimation.pauseAnimation()
        } catch (e: Exception) {
            LogUtils.error("SplashActivity", "Erro ao pausar animações: ${e.message}")
        }
    }
    
    override fun onDestroy() {
        // Remove todos os callbacks pendentes
        handler.removeCallbacksAndMessages(null)
        
        // Cancela as animações Lottie
        try {
            binding.lottieAnimationView.cancelAnimation()
            binding.loadingAnimation.cancelAnimation()
        } catch (e: Exception) {
            LogUtils.error("SplashActivity", "Erro ao cancelar animações no onDestroy: ${e.message}")
        }
        
        super.onDestroy()
    }
} 