package com.productcatalog.app.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productcatalog.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState(isLoading = true))
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadProduct(id: String) {
        viewModelScope.launch {
            _uiState.value = DetailUiState(isLoading = true)
            runCatching {
                repository.getProductById(id)
            }.onSuccess { product ->
                _uiState.value = DetailUiState(product = product)
            }.onFailure { error ->
                _uiState.value = DetailUiState(error = error.localizedMessage ?: "Unable to load product")
            }
        }
    }
}
