package com.example.underbigtreeapplication.viewModel

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Food
import com.example.underbigtreeapplication.model.Option
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrderViewModel(private val foodId: String) : ViewModel() {

    private val _food = MutableStateFlow(Food())
    val food: StateFlow<Food> = _food

    init {
        fetchFoodDetails(foodId)
    }

    @OptIn(UnstableApi::class)
    private fun fetchFoodDetails(foodId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Menu").document(foodId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.toObject(Food::class.java)?.let { food ->
                    _food.value = food.copy(id = snapshot.id)

                    db.collection("Category")
                        .whereEqualTo("name", food.category)
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val categoryDoc = snapshot.documents.firstOrNull()
                            val categoryName = categoryDoc?.getString("name") ?: ""

                            if (categoryName.equals("Drinks", ignoreCase = true)) {
                                _food.value = _food.value.copy(
                                    sauceIds = emptyList(),
                                    addOnIds = emptyList()
                                )
                            } else {
                                if (food.sauce.isNotEmpty()) {
                                    fetchSauces(food.sauce) { sauces ->
                                        _food.value = _food.value.copy(
                                            sauceIds = sauces,
                                            addOnIds = _food.value.addOnIds
                                        )
                                    }
                                }

                                if (food.addOn.isNotEmpty()) {
                                    fetchAddOns(food.addOn) { addOns ->
                                        _food.value = _food.value.copy(
                                            addOnIds = addOns,
                                            sauceIds = _food.value.sauceIds
                                        )
                                    }
                                }
                            }
                        }
                }
            }
    }

    @OptIn(UnstableApi::class)
    private fun fetchSauces(sauceIds: List<String>, onResult: (List<Option>) -> Unit) {
        if (sauceIds.isEmpty()) {
            onResult(emptyList())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("Sauce")
            .whereIn(FieldPath.documentId(), sauceIds)
            .get()
            .addOnSuccessListener { documents ->
                val sauceList = documents.mapNotNull { it.toObject(Option::class.java) }
                onResult(sauceList)
            }
            .addOnFailureListener { e ->
                onResult(emptyList())
            }
    }

    @OptIn(UnstableApi::class)
    private fun fetchAddOns(addOnIds: List<String>, onResult: (List<Option>) -> Unit) {
        if (addOnIds.isEmpty()) {
            onResult(emptyList())
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("AddOn")
            .whereIn(FieldPath.documentId(), addOnIds)
            .get()
            .addOnSuccessListener { documents ->
                val addOnList = documents.mapNotNull { it.toObject(Option::class.java) }
                onResult(addOnList)
            }
            .addOnFailureListener { e ->
                onResult(emptyList())
            }
    }

    private val _selectedSauces = MutableStateFlow(setOf<Option>())
    val selectedSauces: StateFlow<Set<Option>> = _selectedSauces

    private val _selectedAddOns = MutableStateFlow(setOf<Option>())
    val selectedAddOns: StateFlow<Set<Option>> = _selectedAddOns

    private val _takeAway = MutableStateFlow(false)
    val takeAway: StateFlow<Boolean> = _takeAway

    fun toggleTakeAway(checked: Boolean) {
        _takeAway.value = checked
    }

    private val _remarks = MutableStateFlow("")
    val remarks: StateFlow<String> = _remarks

    private val _quantity = MutableStateFlow(1)
    val quantity: StateFlow<Int> = _quantity

    fun toggleSauce(sauce: Option, checked: Boolean) {
        if (!sauce.availability) return
        _selectedSauces.value = if (checked) _selectedSauces.value + sauce else _selectedSauces.value - sauce
    }

    fun toggleAddOn(addOn: Option, checked: Boolean) {
        if (!addOn.availability) return
        _selectedAddOns.value = if (checked) _selectedAddOns.value + addOn else _selectedAddOns.value - addOn
    }

    fun setRemarks(value: String) {
        _remarks.value = value
    }

    fun increaseQuantity() {
        _quantity.value++
    }

    fun decreaseQuantity() {
        if (_quantity.value > 1) _quantity.value--
    }

    fun buildCartItem(): CartItem {
        val base = _food.value.price
        val addOnsPrice = _selectedAddOns.value.sumOf { it.price }
        val saucesPrice = _selectedSauces.value.sumOf { it.price }
        val takeAwayPrice = if (_takeAway.value) 0.50 else 0.0

        val total = (base + addOnsPrice + saucesPrice + takeAwayPrice) * _quantity.value

        return CartItem(
            food = _food.value,
            selectedSauces = _selectedSauces.value.toList(),
            selectedAddOns = _selectedAddOns.value.toList(),
            quantity = _quantity.value,
            takeAway = _takeAway.value,
            remarks = _remarks.value,
            totalPrice = total
        )
    }

    fun saveOrder(
        cartItem: CartItem,
        onSuccess: () -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val counterRef = db.collection("Counter").document("OrdersCounter")

        db.runTransaction { transaction ->
            val snapshot = transaction.get(counterRef)
            val lastNumber = snapshot.getLong("lastOrderNumber") ?: 0
            val newNumber = lastNumber + 1

            transaction.update(counterRef, "lastOrderNumber", newNumber)

            val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

            val orderId = "O" + newNumber.toString().padStart(4, '0')

            val orderWithId = cartItem.copy(orderId = orderId, userId = userId)

            val orderRef = db.collection("Orders").document(orderId)
            transaction.set(orderRef, orderWithId)
        }.addOnSuccessListener { onSuccess() }
    }
}