package com.sol.core.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SolFormattingTest {

    // region parseSolToLamports

    @Test
    fun `parseSolToLamports converts whole SOL`() {
        assertEquals(1_000_000_000L, SolFormatting.parseSolToLamports("1"))
    }

    @Test
    fun `parseSolToLamports converts fractional SOL`() {
        assertEquals(1_500_000_000L, SolFormatting.parseSolToLamports("1.5"))
    }

    @Test
    fun `parseSolToLamports converts smallest unit`() {
        assertEquals(1L, SolFormatting.parseSolToLamports("0.000000001"))
    }

    @Test
    fun `parseSolToLamports accepts comma as decimal separator`() {
        assertEquals(2_250_000_000L, SolFormatting.parseSolToLamports("2,25"))
    }

    @Test
    fun `parseSolToLamports trims surrounding whitespace`() {
        assertEquals(1_000_000_000L, SolFormatting.parseSolToLamports("  1  "))
    }

    @Test
    fun `parseSolToLamports truncates sub-lamport precision toward zero`() {
        // 0.0000000019 SOL -> 1.9 lamports -> truncated DOWN to 1
        assertEquals(1L, SolFormatting.parseSolToLamports("0.0000000019"))
    }

    @Test
    fun `parseSolToLamports returns zero for zero amount`() {
        assertEquals(0L, SolFormatting.parseSolToLamports("0"))
    }

    @Test
    fun `parseSolToLamports throws on empty input`() {
        assertThrows(IllegalArgumentException::class.java) {
            SolFormatting.parseSolToLamports("")
        }
    }

    @Test
    fun `parseSolToLamports throws on blank input`() {
        assertThrows(IllegalArgumentException::class.java) {
            SolFormatting.parseSolToLamports("   ")
        }
    }

    @Test
    fun `parseSolToLamports throws on non-numeric input`() {
        assertThrows(NumberFormatException::class.java) {
            SolFormatting.parseSolToLamports("abc")
        }
    }

    @Test
    fun `parseSolToLamports throws when value overflows Long`() {
        assertThrows(ArithmeticException::class.java) {
            SolFormatting.parseSolToLamports("99999999999999")
        }
    }

    // endregion

    // region formatLamportsAsSol

    @Test
    fun `formatLamportsAsSol formats whole SOL`() {
        assertEquals("1", SolFormatting.formatLamportsAsSol(1_000_000_000L))
    }

    @Test
    fun `formatLamportsAsSol formats fractional SOL`() {
        assertEquals("1.5", SolFormatting.formatLamportsAsSol(1_500_000_000L))
    }

    @Test
    fun `formatLamportsAsSol formats zero`() {
        assertEquals("0", SolFormatting.formatLamportsAsSol(0L))
    }

    @Test
    fun `formatLamportsAsSol formats single lamport`() {
        assertEquals("0.000000001", SolFormatting.formatLamportsAsSol(1L))
    }

    @Test
    fun `formatLamportsAsSol strips trailing zeros`() {
        assertEquals("0.1", SolFormatting.formatLamportsAsSol(100_000_000L))
    }

    @Test
    fun `parse and format are inverse for representable values`() {
        val lamports = SolFormatting.parseSolToLamports("3.14")
        assertEquals("3.14", SolFormatting.formatLamportsAsSol(lamports))
    }

    // endregion
}
