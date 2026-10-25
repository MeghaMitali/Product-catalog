package com.productcatalog.app.domain.repository.impl

import com.productcatalog.app.data.local.ProductDao
import com.productcatalog.app.data.local.toDomain
import com.productcatalog.app.data.local.toEntity
import com.productcatalog.app.data.remote.ProductApi
import com.productcatalog.app.data.remote.dto.toDomain
import com.productcatalog.app.domain.model.Product
import com.productcatalog.app.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val productApi: ProductApi,
    private val productDao: ProductDao
) : ProductRepository {

    override suspend fun getProducts(): List<Product> =
        productDao.getAll().map { it.toDomain() }

    override suspend fun syncProducts(): List<Product> {
        val apiProducts = productApi.getProducts().map { it.toDomain() }
        if (apiProducts.isNotEmpty()) {
            productDao.insertAll(apiProducts.map { it.toEntity() })
        }
        return productDao.getAll().map { it.toDomain() }
    }

    override suspend fun getFeaturedProducts(limit: Int): List<Product> {
        val cached = productDao.getFeatured(limit).map { it.toDomain() }
        if (cached.isNotEmpty()) return cached

        return runCatching {
            productApi.getFeaturedProducts().map { it.toDomain() }
        }.getOrNull()?.take(limit) ?: emptyList()
    }

    override suspend fun setFeaturedProducts(productIds: List<String>) {
        val selected = productIds.toSet()
        productDao.getAll().forEach { product ->
            productDao.updateFeatured(product.id, product.id in selected)
        }
    }

    override suspend fun setFeaturedProduct(productId: String, isFeatured: Boolean) {
        productDao.updateFeatured(productId, isFeatured)
    }

    override suspend fun getCategories(): List<String> {
        val cached = productDao.getCategories()
        if (cached.isNotEmpty()) return cached

        return runCatching { productApi.getCategories() }.getOrNull() ?: emptyList()
    }

    override suspend fun getProductsByCategory(category: String): List<Product> =
        productDao.getByCategory(category).map { it.toDomain() }

    override suspend fun searchProducts(query: String): List<Product> =
        if (query.isBlank()) emptyList() else productDao.search(query).map { it.toDomain() }

    override suspend fun getProductById(id: String): Product? {
        return productDao.getById(id)?.toDomain() ?: runCatching {
            productApi.getProductById(id).toDomain()
        }.getOrNull()?.also { product ->
            productDao.insertAll(listOf(product.toEntity()))
        }
    }
}
