package com.example.alg_gestao_02.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.auth.LoginActivity
import com.example.alg_gestao_02.dashboard.fragments.DashboardFragment
import com.example.alg_gestao_02.dashboard.fragments.client.ClientesFragment
import com.example.alg_gestao_02.dashboard.fragments.company.EmpresasFragment
import com.example.alg_gestao_02.dashboard.fragments.contract.ContratosFragment
import com.example.alg_gestao_02.databinding.ActivityDashboardBinding
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var toggle: ActionBarDrawerToggle
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.debug("DashboardActivity", "Inicializando dashboard")
        
        sessionManager = SessionManager(this)
        setupToolbar()
        setupNavigationDrawer()
        setupUserInfo()
        setupBackPressHandler()
        
        // Exibe o Fragment do Dashboard por padrão
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, DashboardFragment())
                .commit()
            binding.navView.setCheckedItem(R.id.nav_dashboard)
        }
    }
    
    private fun setupUserInfo() {
        // Configurar informações do usuário no toolbar
        binding.tvUsername.text = sessionManager.getUserName()
        binding.tvUserRole.text = sessionManager.getUserRole()
        
        // Configurar clique no ícone de notificação
        binding.ivNotification.setOnClickListener {
            LogUtils.debug("DashboardActivity", "Ícone de notificação clicado")
            // Implementar exibição de notificações (em desenvolvimento)
        }
    }
    
    private fun setupToolbar() {
        // Ocultar o título na toolbar, já que usamos layout personalizado
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun setupNavigationDrawer() {
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        binding.navView.setNavigationItemSelectedListener(this)
        
        // Atualiza os dados do cabeçalho do menu
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        
        tvUserName.text = sessionManager.getUserName()
        tvUserEmail.text = sessionManager.getUserEmail()
    }
    
    private fun setupBackPressHandler() {
        // Registra um callback para o botão voltar
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val fragment: Fragment = when (item.itemId) {
            R.id.nav_dashboard -> {
                DashboardFragment()
            }
            R.id.nav_clientes -> {
                ClientesFragment()
            }
            R.id.nav_empresas -> {
                EmpresasFragment()
            }
            R.id.nav_contratos -> {
                ContratosFragment()
            }
            R.id.nav_logout -> {
                LogUtils.info("DashboardActivity", "Usuário solicitou logout")
                sessionManager.logout()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return true
            }
            else -> DashboardFragment()
        }
        
        // Substitui o fragment atual pelo selecionado
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        
        // Fecha o drawer após a seleção
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
} 