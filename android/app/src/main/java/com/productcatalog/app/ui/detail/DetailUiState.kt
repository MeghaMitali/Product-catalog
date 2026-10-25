package com.productcatalog.app.ui.detail

import com.productcatalog.app.domain.model.Product

data class DetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
