package com.productcatalog.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.productcatalog.app.ui.detail.DetailScreen
import com.productcatalog.app.ui.enquiry.EnquiryScreen
import com.productcatalog.app.ui.home.HomeScreen
import com.productcatalog.app.ui.search.SearchScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data class Search(val query: String = "") : Screen("search") {
        fun buildRoute(): String = if (query.isBlank()) {
            "search"
        } else {
            "search?query=${Uri.encode(query)}"
        }
    }
    data class Detail(val productId: String) : Screen("detail/{productId}") {
        fun buildRoute(): String = "detail/$productId"
    }
    data class Enquiry(val productId: String) : Screen("enquiry/{productId}") {
        fun buildRoute(): String = "enquiry/$productId"
    }
}

@Composable
fun ProductCatalogNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onProductClick = { productId -> navController.navigate(Screen.Detail(productId).buildRoute()) },
                onSearchClick = { query -> navController.navigate(Screen.Search(query).buildRoute()) }
            )
        }

        composable(
            route = Screen.Search().route,
            arguments = listOf(navArgument("query") {
                type = NavType.StringType
                defaultValue = ""
            })
        ) { backStackEntry ->
            val query = backStackEntry.arguments?.getString("query").orEmpty()
            SearchScreen(
                viewModel = hiltViewModel(),
                initialQuery = query,
                onBack = { navController.popBackStack() },
                onProductClick = { productId -> navController.navigate(Screen.Detail(productId).buildRoute()) }
            )
        }

        composable(
            route = Screen.Detail("{productId}").route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            DetailScreen(
                viewModel = hiltViewModel(),
                productId = productId,
                onBack = { navController.popBackStack() },
                onEnquire = { navController.navigate(Screen.Enquiry(productId).buildRoute()) }
            )
        }

        composable(
            route = Screen.Enquiry("{productId}").route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EnquiryScreen(
                viewModel = hiltViewModel(),
                productId = productId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
