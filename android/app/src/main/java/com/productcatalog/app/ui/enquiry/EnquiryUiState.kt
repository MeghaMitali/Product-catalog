package com.productcatalog.app.ui.enquiry

data class EnquiryUiState(
    val productId: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
