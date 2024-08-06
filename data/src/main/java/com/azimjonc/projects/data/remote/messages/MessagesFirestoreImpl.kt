package com.azimjonc.projects.data.remote.messages

import com.google.firebase.Firebase
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.firestore
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import java.util.Date
import java.util.UUID

class MessagesFirestoreImpl : MessagesFirestore {

    private val messages = Firebase.firestore.collection("messages")
    override fun sendMessage(
        fromUserId: String,
        toUserId: String,
        message: String
    ): Completable = Completable.create { emitter ->
        val messageDocument = MessageDocument(
            id = UUID.randomUUID().toString(),
            message = message,
            time = Date(),
            members = listOf(fromUserId, toUserId),
            count = 2,
            from = fromUserId
        )
        messages.document(messageDocument.id!!).set(messageDocument)
            .addOnFailureListener {
                emitter.onError(it)
            }
            .addOnSuccessListener {
                emitter.onComplete()
            }
    }


    private val message = mutableListOf<MessageDocument>()
    override fun getMessages(
        firstUserId: String,
        secondUserId: String
    ): Observable<List<MessageDocument>> = Observable.create { emitter ->
        messages
            .whereEqualTo("count", 2)
            .whereArrayContains("members", firstUserId)
            .whereArrayContains("members", secondUserId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    emitter.onError(e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    emitter.onNext(emptyList())
                    return@addSnapshotListener
                }
                val messageDocuments = snapshot?.documents!!.mapNotNull {
                    it.toObject(MessageDocument::class.java)
                }
                emitter.onNext(messageDocuments)

            }

    }
}