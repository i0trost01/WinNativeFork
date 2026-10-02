package com.winlator.cmod.feature.stores.steam.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Regression test for the "No downloadable content found for this game" bug.
 *
 * The wn-steam-client serializes PICS app access tokens as u64 decimal strings.
 * Kotlin's `String.toLongOrNull()` returns null when the value exceeds
 * `Long.MAX_VALUE` (9.2e18), so a token that Steam only serves to the owning
 * account (token-gated appinfo) silently degraded to 0. Steam then omitted the
 * `depots` section from the appinfo, and the download aborted with
 * "No downloadable content found for this game" for affected titles
 * (Castle Crashers 204360, Touhou Luna Nights 851100, Biped 1071870).
 */
class SteamAccessTokenTest {

    private fun expectBitPreserving(raw: String, signed: Long) {
        // The native side casts jlong -> u64, so the signed bit pattern must
        // round-trip to the original unsigned decimal the client emitted.
        val parsed = parseSteamAccessToken(raw)
        assertEquals(signed, parsed)
        val roundTripped = parsed.toULong() // same bits reinterpreted as u64
        assertEquals(raw.toULong(), roundTripped)
    }

    @Test
    fun parsesHighBitTokensWithoutOverflow() {
        // Real values: these are > Long.MAX_VALUE, which toLongOrNull() rejects.
        expectBitPreserving("15813912832642577962", -2632831241066973654L) // Touhou Luna Nights
        expectBitPreserving("10717629973681495491", -7729114100028056125L) // Castle Crashers
        expectBitPreserving("15753866433190949437", -2692877640518602179L) // Biped
        expectBitPreserving("14660352493174483288", -3786391580535068328L) // HL2 package token
    }

    @Test
    fun preservesSmallTokens() {
        assertEquals(5530704555854153228L, parseSteamAccessToken("5530704555854153228"))
        assertEquals(5275412480773448134L, parseSteamAccessToken("5275412480773448134"))
    }

    @Test
    fun handlesZeroAndGarbage() {
        assertEquals(0L, parseSteamAccessToken("0"))
        assertEquals(0L, parseSteamAccessToken(""))
        assertEquals(0L, parseSteamAccessToken("not-a-number"))
    }

    @Test
    fun doesNotSilentlyZeroAValidToken() {
        // The regression: a valid high-bit token must NOT collapse to 0.
        assertNotEquals(0L, parseSteamAccessToken("15813912832642577962"))
        assertTrue(parseSteamAccessToken("15813912832642577962") < 0L)
    }
}