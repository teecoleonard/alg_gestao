package com.example.alg_gestao_02.dashboard.fragments.client

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.DashboardActivity
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProjectDetailActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private var projectId: String = ""
    private var projectName: String = ""
    private var isFinishing = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_detail)
        
        // Obter o ID do projeto dos extras
        projectId = intent.getStringExtra("project_id") ?: ""
        projectName = intent.getStringExtra("project_name") ?: ""
        
        LogUtils.debug("ProjectDetailActivity", "Inicializando tela de detalhes do projeto: $projectId - $projectName")
        
        setupViews()
        setupViewPager()
        setupListeners()
        setupBackNavigation()
    }
    
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
        
        // Configurar título do projeto
        findViewById<AppCompatTextView>(R.id.tvProjectName)?.text = projectName
    }
    
    private fun setupViewPager() {
        val pagerAdapter = ProjectPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // Conecta o TabLayout com o ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Sumário"
                1 -> "Contratos"
                2 -> "Devoluções"
                3 -> "Faturas"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }
    
    private fun setupListeners() {
        // Botão de voltar
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finishActivity()
        }
        
        // Botão de notificação
        findViewById<ImageButton>(R.id.ivNotification).setOnClickListener {
            LogUtils.debug("ProjectDetailActivity", "Botão de notificação clicado")
        }
        
        // Botão de menu
        findViewById<ImageButton>(R.id.ivMenu).setOnClickListener {
            LogUtils.debug("ProjectDetailActivity", "Botão de menu clicado")
        }
    }
    
    private fun setupBackNavigation() {
        // Registra o callback para o novo sistema de navegação de volta
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishActivity()
            }
        })
    }
    
    private fun finishActivity() {
        if (isFinishing) return
        
        LogUtils.debug("ProjectDetailActivity", "Finalizando activity com navegação forçada")
        isFinishing = true
        
        // Criar um intent explícito para DashboardActivity para garantir a navegação
        val intent = Intent(this, DashboardActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        
        // Aplicar finish() com um pequeno delay para garantir que as transições sejam concluídas
        Handler(Looper.getMainLooper()).postDelayed({
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 100)
    }
    
    @Deprecated("Substituído pelo novo mecanismo OnBackPressedDispatcher")
    override fun onBackPressed() {
        LogUtils.debug("ProjectDetailActivity", "onBackPressed")
        finishActivity()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finishActivity()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
    
    /**
     * Adapter para o ViewPager que gerencia os fragments das abas
     */
    inner class ProjectPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        
        override fun getItemCount(): Int = 4
        
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> ProjectSummaryFragment.newInstance(projectId)
                1 -> ProjectContractsFragment.newInstance(projectId)
                2 -> EmptyTabFragment.newInstance("Devoluções")
                3 -> ProjectInvoicesFragment.newInstance(projectId)
                else -> EmptyTabFragment.newInstance("Tab")
            }
        }
    }
    
    /**
     * Fragment temporário para as abas que ainda não estão implementadas
     */
    class EmptyTabFragment : Fragment() {
        
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            return inflater.inflate(R.layout.fragment_empty_tab, container, false)
        }
        
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            
            // Configura o título da aba vazia
            val title = arguments?.getString(ARG_TITLE) ?: "Tab"
            view.findViewById<androidx.appcompat.widget.AppCompatTextView>(R.id.tvEmptyTitle).text = 
                "$title em desenvolvimento"
        }
        
        companion object {
            private const val ARG_TITLE = "arg_title"
            
            fun newInstance(title: String): EmptyTabFragment {
                val fragment = EmptyTabFragment()
                val args = Bundle()
                args.putString(ARG_TITLE, title)
                fragment.arguments = args
                return fragment
            }
        }
    }
} 