// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.kokoro

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Synthesizes speech with Kokoro 82M through OpenRouter.
 *
 * https://openrouter.ai/hexgrad/kokoro-82m
 */
class KokoroSpeechClient(
    private val client: OkHttpClient = defaultClient,
    private val endpoint: HttpUrl = ENDPOINT,
) {
    /**
     * @return MP3 bytes
     * @throws KokoroSpeechException when the request fails or the body is empty
     */
    fun synthesize(
        apiKey: String,
        input: String,
        voice: String,
    ): ByteArray {
        val payload =
            JSONObject()
                .put("model", MODEL)
                .put("input", input)
                .put("voice", voice)
                .put("response_format", "mp3")
                .toString()
        val request =
            Request
                .Builder()
                .url(endpoint)
                .header("Authorization", "Bearer $apiKey")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()
        client.newCall(request).execute().use { response ->
            val bytes = response.body.bytes()
            if (!response.isSuccessful) {
                throw KokoroSpeechException(errorMessage(bytes) ?: "HTTP ${response.code}")
            }
            if (bytes.isEmpty()) {
                throw KokoroSpeechException("Empty audio")
            }
            return bytes
        }
    }

    private fun errorMessage(bytes: ByteArray): String? =
        try {
            JSONObject(bytes.toString(Charsets.UTF_8))
                .optJSONObject("error")
                ?.optString("message")
                ?.takeIf { it.isNotEmpty() }
        } catch (_: Exception) {
            null
        }

    companion object {
        const val MODEL = "hexgrad/kokoro-82m"
        const val DEFAULT_VOICE = "af_alloy"
        val ENDPOINT: HttpUrl = "https://openrouter.ai/api/v1/audio/speech".toHttpUrl()

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private val defaultClient: OkHttpClient =
            OkHttpClient
                .Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
    }
}

class KokoroSpeechException(
    message: String,
) : Exception(message)
