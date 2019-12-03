package com.quockhanhng.training.messenger.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        user = intent.getSerializableExtra("CurrentUser") as User
        updateUI()
    }

    private fun updateUI() {
        val name = user.surname + " " + user.name
        profileName.text = name
        profileEmail.text = user.email
        profilePhone.text = user.phoneNumber
        profileDoB.text = user.dob
    }

}