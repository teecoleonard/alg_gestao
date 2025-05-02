package com.example.alg_gestao_02.auth

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.databinding.ActivityRegisterBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.TextMaskUtils

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.debug("RegisterActivity", "Inicializando tela de registro")
        
        viewModel = ViewModelProvider(this)[RegisterViewModel::class.java]
        
        setupToolbar()
        setupMasks()
        setupRoleSpinner()
        setupListeners()
        observeViewModel()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
    
    private fun setupMasks() {
        // Aplicar máscara de CPF
        binding.etCpf.addTextChangedListener(TextMaskUtils.insertCpfMask(binding.etCpf))
    }
    
    private fun setupRoleSpinner() {
        val roles = arrayOf("cliente", "funcionario")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
    }
    
    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val cpf = binding.etCpf.text.toString().trim()
            val nome = binding.etNome.text.toString().trim()
            val senha = binding.etPassword.text.toString().trim()
            val confirmaSenha = binding.etConfirmPassword.text.toString().trim()
            val role = binding.spinnerRole.selectedItem.toString()
            
            if (cpf.isEmpty() || nome.isEmpty() || senha.isEmpty() || confirmaSenha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação básica de CPF
            if (cpf.replace("[^0-9]".toRegex(), "").length != 11) {
                Toast.makeText(this, "Digite um CPF válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de senhas iguais
            if (senha != confirmaSenha) {
                Toast.makeText(this, "As senhas não coincidem", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validação de comprimento da senha
            if (senha.length < 6) {
                Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            LogUtils.debug("RegisterActivity", "Tentativa de registro para: $nome")
            
            viewModel.register(cpf.replace("[^0-9]".toRegex(), ""), nome, senha, role)
        }
    }
    
    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            if (isFinishing || isDestroyed) return@observe
            
            when (state) {
                is RegisterViewModel.RegisterState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }
                
                is RegisterViewModel.RegisterState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    
                    Toast.makeText(this, "Usuário registrado com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Volta para a tela de login
                }
                
                is RegisterViewModel.RegisterState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                    LogUtils.error("RegisterActivity", "Erro de registro: ${state.message}")
                }
            }
        }
    }
} 