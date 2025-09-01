package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderSummaryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val _orders = MutableStateFlow<List<CartItem>>(emptyList())
    val orders: StateFlow<List<CartItem>> = _orders

    fun fetchOrders() {
        db.collection("Orders")
            .get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(CartItem::class.java) }
                _orders.value = list
            }
    }
}

