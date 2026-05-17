package com.example.alg_gestao_02.data.models

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class DevolucaoTest {
    private val gson = Gson()

    @Test
    fun `maps createdAt returned by api sql into createdAt property`() {
        val devolucao = gson.fromJson(
            """
            {
              "id": 10,
              "contrato_id": 20,
              "cliente_id": 30,
              "equipamento_id": 40,
              "dev_num": "DEV-001",
              "data_devolucao_prevista": "2026-05-01",
              "quantidade_contratada": 1,
              "quantidade_devolvida": 0,
              "status_item_devolucao": "Pendente",
              "createdAt": "2026-05-04T12:34:56.000Z",
              "updatedAt": "2026-05-04T13:34:56.000Z"
            }
            """.trimIndent(),
            Devolucao::class.java
        )

        assertEquals("2026-05-04T12:34:56.000Z", devolucao.createdAt)
        assertEquals("2026-05-04T13:34:56.000Z", devolucao.updatedAt)
    }

    @Test
    fun `keeps mapping snake case created_at for pdf payload compatibility`() {
        val devolucao = gson.fromJson(
            """
            {
              "id": 11,
              "contrato_id": 21,
              "cliente_id": 31,
              "equipamento_id": 41,
              "dev_num": "DEV-002",
              "data_devolucao_prevista": "2026-05-02",
              "quantidade_contratada": 1,
              "quantidade_devolvida": 0,
              "status_item_devolucao": "Pendente",
              "created_at": "2026-05-03T10:00:00.000Z",
              "updated_at": "2026-05-03T11:00:00.000Z"
            }
            """.trimIndent(),
            Devolucao::class.java
        )

        assertEquals("2026-05-03T10:00:00.000Z", devolucao.createdAt)
        assertEquals("2026-05-03T11:00:00.000Z", devolucao.updatedAt)
    }
}
