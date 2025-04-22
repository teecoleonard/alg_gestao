package com.example.alg_gestao_02.dashboard.fragments.company

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmpresasFragment : Fragment() {
    
    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerEmpresas: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var fabAddEmpresa: FloatingActionButton
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_empresas, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        LogUtils.debug("EmpresasFragment", "Inicializando fragmento de empresas")
        
        // Inicializar views
        initViews(view)
        setupListeners()
        
        // Mostrar mensagem de "em construção"
        showEmptyState("Empresas em construção")
    }
    
    private fun initViews(view: View) {
        progressBar = view.findViewById(R.id.progressBar)
        recyclerEmpresas = view.findViewById(R.id.rvEmpresas)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
        fabAddEmpresa = view.findViewById(R.id.fabAddEmpresa)
        
        // Inicialmente, ocultar o layout vazio e mostrar o progressBar
        layoutEmpty.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
        recyclerEmpresas.visibility = View.GONE
    }
    
    private fun setupListeners() {
        fabAddEmpresa.setOnClickListener {
            LogUtils.debug("EmpresasFragment", "Botão adicionar empresa clicado")
            Toast.makeText(context, "Adicionar nova empresa", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showEmptyState(message: String) {
        progressBar.visibility = View.GONE
        recyclerEmpresas.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        
        // Configurar a mensagem
        val tvMessage = layoutEmpty.findViewById<TextView>(R.id.tvEmptyMessage)
        tvMessage?.text = message
    }
} 