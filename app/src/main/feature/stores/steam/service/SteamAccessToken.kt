package com.winlator.cmod.feature.stores.steam.service

/**
 * Parses a PICS access-token decimal string into the signed [Long] bit pattern
 * the native client expects.
 *
 * wn-steam-client serializes PICS app/package access tokens as unsigned 64-bit
 * decimal strings (e.g. "15813912832642577962"). A plain `toLongOrNull()` fails
 * for values above [Long.MAX_VALUE] and would silently drop the token to 0 —
 * which made Steam omit the `depots` section of token-gated appinfo and aborted
 * downloads with "No downloadable content found for this game".
 *
 * Tokens that fit in a signed Long are preserved; larger values are
 * re-interpreted bit-for-bit (two's complement), which matches how the native
 * side casts a jlong back to u64.
 */
internal fun parseSteamAccessToken(value: String?): Long {
    if (value.isNullOrBlank()) return 0L
    val trimmed = value.trim()
    // ULong parses the full 64-bit range; .toLong() reinterprets the bits so the
    // jlong -> u64 cast on the native side round-trips exactly.
    return trimmed.toULongOrNull()?.toLong() ?: 0L
}