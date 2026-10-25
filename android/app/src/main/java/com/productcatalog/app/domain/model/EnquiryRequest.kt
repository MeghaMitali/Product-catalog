package com.productcatalog.app.domain.model

data class EnquiryRequest(
    val productId: String,
    val name: String,
    val email: String,
    val phone: String = "",
    val message: String
)
