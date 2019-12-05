package com.quockhanhng.training.messenger.activity

import android.content.Intent
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.adapter.LastChatAdapter
import com.quockhanhng.training.messenger.model.LastMessageResponse
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var rootLayout: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private var user: FirebaseUser? = null
    private lateinit var myUser: User
    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var database: FirebaseFirestore
    private var adapter: RecyclerView.Adapter<LastChatAdapter.LastChatHolder>? = null
    private var friendList = ArrayList<LastMessageResponse>()

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

            for (friend in myUser.friends) {
                database.collection("users").document(friend.key.trim()).get()
                    .addOnSuccessListener { documentSnapshot ->
                        val user = documentSnapshot.toObject(User::class.java)!!
                        friendList.add(LastMessageResponse(user, friend.value))
                    }
                    .addOnCompleteListener {
                        adapter = LastChatAdapter(friendList) { itemCLicked ->
                            lastMessageItemClicked(itemCLicked)
                        }
                        recyclerChatList.adapter = adapter

                        progress_loader.visibility = View.INVISIBLE
                    }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        drawerToggle.onConfigurationChanged(newConfig)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
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

    private fun lastMessageItemClicked(lastMessageResponse: LastMessageResponse) {
        val intent = Intent(this, ChatActivity::class.java)
        val id =
            if (myUser.userId < lastMessageResponse.user.userId)
                myUser.userId + lastMessageResponse.user.userId
            else
                lastMessageResponse.user.userId + myUser.userId
        intent.putExtra("ConversationId", id)
        intent.putExtra("FriendId", lastMessageResponse.user.userId)
        startActivity(intent)
    }
}
