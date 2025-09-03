package com.example.underbigtreeapplication.repository

import android.net.Uri
import com.example.underbigtreeapplication.data.local.ProfileDao
import com.example.underbigtreeapplication.model.ProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

enum class UserRole { CUSTOMER, STAFF }

//@IgnoreExtraProperties
data class Profile(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val gender: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val role: UserRole = UserRole.CUSTOMER
)

fun ProfileEntity.toDomain() = Profile(
    uid, name, phone, gender, email, photoUrl, UserRole.valueOf(role)
)

fun Profile.toEntity() = ProfileEntity(
    uid, name, phone, gender, email, photoUrl, role.name
)

class ProfileRepository(
    private val dao: ProfileDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val col get() = firestore.collection("Profiles")

    fun observeProfile(uid: String): Flow<Profile?> =
        dao.observe(uid).map { it?.toDomain() }

    fun observeProfileByEmail(email: String) : Flow<Profile?> = callbackFlow{
        val listener = firestore.collection("Profiles")
            .whereEqualTo("email", email)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val profile = snapshot?.documents?.firstOrNull()?.toObject(Profile::class.java)
                trySend(profile)
            }
        awaitClose { listener.remove() }
    }

    suspend fun refreshFromFirebase(uid: String) {
        val snap = col.document(uid).get().await()
        if (snap.exists()) {
            val data = snap.data ?: return
            val profile = Profile(
                uid = uid,
                name = data["name"] as? String ?: "",
                phone = data["phone"] as? String ?: "",
                gender = data["gender"] as? String ?: "",
                email = data["email"] as? String ?: "",
                photoUrl = data["photoUrl"] as? String,
                role = (data["role"] as? String)?.let { UserRole.valueOf(it) } ?: UserRole.CUSTOMER
            )
            dao.upsert(profile.toEntity())
        }
    }

    suspend fun createProfile(name: String, phone: String, email: String, gender: String): Profile {
        val role = if (email.equals("underbigtree@gmail.com", ignoreCase = true)) {
            UserRole.STAFF
        } else {
            UserRole.CUSTOMER
        }

        val uid = if (role == UserRole.STAFF) {
            "S0001"
        } else {
            val count = col.whereEqualTo("role", "CUSTOMER").get().await().size()
            "C" + (count + 1).toString().padStart(4, '0')
        }

        val profile = Profile(
            uid = uid,
            name = name,
            phone = phone,
            gender = gender,
            email = email,
            role = role
        )

        col.document(uid).set(profile.toEntity()).await()
        dao.upsert(profile.toEntity())
        return profile
    }

    fun getProfileFlow(email: String): Flow<Profile?> = flow {
        val local = dao.getByEmail(email).firstOrNull()
        if (local != null) emit(local.toDomain())

        val snapshot = col.whereEqualTo("email", email).get().await()
        val remote = snapshot.documents.firstOrNull()?.toObject(ProfileEntity::class.java)
        if (remote != null) {
            dao.upsert(remote)
            emit(remote.toDomain())
        }
    }

    suspend fun updateProfile(profile: Profile) {
        if (profile.role == UserRole.STAFF) {
            throw IllegalAccessException("Staff profile cannot be edited")
        }
        col.document(profile.uid).set(profile.toEntity()).await()
        dao.upsert(profile.toEntity())
    }
}
