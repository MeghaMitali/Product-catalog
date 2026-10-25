package com.productcatalog.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.productcatalog.app.R
import com.productcatalog.app.domain.model.Product
import com.productcatalog.app.ui.components.EmptyView
import com.productcatalog.app.ui.components.ErrorView
import com.productcatalog.app.ui.components.LoadingView

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (String) -> Unit,
    onSearchClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when {
        uiState.isLoading -> LoadingView()
        uiState.message != null -> ErrorView(uiState.message.orEmpty()) { viewModel.loadProducts() }
        uiState.products.isEmpty() -> EmptyView(stringResource(R.string.empty_products))
        else -> {
            val selectedCategory = uiState.selectedCategory
            val filteredProducts = if (selectedCategory == "All") {
                uiState.products
            } else {
                uiState.products.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            val featuredForSelected = if (selectedCategory == "All") {
                uiState.featuredProducts
            } else {
                uiState.featuredProducts.filter { it.category.equals(selectedCategory, ignoreCase = true) }
            }
            val nonFeaturedProducts = filteredProducts.filterNot { product ->
                featuredForSelected.any { it.id == product.id }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    label = { Text(stringResource(R.string.search_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearchClick(uiState.searchQuery) }),
                    trailingIcon = {
                        IconButton(onClick = { onSearchClick(uiState.searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_title))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                onSearchClick(uiState.searchQuery)
                            }
                        }
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) },
                            label = { Text(category) }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (featuredForSelected.isNotEmpty()) {
                        item {
                            FeaturedCarousel(
                                products = featuredForSelected,
                                onProductClick = onProductClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    item {
                        Text(
                            text = stringResource(R.string.products_section_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(nonFeaturedProducts.chunked(2)) { rowProducts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowProducts.forEach { product ->
                                ProductCard(
                                    product = product,
                                    isFeatured = product.isFeatured,
                                    onClick = { onProductClick(product.id) },
                                    onToggleFeatured = { viewModel.toggleFeatured(product.id) },
                                    canAddToFeatured = !product.isFeatured && featuredForSelected.size >= 5,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            if (rowProducts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FeaturedCarousel(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (products.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { products.size })

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.featured_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF111827)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp,
                contentPadding = PaddingValues(horizontal = 30.dp),
                pageSize = androidx.compose.foundation.pager.PageSize.Fixed(260.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) { page ->
                val product = products[page]
                Card(
                    modifier = Modifier
                        .fillMaxHeight()
                        .clickable { onProductClick(product.id) }
                        .shadow(8.dp, RoundedCornerShape(20.dp), ambientColor = Color(0x22000000), spotColor = Color(0x22000000)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    AsyncImage(
                        model = product.imageUrl.ifBlank { null },
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            products.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == pagerState.currentPage) 10.dp else 8.dp)
                        .background(
                            color = if (index == pagerState.currentPage) Color(0xFF1E88E5) else Color(0xFFCBD5E1),
                            shape = CircleShape
                        )
                )
            }
        }

        Text(
            text = stringResource(R.string.swipe_featured_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isFeatured: Boolean,
    onClick: () -> Unit,
    onToggleFeatured: () -> Unit,
    canAddToFeatured: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            AsyncImage(
                model = product.imageUrl.ifBlank { null },
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(180.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${'$'}${product.price}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
