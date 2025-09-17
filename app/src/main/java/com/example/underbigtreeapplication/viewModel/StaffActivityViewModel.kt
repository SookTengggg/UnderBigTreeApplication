package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Payment
import com.example.underbigtreeapplication.model.RewardItem
import com.google.firebase.firestore.FirebaseFirestore

class StaffActivityViewModel : ViewModel() {

    data class PaymentWithOrders(
        val payment: Payment,
        val orders: List<CartItem>,
        val redeemedRewards: List<RewardItem>
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
                    val orderQuery = if (!payment.orderIds.isNullOrEmpty()) {
                        db.collection("Orders")
                            .whereIn("orderId", payment.orderIds)
                            .get()
                    } else {
                        null
                    }

                    val handleOrdersAndRewards: (List<CartItem>) -> Unit = { orders ->
                        fetchRewardsForUser(payment.userId, payment.paymentId) { rewards ->
                            allPaymentWithOrders.add(
                                PaymentWithOrders(payment, orders, rewards)
                            )
                            processedCount++
                            if (processedCount == payments.size) {
                                _paymentsWithOrders.value =
                                    allPaymentWithOrders.sortedByDescending { it.payment.transactionDate }
                            }
                        }
                    }

                    if (orderQuery != null) {
                        orderQuery
                            .addOnSuccessListener { orderSnapshot ->
                                val orders = orderSnapshot.toObjects(CartItem::class.java)
                                handleOrdersAndRewards(orders)
                            }
                            .addOnFailureListener {
                                handleOrdersAndRewards(emptyList())
                            }
                    } else {
                        handleOrdersAndRewards(emptyList())
                    }
                }
            }
            .addOnFailureListener {
                _paymentsWithOrders.value = emptyList()
            }
    }

    private fun fetchRewardsForUser(authId: String, paymentId: String, onComplete: (List<RewardItem>) -> Unit) {
        db.collection("Profiles")
            .whereEqualTo("customerId", authId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onComplete(emptyList())
                    return@addOnSuccessListener
                }

                val profileDocId = snapshot.documents[0].id

                db.collection("Profiles")
                    .document(profileDocId)
                    .collection("RedeemedRewards")
                    .whereEqualTo("paymentId", paymentId)
                    .get()
                    .addOnSuccessListener { rewardsSnapshot ->
                        val rewards = rewardsSnapshot.documents.map { doc ->
                            RewardItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "Unknown",
                                condition = doc.getString("condition") ?: "Can only redeem one time",
                                pointsRequired = doc.getLong("pointsRequired")?.toInt() ?: 0,
                                isRedeemed = doc.getBoolean("isRedeemed") ?: true,
                                isPaid = doc.getBoolean("isPaid") ?: false
                            )
                        }
                        onComplete(rewards)
                    }
                    .addOnFailureListener { onComplete(emptyList()) }
            }
            .addOnFailureListener { onComplete(emptyList()) }
    }


    fun updateOrdersToComplete(orderIds: List<String>) {
        orderIds.forEach { orderId ->
            db.collection("Orders").document(orderId)
                .update("orderStatus", "completed")
            completedOrders.add(orderId)
        }

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
        val order = _paymentsWithOrders.value
            ?.flatMap { it.orders }
            ?.find { it.orderId == orderId }

        return completedOrders.contains(orderId) ||
                (order?.orderStatus == "completed" || order?.orderStatus == "received")
    }
}
