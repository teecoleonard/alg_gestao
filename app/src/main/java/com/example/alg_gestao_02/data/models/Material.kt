package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Material(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("nome")
    val nome: String,

    @SerializedName("codigo")
    val codigo: String? = null,

    @SerializedName("valorUnitario")
    val valorUnitario: Double,

    @SerializedName("quantidadeDisponivel")
    val quantidadeDisponivel: Int,

    @SerializedName("ativo")
    val ativo: Boolean = true,

    @SerializedName("quantidade_total")
    val quantidadeTotal: Int? = null,

    @SerializedName("quantidade_em_uso")
    val quantidadeEmUso: Int? = null,

    @SerializedName("percentualDisponivel")
    val percentualDisponivel: Int? = null
) : Parcelable {
    fun isDisponivel(): Boolean = getQuantidadeDisponivelAtual() > 0

    fun getQuantidadeDisponivelAtual(): Int {
        return quantidadeTotal?.let { total ->
            val emUso = quantidadeEmUso ?: 0
            (total - emUso).coerceAtLeast(0)
        } ?: quantidadeDisponivel
    }

    fun temQuantidadeDisponivel(quantidade: Int): Boolean {
        return getQuantidadeDisponivelAtual() >= quantidade
    }
}
