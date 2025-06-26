package com.example.alg_gestao_02.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.adapter.NotificationAdapter
import com.example.alg_gestao_02.auth.LoginActivity
import com.example.alg_gestao_02.databinding.ActivityDashboardBinding
import com.example.alg_gestao_02.databinding.LayoutNotificationsPanelBinding
import com.example.alg_gestao_02.manager.NotificationManager
import com.example.alg_gestao_02.model.Notification
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.SessionManager
import com.google.android.material.navigation.NavigationView

class DashboardActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var notificationManager: NotificationManager
    private var notificationPopup: PopupWindow? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        LogUtils.debug("DashboardActivity", "Inicializando dashboard")
        
        sessionManager = SessionManager(this)
        notificationManager = NotificationManager.getInstance()
        setupToolbar()
        setupNavigation()
        setupUserInfo()
        setupBackPressHandler()
        setupNotifications()
        
        // Garantir que a fonte seja aplicada corretamente em toda a aplicação
        enforceFontConsistency()
    }
    
    private fun setupUserInfo() {
        // Configurar informações do usuário no toolbar
        binding.tvUsername.text = sessionManager.getUserName()
        binding.tvUserRole.text = sessionManager.getUserRole()
        
        // Configurar clique no ícone de notificação
        binding.ivNotification.setOnClickListener {
            LogUtils.debug("DashboardActivity", "Ícone de notificação clicado")
            showNotificationPanel()
        }
    }
    
    private fun setupToolbar() {
        // Ocultar o título na toolbar, já que usamos layout personalizado
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    
    private fun setupNavigation() {
        // Configurar NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Configurar AppBarConfiguration com os destinos de nível superior
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment,
                R.id.clientesFragment, 
                R.id.contratosFragment,
                R.id.equipamentosFragment,
                R.id.devolucoesFragment
            ),
            binding.drawerLayout
        )
        
        // Configurar ActionBar com NavController
        setupActionBarWithNavController(navController, appBarConfiguration)
        
        // Configurar NavigationView com NavController
        binding.navView.setupWithNavController(navController)
        
        // Configurar drawer toggle
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        // Manter o listener para o logout e outras ações personalizadas
        binding.navView.setNavigationItemSelectedListener(this)
        
        // Aplicar fonte aos itens do menu imediatamente
        binding.navView.post {
            applyFontToMenuItems()
        }
        
        // Atualiza os dados do cabeçalho do menu
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvUserName)
        val tvUserEmail = headerView.findViewById<TextView>(R.id.tvUserEmail)
        
        tvUserName.text = sessionManager.getUserName()
        tvUserEmail.text = sessionManager.getUserCpf()
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
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_dashboard -> {
                navController.navigate(R.id.dashboardFragment)
            }
            R.id.nav_clientes -> {
                navController.navigate(R.id.clientesFragment)
            }
            R.id.nav_contratos -> {
                navController.navigate(R.id.contratosFragment)
            }
            R.id.nav_equipamentos -> {
                navController.navigate(R.id.equipamentosFragment)
            }
            R.id.nav_devolucoes -> {
                navController.navigate(R.id.devolucoesFragment)
            }
            R.id.nav_financial -> {
                navController.navigate(R.id.financialFragment)
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
        }
        
        // Fecha o drawer após a seleção
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    // Método para garantir consistência na fonte após navegações
    private fun enforceFontConsistency() {
        // Observar mudanças na navegação para garantir consistência nas fontes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            LogUtils.debug("DashboardActivity", "Navegando para ${destination.label}")
            
            // Aplicar a fonte ao menu após a navegação
            binding.navView.post {
                applyFontToMenuItems()
            }
            
            // Fechar o drawer após a navegação
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }
    }
    
    private fun setupNotifications() {
        updateNotificationBadge()
    }
    
    private fun updateNotificationBadge() {
        val unreadCount = notificationManager.getUnreadCount()
        binding.tvNotificationBadge.apply {
            if (unreadCount > 0) {
                visibility = View.VISIBLE
                text = if (unreadCount > 99) "99+" else unreadCount.toString()
            } else {
                visibility = View.GONE
            }
        }
    }
    
    private fun showNotificationPanel() {
        if (notificationPopup?.isShowing == true) {
            notificationPopup?.dismiss()
            return
        }
        
        // Criar binding para o painel de notificações
        val panelBinding = LayoutNotificationsPanelBinding.inflate(layoutInflater)
        
        // Configurar RecyclerView
        val adapter = NotificationAdapter(
            notifications = notificationManager.getAllNotifications(),
            onNotificationClick = { notification ->
                LogUtils.debug("DashboardActivity", "Notificação clicada: ${notification.title}")
                notificationPopup?.dismiss()
                // Aqui você pode implementar navegação baseada no tipo de notificação
                handleNotificationClick(notification)
            },
            onMarkAsRead = { notification ->
                notificationManager.markAsRead(notification.id)
                updateNotificationBadge()
            }
        )
        
        panelBinding.rvNotifications.apply {
            layoutManager = LinearLayoutManager(this@DashboardActivity)
            this.adapter = adapter
        }
        
        // Configurar estado vazio
        val notifications = notificationManager.getAllNotifications()
        if (notifications.isEmpty()) {
            panelBinding.rvNotifications.visibility = View.GONE
            panelBinding.layoutEmptyNotifications.visibility = View.VISIBLE
        } else {
            panelBinding.rvNotifications.visibility = View.VISIBLE
            panelBinding.layoutEmptyNotifications.visibility = View.GONE
        }
        
        // Configurar "Marcar todas como lidas"
        panelBinding.tvMarkAllRead.setOnClickListener {
            notificationManager.markAllAsRead()
            adapter.updateNotifications(notificationManager.getAllNotifications())
            updateNotificationBadge()
        }
        
        // Criar e mostrar popup
        notificationPopup = PopupWindow(
            panelBinding.root,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            elevation = 8f
            setBackgroundDrawable(null)
            
            // Posicionar o popup abaixo do ícone de notificação
            val location = IntArray(2)
            binding.ivNotification.getLocationOnScreen(location)
            
            showAtLocation(
                binding.root,
                Gravity.NO_GRAVITY,
                location[0] - 280, // Ajustar posição horizontal
                location[1] + binding.ivNotification.height + 8 // Abaixo do ícone
            )
        }
    }
    
    private fun handleNotificationClick(notification: Notification) {
        // Implementar navegação baseada no tipo de notificação
        when (notification.type) {
            com.example.alg_gestao_02.model.NotificationType.CONTRACT_CREATED -> {
                navController.navigate(R.id.contratosFragment)
            }
            com.example.alg_gestao_02.model.NotificationType.CLIENT_ADDED -> {
                navController.navigate(R.id.clientesFragment)
            }
            com.example.alg_gestao_02.model.NotificationType.EQUIPMENT_AVAILABLE -> {
                navController.navigate(R.id.equipamentosFragment)
            }
            com.example.alg_gestao_02.model.NotificationType.RETURN_PENDING,
            com.example.alg_gestao_02.model.NotificationType.RETURN_COMPLETED -> {
                navController.navigate(R.id.devolucoesFragment)
            }
            else -> {
                // Para notificações gerais, não navegar
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        notificationPopup?.dismiss()
    }

    /**
     * Aplica a fonte Poppins a todos os itens do menu de navegação
     * Nota: Esta é uma solução parcial, pois o Android não fornece uma API direta 
     * para alterar a fonte dos itens de menu após eles serem criados
     */
    private fun applyFontToMenuItems() {
        try {
            // A solução mais robusta é usar o estilo XML no layout do NavigationView
            // Aqui apenas garantimos que o menu seja recarregado para aplicar o estilo
            binding.navView.menu.clear()
            binding.navView.inflateMenu(R.menu.drawer_menu)
            
            // Forçar a invalidação do layout para aplicar os estilos
            binding.navView.invalidate()
            
            LogUtils.debug("DashboardActivity", "Menu recriado para aplicar estilo")
        } catch (e: Exception) {
            LogUtils.error("DashboardActivity", "Erro ao recriar menu: ${e.message}")
        }
    }
} 