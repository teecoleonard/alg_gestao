package com.example.alg_gestao_02.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContratoMesTest {

    @Test
    fun `extrai mes e ano de data com hora`() {
        val contratoMes = ContratoMes.fromDateTimeString("2026-05-04T12:34:56")

        assertEquals(2026, contratoMes?.ano)
        assertEquals(5, contratoMes?.mes)
    }

    @Test
    fun `extrai mes e ano de data sem hora`() {
        val contratoMes = ContratoMes.fromDateTimeString("2026-05-04")

        assertEquals(2026, contratoMes?.ano)
        assertEquals(5, contratoMes?.mes)
    }

    @Test
    fun `retorna null para data invalida`() {
        assertNull(ContratoMes.fromDateTimeString("data-invalida"))
        assertNull(ContratoMes.fromDateTimeString(""))
    }
}
