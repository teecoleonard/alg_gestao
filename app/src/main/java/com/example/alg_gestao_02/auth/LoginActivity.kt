package com.example.alg_gestao_02.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.DashboardActivity
import com.example.alg_gestao_02.databinding.ActivityLoginBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager
    
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
    }
    
    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            LogUtils.debug("LoginActivity", "Tentativa de login para: $email")
            viewModel.login(email, password)
        }
    }
    
    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
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
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
} 