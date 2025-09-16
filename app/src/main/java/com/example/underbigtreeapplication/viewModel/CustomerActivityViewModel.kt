package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Payment
import com.example.underbigtreeapplication.model.RewardItem
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
    private val _redeemedRewardsForPayment = MutableLiveData<List<RewardItem>>()
    val redeemedRewardsForPayment: LiveData<List<RewardItem>> = _redeemedRewardsForPayment

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
                    if (payment.orderIds.isNotEmpty()) {
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
                    } else {
                        allPaymentWithOrders.add(PaymentWithOrders(payment, emptyList()))
                        processedCount++
                        if(processedCount == payments.size) {
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

    fun updateOrdersToReceive(orderIds: List<String>) {
        orderIds.forEach { orderId ->
            updateOrderStatus(orderId, "received")
        }

        val updatedList = _paymentsWithOrders.value?.map { pw ->
            pw.copy(
                orders = pw.orders.map {order ->
                    if (orderIds.contains(order.orderId))
                        order.copy(orderStatus = "received")
                    else order
                }
            )
        } ?: emptyList()
        _paymentsWithOrders.value = updatedList
    }

    fun fetchRedeemedRewardsForPayment(paymentId: String){
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if(snapshot.isEmpty) return@addOnSuccessListener
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

                        _redeemedRewardsForPayment.value = rewards
                    }
            }
    }
}