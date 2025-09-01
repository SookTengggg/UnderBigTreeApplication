package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems

    //add new item into a list (current + item --> create new list with the item added)
    fun addToCart(item: CartItem){
        _cartItems.update { current ->
            current + item
        }
    }

    //remove specific item from cart
    fun removeFromCart(item: CartItem){
        _cartItems.update { current ->
            current - item
        }
    }

    //remove everything from the cart
    fun clearCart(){
        _cartItems.value = emptyList()
    }

    //calculates total cart by summing cartItem.totalPrice
    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf {it.totalPrice}
    }

    //calculate total number of item in cart
    fun getTotalQuantity(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
}
