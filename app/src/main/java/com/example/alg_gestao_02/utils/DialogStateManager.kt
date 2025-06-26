package com.example.alg_gestao_02.utils

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

/**
 * Gerenciador para preservar o estado dos DialogFragments
 * durante mudanças de configuração e recriação de atividades
 */
object DialogStateManager {
    
    private const val KEY_DIALOGS_STATE = "dialogs_state"
    private const val KEY_DIALOG_COUNT = "dialog_count"
    private const val KEY_DIALOG_TAG_PREFIX = "dialog_tag_"
    private const val KEY_DIALOG_CLASS_PREFIX = "dialog_class_"
    private const val KEY_DIALOG_ARGS_PREFIX = "dialog_args_"
    
    /**
     * Salva o estado de todos os DialogFragments ativos
     */
    fun saveDialogStates(fragmentManager: FragmentManager, outState: Bundle) {
        try {
            LogUtils.debug("DialogStateManager", "💾 Salvando estado dos dialogs...")
            
            val dialogStates = Bundle()
            var dialogCount = 0
            
            // Encontrar todos os DialogFragments ativos
            fragmentManager.fragments.forEachIndexed { index, fragment ->
                if (fragment is DialogFragment && fragment.dialog?.isShowing == true) {
                    LogUtils.debug("DialogStateManager", "💾 Salvando dialog: ${fragment.javaClass.simpleName}")
                    
                    // Salvar informações do dialog
                    dialogStates.putString("${KEY_DIALOG_TAG_PREFIX}$index", fragment.tag)
                    dialogStates.putString("${KEY_DIALOG_CLASS_PREFIX}$index", fragment.javaClass.name)
                    
                    // Salvar argumentos do dialog
                    fragment.arguments?.let { args ->
                        dialogStates.putBundle("${KEY_DIALOG_ARGS_PREFIX}$index", args)
                    }
                    
                    dialogCount++
                }
            }
            
            dialogStates.putInt(KEY_DIALOG_COUNT, dialogCount)
            outState.putBundle(KEY_DIALOGS_STATE, dialogStates)
            
            LogUtils.debug("DialogStateManager", "✅ $dialogCount dialogs salvos com sucesso")
            
        } catch (e: Exception) {
            LogUtils.error("DialogStateManager", "❌ Erro ao salvar estado dos dialogs: ${e.message}", e)
        }
    }
    
    /**
     * Restaura o estado de todos os DialogFragments salvos
     */
    fun restoreDialogStates(fragmentManager: FragmentManager, savedInstanceState: Bundle) {
        try {
            LogUtils.debug("DialogStateManager", "🔄 Restaurando estado dos dialogs...")
            
            val dialogStates = savedInstanceState.getBundle(KEY_DIALOGS_STATE) ?: return
            val dialogCount = dialogStates.getInt(KEY_DIALOG_COUNT, 0)
            
            if (dialogCount == 0) {
                LogUtils.debug("DialogStateManager", "ℹ️ Nenhum dialog para restaurar")
                return
            }
            
            LogUtils.debug("DialogStateManager", "🔄 Tentando restaurar $dialogCount dialogs...")
            
            // Aguardar um pouco para garantir que a atividade foi totalmente iniciada
            // Isso evita problemas com o FragmentManager
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                restoreDialogsDelayed(fragmentManager, dialogStates, dialogCount)
            }, 500) // Aguardar 500ms
            
        } catch (e: Exception) {
            LogUtils.error("DialogStateManager", "❌ Erro ao restaurar estado dos dialogs: ${e.message}", e)
        }
    }
    
    private fun restoreDialogsDelayed(fragmentManager: FragmentManager, dialogStates: Bundle, dialogCount: Int) {
        try {
            var restoredCount = 0
            
            for (i in 0 until dialogCount) {
                val dialogClass = dialogStates.getString("${KEY_DIALOG_CLASS_PREFIX}$i")
                val dialogTag = dialogStates.getString("${KEY_DIALOG_TAG_PREFIX}$i")
                val dialogArgs = dialogStates.getBundle("${KEY_DIALOG_ARGS_PREFIX}$i")
                
                if (dialogClass != null && dialogTag != null) {
                    try {
                        // Verificar se o dialog já existe (evitar duplicatas)
                        val existingDialog = fragmentManager.findFragmentByTag(dialogTag)
                        if (existingDialog != null) {
                            LogUtils.debug("DialogStateManager", "⚠️ Dialog $dialogTag já existe, pulando...")
                            continue
                        }
                        
                        // Criar nova instância do dialog
                        val clazz = Class.forName(dialogClass)
                        val constructor = clazz.getConstructor()
                        val dialogFragment = constructor.newInstance() as DialogFragment
                        
                        // Restaurar argumentos
                        if (dialogArgs != null) {
                            dialogFragment.arguments = dialogArgs
                        }
                        
                        // Mostrar o dialog
                        dialogFragment.show(fragmentManager, dialogTag)
                        restoredCount++
                        
                        LogUtils.debug("DialogStateManager", "✅ Dialog restaurado: ${clazz.simpleName}")
                        
                    } catch (e: Exception) {
                        LogUtils.warning("DialogStateManager", "⚠️ Erro ao restaurar dialog $dialogClass: ${e.message}")
                    }
                }
            }
            
            LogUtils.debug("DialogStateManager", "✅ $restoredCount de $dialogCount dialogs restaurados")
            
        } catch (e: Exception) {
            LogUtils.error("DialogStateManager", "❌ Erro na restauração atrasada: ${e.message}", e)
        }
    }
    
    /**
     * Limpa o estado salvo dos dialogs (para evitar restauração desnecessária)
     */
    fun clearDialogStates(outState: Bundle) {
        try {
            outState.remove(KEY_DIALOGS_STATE)
            LogUtils.debug("DialogStateManager", "🧹 Estado dos dialogs limpo")
        } catch (e: Exception) {
            LogUtils.warning("DialogStateManager", "⚠️ Erro ao limpar estado dos dialogs: ${e.message}")
        }
    }
} 