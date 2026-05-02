package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.api.MaterialDisponibilidadeResponse
import com.example.alg_gestao_02.data.models.Material
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource

class MaterialRepository {
    private val apiService = ApiClient.apiService

    suspend fun getMateriais(): Resource<List<Material>> {
        return try {
            val response = apiService.getMateriais()
            if (response.isSuccessful) {
                Resource.Success(response.body()?.data ?: emptyList())
            } else {
                Resource.Error("Erro ao buscar materiais: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao buscar materiais", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun getMateriaisDisponiveis(): Resource<List<Material>> {
        return try {
            val response = apiService.getMateriaisDisponiveis()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Erro ao buscar materiais disponiveis: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao buscar materiais disponiveis", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun getMateriaisComDisponibilidade(): Resource<List<Material>> {
        return try {
            val response = apiService.getMateriaisComDisponibilidade()
            if (response.isSuccessful) {
                Resource.Success(response.body() ?: emptyList())
            } else {
                Resource.Error("Erro ao buscar materiais: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao buscar materiais com disponibilidade", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun getMaterialById(id: Int): Resource<Material> {
        return try {
            val response = apiService.getMaterialById(id)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                Resource.Error("Erro ao buscar material: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao buscar material por ID", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun createMaterial(material: Material): Resource<Material> {
        return try {
            val response = apiService.createMaterial(material)
            if (response.isSuccessful) {
                response.body()?.material?.let { Resource.Success(it) }
                    ?: Resource.Error("Resposta invalida ao criar material")
            } else {
                Resource.Error("Erro ao criar material: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao criar material", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun updateMaterial(id: Int, material: Material): Resource<Material> {
        return try {
            val response = apiService.updateMaterial(id, material)
            if (response.isSuccessful) {
                response.body()?.material?.let { Resource.Success(it) }
                    ?: Resource.Error("Resposta invalida ao atualizar material")
            } else {
                Resource.Error("Erro ao atualizar material: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao atualizar material", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun deleteMaterial(id: Int): Resource<Boolean> {
        return try {
            val response = apiService.deleteMaterial(id)
            if (response.isSuccessful) {
                Resource.Success(true)
            } else {
                Resource.Error("Erro ao excluir material: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao excluir material", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }

    suspend fun verificarDisponibilidade(
        id: Int,
        quantidade: Int? = null
    ): Resource<MaterialDisponibilidadeResponse> {
        return try {
            val response = apiService.verificarDisponibilidadeMaterial(id, quantidade)
            if (response.isSuccessful) {
                response.body()?.let { Resource.Success(it) } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                Resource.Error("Erro ao verificar disponibilidade: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("MaterialRepository", "Erro ao verificar disponibilidade do material", e)
            Resource.Error("Erro de conexao: ${e.message}")
        }
    }
}
