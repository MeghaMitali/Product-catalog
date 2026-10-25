package com.productcatalog.app.ui.search

import com.productcatalog.app.domain.model.Product

data class SearchUiState(
    val query: String = "",
    val results: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
