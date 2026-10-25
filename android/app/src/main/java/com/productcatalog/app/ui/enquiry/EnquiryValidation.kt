package com.productcatalog.app.ui.enquiry

import java.util.regex.Pattern

data class EnquiryFieldErrors(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val message: String? = null
)

fun validateEnquiry(
    name: String,
    email: String,
    phone: String,
    message: String
): EnquiryFieldErrors {
    val trimmedName = name.trim()
    val trimmedEmail = email.trim()
    val trimmedPhone = phone.trim()
    val trimmedMessage = message.trim()

    val nameError = when {
        trimmedName.isBlank() -> "Name is required"
        trimmedName.length > 100 -> "Name is too long"
        else -> null
    }

    val emailError = when {
        trimmedEmail.isBlank() -> "Email is required"
        trimmedEmail.length > 254 -> "Email is too long"
        !trimmedEmail.contains("@") -> "Enter a valid email"
        else -> null
    }

    val phonePattern = Pattern.compile("""^[0-9+()\-\s]+$""")
    val phoneError = when {
        trimmedPhone.isNotEmpty() && (trimmedPhone.length > 30 || !phonePattern.matcher(trimmedPhone).matches()) ->
            "Enter a valid phone number"
        else -> null
    }

    val messageError = when {
        trimmedMessage.isBlank() -> "Message is required"
        trimmedMessage.length > 2000 -> "Message is too long"
        else -> null
    }

    return EnquiryFieldErrors(
        name = nameError,
        email = emailError,
        phone = phoneError,
        message = messageError
    )
}
