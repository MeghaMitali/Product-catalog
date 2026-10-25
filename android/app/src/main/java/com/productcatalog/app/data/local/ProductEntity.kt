package com.productcatalog.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.productcatalog.app.domain.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val currency: String = "USD",
    val description: String,
    val imageUrl: String = "",
    val ratingAverage: Double = 0.0,
    val ratingCount: Int = 0,
    val featured: Boolean = false
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    name = name,
    category = category,
    price = price,
    currency = currency,
    description = description,
    imageUrl = imageUrl,
    ratingAverage = ratingAverage,
    ratingCount = ratingCount,
    featured = isFeatured
)

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    name = name,
    category = category,
    price = price,
    currency = currency,
    description = description,
    imageUrl = imageUrl,
    ratingAverage = ratingAverage,
    ratingCount = ratingCount,
    isFeatured = featured
)
