package com.example.alg_gestao_02.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Assinatura(
    val id: Int,
    val nome_arquivo: String?,
    val cliente_id: Int,
    val contrato_id: Int
) : Parcelable