package com.productcatalog.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.productcatalog.app.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun onCategorySelected(category: String) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFeatured(productId: String) {
        val currentProducts = _uiState.value.products
        val selectedCount = currentProducts.count { it.isFeatured }
        val currentProduct = currentProducts.firstOrNull { it.id == productId } ?: return

        if (!currentProduct.isFeatured && selectedCount >= 5) return

        viewModelScope.launch {
            val updatedProducts = currentProducts.map { product ->
                if (product.id == productId) product.copy(isFeatured = !product.isFeatured) else product
            }
            val featuredIds = updatedProducts.filter { it.isFeatured }.take(5).map { it.id }
            repository.setFeaturedProducts(featuredIds)
            val refreshedProducts = repository.getProducts()
            _uiState.value = _uiState.value.copy(
                products = refreshedProducts,
                featuredProducts = refreshedProducts.filter { it.isFeatured }.take(5)
            )
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)
            runCatching {
                val productsDeferred = async { repository.syncProducts() }
                val featuredProductsDeferred = async { repository.getFeaturedProducts(5) }
                val categoriesDeferred = async { repository.getCategories() }

                val products = productsDeferred.await()
                val featuredProducts = featuredProductsDeferred.await()
                val categories = categoriesDeferred.await()
                val effectiveCategories = listOf("All") + categories
                val selectedCategory = if (effectiveCategories.contains(_uiState.value.selectedCategory)) {
                    _uiState.value.selectedCategory
                } else {
                    "All"
                }

                _uiState.value = HomeUiState(
                    products = products,
                    featuredProducts = featuredProducts,
                    categories = effectiveCategories,
                    selectedCategory = selectedCategory,
                    searchQuery = _uiState.value.searchQuery,
                    isLoading = false
                )
            }.onFailure { error ->
                val fallbackProductsDeferred = async { repository.getProducts() }
                val fallbackFeaturedDeferred = async { repository.getFeaturedProducts(5) }
                val fallbackCategoriesDeferred = async { repository.getCategories() }

                val fallbackProducts = fallbackProductsDeferred.await()
                val fallbackFeatured = fallbackFeaturedDeferred.await()
                val fallbackCategories = listOf("All") + fallbackCategoriesDeferred.await()

                _uiState.value = HomeUiState(
                    products = fallbackProducts,
                    featuredProducts = fallbackFeatured,
                    categories = fallbackCategories,
                    searchQuery = _uiState.value.searchQuery,
                    isLoading = false,
                    message = error.localizedMessage ?: "Unable to load products"
                )
            }
        }
    }
}
