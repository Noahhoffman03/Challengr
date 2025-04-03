package com.example.maptesting

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.io.File

class FirestoreClient {

    private val tag = "FirestoreClient"
    private val db = FirebaseFirestore.getInstance()
    private val collection = "Users"
    private val collection2 = "Challenges"


    fun insertUser(
        user: User
    ): Flow<String?> {
        return callbackFlow {
            db.collection(collection)
                .add(user.toHashMap())
                .addOnSuccessListener { document ->
                    println(tag + "inserted user")
                    trySend(document.id)
                }
                .addOnFailureListener {
                    println(tag + "insert user error")
                    trySend(null)
                }

            awaitClose{}
        }
    }


    fun updateUser(
        user: User
    ): Flow<Boolean> {
        return callbackFlow {
            db.collection(collection)
                .document(user.id)
                .set(user.toHashMap())
                .addOnSuccessListener {
                    println(tag + "update user with id")
                    trySend(true)
                }
                .addOnFailureListener {
                    println(tag + "update user error")
                    trySend(false)
                }

            awaitClose{}
        }
    }


    private fun User.toHashMap(): HashMap<String, Any> {
        return hashMapOf(
            "id" to id,
            "username" to username,
            "bio" to bio,
            "mainLocation" to mainLocation,
            "password" to password,
            "email" to email
        )
    }

    private fun Map<String, Any>.toUser(): User {
        return User(
            id = this["id"] as String,
            username = this["username"] as String,
            bio = this["bio"] as String,
            mainLocation = this["mainLocation"] as String,
            password = this["password"] as String,
            email = this["email"] as String
        )
    }

    fun getUser(
        username: String
    ): Flow<User?> {
        return callbackFlow {
            db.collection(collection)
                .get()
                .addOnSuccessListener { result ->
                    var user: User? = null

                    for (document in result) {
                        if (document.data["username"] == username) {
                            println(tag + "user found")
                            user = document.data.toUser()
                            trySend(user)
                            break
                        }
                    }
                    if (user == null) {
                        println(tag + "user not found")
                        trySend(null)
                    }
                }
                .addOnFailureListener {
                    println(tag + "get user error")
                    trySend(null)
                }

            awaitClose{}
        }
    }

    fun insertChallenge(
        challenge: Challenge
    ): Flow<String?> {
        return callbackFlow {
            db.collection(collection2)
                .add(challenge.toHashMap())
                .addOnSuccessListener { document ->
                    println(tag + "inserted challenge")
                    trySend(document.id)
                }
                .addOnFailureListener {
                    println(tag + "insert challenge error")
                    trySend(null)
                }

            awaitClose{}
        }
    }

    fun updateChallenge(
        challenge: Challenge
    ): Flow<Boolean> {
        return callbackFlow {
            db.collection(collection2)
                .document(challenge.id)
                .set(challenge.toHashMap())
                .addOnSuccessListener {
                    println(tag + "update challenge with id")
                    trySend(true)
                }
                .addOnFailureListener {
                    println(tag + "update challenge error")
                    trySend(false)
                }

            awaitClose{}
        }
    }

    private fun Challenge.toHashMap(): HashMap<String, Any?> {
        return hashMapOf(
            "id" to id,
            "creatorId" to creatorId,
            "title" to title,
            "desc" to desc,
            "photo" to photo,
            "lat" to lat,
            "lng" to lng
        )
    }

    private fun Map<String, Any>.toChallenge(): Challenge {
        return Challenge(
            id = this["id"] as String,
            creatorId = this["creatorId"] as String,
            title = this["title"] as String,
            desc = this["desc"] as String,
            photo = this["photo"] as String,
            lat = this["lat"] as Double,
            lng = this["lng"] as Double
        )
    }

    fun getChallenge(
        title: String
    ): Flow<Challenge?> {
        return callbackFlow {
            db.collection(collection2)
                .get()
                .addOnSuccessListener { result ->
                    var challenge: Challenge? = null

                    for (document in result) {
                        if (document.data["title"] == title) {
                            println(tag + "challenge found")
                            challenge = document.data.toChallenge()
                            trySend(challenge)
                            break
                        }
                    }
                    if (challenge == null) {
                        println(tag + "challenge not found")
                        trySend(null)
                    }
                }
                .addOnFailureListener {
                    println(tag + "get challenge error")
                    trySend(null)
                }

            awaitClose{}
        }
    }
}
