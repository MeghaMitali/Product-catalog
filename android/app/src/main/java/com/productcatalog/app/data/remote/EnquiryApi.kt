package com.productcatalog.app.data.remote

import com.productcatalog.app.data.remote.dto.EnquiryRequestDto
import com.productcatalog.app.data.remote.dto.EnquiryResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface EnquiryApi {
    @POST("enquiries")
    suspend fun submitEnquiry(@Body enquiry: EnquiryRequestDto): EnquiryResponseDto
}
