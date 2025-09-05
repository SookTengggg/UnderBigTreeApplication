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
        totalAmount: Double,
        method: String,
        onSuccess: () -> Unit = {},
    ) {
        val db = FirebaseFirestore.getInstance()
        val counterRef = db.collection("Counter").document("PaymentCounter")

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
        }.addOnSuccessListener {
            onSuccess()
        }
    }
}
