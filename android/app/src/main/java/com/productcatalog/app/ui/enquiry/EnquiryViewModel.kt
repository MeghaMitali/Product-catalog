package com.productcatalog.app.ui.enquiry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productcatalog.app.domain.model.EnquiryRequest
import com.productcatalog.app.domain.repository.EnquiryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EnquiryViewModel @Inject constructor(
    private val repository: EnquiryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EnquiryUiState())
    val uiState: StateFlow<EnquiryUiState> = _uiState.asStateFlow()

    fun setProductId(productId: String) {
        _uiState.value = _uiState.value.copy(productId = productId)
    }

    fun onNameChanged(value: String) = update { copy(name = value, error = null) }
    fun onEmailChanged(value: String) = update { copy(email = value, error = null) }
    fun onPhoneChanged(value: String) = update { copy(phone = value, error = null) }
    fun onMessageChanged(value: String) = update { copy(message = value, error = null) }

    fun submit() {
        val current = _uiState.value
        val errors = validateEnquiry(current.name, current.email, current.phone, current.message)
        if (errors.name != null || errors.email != null || errors.phone != null || errors.message != null) {
            _uiState.value = current.copy(
                error = errors.name ?: errors.email ?: errors.phone ?: errors.message,
                successMessage = null
            )
            return
        }

        val request = EnquiryRequest(
            productId = current.productId,
            name = current.name.trim(),
            email = current.email.trim(),
            phone = current.phone.trim(),
            message = current.message.trim()
        )

        viewModelScope.launch {
            _uiState.value = current.copy(isSubmitting = true, error = null, successMessage = null)
            val result = repository.submitEnquiry(request)
            result.onSuccess {
                _uiState.value = current.copy(
                    isSubmitting = false,
                    successMessage = "Thank you! Your enquiry has been sent.",
                    name = "",
                    email = "",
                    phone = "",
                    message = "",
                    error = null
                )
            }.onFailure { error ->
                _uiState.value = current.copy(
                    isSubmitting = false,
                    error = error.localizedMessage ?: "Unable to submit enquiry",
                    successMessage = null
                )
            }
        }
    }

    private fun update(transform: EnquiryUiState.() -> EnquiryUiState) {
        _uiState.value = _uiState.value.transform()
    }
}
