package com.quockhanhng.training.messenger.activity

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.adapter.MessageAdapter
import com.quockhanhng.training.messenger.model.Message
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MessageAdapter.MessageClickListener {

    private lateinit var rootLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
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

        rootLayout = findViewById(R.id.rootLayout)
        drawerToggle =
            ActionBarDrawerToggle(
                this,
                rootLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
        rootLayout.addDrawerListener(drawerToggle)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_drawable)

        drawerToggle.syncState()

        navigation_menu.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.mi_account -> goToProfileAccount()
                    R.id.mi_settings -> goToSettings()
                    R.id.mi_logout -> logOut()
                    else -> return true
                }

                return true
            }
        })

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        // Check if user has signed in before else redirect to login page
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

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

        query = database.collection("messages").orderBy("messageTime")
        query.addSnapshotListener { queryDocumentSnapshots, _ ->
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty) {
                progress_loader.visibility = View.GONE
            }
        }

        adapter = MessageAdapter(query, userId, this@MainActivity)
        recyclerViewList.adapter = adapter
        recyclerViewList.smoothScrollToPosition(adapter?.itemCount!! + 1)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        drawerToggle.onConfigurationChanged(newConfig)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }

    fun sendMessage(v: View) {
        val message = input.text.toString()
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this@MainActivity, "Write something first", Toast.LENGTH_SHORT).show()
            return
        }

        val newMessage = Message("", userName, message, userId, 0)
        database.collection("messages").add(newMessage)
            .addOnSuccessListener {
                newMessage.messageId = it.id

                database.collection("messages").document(newMessage.messageId).set(newMessage)
            }
        Log.d("Message", newMessage.messageId)

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

    private fun goToProfileAccount() {
        Toast.makeText(this, "Profile Account", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("CurrentUser", myUser)
        startActivity(intent)
    }

    private fun goToSettings() {
        Toast.makeText(this, "Settings", Toast.LENGTH_SHORT).show()
    }

    private fun logOut() {
        auth.signOut()
        finish()
        startActivity(Intent(this, LoginActivity::class.java))
    }

    override fun onClickLike(id: String): Int {
        var status = -1

        val docRef = database.collection("messages").document(id)
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
        val docRef = database.collection("messages").document(id)
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
