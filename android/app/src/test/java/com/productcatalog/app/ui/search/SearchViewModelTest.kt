package com.productcatalog.app.ui.search

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
class SearchViewModelTest {
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
    fun `onQueryChanged stores the current query`() = runTest {
        val repository = mockk<ProductRepository>(relaxed = true)
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("headset")
        advanceUntilIdle()

        assertEquals("headset", viewModel.uiState.value.query)
    }

    @Test
    fun `blank query clears current results`() = runTest {
        val repository = mockk<ProductRepository>(relaxed = true)
        val viewModel = SearchViewModel(repository)
        viewModel.onQueryChanged("headset")
        advanceUntilIdle()

        viewModel.onQueryChanged("")
        advanceUntilIdle()

        assertEquals("", viewModel.uiState.value.query)
        assertEquals(emptyList<Product>(), viewModel.uiState.value.results)
    }


    @Test
    fun `search delegates query to repository`() = runTest {
        val repository = mockk<ProductRepository>(relaxed = true)
        val product = Product(
            id = "p1",
            name = "Wireless Mouse",
            category = "Electronics",
            price = 29.99,
            description = "Smooth mouse",
            imageUrl = ""
        )
        coEvery { repository.searchProducts("mouse") } returns listOf(product)

        val viewModel = SearchViewModel(repository)
        viewModel.onQueryChanged("mouse")
        advanceUntilIdle()

        assertEquals("mouse", viewModel.uiState.value.query)
        assertEquals(listOf(product), viewModel.uiState.value.results)
    }
}
