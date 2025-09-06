package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Payment
import com.google.firebase.firestore.FirebaseFirestore

class StaffActivityViewModel : ViewModel() {

    data class PaymentWithOrders(
        val payment: Payment,
        val orders: List<CartItem>
    )

    private val db = FirebaseFirestore.getInstance()
    private val _paymentsWithOrders = MutableLiveData<List<PaymentWithOrders>>(emptyList())
    val paymentsWithOrders: LiveData<List<PaymentWithOrders>> = _paymentsWithOrders
    private val completedOrders = mutableSetOf<String>()

    fun fetchAllPayments() {
        db.collection("Payments")
            .get()
            .addOnSuccessListener { paymentSnapshot ->
                val payments = paymentSnapshot.toObjects(Payment::class.java)
                if (payments.isEmpty()) {
                    _paymentsWithOrders.value = emptyList()
                    return@addOnSuccessListener
                }

                val allPaymentWithOrders = mutableListOf<PaymentWithOrders>()
                var processedCount = 0

                payments.forEach { payment ->
                    if (payment.orderIds.isNullOrEmpty()) {
                        processedCount++
                        if (processedCount == payments.size) {
                            _paymentsWithOrders.value =
                                allPaymentWithOrders.sortedByDescending { it.payment.transactionDate }
                        }
                        return@forEach
                    }

                    db.collection("Orders")
                        .whereIn("orderId", payment.orderIds)
                        .get()
                        .addOnSuccessListener { orderSnapshot ->
                            val orders = orderSnapshot.toObjects(CartItem::class.java)
                            allPaymentWithOrders.add(PaymentWithOrders(payment, orders))
                            processedCount++
                            if (processedCount == payments.size) {
                                _paymentsWithOrders.value =
                                    allPaymentWithOrders.sortedByDescending { it.payment.transactionDate }
                            }
                        }
                        .addOnFailureListener {
                            processedCount++
                            if (processedCount == payments.size) {
                                _paymentsWithOrders.value =
                                    allPaymentWithOrders.sortedByDescending { it.payment.transactionDate }
                            }
                        }
                }
            }
            .addOnFailureListener {
                _paymentsWithOrders.value = emptyList()
            }
    }

    fun updateOrdersToComplete(orderIds: List<String>) {
        orderIds.forEach { orderId ->
            db.collection("Orders").document(orderId)
                .update("orderStatus", "completed")
            completedOrders.add(orderId)
        }

        // Update LiveData safely
        _paymentsWithOrders.value = _paymentsWithOrders.value?.map { pw ->
            pw.copy(
                orders = pw.orders.map { order ->
                    if (completedOrders.contains(order.orderId)) {
                        order.copy(orderStatus = "completed")
                    } else order
                }
            )
        } ?: emptyList()
    }

    fun isOrderCompleted(orderId: String): Boolean {
        return completedOrders.contains(orderId) || _paymentsWithOrders.value
            ?.flatMap { it.orders }?.find { it.orderId == orderId }
            ?.orderStatus == "completed"
    }
}
