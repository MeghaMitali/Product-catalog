package com.productcatalog.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.productcatalog.app.R
import com.productcatalog.app.domain.model.Product
import com.productcatalog.app.ui.components.EmptyView
import com.productcatalog.app.ui.components.ErrorView
import com.productcatalog.app.ui.components.LoadingView

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    initialQuery: String = "",
    onBack: () -> Unit,
    onProductClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(initialQuery) {
        viewModel.initializeQuery(initialQuery)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
            }
            Text(text = stringResource(R.string.search_title), style = MaterialTheme.typography.headlineSmall)
        }

        OutlinedTextField(
            value = uiState.query,
            onValueChange = viewModel::onQueryChanged,
            label = { Text(stringResource(R.string.search_hint)) },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            singleLine = true
        )

        val errorMessage = uiState.error

        when {
            uiState.isLoading -> LoadingView()
            errorMessage != null -> ErrorView(errorMessage) { viewModel.onQueryChanged(uiState.query) }
            uiState.results.isEmpty() && uiState.query.isNotBlank() -> EmptyView(stringResource(R.string.empty_search))
            uiState.results.isEmpty() -> EmptyView(stringResource(R.string.empty_products))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.results) { product ->
                    SearchResultItem(product = product, onClick = { onProductClick(product.id) })
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(product: Product, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                Text(text = product.category, style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "${'$'}${product.price}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
