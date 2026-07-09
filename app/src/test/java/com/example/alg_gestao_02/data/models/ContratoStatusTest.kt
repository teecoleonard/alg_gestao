package com.example.alg_gestao_02.data.models

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ContratoStatusTest {

    private fun contrato(status: String?) = Contrato(
        clienteId = 1,
        contratoValor = 0.0,
        statusContrato = status
    )

    @Test
    fun `isEmAndamento reconhece status exato`() {
        assertTrue(contrato("EM_ANDAMENTO").isEmAndamento())
    }

    @Test
    fun `isEmAndamento reconhece variacoes de caixa e separador`() {
        assertTrue(contrato("em_andamento").isEmAndamento())
        assertTrue(contrato("Em Andamento").isEmAndamento())
        assertTrue(contrato(" EM-ANDAMENTO ").isEmAndamento())
    }

    @Test
    fun `isEmAndamento rejeita outros status`() {
        assertFalse(contrato("PENDENTE").isEmAndamento())
        assertFalse(contrato(null).isEmAndamento())
    }

    @Test
    fun `isFinalizado reconhece variacoes de caixa`() {
        assertTrue(contrato("FINALIZADO").isFinalizado())
        assertTrue(contrato("finalizado").isFinalizado())
        assertTrue(contrato(" Finalizado ").isFinalizado())
    }

    @Test
    fun `isFinalizado rejeita outros status`() {
        assertFalse(contrato("EM_ANDAMENTO").isFinalizado())
        assertFalse(contrato(null).isFinalizado())
    }
}
