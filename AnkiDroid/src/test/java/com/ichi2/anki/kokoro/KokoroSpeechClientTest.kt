// SPDX-License-Identifier: GPL-3.0-or-later

package com.ichi2.anki.kokoro

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.json.JSONObject
import org.junit.Assert.assertThrows
import org.junit.Test

class KokoroSpeechClientTest {
    @Test
    fun synthesizePostsMp3RequestAndReturnsBody() {
        lateinit var capturedBody: String
        var authorization: String? = null
        var url: String? = null
        val client =
            clientResponding(200, "ID3fake".toByteArray(), "audio/mpeg") { request ->
                authorization = request.header("Authorization")
                url = request.url.toString()
                val buffer = Buffer()
                request.body!!.writeTo(buffer)
                capturedBody = buffer.readUtf8()
            }
        val audio =
            KokoroSpeechClient(client).synthesize(
                apiKey = "secret-key",
                input = "Hello",
                voice = "af_alloy",
            )

        assertThat(audio.toString(Charsets.UTF_8), equalTo("ID3fake"))
        assertThat(authorization, equalTo("Bearer secret-key"))
        assertThat(url, equalTo(KokoroSpeechClient.ENDPOINT.toString()))
        val json = JSONObject(capturedBody)
        assertThat(json.getString("model"), equalTo(KokoroSpeechClient.MODEL))
        assertThat(json.getString("input"), equalTo("Hello"))
        assertThat(json.getString("voice"), equalTo("af_alloy"))
        assertThat(json.getString("response_format"), equalTo("mp3"))
    }

    @Test
    fun synthesizeThrowsApiMessageOnError() {
        val client =
            clientResponding(
                code = 401,
                body = """{"error":{"code":401,"message":"Missing Authentication header"}}""".toByteArray(),
                mediaType = "application/json",
            )
        val error =
            assertThrows(KokoroSpeechException::class.java) {
                KokoroSpeechClient(client).synthesize("bad", "Hello", "af_alloy")
            }
        assertThat(error.message, equalTo("Missing Authentication header"))
    }

    private fun clientResponding(
        code: Int,
        body: ByteArray,
        mediaType: String,
        onRequest: (Request) -> Unit = {},
    ): OkHttpClient =
        OkHttpClient
            .Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                onRequest(request)
                Response
                    .Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message("test")
                    .body(body.toResponseBody(mediaType.toMediaType()))
                    .build()
            }.build()
}
