package com.productcatalog.app.ui.home

import com.productcatalog.app.domain.model.Product
import com.productcatalog.app.domain.repository.ProductRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onCategorySelected updates selected category`() = runTest {
        val repository = mockk<ProductRepository>(relaxed = true)
        coEvery { repository.syncProducts() } returns emptyList()
        coEvery { repository.getFeaturedProducts(any()) } returns emptyList()
        coEvery { repository.getCategories() } returns emptyList()

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onCategorySelected("Electronics")

        assertEquals("Electronics", viewModel.uiState.value.selectedCategory)
    }

    @Test
    fun `onSearchQueryChanged updates search query`() = runTest {
        val repository = mockk<ProductRepository>(relaxed = true)
        coEvery { repository.syncProducts() } returns emptyList()
        coEvery { repository.getFeaturedProducts(any()) } returns emptyList()
        coEvery { repository.getCategories() } returns emptyList()

        val viewModel = HomeViewModel(repository)
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("keyboard")
        advanceUntilIdle()

        assertEquals("keyboard", viewModel.uiState.value.searchQuery)
    }
}
