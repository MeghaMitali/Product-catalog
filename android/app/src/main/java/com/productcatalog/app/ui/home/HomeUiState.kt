package com.productcatalog.app.ui.home

import com.productcatalog.app.domain.model.Product

data class HomeUiState(
    val products: List<Product> = emptyList(),
    val featuredProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null
)
