package com.productcatalog.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Query("SELECT * FROM products ORDER BY featured DESC, ratingAverage DESC, name ASC")
    suspend fun getAll(): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY featured DESC, ratingAverage DESC, name ASC")
    suspend fun getByCategory(category: String): List<ProductEntity>

    @Query("SELECT * FROM products WHERE featured = 1 ORDER BY ratingAverage DESC, name ASC LIMIT :limit")
    suspend fun getFeatured(limit: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE category = :category AND featured = 1 ORDER BY ratingAverage DESC, name ASC LIMIT :limit")
    suspend fun getFeaturedByCategory(category: String, limit: Int): List<ProductEntity>

    @Query("SELECT DISTINCT category FROM products ORDER BY category ASC")
    suspend fun getCategories(): List<String>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ProductEntity?

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY featured DESC, ratingAverage DESC, name ASC")
    suspend fun search(query: String): List<ProductEntity>

    @Query("UPDATE products SET featured = :featured WHERE id = :productId")
    suspend fun updateFeatured(productId: String, featured: Boolean)

    @Query("DELETE FROM products")
    suspend fun clearAll()
}
