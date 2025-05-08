package com.example.alg_gestao_02.data.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Modelo de dados para Equipamento
 */
data class Equipamento(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("nomeEquip")
    val nomeEquip: String,
    
    @SerializedName("precoDiaria")
    val precoDiaria: Double,
    
    @SerializedName("precoSemanal")
    val precoSemanal: Double,
    
    @SerializedName("precoQuinzenal")
    val precoQuinzenal: Double,
    
    @SerializedName("precoMensal")
    val precoMensal: Double,
    
    @SerializedName("codigoEquip")
    val codigoEquip: String,
    
    @SerializedName("quantidadeDisp")
    val quantidadeDisp: Int,
    
    @SerializedName("valorPatrimonio")
    val valorPatrimonio: Double? = null
) : Parcelable {
    
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readInt(),
        if (parcel.readByte() == 0.toByte()) null else parcel.readDouble()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(nomeEquip)
        parcel.writeDouble(precoDiaria)
        parcel.writeDouble(precoSemanal)
        parcel.writeDouble(precoQuinzenal)
        parcel.writeDouble(precoMensal)
        parcel.writeString(codigoEquip)
        parcel.writeInt(quantidadeDisp)
        if (valorPatrimonio == null) {
            parcel.writeByte(0)
        } else {
            parcel.writeByte(1)
            parcel.writeDouble(valorPatrimonio)
        }
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Parcelable.Creator<Equipamento> {
        override fun createFromParcel(parcel: Parcel): Equipamento {
            return Equipamento(parcel)
        }
        
        override fun newArray(size: Int): Array<Equipamento?> {
            return arrayOfNulls(size)
        }
    }
} 