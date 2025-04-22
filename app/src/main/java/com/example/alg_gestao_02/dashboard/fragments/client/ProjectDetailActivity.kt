package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class ProjectDetailActivity : AppCompatActivity() {
    
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_project_detail)
        
        LogUtils.debug("ProjectDetailActivity", "Inicializando tela de detalhes do projeto")
        
        setupViews()
        setupViewPager()
        setupListeners()
    }
    
    private fun setupViews() {
        viewPager = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tabLayout)
    }
    
    private fun setupViewPager() {
        val pagerAdapter = ProjectPagerAdapter(this)
        viewPager.adapter = pagerAdapter
        
        // Conecta o TabLayout com o ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Sumário"
                1 -> "Contratos"
                2 -> "Faturas"
                3 -> "Devoluções"
                else -> "Tab ${position + 1}"
            }
        }.attach()
    }
    
    private fun setupListeners() {
        // Botão de voltar
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            onBackPressed()
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
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
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
                0 -> ProjectSummaryFragment.newInstance()
                1 -> EmptyTabFragment.newInstance("Contratos")
                2 -> EmptyTabFragment.newInstance("Faturas")
                3 -> EmptyTabFragment.newInstance("Devoluções")
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