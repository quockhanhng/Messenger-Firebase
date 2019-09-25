package com.quockhanhng.training.messenger.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.adapter.MessageAdapter
import com.quockhanhng.training.messenger.model.Message
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var myUser: User
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var database: FirebaseFirestore
    private lateinit var query: Query
    private var adapter: FirestoreRecyclerAdapter<Message, MessageAdapter.MessageHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        //Check if user has signed in before else redirect to login page
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        userId = user?.uid.toString()
        database = FirebaseFirestore.getInstance()

        val userRef = database.collection("users").document(userId)
        userRef.get().addOnSuccessListener {
            myUser = it.toObject(User::class.java)!!
            userName = myUser.surname + " " + myUser.name
        }

        query = database.collection("messages").orderBy("messageTime")
        query.addSnapshotListener { queryDocumentSnapshots, _ ->
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                progress_loader.visibility = View.GONE
            }
        }

        adapter = MessageAdapter(query, userId, this@MainActivity)
        recyclerViewList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_sign_out -> {
                auth.signOut()
                finish()
                startActivity(Intent(this, LoginActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun sendMessage(v: View) {
        val message = input.text.toString()
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this@MainActivity, "Write something first", Toast.LENGTH_SHORT).show()
            return
        }
        database.collection("messages").add(Message(userName, message, userId, 0, null))
        adapter?.notifyDataSetChanged()
        input.setText("")
    }

    override fun onStart() {
        super.onStart()

        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()

        adapter?.stopListening()
    }
}
