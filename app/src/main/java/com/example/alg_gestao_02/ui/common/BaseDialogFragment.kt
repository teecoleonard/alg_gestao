package com.example.alg_gestao_02.ui.common

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.example.alg_gestao_02.utils.LogUtils

/**
 * Classe base para DialogFragments que facilita o gerenciamento de estado
 * e garante que os dialogs sejam preservados durante mudanças de configuração
 */
abstract class BaseDialogFragment : DialogFragment() {
    
    /**
     * Tag único para este dialog (deve ser implementado por subclasses)
     */
    protected abstract fun getDialogTag(): String
    
    /**
     * Método para mostrar o dialog com tag única
     */
    fun showWithUniqueTag(manager: FragmentManager) {
        val tag = getDialogTag()
        
        // Verificar se já existe um dialog com a mesma tag
        val existingFragment = manager.findFragmentByTag(tag)
        if (existingFragment != null && existingFragment.isAdded) {
            LogUtils.debug("BaseDialogFragment", "⚠️ Dialog $tag já está sendo exibido, ignorando...")
            return
        }
        
        LogUtils.debug("BaseDialogFragment", "📱 Exibindo dialog: $tag")
        show(manager, tag)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.debug("BaseDialogFragment", "🔧 Criando dialog: ${getDialogTag()}")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        LogUtils.debug("BaseDialogFragment", "🗑️ Destruindo dialog: ${getDialogTag()}")
    }
    
    /**
     * Método utilitário para fechar o dialog de forma segura
     */
    protected fun dismissSafely() {
        try {
            if (isAdded && !isStateSaved) {
                dismiss()
                LogUtils.debug("BaseDialogFragment", "✅ Dialog ${getDialogTag()} fechado com segurança")
            } else {
                dismissAllowingStateLoss()
                LogUtils.debug("BaseDialogFragment", "⚠️ Dialog ${getDialogTag()} fechado com allowStateLoss")
            }
        } catch (e: Exception) {
            LogUtils.error("BaseDialogFragment", "❌ Erro ao fechar dialog ${getDialogTag()}: ${e.message}", e)
        }
    }
} 