package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.Payment
import com.example.underbigtreeapplication.utils.maskPhoneNumber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.Instant
import java.time.ZoneId

class PaymentViewModel : ViewModel() {
    private val phoneNumber = "+60123456780" // from profile

    fun getMaskedPhone(): String = maskPhoneNumber(phoneNumber)

    fun getTransactionDate(timestamp: Long): String {
        //val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
        return Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()
            .format(formatter)
    }

    fun storePayment(
        orderIds: List<String>,
        redeemedRewardsId: String,
        totalAmount: Double,
        method: String,
        onSuccess: (paymentId: String) -> Unit = {},
    ) {
        val db = FirebaseFirestore.getInstance()
        val counterRef = db.collection("Counter").document("PaymentCounter")
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { profileSnapshot ->
                if (profileSnapshot.isEmpty) return@addOnSuccessListener
                val profileDocId = profileSnapshot.documents[0].id  // <-- declared here

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(counterRef)
                    val lastNumber = snapshot.getLong("lastPaymentNumber") ?: 0
                    val newNumber = lastNumber + 1

                    transaction.update(counterRef, "lastPaymentNumber", newNumber)

                    val paymentId = "P" + newNumber.toString().padStart(4, '0')
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

                    val payment = Payment(
                        paymentId = paymentId,
                        orderIds = orderIds,
                        totalPrice = totalAmount,
                        paymentMethod = method,
                        transactionDate = System.currentTimeMillis(),
                        phone = phoneNumber,
                        userId = userId
                    )

                    val paymentRef = db.collection("Payments").document(paymentId)
                    transaction.set(paymentRef, payment)

                    orderIds.forEach { orderId ->
                        val orderRef = db.collection("Orders").document(orderId)
                        transaction.update(orderRef, mapOf(
                            "paymentId" to paymentId,
                            "status" to "paid"
                        ))
                    }

                    if (redeemedRewardsId.isNotEmpty()) {
                        val rewardRef = db.collection("Profiles")
                            .document(profileDocId)
                            .collection("RedeemedRewards")
                            .document(redeemedRewardsId)

                        transaction.update(rewardRef, mapOf(
                            "paymentId" to paymentId,
                            "isPaid" to true
                        ))
                    }
                    paymentId
                }.addOnSuccessListener { paymentResult ->
                    onSuccess(paymentResult)
                }
            }
    }


    fun addPointsToUser(points: Int, onComplete: () -> Unit = {}) {
        val auth = FirebaseAuth.getInstance()
        val email = auth.currentUser?.email ?: return

        val db = FirebaseFirestore.getInstance()
        val profileRef = db.collection("Profiles").whereEqualTo("email", email)

        profileRef.get().addOnSuccessListener { snapshot ->
            if (!snapshot.isEmpty) {
                val docRef = snapshot.documents[0].reference
                db.runTransaction { transaction ->
                    val currentPoints = transaction.get(docRef).getLong("points") ?: 0
                    transaction.update(docRef, "points", currentPoints + points)
                }.addOnSuccessListener {
                    onComplete()
                }
            }
        }
    }

    fun updatePaymentIdForRedeemedRewards(paymentId: String) {
        val email = FirebaseAuth.getInstance().currentUser?.email ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                val profileDocId = snapshot.documents[0].id

                db.collection("Profiles")
                    .document(profileDocId)
                    .collection("RedeemedRewards")
                    .whereEqualTo("isPaid", false)
                    .get()
                    .addOnSuccessListener { rewardsSnapshot ->
                        if (rewardsSnapshot.isEmpty) return@addOnSuccessListener

                        for (doc in rewardsSnapshot.documents) {
                            db.collection("Profiles")
                                .document(profileDocId)
                                .collection("RedeemedRewards")
                                .document(doc.id)
                                .update("paymentId", paymentId)
                        }
                    }
            }
    }
}