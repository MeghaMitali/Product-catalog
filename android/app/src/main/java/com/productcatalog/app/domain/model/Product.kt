package com.productcatalog.app.domain.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val currency: String = "USD",
    val description: String,
    val imageUrl: String,
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val isFeatured: Boolean = false
)
