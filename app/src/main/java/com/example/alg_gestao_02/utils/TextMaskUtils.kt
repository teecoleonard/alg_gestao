package com.example.alg_gestao_02.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

/**
 * Utilitário para aplicar máscaras em campos de texto
 */
object TextMaskUtils {
    
    /**
     * Aplica máscara de CPF (000.000.000-00)
     */
    fun insertCpfMask(editText: EditText): TextWatcher {
        return object : TextWatcher {
            var isUpdating = false
            var oldString = ""
            
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = s.toString().replace("[^0-9]".toRegex(), "")
                var mascara = ""
                
                if (isUpdating) {
                    oldString = str
                    isUpdating = false
                    return
                }
                
                if (str.length <= 11) {
                    mascara = when {
                        str.length <= 3 -> str
                        str.length <= 6 -> "${str.substring(0, 3)}.${str.substring(3)}"
                        str.length <= 9 -> "${str.substring(0, 3)}.${str.substring(3, 6)}.${str.substring(6)}"
                        else -> "${str.substring(0, 3)}.${str.substring(3, 6)}.${str.substring(6, 9)}-${str.substring(9)}"
                    }
                } else {
                    mascara = "${str.substring(0, 3)}.${str.substring(3, 6)}.${str.substring(6, 9)}-${str.substring(9, 11)}"
                }
                
                isUpdating = true
                editText.setText(mascara)
                editText.setSelection(mascara.length)
            }
            
            override fun afterTextChanged(s: Editable?) {}
        }
    }
} 