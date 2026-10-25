package com.productcatalog.app.data.remote

import com.productcatalog.app.data.remote.dto.ProductDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("products/all")
    suspend fun getProducts(): List<ProductDto>

    @GET("products/featured")
    suspend fun getFeaturedProducts(): List<ProductDto>

    @GET("categories")
    suspend fun getCategories(): List<String>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: String): ProductDto
}
