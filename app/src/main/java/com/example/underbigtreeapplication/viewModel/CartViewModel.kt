package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    fun addToCart(item: CartItem) {
        _cartItems.update { current ->
            val existing = current.find { it.isSameItem(item) }
            if (existing != null) {
                current.map {
                    if (it.isSameItem(item)) {
                        it.copy(
                            quantity = it.quantity + item.quantity,
                            totalPrice = (it.quantity + item.quantity) * it.food.price
                        )
                    } else it
                }
            } else {
                current + item.copy(
                    totalPrice = item.quantity * item.food.price
                )
            }
        }
    }

    val CartItem.computedTotalPrice: Double
        get() = quantity * food.price


    fun updateQuantity(item: CartItem, newQuantity: Int) {
        _cartItems.update { current ->
            if (newQuantity <= 0) {
                current.filterNot { it.isSameItem(item) }
            } else {
                current.map {
                    if (it.isSameItem(item)) {
                        it.copy(
                            quantity = newQuantity,
                            totalPrice = newQuantity * it.food.price
                        )
                    } else it
                }
            }
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.update { current ->
            current.filterNot { it.isSameItem(item) }
        }
    }

    fun clearCart(){
        _cartItems.value = emptyList()
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf {it.totalPrice}
    }

    fun getTotalQuantity(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
}

fun CartItem.isSameItem(other: CartItem): Boolean{
    return food.id == other.food.id &&
            selectedSauces == other.selectedSauces &&
            selectedAddOns == other.selectedAddOns &&
            takeAway == other.takeAway &&
            remarks == other.remarks
}
