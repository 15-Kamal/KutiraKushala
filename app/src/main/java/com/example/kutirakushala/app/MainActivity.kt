package com.kutirakushala.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kutirakushala.app.ui.navigation.Routes
import com.kutirakushala.app.ui.screens.*
import com.kutirakushala.app.ui.theme.KutiraKushalaTheme
import com.kutirakushala.app.viewmodel.AppViewModel
import com.kutirakushala.app.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KutiraKushalaTheme {
                val authViewModel: AuthViewModel = viewModel()
                val appViewModel:  AppViewModel  = viewModel()
                val navController = rememberNavController()

                val startDestination = if (authViewModel.isLoggedIn) Routes.HOME else Routes.LOGIN

                NavHost(
                    navController         = navController,
                    startDestination      = startDestination,
                    enterTransition       = { slideInHorizontally(tween(280)) { it } + fadeIn(tween(280)) },
                    exitTransition        = { slideOutHorizontally(tween(280)) { -it } + fadeOut(tween(280)) },
                    popEnterTransition    = { slideInHorizontally(tween(280)) { -it } + fadeIn(tween(280)) },
                    popExitTransition     = { slideOutHorizontally(tween(280)) { it } + fadeOut(tween(280)) }
                ) {

                    // ── Login ────────────────────────────────────────────────────
                    composable(Routes.LOGIN) {
                        LoginScreen(
                            authViewModel  = authViewModel,
                            onLoginSuccess = {
                                // Load the owner's business immediately after login
                                appViewModel.loadMyBusiness()
                                navController.navigate(Routes.HOME) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onRegisterClick = { navController.navigate(Routes.REGISTER) }
                        )
                    }

                    // ── Register ─────────────────────────────────────────────────
                    composable(Routes.REGISTER) {
                        RegisterScreen(
                            authViewModel = authViewModel,
                            appViewModel  = appViewModel,
                            onSuccess     = { id ->
                                // After registering business, reload myBusiness + go home
                                appViewModel.loadMyBusiness()
                                navController.navigate(Routes.business(id)) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // ── Home (bottom-nav: Marketplace + My Business) ─────────────
                    composable(Routes.HOME) {
                        HomeScreen(
                            viewModel       = appViewModel,
                            onBusinessClick = { id -> navController.navigate(Routes.business(id)) },
                            onAddBusiness   = { navController.navigate(Routes.ADD_BUSINESS) },
                            onSearch        = { navController.navigate(Routes.SEARCH) },
                            onLogout        = {
                                authViewModel.logout()
                                navController.navigate(Routes.LOGIN) {
                                    popUpTo(Routes.HOME) { inclusive = true }
                                }
                            }
                        )
                    }

                    // ── Search ────────────────────────────────────────────────────
                    composable(Routes.SEARCH) {
                        SearchScreen(
                            viewModel       = appViewModel,
                            onBusinessClick = { id -> navController.navigate(Routes.business(id)) },
                            onBack          = { navController.popBackStack() }
                        )
                    }

                    // ── Business profile ──────────────────────────────────────────
                    composable(Routes.BUSINESS) { backStack ->
                        val businessId = backStack.arguments?.getString("businessId") ?: return@composable
                        BusinessProfileScreen(
                            businessId   = businessId,
                            viewModel    = appViewModel,
                            onBack       = { navController.popBackStack() },
                            onAddProduct = { navController.navigate(Routes.addProduct(businessId)) }
                        )
                    }

                    // ── Add / register business ────────────────────────────────────
                    composable(Routes.ADD_BUSINESS) {
                        AddBusinessScreen(
                            viewModel = appViewModel,
                            onBack    = { navController.popBackStack() },
                            onSuccess = { id ->
                                appViewModel.loadMyBusiness()
                                navController.navigate(Routes.business(id)) {
                                    popUpTo(Routes.HOME)
                                }
                            }
                        )
                    }

                    // ── Add product ────────────────────────────────────────────────
                    composable(Routes.ADD_PRODUCT) { backStack ->
                        val businessId = backStack.arguments?.getString("businessId") ?: return@composable
                        AddProductScreen(
                            businessId = businessId,
                            viewModel  = appViewModel,
                            onBack     = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}