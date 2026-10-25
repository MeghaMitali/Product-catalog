package com.productcatalog.app.domain.repository

import com.productcatalog.app.data.local.ProductDao
import com.productcatalog.app.data.local.ProductEntity
import com.productcatalog.app.data.local.toDomain
import com.productcatalog.app.data.local.toEntity
import com.productcatalog.app.data.remote.ProductApi
import com.productcatalog.app.data.remote.dto.ProductDto
import com.productcatalog.app.data.remote.dto.toDomain
import com.productcatalog.app.domain.repository.impl.ProductRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductRepositoryImplTest {
    @Test
    fun `getProducts returns cached products`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entity = ProductEntity(
            id = "1",
            name = "Keyboard",
            category = "Accessories",
            price = 89.0,
            description = "Mechanical",
            imageUrl = "https://example.com/keyboard.png",
            featured = false
        )
        coEvery { dao.getAll() } returns listOf(entity)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getProducts()

        assertEquals(listOf(entity.toDomain()), result)
    }

    @Test
    fun `syncProducts stores api products in cache and returns them`() = runTest {
        val api = mockk<ProductApi>()
        val dao = mockk<ProductDao>(relaxed = true)
        val dto = ProductDto(
            id = 1,
            title = "Keyboard",
            description = "Mechanical",
            price = 89.0,
            category = "Accessories",
            imageUrl = "https://example.com/keyboard.png",
            rating = null
        )
        val entity = dto.toDomain().toEntity()
        coEvery { api.getProducts() } returns listOf(dto)
        coEvery { dao.getAll() } returns listOf(entity)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.syncProducts()

        assertEquals(1, result.size)
        coVerify { dao.insertAll(any()) }
    }

    @Test
    fun `getFeaturedProducts returns cached featured items when available`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entity = ProductEntity(
            id = "1",
            name = "Keyboard",
            category = "Accessories",
            price = 89.0,
            description = "Mechanical",
            imageUrl = "https://example.com/keyboard.png",
            featured = true
        )
        coEvery { dao.getFeatured(5) } returns listOf(entity)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getFeaturedProducts(5)

        assertEquals(listOf(entity.toDomain()), result)
    }

    @Test
    fun `getFeaturedProducts fetches from api when cache is empty`() = runTest {
        val api = mockk<ProductApi>()
        val dao = mockk<ProductDao>(relaxed = true)
        val dto = ProductDto(
            id = 11,
            title = "Mouse",
            description = "Wireless",
            price = 49.0,
            category = "Accessories",
            imageUrl = "https://example.com/mouse.png",
            rating = null
        )
        coEvery { dao.getFeatured(5) } returns emptyList()
        coEvery { api.getFeaturedProducts() } returns listOf(dto)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getFeaturedProducts(5)

        assertEquals(1, result.size)
        assertEquals("Mouse", result.first().name)
    }

    @Test
    fun `getCategories returns cached categories`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        coEvery { dao.getCategories() } returns listOf("Accessories", "Electronics")

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getCategories()

        assertEquals(listOf("Accessories", "Electronics"), result)
    }

    @Test
    fun `getCategories fetches from api when cache is empty`() = runTest {
        val api = mockk<ProductApi>()
        val dao = mockk<ProductDao>(relaxed = true)
        coEvery { dao.getCategories() } returns emptyList()
        coEvery { api.getCategories() } returns listOf("Accessories")

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getCategories()

        assertEquals(listOf("Accessories"), result)
    }

    @Test
    fun `getProductsByCategory delegates to dao`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entity = ProductEntity(
            id = "2",
            name = "Monitor",
            category = "Electronics",
            price = 250.0,
            description = "HDR monitor",
            imageUrl = "https://example.com/monitor.png",
            featured = false
        )
        coEvery { dao.getByCategory("Electronics") } returns listOf(entity)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getProductsByCategory("Electronics")

        assertEquals(1, result.size)
        assertEquals("Monitor", result.first().name)
    }

    @Test
    fun `searchProducts returns empty list for blank query`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.searchProducts("   ")

        assertEquals(emptyList(), result)
    }

    @Test
    fun `searchProducts queries dao for non-empty input`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entity = ProductEntity(
            id = "3",
            name = "Mouse",
            category = "Accessories",
            price = 30.0,
            description = "Wireless",
            imageUrl = "https://example.com/mouse.png",
            featured = false
        )
        coEvery { dao.search("mouse") } returns listOf(entity)

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.searchProducts("mouse")

        assertEquals(listOf(entity.toDomain()), result)
    }

    @Test
    fun `setFeaturedProduct updates a single product flag`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)

        val repository = ProductRepositoryImpl(api, dao)
        repository.setFeaturedProduct("p-1", true)

        coVerify { dao.updateFeatured("p-1", true) }
    }

    @Test
    fun `setFeaturedProducts marks selected ids as featured`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entities = listOf(
            ProductEntity("a", "A", "Accessories", 1.0, "USD", "desc", "", 0.0, 0, false),
            ProductEntity("b", "B", "Accessories", 2.0, "USD", "desc", "", 0.0, 0, false)
        )
        coEvery { dao.getAll() } returns entities

        val repository = ProductRepositoryImpl(api, dao)
        repository.setFeaturedProducts(listOf("b"))

        coVerify { dao.updateFeatured("a", false) }
        coVerify { dao.updateFeatured("b", true) }
    }

    @Test
    fun `getProductById returns cached product when available`() = runTest {
        val api = mockk<ProductApi>(relaxed = true)
        val dao = mockk<ProductDao>(relaxed = true)
        val entity = ProductEntity(
            id = "5",
            name = "Camera",
            category = "Electronics",
            price = 600.0,
            description = "DSLR",
            imageUrl = "https://example.com/camera.png",
            featured = false
        )
        coEvery { dao.getById("5") } returns entity

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getProductById("5")

        assertEquals(entity.toDomain(), result)
    }

    @Test
    fun `getProductById fetches from api and caches when not present`() = runTest {
        val api = mockk<ProductApi>()
        val dao = mockk<ProductDao>(relaxed = true)
        val dto = ProductDto(
            id = 9,
            title = "Router",
            description = "WiFi 6",
            price = 120.0,
            category = "Electronics",
            imageUrl = "https://example.com/router.png",
            rating = null
        )
        coEvery { dao.getById("9") } returns null
        coEvery { api.getProductById("9") } returns dto

        val repository = ProductRepositoryImpl(api, dao)
        val result = repository.getProductById("9")

        assertEquals(dto.toDomain(), result)
        coVerify { dao.insertAll(any()) }
    }
}
