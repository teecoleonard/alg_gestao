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
            // Sequência de animações elegante
            startLogoAnimation()
            
            // Navegar para o Login após 3.5 segundos para garantir que todas as animações terminem
            handler.postDelayed({
                if (!isFinishing && !isDestroyed && !isNavigating) {
                    navigateToLogin()
                }
            }, 3500)
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
    
    private fun startLogoAnimation() {
        // 1. Animação Lottie aparece primeiro com efeito suave
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                binding.lottieAnimationView.animate()
                    .alpha(1f)
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(800)
                    .withEndAction {
                        // Volta ao tamanho normal
                        binding.lottieAnimationView.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }
        }, 300)
        
        // 2. Nome do app aparece (com delay)
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                binding.tvAppName.animate()
                    .alpha(1f)
                    .translationY(-20f)
                    .setDuration(600)
                    .withEndAction {
                        // Volta à posição normal
                        binding.tvAppName.animate()
                            .translationY(0f)
                            .setDuration(200)
                            .start()
                    }
                    .start()
            }
        }, 800)
        
        // 3. Subtítulo aparece
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                binding.tvSubtitle.animate()
                    .alpha(1f)
                    .setDuration(600)
                    .start()
            }
        }, 1200)
        
        // 4. Container de loading aparece
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                binding.loadingContainer.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
        }, 1800)
        
        // 5. Versão aparece por último
        handler.postDelayed({
            if (!isFinishing && !isDestroyed) {
                binding.tvVersion.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .start()
            }
        }, 2200)
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