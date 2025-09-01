package com.example.underbigtreeapplication.repository

import com.example.underbigtreeapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class UserRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun registerUser(email: String, password: String, name: String, phone: String): Boolean {
        return suspendCoroutine { continuation ->
            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    val user = User(uid, name, email, phone, 0)
                    firestore.collection("users").document(uid).set(user)
                        .addOnSuccessListener { continuation.resume(true) }
                        .addOnFailureListener { e -> continuation.resumeWithException(e) }
                }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    suspend fun loginUser(email: String, password: String): User {
        return suspendCoroutine { continuation ->
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: ""
                    firestore.collection("users").document(uid).get()
                        .addOnSuccessListener { doc ->
                            val user = doc.toObject(User::class.java)
                            if (user != null) continuation.resume(user)
                            else continuation.resumeWithException(Exception("User not found"))
                        }
                        .addOnFailureListener { e -> continuation.resumeWithException(e) }
                }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    suspend fun getUser(uid: String): User {
        return suspendCoroutine { continuation ->
            firestore.collection("users").document(uid).get()
                .addOnSuccessListener { doc ->
                    val user = doc.toObject(User::class.java)
                    if (user != null) continuation.resume(user)
                    else continuation.resumeWithException(Exception("User not found"))
                }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
    }

    suspend fun updatePoints(uid: String, newPoints: Int) {
        firestore.collection("users").document(uid).update("points", newPoints)
    }
}