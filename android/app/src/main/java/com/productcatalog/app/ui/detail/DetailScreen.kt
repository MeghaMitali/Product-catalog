package com.productcatalog.app.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.productcatalog.app.R
import com.productcatalog.app.ui.components.ErrorView
import com.productcatalog.app.ui.components.LoadingView

@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    productId: String,
    onBack: () -> Unit,
    onEnquire: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val errorMessage = uiState.error
    val product = uiState.product

    when {
        uiState.isLoading -> LoadingView()
        errorMessage != null -> ErrorView(errorMessage) { viewModel.loadProduct(productId) }
        product == null -> ErrorView("Product not found") { viewModel.loadProduct(productId) }
        else -> {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_desc))
                }

                AsyncImage(
                    model = product.imageUrl.ifBlank { null },
                    contentDescription = product.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .padding(bottom = 16.dp),
                    contentScale = ContentScale.Crop
                )

                Text(product.name, style = MaterialTheme.typography.headlineMedium)
                Text(product.category, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                Text("${'$'}${product.price}", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(top = 8.dp))
                Text(product.description, modifier = Modifier.padding(top = 16.dp))
                Button(
                    onClick = onEnquire,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Text(stringResource(R.string.enquire))
                }
            }
        }
    }
}
