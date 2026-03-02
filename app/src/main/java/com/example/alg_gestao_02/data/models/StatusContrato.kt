package com.example.alg_gestao_02.data.models

/**
 * Enum para representar os diferentes status do ciclo de vida de um contrato
 */
enum class StatusContrato(val valor: String, val descricao: String) {
    PENDENTE("PENDENTE", "Pendente"),
    ASSINADO("ASSINADO", "Assinado"),
    EM_ANDAMENTO("EM_ANDAMENTO", "Em Andamento"),
    FINALIZADO("FINALIZADO", "Finalizado"),
    FATURADO("FATURADO", "Faturado"),
    CANCELADO("CANCELADO", "Cancelado");
    
    companion object {
        /**
         * Converte uma string para o enum correspondente
         * @param valor String do status
         * @return StatusContrato correspondente ou PENDENTE se não encontrado
         */
        fun fromString(valor: String?): StatusContrato {
            if (valor == null) return PENDENTE
            return values().find { it.valor == valor } ?: PENDENTE
        }
    }
    
    /**
     * Retorna a cor correspondente ao status para exibição na UI
     * @return Código de cor em formato hexadecimal
     */
    fun getCor(): Int {
        return when (this) {
            PENDENTE -> 0xFFFFA726.toInt()    // Laranja
            ASSINADO -> 0xFF42A5F5.toInt()    // Azul
            EM_ANDAMENTO -> 0xFF66BB6A.toInt() // Verde
            FINALIZADO -> 0xFF9E9E9E.toInt()   // Cinza
            FATURADO -> 0xFFEF5350.toInt()     // Vermelho
            CANCELADO -> 0xFFEF5350.toInt()    // Vermelho
        }
    }
    
    /**
     * Retorna o ícone emoji correspondente ao status
     */
    fun getIcone(): String {
        return when (this) {
            PENDENTE -> "🟡"
            ASSINADO -> "🔵"
            EM_ANDAMENTO -> "🟢"
            FINALIZADO -> "✅"
            FATURADO -> "💰"
            CANCELADO -> "❌"
        }
    }
}

