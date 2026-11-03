package com.example.smarthome_app_v11

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class LedRepositoryTest {
    @Test fun sendsExpectedJsonAndReportsHttpFailure() = runTest {
        MockWebServer().use { server ->
            server.start()
            val client = OkHttpClient.Builder().addInterceptor { chain ->
                assertEquals("http", chain.request().url.scheme)
                chain.proceed(chain.request().newBuilder().url(server.url(chain.request().url.encodedPath)).build())
            }.build()
            val repository = LedRepository(client)
            server.enqueue(MockResponse().setResponseCode(204))
            assertTrue(repository.sendCommand("192.168.1.50", LedCommandDto(10, 20, 30, 40)).isSuccess)
            val request = server.takeRequest(2, TimeUnit.SECONDS)!!
            assertEquals("POST", request.method)
            assertEquals("/api/led", request.path)
            assertEquals("{\"red\":10,\"green\":20,\"blue\":30,\"brightness\":40}", request.body.readUtf8())
            server.enqueue(MockResponse().setResponseCode(503))
            assertTrue(repository.sendCommand("192.168.1.50", LedCommandDto(10, 20, 30, 40)).isFailure)
        }
    }
    @Test fun usesHttpWithoutAdditionalSettings() = runTest {
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            assertEquals("http", chain.request().url.scheme)
            throw java.io.IOException("Test connection failure")
        }.build()
        assertTrue(LedRepository(client).sendCommand("192.168.1.50", LedCommandDto(1, 2, 3, 40)).isFailure)
    }
    @Test fun invalidIpIsRejectedBeforeNetworkRequest() = runTest {
        assertTrue(LedRepository().sendCommand("localhost:80", LedCommandDto(1, 2, 3, 40)).isFailure)
    }
}

