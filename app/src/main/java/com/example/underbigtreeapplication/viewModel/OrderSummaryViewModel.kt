package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.compareTo
import kotlin.times

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

    fun increaseQuantity(item: CartItem) {
        val updatedList = _orders.value.toMutableList().map {
            if (it.food.id == item.food.id && it.takeAway == item.takeAway) {
                it.copy(
                    quantity = it.quantity + 1,
                    totalPrice = (it.food.price * (it.quantity + 1))
                )
            } else it
        }
        _orders.value = updatedList
    }

    fun decreaseQuantity(item: CartItem) {
        val updatedList = _orders.value.toMutableList().mapNotNull {
            if (it.food.id == item.food.id && it.takeAway == item.takeAway) {
                if (it.quantity > 1) {
                    it.copy(
                        quantity = it.quantity - 1,
                        totalPrice = (it.food.price * (it.quantity - 1))
                    )
                } else {
                    null // remove if quantity reaches 0
                }
            } else it
        }
        _orders.value = updatedList
    }

    fun removeItem(item: CartItem) {
        val updatedList = _orders.value.toMutableList().filterNot {
            it.food.id == item.food.id && it.takeAway == item.takeAway
        }
        _orders.value = updatedList
    }

}

