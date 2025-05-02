package com.example.alg_gestao_02.data.repository

import com.example.alg_gestao_02.data.api.ApiClient
import com.example.alg_gestao_02.data.api.ApiService
import com.example.alg_gestao_02.data.models.User
import com.example.alg_gestao_02.utils.LogUtils
import com.example.alg_gestao_02.utils.Resource

/**
 * Repositório para gerenciar autenticação
 */
class AuthRepository {
    private val apiService = ApiClient.apiService
    
    /**
     * Realiza o login do usuário
     * @param cpf CPF do usuário
     * @param senha Senha do usuário
     * @return Resource contendo o resultado da operação
     */
    suspend fun login(cpf: String, senha: String): Resource<ApiService.LoginResponse> {
        return try {
            LogUtils.debug("AuthRepository", "Tentando login para o cpf: $cpf")
            val loginRequest = ApiService.LoginRequest(cpf, senha)
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    LogUtils.info("AuthRepository", "Login bem-sucedido para: $cpf")
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("AuthRepository", "Falha ao fazer login: ${response.code()}")
                Resource.Error("Erro ao fazer login: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("AuthRepository", "Erro ao fazer login", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
    
    /**
     * Registra um novo usuário
     * @param cpf CPF do usuário
     * @param nome Nome do usuário
     * @param senha Senha do usuário
     * @param role Função do usuário (funcionario ou cliente)
     * @return Resource contendo o resultado da operação
     */
    suspend fun register(cpf: String, nome: String, senha: String, role: String): Resource<ApiService.LoginResponse> {
        return try {
            LogUtils.debug("AuthRepository", "Tentando registrar usuário: $nome")
            val registerRequest = ApiService.RegisterRequest(cpf, nome, senha, role)
            val response = apiService.register(registerRequest)
            
            if (response.isSuccessful) {
                response.body()?.let {
                    LogUtils.info("AuthRepository", "Registro bem-sucedido para: $nome")
                    Resource.Success(it)
                } ?: Resource.Error("Resposta vazia do servidor")
            } else {
                LogUtils.warning("AuthRepository", "Falha ao registrar: ${response.code()}")
                Resource.Error("Erro ao registrar: ${response.message()}")
            }
        } catch (e: Exception) {
            LogUtils.error("AuthRepository", "Erro ao registrar usuário", e)
            Resource.Error("Erro de conexão: ${e.message}")
        }
    }
} 