package com.example.alg_gestao_02.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.alg_gestao_02.utils.LogUtils
import com.google.android.material.snackbar.Snackbar

/**
 * Fragment base que implementa funcionalidades comuns para todos os fragments.
 * Fornece tratamento centralizado de erros para facilitar a consistência na UI.
 */
abstract class BaseFragment : Fragment() {
    
    /**
     * Sobrescreve onViewCreated para iniciar observers de erro.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Configurar observadores de erro para cada ViewModel retornado pelo método abstrato
        setupErrorObservers()
    }
    
    /**
     * Configura observadores para eventos de erro de todos os ViewModels relacionados.
     */
    private fun setupErrorObservers() {
        getErrorViewModels().forEach { errorViewModel ->
            errorViewModel.errorEvent.observe(viewLifecycleOwner) { event ->
                // Obtém o conteúdo apenas se ainda não foi tratado
                event.getContentIfNotHandled()?.let { errorEvent ->
                    handleErrorEvent(errorEvent)
                }
            }
        }
    }
    
    /**
     * Trata um evento de erro exibindo feedback adequado.
     */
    private fun handleErrorEvent(errorEvent: ErrorViewModel.ErrorEvent) {
        LogUtils.debug("BaseFragment", "Tratando erro: ${errorEvent.message}")
        
        // Exibir Snackbar com a mensagem de erro
        val snackbar = Snackbar.make(
            requireView(),
            errorEvent.message,
            Snackbar.LENGTH_LONG
        )
        
        // Adicionar ação de retry se o erro permitir
        if (errorEvent.exception != null) {
            snackbar.setAction("Tentar novamente") {
                onErrorRetry(errorEvent)
            }
        }
        
        snackbar.show()
    }
    
    /**
     * Método abstrato que deve ser implementado por subclasses para fornecer
     * a lista de ViewModels que contenham ErrorHandler a ser observado.
     */
    abstract fun getErrorViewModels(): List<ErrorViewModel>
    
    /**
     * Método que deve ser implementado por subclasses para tratar a ação de retry.
     */
    abstract fun onErrorRetry(errorEvent: ErrorViewModel.ErrorEvent)
} 