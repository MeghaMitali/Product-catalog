package com.productcatalog.app.domain.repository

import com.productcatalog.app.data.remote.EnquiryApi
import com.productcatalog.app.data.remote.dto.EnquiryResponseDto
import com.productcatalog.app.domain.model.EnquiryRequest
import com.productcatalog.app.domain.repository.impl.EnquiryRepositoryImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertTrue

class EnquiryRepositoryImplTest {

    @Test
    fun `submitEnquiry succeeds when backend confirms receipt`() = runTest {
        val api = mockk<EnquiryApi>()
        val request = EnquiryRequest(
            productId = "p-1",
            name = "Ava",
            email = "ava@example.com",
            phone = "123456",
            message = "Need details"
        )
        coEvery { api.submitEnquiry(any()) } returns EnquiryResponseDto(status = "received")

        val repository = EnquiryRepositoryImpl(api)
        val result = repository.submitEnquiry(request)

        assertTrue(result.isSuccess)
    }

    @Test
    fun `submitEnquiry fails when backend returns a non received status`() = runTest {
        val api = mockk<EnquiryApi>()
        val request = EnquiryRequest(
            productId = "p-1",
            name = "Ava",
            email = "ava@example.com",
            phone = "123456",
            message = "Need details"
        )
        coEvery { api.submitEnquiry(any()) } returns EnquiryResponseDto(status = "rejected")

        val repository = EnquiryRepositoryImpl(api)
        val result = repository.submitEnquiry(request)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }

    @Test
    fun `submitEnquiry propagates io exceptions`() = runTest {
        val api = mockk<EnquiryApi>()
        val request = EnquiryRequest(
            productId = "p-1",
            name = "Ava",
            email = "ava@example.com",
            phone = "123456",
            message = "Need details"
        )
        coEvery { api.submitEnquiry(any()) } throws IOException("No network")

        val repository = EnquiryRepositoryImpl(api)
        val result = repository.submitEnquiry(request)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IOException)
    }

    @Test
    fun `submitEnquiry wraps http errors with response body details`() = runTest {
        val api = mockk<EnquiryApi>()
        val request = EnquiryRequest(
            productId = "p-1",
            name = "Ava",
            email = "ava@example.com",
            phone = "123456",
            message = "Need details"
        )
        val response = Response.error<EnquiryResponseDto>(
            400,
            "bad request".toResponseBody("text/plain".toMediaType())
        )
        coEvery { api.submitEnquiry(any()) } throws HttpException(response)

        val repository = EnquiryRepositoryImpl(api)
        val result = repository.submitEnquiry(request)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
}
