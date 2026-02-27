/*
 * Copyright (c) 2026 llh
 * Contact: lilinhong_coding@foxmail.com
 */

package llh.fanclubvup.utils

import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.test.Test
import kotlin.test.assertTrue

class HmacSHAUtilsTest {

    @Test
    fun testGenerateHmacSha256Signature() {
        val raw = "test data"
        val secretKey = "secret key"
        val result = HmacSHAUtils.generateHmacSha256Signature(raw, secretKey)
        assertTrue(HmacSHAUtils.verifyHmacSha256Signature(raw, result.getOrElse { "" }, secretKey))
        assertFalse(HmacSHAUtils.verifyHmacSha256Signature("Tampered data", result.getOrElse { "" }, secretKey))
    }
}