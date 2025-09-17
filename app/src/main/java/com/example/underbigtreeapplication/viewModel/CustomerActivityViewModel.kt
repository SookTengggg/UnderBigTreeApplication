package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.CartItem
import com.example.underbigtreeapplication.model.Payment
import com.example.underbigtreeapplication.model.RewardItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class CustomerActivityViewModel: ViewModel() {

    data class PaymentWithOrders(
        val payment: Payment,
        val orders: List<CartItem>,
        val rewards: List<RewardItem> = emptyList()
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

                                fetchRewardsForPayment(payment.paymentId) { rewards ->
                                    allPaymentWithOrders.add(
                                        PaymentWithOrders(payment, orders, rewards)
                                    )

                                    processedCount++
                                    if (processedCount == payments.size) {
                                        _paymentsWithOrders.value =
                                            allPaymentWithOrders.sortedByDescending {
                                                it.payment.transactionDate
                                            }
                                    }
                                }
                            }
                    } else {
                        fetchRewardsForPayment(payment.paymentId) { rewards ->
                            allPaymentWithOrders.add(
                                PaymentWithOrders(payment, emptyList(), rewards)
                            )

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
                                isPaid = doc.getBoolean("isPaid") ?: false,
                                paymentId = doc.getString("paymentId"),
                                status = doc.getString("status") ?: "pending"
                            )
                        }

                        _redeemedRewardsForPayment.value = rewards
                    }
            }
    }

    fun fetchRewardsForPayment(paymentId: String, onResult: (List<RewardItem>) -> Unit) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onResult(emptyList())
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
                                isPaid = doc.getBoolean("isPaid") ?: false,
                                paymentId = doc.getString("paymentId"),
                                status = doc.getString("status") ?: "pending"
                            )
                        }
                        onResult(rewards)
                    }
            }
    }

    fun updatePaymentToReceived(paymentId: String, orderIds: List<String>, rewardIds: List<String>) {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()

        if (orderIds.isNotEmpty()) {
            orderIds.forEach { orderId ->
                val orderRef = db.collection("Orders").document(orderId)
                batch.update(orderRef, "orderStatus", "received")
            }
        }

        if (rewardIds.isNotEmpty()) {
            val email = FirebaseAuth.getInstance().currentUser?.email ?: return
            db.collection("Profiles")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.isEmpty) return@addOnSuccessListener
                    val profileDocId = snapshot.documents[0].id

                    rewardIds.forEach { rewardId ->
                        val rewardRef = db.collection("Profiles")
                            .document(profileDocId)
                            .collection("RedeemedRewards")
                            .document(rewardId)
                        batch.update(rewardRef, "status", "received")
                    }

                    batch.commit().addOnSuccessListener {
                        updateLocalState(paymentId, orderIds, rewardIds)
                    }
                }
        } else {
            batch.commit().addOnSuccessListener {
                updateLocalState(paymentId, orderIds, rewardIds)
            }
        }
    }

    private fun updateLocalState(paymentId: String, orderIds: List<String>, rewardIds: List<String>) {
        val updatedList = _paymentsWithOrders.value?.map { pw ->
            if (pw.payment.paymentId == paymentId) {
                pw.copy(
                    orders = pw.orders.map { order ->
                        if (orderIds.contains(order.orderId)) order.copy(orderStatus = "received") else order
                    },
                    rewards = pw.rewards.map { reward ->
                        if (rewardIds.contains(reward.id)) reward.copy(status = "received") else reward
                    }
                )
            } else pw
        } ?: emptyList()

        _paymentsWithOrders.value = updatedList
    }
}