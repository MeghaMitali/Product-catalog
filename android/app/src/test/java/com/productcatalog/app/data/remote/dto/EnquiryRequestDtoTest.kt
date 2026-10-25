package com.productcatalog.app.data.remote.dto

import com.productcatalog.app.domain.model.EnquiryRequest
import kotlin.test.Test
import kotlin.test.assertEquals

class EnquiryRequestDtoTest {

    @Test
    fun `toDto maps all enquiry fields without altering the product id`() {
        val request = EnquiryRequest(
            productId = "sku-42",
            name = "Ava",
            email = "ava@example.com",
            phone = "+1 555 567 890",
            message = "Need a quote"
        )

        val dto = request.toDto()

        assertEquals("sku-42", dto.productId)
        assertEquals("Ava", dto.name)
        assertEquals("ava@example.com", dto.email)
        assertEquals("+1 555 567 890", dto.phone)
        assertEquals("Need a quote", dto.message)
    }
}
