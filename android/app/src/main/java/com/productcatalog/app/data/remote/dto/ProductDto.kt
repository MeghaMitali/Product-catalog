package com.productcatalog.app.data.remote.dto

import com.productcatalog.app.domain.model.Product
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class ProductDto(
    @SerialName("id") val id: Int,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("price") val price: Double,
    @SerialName("currency") val currency: String = "USD",
    @SerialName("category") val category: String,
    @SerialName("imageUrl") val imageUrl: String = "",
    @SerialName("rating") val rating: RatingDto? = null
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class RatingDto(
    @SerialName("average") val average: Double = 0.0,
    @SerialName("count") val count: Int = 0
)

fun ProductDto.toDomain(): Product = Product(
    id = id.toString(),
    name = title,
    category = category,
    price = price,
    currency = currency,
    description = description,
    imageUrl = imageUrl,
    ratingAverage = rating?.average ?: 0.0,
    ratingCount = rating?.count ?: 0,
    isFeatured = (rating?.average ?: 0.0) >= 4.5
)
