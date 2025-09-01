package com.example.underbigtreeapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.underbigtreeapplication.navigation.NavigationFlow
import com.example.underbigtreeapplication.ui.theme.UnderBigTreeApplicationTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            UnderBigTreeApplicationTheme {
                UnderBigTreeApp()
            }
        }
    }
}

@Composable
fun UnderBigTreeApp() {
    val navController = rememberNavController()
    NavigationFlow(navController = navController)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UnderBigTreeApplicationTheme {
        UnderBigTreeApp()
    }
}