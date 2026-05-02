package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MaterialContrato(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName(value = "contratoId", alternate = ["contrato_id"])
    val contratoId: Int = 0,

    @SerializedName(value = "materialId", alternate = ["material_id"])
    val materialId: Int,

    @SerializedName(value = "quantidade", alternate = ["quantidadeMaterial"])
    val quantidade: Int,

    @SerializedName("valorUnitario")
    val valorUnitario: Double,

    @SerializedName("valorTotal")
    val valorTotal: Double,

    @SerializedName("material")
    val material: Material? = null
) : Parcelable {
    val nomeMaterialExibicao: String
        get() = material?.nome ?: "Material $materialId"

    companion object {
        fun generateTempId(): Int {
            val timestamp = System.currentTimeMillis()
            return (timestamp % 9_000_000).toInt() + 1_000_001
        }
    }
}
