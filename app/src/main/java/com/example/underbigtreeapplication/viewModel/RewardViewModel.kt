package com.example.underbigtreeapplication.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.underbigtreeapplication.model.RewardItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RewardViewModel : ViewModel() {

    private val _rewards = MutableLiveData<List<RewardItem>>()
    val rewards: LiveData<List<RewardItem>> = _rewards

    private val _userPoints = MutableLiveData<Int>()
    val userPoints: LiveData<Int> = _userPoints

    private val _selectedReward = MutableLiveData<RewardItem?>()
    val selectedReward: LiveData<RewardItem?> = _selectedReward

    private val _redeemedReward = MutableLiveData<RewardItem?>()
    val redeemedReward: LiveData<RewardItem?> = _redeemedReward

    fun setRedeemedReward(reward: RewardItem) {
        _redeemedReward.value = reward
    }

    private val _unpaidRewards = MutableLiveData<List<RewardItem>>()
    val unpaidRewards: LiveData<List<RewardItem>> = _unpaidRewards

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchAvailableRewards()
        fetchUserPointsByEmail()
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
                _rewards.value = emptyList()
            }
    }

    private fun fetchUserPointsByEmail() {
        val email = auth.currentUser?.email ?: return

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _userPoints.value = 0
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val points = snapshot.documents[0].getLong("points")?.toInt() ?: 0
                    _userPoints.value = points
                } else {
                    _userPoints.value = 0
                }
            }
    }

    fun selectReward(reward: RewardItem) {
        _selectedReward.value = reward
    }

    fun redeemSelectedReward(onComplete: (Boolean, String, RewardItem?) -> Unit) {
        val reward = selectedReward.value
        val currentPoints = _userPoints.value ?: 0

        if (reward == null) {
            onComplete(false, "Please select a reward first.", null)
            return
        }

        if (currentPoints < reward.pointsRequired) {
            onComplete(false, "Not enough points to redeem this reward.", null)
            return
        }

        val email = auth.currentUser?.email ?: return

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    onComplete(false, "User profile not found.", null)
                    return@addOnSuccessListener
                }

                val docRef = snapshot.documents[0].reference
                val newPoints = currentPoints - reward.pointsRequired

                docRef.update("points", newPoints)
                    .addOnSuccessListener {
                        _userPoints.value = newPoints

                        db.collection("Profiles")
                            .document(snapshot.documents[0].id)
                            .collection("RedeemedRewards")
                            .get()
                            .addOnSuccessListener { rewardsSnapshot ->
                                val lastId = rewardsSnapshot.documents
                                    .mapNotNull { it.id.removePrefix("R").toIntOrNull() }
                                    .maxOrNull() ?: 0
                                val newRewardId = "R%04d".format(lastId + 1)

                                val redeemedRewardData = hashMapOf(
                                    "id" to newRewardId,
                                    "name" to reward.name,
                                    "condition" to reward.condition,
                                    "pointsRequired" to reward.pointsRequired,
                                    "isRedeemed" to true,
                                    "isPaid" to false,
                                    "paymentId" to null,
                                    "status" to "pending"
                                )

                                db.collection("Profiles")
                                    .document(snapshot.documents[0].id)
                                    .collection("RedeemedRewards")
                                    .document(newRewardId)
                                    .set(redeemedRewardData)
                                    .addOnSuccessListener {
                                        val redeemedRewardItem = reward.copy(id = newRewardId, isRedeemed = true)
                                        _redeemedReward.value = redeemedRewardItem
                                        onComplete(true, "Reward redeemed successfully.", redeemedRewardItem)
                                    }
                                    .addOnFailureListener {
                                        onComplete(false, "Failed to save redeemed reward.", null)
                                    }
                            }
                    }
                    .addOnFailureListener {
                        onComplete(false, "Failed to update points.", null)
                    }
            }
            .addOnFailureListener {
                onComplete(false, "Error fetching profile.", null)
            }
    }

    fun fetchUnpaidRedeemedRewards() {
        val email = auth.currentUser?.email ?: return

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || snapshot.isEmpty) {
                    _unpaidRewards.value = emptyList()
                    return@addSnapshotListener
                }

                val profileDocId = snapshot.documents[0].id

                db.collection("Profiles")
                    .document(profileDocId)
                    .collection("RedeemedRewards")
                    .whereEqualTo("isPaid", false)
                    .addSnapshotListener { rewardsSnapshot, e ->
                        if (e != null || rewardsSnapshot == null || rewardsSnapshot.isEmpty) {
                            _unpaidRewards.value = emptyList()
                            return@addSnapshotListener
                        }

                        val rewards = rewardsSnapshot.documents.map { doc ->
                            RewardItem(
                                id = doc.id,
                                name = doc.getString("name") ?: "Unknown",
                                condition = doc.getString("condition") ?: "Can only redeem one time",
                                pointsRequired = doc.getLong("pointsRequired")?.toInt() ?: 0,
                                isRedeemed = doc.getBoolean("isRedeemed") ?: true,
                                isPaid = doc.getBoolean("isPaid") ?: false,
                                status = doc.getString("status") ?: "pending"
                            )
                        }

                        _unpaidRewards.value = rewards
                    }
            }
    }

    fun markAllUnpaidRewardsAsPaid() {
        val email = auth.currentUser?.email ?: return

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
                    .whereEqualTo("status", "pending")
                    .get()
                    .addOnSuccessListener { rewardsSnapshot ->
                        if (rewardsSnapshot.isEmpty) return@addOnSuccessListener

                        for (doc in rewardsSnapshot.documents) {
                            db.collection("Profiles")
                                .document(profileDocId)
                                .collection("RedeemedRewards")
                                .document(doc.id)
                                .update(
                                    mapOf(
                                        "isPaid" to true
                                    )
                                )
                        }
                    }
            }
    }

    fun markRewardAsReceived(rewardId: String) {
        val email = auth.currentUser?.email ?: return

        db.collection("Profiles")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) return@addOnSuccessListener
                val profileDocId = snapshot.documents[0].id

                db.collection("Profiles")
                    .document(profileDocId)
                    .collection("RedeemedRewards")
                    .document(rewardId)
                    .update("status", "received")
            }
    }

}