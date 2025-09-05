package com.example.underbigtreeapplication.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.underbigtreeapplication.data.local.AppDatabase
import com.example.underbigtreeapplication.model.MenuEntity
import com.example.underbigtreeapplication.repository.MenuRepository
import com.example.underbigtreeapplication.repository.ProfileRepository
import com.example.underbigtreeapplication.ui.customerHomePage.CustHomeScreen
import com.example.underbigtreeapplication.ui.loginPage.LoginScreen
import com.example.underbigtreeapplication.ui.order.OrderScreen
import com.example.underbigtreeapplication.ui.order.OrderSummaryScreen
import com.example.underbigtreeapplication.ui.payment.BankPaymentScreen
import com.example.underbigtreeapplication.ui.payment.BankPaymentSuccess
import com.example.underbigtreeapplication.ui.payment.TngPaymentScreen
import com.example.underbigtreeapplication.ui.payment.TngPaymentSuccess
import com.example.underbigtreeapplication.ui.profile.EditProfileScreen
import com.example.underbigtreeapplication.ui.profile.ProfileScreen
import com.example.underbigtreeapplication.ui.pointPage.RewardsScreen
import com.example.underbigtreeapplication.ui.profile.StaffEditProfileScreen
import com.example.underbigtreeapplication.ui.profile.StaffProfileScreen
import com.example.underbigtreeapplication.ui.signupPage.SignupScreen
import com.example.underbigtreeapplication.ui.staff.FoodEditScreen
import com.example.underbigtreeapplication.ui.staff.StaffAddDrinkScreen
import com.example.underbigtreeapplication.ui.staff.StaffAddFoodScreen
import com.example.underbigtreeapplication.ui.staff.StaffAddOnScreen
import com.example.underbigtreeapplication.ui.staff.StaffAddSauceScreen
import com.example.underbigtreeapplication.ui.staff.StaffFoodAvailabilityScreen
import com.example.underbigtreeapplication.ui.staff.StaffFoodMenuScreen
import com.example.underbigtreeapplication.ui.staff.StaffWelcomeScreen
import com.example.underbigtreeapplication.ui.welcomePage.WelcomeScreen
import com.example.underbigtreeapplication.viewModel.CartViewModel
import com.example.underbigtreeapplication.viewModel.CustHomeViewModel
import com.example.underbigtreeapplication.viewModel.CustHomeViewModelFactory
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModel
import com.example.underbigtreeapplication.viewModel.OrderSummaryViewModelFactory
import com.example.underbigtreeapplication.viewModel.ProfileUiState
import com.example.underbigtreeapplication.viewModel.ProfileViewModel
import com.example.underbigtreeapplication.viewModel.ProfileViewModelFactory
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
                onMenuClick = { menuItem -> navController.navigate("editMenu/${menuItem.id}") }
            )
        }

        composable("staffAvailability") {
            val database = AppDatabase.getDatabase(context)
            val repository = MenuRepository(database)
            val staffViewModel: StaffViewModel = viewModel(
                factory = StaffViewModelFactory(repository)
            )
            StaffFoodAvailabilityScreen(
                navController = navController,
                staffViewModel = staffViewModel,
                onMenuClick = { menuItem -> navController.navigate("editMenu/${menuItem.id}") }
            )
        }

        composable("addFood") {
            StaffAddFoodScreen(
                navController = navController,
                staffViewModel = viewModel(
                    factory = StaffViewModelFactory(
                        MenuRepository(
                            AppDatabase.getDatabase(context)
                        )
                    )
                )
            )
        }

        composable("addDrink") {
            StaffAddDrinkScreen(
                navController = navController,
                staffViewModel = viewModel(
                    factory = StaffViewModelFactory(
                        MenuRepository(
                            AppDatabase.getDatabase(context)
                        )
                    )
                )
            )
        }

        composable("addSauce") {
            StaffAddSauceScreen(
                navController = navController,
                staffViewModel = viewModel(factory = StaffViewModelFactory(MenuRepository(AppDatabase.getDatabase(context))))
            )
        }

        composable("addAddOn") {
            StaffAddOnScreen(
                navController = navController,
                staffViewModel = viewModel(factory = StaffViewModelFactory(MenuRepository(AppDatabase.getDatabase(context))))
            )
        }

        composable(
            route = "foodEdit/{foodId}/{foodType}",
            arguments = listOf(
                navArgument("foodId") { type = NavType.StringType },
                navArgument("foodType") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            val foodType = backStackEntry.arguments?.getString("foodType") ?: "menu"
            val staffViewModel: StaffViewModel = viewModel()

            FoodEditScreen(
                navController = navController,
                foodId = foodId,
                foodType = foodType,
                staffViewModel = staffViewModel
            )
        }

        composable("home") {
            val context = LocalContext.current
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

        composable ("custProfile"){
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = remember { ProfileRepository(database.profileDao()) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))

            ProfileScreen(navController, profileViewModel)
        }

        composable ("staffProfile"){
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = remember { ProfileRepository(database.profileDao()) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))

            StaffProfileScreen(navController, profileViewModel)
        }

        composable ("editProfile") {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = remember { ProfileRepository(database.profileDao()) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))

            EditProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }

        composable ("staffEditProfile") {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)
            val repository = remember { ProfileRepository(database.profileDao()) }
            val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(repository))

            StaffEditProfileScreen(
                navController = navController,
                viewModel = profileViewModel
            )
        }

        composable("point") {
            RewardsScreen(
                onBackClick = { navController.popBackStack() },
                onRedeemClick={}
            )
        }

        composable("order/{foodId}") {backStackEntry ->
            val foodId = backStackEntry.arguments?.getString("foodId") ?: ""
            OrderScreen(
                foodId = foodId,
                cartViewModel = cartViewModel,
                onBackClick = { navController.popBackStack() },
                onPlaceOrder = { cartItem ->
                    cartViewModel.addToCart(cartItem)
                    navController.popBackStack()
                }
            )
        }

        composable("orderSummaryScreen") {
            val summaryViewModel: OrderSummaryViewModel = viewModel(
                factory = OrderSummaryViewModelFactory(cartViewModel)
            )
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
            val summaryViewModel: OrderSummaryViewModel = viewModel(
                factory = OrderSummaryViewModelFactory(cartViewModel)
            )
            TngPaymentSuccess(
                totalAmount = totalAmount,
                summaryViewModel = summaryViewModel,
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
            val summaryViewModel: OrderSummaryViewModel = viewModel(
                factory = OrderSummaryViewModelFactory(cartViewModel)
            )
            BankPaymentSuccess(
                totalAmount = totalAmount,
                summaryViewModel = summaryViewModel,
                onDoneClick ={navController.navigate("home")}
            )
        }
    }
}

