package com.quockhanhng.training.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.adapter.MessageAdapter
import com.quockhanhng.training.messenger.model.Message
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_chat.*

class ChatActivity : AppCompatActivity(), MessageAdapter.MessageClickListener {

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var myUser: User
    private lateinit var friend: User
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var database: FirebaseFirestore
    private lateinit var query: Query
    private var adapter: FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder>? = null
    private lateinit var conversationId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser
        conversationId = intent.getStringExtra("ConversationId")!!

        setUpData()
    }

    private fun setUpData() {
        userId = user?.uid.toString()
        database = FirebaseFirestore.getInstance()

        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener {
            myUser = it.toObject(User::class.java)!!
            userName = myUser.surname + " " + myUser.name
        }

        val friendId = intent.getStringExtra("FriendId")!!
        database.collection("users").document(friendId).get().addOnSuccessListener {
            friend = it.toObject(User::class.java)!!
        }

        query = database.collection("conversations").document(conversationId).collection("messages")
            .orderBy("messageTime")
        query.addSnapshotListener { queryDocumentSnapshots, _ ->
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                progress_loader.visibility = View.GONE
            }
        }

        adapter = MessageAdapter(query, userId, this@ChatActivity)
        recyclerViewList.adapter = adapter
        recyclerViewList.smoothScrollToPosition(adapter?.itemCount!! + 1)
    }

    fun sendMessage(v: View) {
        val message = input.text.toString()
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this@ChatActivity, "Write something first", Toast.LENGTH_SHORT).show()
            return
        }

        val newMessage = Message("", userName, message, userId, 0)
        database.collection("conversations").document(conversationId).collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                newMessage.messageId = it.id

                database.collection("conversations").document(conversationId).collection("messages")
                    .document(newMessage.messageId).set(newMessage)

                myUser.friends[friend.userId] = newMessage.messageTime
                friend.friends[myUser.userId] = newMessage.messageTime

                database.collection("users").document(myUser.userId).set(myUser)
                database.collection("users").document(friend.userId).set(friend)
            }

        adapter?.notifyDataSetChanged()
        input.setText("")
        recyclerViewList.smoothScrollToPosition(adapter?.itemCount!! + 1)
    }

    override fun onStart() {
        super.onStart()

        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()

        adapter?.stopListening()
    }

    override fun onClickLike(id: String): Int {
        var status = -1

        val docRef =
            database.collection("conversations").document(conversationId).collection("messages")
                .document(id)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val message = documentSnapshot.toObject(Message::class.java)!!

            if (message.messageLikes == null)
                message.messageLikes = ArrayList()

            // Check if user has liked this message
            if (message.messageLikes!!.contains(myUser.userId)) {
                status = 0
                message.messageLikes!!.remove(myUser.userId)
                message.messageLikesCount--
            } else {
                status = 1
                message.messageLikes!!.add(myUser.userId)
                message.messageLikesCount++
            }

            // Update status
            docRef.set(message)
        }

        return status
    }

    override fun onClickDropdownButton(id: String) {
        val docRef =
            database.collection("conversations").document(conversationId).collection("messages")
                .document(id)
        docRef.get().addOnSuccessListener { documentSnapshot ->
            val message = documentSnapshot.toObject(Message::class.java)!!

            // If user is the owner of the message
            if (message.messageUserId == myUser.userId)
                docRef
                    .delete()
                    .addOnCompleteListener { Log.d("LOG", "Message successfully deleted!") }
                    .addOnFailureListener { Log.w("LOG", "Error deleting message", it) }
        }
    }
}
