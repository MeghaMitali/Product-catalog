package com.productcatalog.app.data.local

import com.productcatalog.app.domain.model.Product
import kotlin.test.Test
import kotlin.test.assertEquals

class ProductEntityTest {

    @Test
    fun `product and entity conversion preserves values`() {
        val product = Product(
            id = "p-42",
            name = "Laptop",
            category = "Electronics",
            price = 999.99,
            currency = "USD",
            description = "Gaming laptop",
            imageUrl = "https://example.com/laptop.png",
            ratingAverage = 4.8,
            ratingCount = 45,
            isFeatured = true
        )

        val entity = product.toEntity()
        val restored = entity.toDomain()

        assertEquals(product.id, entity.id)
        assertEquals(product.name, entity.name)
        assertEquals(product.category, entity.category)
        assertEquals(product.price, entity.price)
        assertEquals(product.description, entity.description)
        assertEquals(product.imageUrl, entity.imageUrl)
        assertEquals(product.ratingAverage, entity.ratingAverage)
        assertEquals(product.ratingCount, entity.ratingCount)
        assertEquals(product.isFeatured, entity.featured)
        assertEquals(product, restored)
    }
}
