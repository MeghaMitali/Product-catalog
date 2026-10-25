package com.productcatalog.app.data.remote.dto

import com.productcatalog.app.domain.model.EnquiryRequest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EnquiryRequestDto(
    @SerialName("productId") val productId: String,
    @SerialName("name") val name: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String = "",
    @SerialName("message") val message: String
)

@Serializable
data class EnquiryResponseDto(
    @SerialName("status") val status: String = "received"
)

fun EnquiryRequest.toDto(): EnquiryRequestDto = EnquiryRequestDto(
    productId = productId,
    name = name,
    email = email,
    phone = phone,
    message = message
)
