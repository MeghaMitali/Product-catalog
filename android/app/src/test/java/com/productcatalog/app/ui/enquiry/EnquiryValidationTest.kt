package com.productcatalog.app.ui.enquiry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EnquiryValidationTest {

    @Test
    fun `validateEnquiry returns null errors for valid data`() {
        val errors = validateEnquiry(
            name = "Ava Smith",
            email = "ava@example.com",
            phone = "+1 (555) 123-4567",
            message = "I would like to know more about this product."
        )

        assertNull(errors.name)
        assertNull(errors.email)
        assertNull(errors.phone)
        assertNull(errors.message)
    }

    @Test
    fun `validateEnquiry catches required and format errors`() {
        val errors = validateEnquiry(
            name = "",
            email = "invalid-email",
            phone = "bad-phone!",
            message = ""
        )

        assertEquals("Name is required", errors.name)
        assertEquals("Enter a valid email", errors.email)
        assertEquals("Enter a valid phone number", errors.phone)
        assertEquals("Message is required", errors.message)
    }
}
