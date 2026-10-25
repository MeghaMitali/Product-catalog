package com.productcatalog.app.data.remote.dto

import kotlin.test.Test
import kotlin.test.assertEquals

class ProductDtoTest {

    @Test
    fun `toDomain maps dto fields into the app model`() {
        val dto = ProductDto(
            id = 7,
            title = "Headphones",
            description = "Noise cancelling",
            price = 199.99,
            currency = "USD",
            category = "Audio",
            imageUrl = "https://example.com/headphones.png",
            rating = RatingDto(average = 4.7, count = 12)
        )

        val product = dto.toDomain()

        assertEquals("7", product.id)
        assertEquals("Headphones", product.name)
        assertEquals("Audio", product.category)
        assertEquals(199.99, product.price)
        assertEquals("USD", product.currency)
        assertEquals("Noise cancelling", product.description)
        assertEquals("https://example.com/headphones.png", product.imageUrl)
        assertEquals(4.7, product.ratingAverage)
        assertEquals(12, product.ratingCount)
        assertEquals(true, product.isFeatured)
    }
}
