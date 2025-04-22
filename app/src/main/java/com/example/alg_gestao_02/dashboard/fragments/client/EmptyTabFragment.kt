package com.example.alg_gestao_02.dashboard.fragments.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.R

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
        view.findViewById<AppCompatTextView>(R.id.tvEmptyTitle).text = 
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