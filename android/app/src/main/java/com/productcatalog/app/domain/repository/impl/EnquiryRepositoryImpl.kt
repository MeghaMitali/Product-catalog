package com.productcatalog.app.domain.repository.impl

import com.productcatalog.app.data.remote.EnquiryApi
import com.productcatalog.app.data.remote.dto.toDto
import com.productcatalog.app.domain.model.EnquiryRequest
import com.productcatalog.app.domain.repository.EnquiryRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class EnquiryRepositoryImpl @Inject constructor(
    private val api: EnquiryApi
) : EnquiryRepository {
    override suspend fun submitEnquiry(request: EnquiryRequest): Result<Unit> =
        runCatching {
            val response = api.submitEnquiry(request.toDto())
            if (response.status != "received") {
                throw IllegalStateException(response.status)
            }
        }.recoverCatching { throwable ->
            when (throwable) {
                is IOException -> throw throwable
                is HttpException -> {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    throw IllegalStateException(errorBody ?: throwable.message())
                }
                else -> throw throwable
            }
        }
}
