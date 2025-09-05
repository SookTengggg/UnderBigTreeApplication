package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Payment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CustomerActivityViewModel: ViewModel() {

    data class PaymentWithOrders(
        val payment: Payment,
        val orders: List<CartItem>
    )

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _paymentsWithOrders = MutableLiveData<List<PaymentWithOrders>>()
    val paymentsWithOrders: LiveData<List<PaymentWithOrders>> = _paymentsWithOrders
    private val completedOrders = mutableSetOf<String>()

    fun fetchUserPayments() {
        if (userId == null) return

        db.collection("Payments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { paymentSnapshot ->
                val payments = paymentSnapshot.toObjects(Payment::class.java)

                val allPaymentWithOrders = mutableListOf<PaymentWithOrders>()
                var processedCount = 0

                if (payments.isEmpty()) {
                    _paymentsWithOrders.value = emptyList()
                    return@addOnSuccessListener
                }

                payments.forEach { payment ->
                    db.collection("Orders")
                        .whereIn("orderId", payment.orderIds)
                        .get()
                        .addOnSuccessListener { orderSnapshot ->
                            val orders = orderSnapshot.toObjects(CartItem::class.java)
                            allPaymentWithOrders.add(PaymentWithOrders(payment, orders))

                            processedCount++
                            if (processedCount == payments.size) {
                                _paymentsWithOrders.value =
                                    allPaymentWithOrders.sortedByDescending {
                                        it.payment.transactionDate
                                    }
                            }
                        }
                }
            }
    }

    fun updateOrderStatus(orderId: String, newStatus: String) {
        db.collection("Orders")
            .document(orderId)
            .update("orderStatus", newStatus)
    }

    fun updateOrdersToComplete(orderIds: List<String>) {
        orderIds.forEach { orderId ->
            updateOrderStatus(orderId, "completed")
            completedOrders.add(orderId)
        }

        val updatedList = _paymentsWithOrders.value?.map { pw ->
            pw.copy(
                orders = pw.orders.map {order ->
                    if (completedOrders.contains(order.orderId))
                        order.copy(orderStatus = "completed")
                    else order
                }
            )
        } ?: emptyList()
        _paymentsWithOrders.value = updatedList
    }

    fun isOrderCompleted(orderId: String): Boolean {
        return completedOrders.contains(orderId) || _paymentsWithOrders.value
            ?.flatMap { it.orders }?. find { it.orderId == orderId }
            ?.orderStatus == "completed"
    }
}