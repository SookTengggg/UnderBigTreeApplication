package com.example.underbigtreeapplication.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.repository.MenuRepository
import com.example.underbigtreeapplication.ui.customerHomePage.CustHomeScreen
import com.example.underbigtreeapplication.ui.loginPage.LoginScreen
import com.example.underbigtreeapplication.ui.order.OrderScreen
import com.example.underbigtreeapplication.ui.order.OrderSummaryScreen
import com.example.underbigtreeapplication.ui.payment.BankPaymentScreen
import com.example.underbigtreeapplication.ui.payment.BankPaymentSuccess
import com.example.underbigtreeapplication.ui.payment.TngPaymentScreen
import com.example.underbigtreeapplication.ui.payment.TngPaymentSuccess
import com.example.underbigtreeapplication.ui.signupPage.SignupScreen
import com.example.underbigtreeapplication.ui.staff.StaffFoodMenuScreen
import com.example.underbigtreeapplication.ui.staff.StaffWelcomeScreen
import com.example.underbigtreeapplication.ui.welcomePage.WelcomeScreen
import com.example.underbigtreeapplication.viewModel.CartViewModel
import com.example.underbigtreeapplication.viewModel.CustHomeViewModel
import com.example.underbigtreeapplication.viewModel.CustHomeViewModelFactory
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModel
import com.example.underbigtreeapplication.viewModel.StaffViewModel
import com.example.underbigtreeapplication.viewModel.StaffViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavigationFlow(navController: NavHostController) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)
    val userType = sharedPref.getString("userType", "")

    val startDestination = when {
        isLoggedIn && userType == "staff" -> "staffHome"
        isLoggedIn && userType == "customer" -> "welcome"
        else -> "login"
    }
    val cartViewModel: CartViewModel = viewModel()

    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("welcome") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onStaffLogin = {
                    navController.navigate("staffHome") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            SignupScreen(
                onRegisterSuccess = {
                    navController.navigate("welcome") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate("login")
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onLogout = {
                    sharedPref.edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                onContinue = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("staffHome") {
            StaffWelcomeScreen(
                onLogout = {
                    sharedPref.edit().clear().apply()
                    navController.navigate("login") {
                        popUpTo("staffHome") { inclusive = true }
                    }
                }, onContinue = {
                    navController.navigate("staffFood"){
                        popUpTo("staffHome"){ inclusive = true }
                    }
                }
            )
        }

        composable("staffFood") {
            val database = AppDatabase.getDatabase(context)
            val repository = MenuRepository(database)
            val staffViewModel: StaffViewModel = viewModel(
                factory = StaffViewModelFactory(repository)
            )
            StaffFoodMenuScreen(
                navController = navController,
                staffViewModel = staffViewModel,
                onAddMenu = { navController.navigate("addMenu") },
                onMenuClick = { menuItem -> navController.navigate("editMenu/${menuItem.id}") }
            )
        }

        composable("home") {
            val database = AppDatabase.getDatabase(context)
            val repository = remember { MenuRepository(database) }
            val viewModel: CustHomeViewModel = viewModel(factory = CustHomeViewModelFactory(repository))

            CustHomeScreen(
                points = 0,
                modifier = Modifier,
                viewModel = viewModel,
                navController = navController,
                cartViewModel = cartViewModel
            )
        }

        composable("order/{foodId}") {backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            OrderScreen(
                foodId = foodId,
                onBackClick = { navController.popBackStack() },
                onPlaceOrder = { cartItem ->
                    cartViewModel.addToCart(cartItem)
                    navController.popBackStack()
                    //navController.navigate("orderSummaryScreen")
                }
            )
        }

        composable("orderSummaryScreen") {
            val summaryViewModel: OrderSummaryViewModel = viewModel()
            OrderSummaryScreen(
                viewModel = summaryViewModel,
                navController,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable("tngPayment/{totalAmount}") { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            TngPaymentScreen(
                totalAmount = totalAmount,
                onPayClick = { formattedAmount ->
                    navController.navigate("tngSuccess/$totalAmount")
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("tngSuccess/{totalAmount}") { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            TngPaymentSuccess(
                totalAmount = totalAmount,
                onReturnClick = {
                    navController.navigate("home")
                }
            )
        }

        composable("bankPayment/{totalAmount}") {backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            BankPaymentScreen(
                totalAmount = totalAmount,
                onReject = {navController.popBackStack()},
                onApprove = { formattedAmount->
                    navController.navigate("bankSuccess/$totalAmount")
                },
            )
        }

        composable("bankSuccess/{totalAmount}") { backStackEntry ->
            val totalAmount = backStackEntry.arguments?.getString("totalAmount")?.toDoubleOrNull() ?: 0.0
            BankPaymentSuccess(
                totalAmount = totalAmount,
                onDoneClick ={navController.navigate("home")}
            )
        }
    }
}