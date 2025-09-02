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
        val updatedList = _orders.value.orEmpty().map {
            if (it.orderId == item.orderId) {
                val newQuantity = it.quantity + 1
                val updatedItem = it.copy(
                    quantity = newQuantity,
                    totalPrice = it.food.price * newQuantity
                )

                FirebaseFirestore.getInstance()
                    .collection("Orders")
                    .document(it.orderId)
                    .update(
                        mapOf(
                            "quantity" to updatedItem.quantity,
                            "totalPrice" to updatedItem.totalPrice
                        )
                    )
                updatedItem
            } else it
        }

        _orders.value = updatedList
    }


    fun decreaseQuantity(item: CartItem) {
        val updatedList = _orders.value.orEmpty().mapNotNull {
            if (it.orderId == item.orderId) {
                val newQuantity = it.quantity - 1
                if (newQuantity > 0) {
                    val updatedItem = it.copy(
                        quantity = newQuantity,
                        totalPrice = it.food.price * newQuantity
                    )

                    FirebaseFirestore.getInstance()
                        .collection("Orders")
                        .document(it.orderId)
                        .update(
                            mapOf(
                                "quantity" to updatedItem.quantity,
                                "totalPrice" to updatedItem.totalPrice
                            )
                        )
                    updatedItem
                } else {
                    FirebaseFirestore.getInstance()
                        .collection("Orders")
                        .document(it.orderId)
                        .delete()
                    null
                }
            } else it
        }

        _orders.value = updatedList
    }


    fun removeItem(item: CartItem) {
        val updatedList = _orders.value.orEmpty().filterNot { it.orderId == item.orderId }
        _orders.value = updatedList

        if (item.orderId.isNotEmpty()) {
            FirebaseFirestore.getInstance()
                .collection("Orders")
                .document(item.orderId)
                .delete()
        }
    }
}

