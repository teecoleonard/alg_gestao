package com.example.alg_gestao_02.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.DashboardActivity
import com.example.alg_gestao_02.databinding.ActivityLoginBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import com.example.alg_gestao_02.utils.TextMaskUtils

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager
    private var isNavigating = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.debug("LoginActivity", "Inicializando tela de login")
        
        sessionManager = SessionManager(this)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        
        // Verifica se já existe uma sessão ativa
        if (sessionManager.isLoggedIn()) {
            LogUtils.info("LoginActivity", "Sessão ativa encontrada, redirecionando para DashboardActivity")
            navigateToDashboard()
            return
        }
        
        setupListeners()
        observeViewModel()
        animateUI()
        setupBackNavigation()
        setupMasks()
    }
    
    private fun setupMasks() {
        // Aplicar máscara de CPF
        binding.etLogin.addTextChangedListener(TextMaskUtils.insertCpfMask(binding.etLogin))
    }
    
    private fun setupBackNavigation() {
        // Configura o comportamento do botão voltar para sair do aplicativo
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                LogUtils.debug("LoginActivity", "Botão voltar pressionado, finalizando aplicativo")
                finishAffinity()
            }
        })
    }
    
    private fun animateUI() {
        try {
            // Carrega a animação de fade in com escala
            val scaleAndFade = AnimationUtils.loadAnimation(this, R.anim.scale_fade)
            
            // Aplica a animação nos elementos principais
            binding.apply {
                ivLogo.startAnimation(scaleAndFade)
                tilLogin.startAnimation(scaleAndFade)
                tilPassword.startAnimation(scaleAndFade)
                btnLogin.startAnimation(scaleAndFade)
            }
        } catch (e: Exception) {
            LogUtils.error("LoginActivity", "Erro ao aplicar animações: ${e.message}")
        }
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val cpf = binding.etLogin.text.toString().trim()
            val senha = binding.etPassword.text.toString().trim()
            
            if (cpf.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação básica de CPF
            if (cpf.replace("[^0-9]".toRegex(), "").length != 11) {
                Toast.makeText(this, "Digite um CPF válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            LogUtils.debug("LoginActivity", "Tentativa de login para CPF: $cpf")
            
            // Usa as credenciais fornecidas pelo usuário
            viewModel.login(cpf, senha)
        }
        
        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Função de recuperação de senha em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
        
        binding.tvTerms.setOnClickListener {
            Toast.makeText(this, "Termos de uso em desenvolvimento", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            if (isFinishing || isDestroyed) return@observe
            
            when (state) {
                is LoginViewModel.LoginState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnLogin.isEnabled = false
                }
                
                is LoginViewModel.LoginState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    
                    // Salva os dados da sessão
                    sessionManager.saveUserSession(state.token, state.user)
                    
                    // Navega para o Dashboard
                    navigateToDashboard()
                }
                
                is LoginViewModel.LoginState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true
                    
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    LogUtils.error("LoginActivity", "Erro de login: ${state.message}")
                }
            }
        }
    }
    
    private fun navigateToDashboard() {
        if (isNavigating || isFinishing || isDestroyed) {
            return
        }
        
        try {
            isNavigating = true
            LogUtils.debug("LoginActivity", "Navegando para DashboardActivity")
            
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Usar transições simples para evitar problemas
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out_transition)
        } catch (e: Exception) {
            isNavigating = false
            LogUtils.error("LoginActivity", "Erro ao navegar para Dashboard: ${e.message}")
            Toast.makeText(this, "Erro ao iniciar o Dashboard", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Substituído pelo método setupBackNavigation() que usa OnBackPressedDispatcher
     */
    @Deprecated("Substituído pelo novo mecanismo OnBackPressedDispatcher")
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }
} 