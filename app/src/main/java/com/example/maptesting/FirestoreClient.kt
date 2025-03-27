package com.example.maptesting

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class FirestoreClient {

    private val tag = "FirestoreClient"
    private val db = FirebaseFirestore.getInstance()
    private val collection = "Users"


    fun insertUser(
        user: User
    ): Flow<String?> {
        return callbackFlow {
            db.collection((collection))
                .add(user.toHashMap())
                .addOnSuccessListener { document ->
                    println(tag + "inserted user")
                    CoroutineScope(Dispatchers.IO).launch {
                        updateUser(user).collect{}
                    }
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
            db.collection((collection))
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


    private fun User.toHashMap(): HashMap<String, Any>{
        return hashMapOf(
            "id" to id,
            "username" to username,
            "bio" to bio,
            "mainLocation" to mainLocation
        )
    }

    private fun Map<String, Any>.toUser(): User{
        return User(
            id = this["id"] as String,
            username = this["username"] as String,
            bio = this["bio"] as String,
            mainLocation = this["mainLocation"] as String
            //firestore stashes numbers as long
        )
    }
    fun getUser(
        username: String
    ): Flow<User?> {
        return callbackFlow {
            db.collection((collection))
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
                    if (user == null){
                        println(tag + "user not found")
                        trySend(null)
                    }

                }

                .addOnFailureListener {
                    println(tag + "insert getting error")
                    trySend(null)
                }

            awaitClose{}

        }
    }
}