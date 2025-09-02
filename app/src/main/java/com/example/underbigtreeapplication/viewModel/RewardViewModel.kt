package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.RewardItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class RewardViewModel : ViewModel() {

    private val _rewards = MutableLiveData<List<RewardItem>>()
    val rewards: LiveData<List<RewardItem>> = _rewards

    private val db = FirebaseFirestore.getInstance()

    init {
        fetchAvailableRewards()
    }

    private fun fetchAvailableRewards() {
        db.collection("Menu")
            .whereEqualTo("availability", true)
            .get()
            .addOnSuccessListener { snapshot ->
                val allAvailable = snapshot.documents.map { doc ->
                    RewardItem(
                        id = doc.id,
                        name = doc.getString("name") ?: "Unknown",
                        condition = "Can only redeem one time",
                        pointsRequired = 50
                    )
                }

                // Pick 3 random items
                val randomThree = if (allAvailable.size <= 3) {
                    allAvailable
                } else {
                    allAvailable.shuffled().take(3)
                }

                _rewards.value = randomThree
            }
            .addOnFailureListener { e ->
                // Handle error
                _rewards.value = emptyList()
            }
    }
}
