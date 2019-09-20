package com.quockhanhng.training.messenger.activity

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.quockhanhng.training.messenger.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        const val TAG = "Login Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
    }

    @SuppressLint("SetTextI18n")
    fun login(v: View) {
        if (login_email.text.toString().trim().isNotEmpty() && login_password.text.toString().trim().isNotEmpty()) {

            login_email.setBackgroundResource(R.drawable.edit_text_shape)
            login_password.setBackgroundResource(R.drawable.edit_text_shape)
            login_error.visibility = View.INVISIBLE

            auth.signInWithEmailAndPassword(login_email.text.toString(), login_password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        val user = auth.currentUser
//                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        login_error.visibility = View.VISIBLE
                        login_error.text = "Authentication failed."
//                        updateUI(null)
                    }
                }
        } else {
            if (login_email.text.toString().trim().isEmpty()) {
                login_email.setBackgroundResource(R.drawable.edit_text_shape_error)
                login_error.visibility = View.VISIBLE
                login_error.text = "Email or password must not be null."
            } else
                login_email.setBackgroundResource(R.drawable.edit_text_shape)

            if (login_password.text.toString().trim().isEmpty()) {
                login_password.setBackgroundResource(R.drawable.edit_text_shape_error)
                login_error.visibility = View.VISIBLE
                login_error.text = "Email or password must not be null."
            } else
                login_password.setBackgroundResource(R.drawable.edit_text_shape)
        }
    }

    fun signUp(v: View) {
        val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
        startActivity(intent)
    }
}
