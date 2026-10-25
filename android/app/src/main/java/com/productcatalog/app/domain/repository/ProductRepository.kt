package com.productcatalog.app.domain.repository

import com.productcatalog.app.domain.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun syncProducts(): List<Product>
    suspend fun getFeaturedProducts(limit: Int = 5): List<Product>
    suspend fun setFeaturedProducts(productIds: List<String>)
    suspend fun setFeaturedProduct(productId: String, isFeatured: Boolean)
    suspend fun getCategories(): List<String>
    suspend fun getProductsByCategory(category: String): List<Product>
    suspend fun searchProducts(query: String): List<Product>
    suspend fun getProductById(id: String): Product?
}
