package com.quockhanhng.training.messenger.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.Toast
import androidx.core.app.NavUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.quockhanhng.training.messenger.R
import com.quockhanhng.training.messenger.model.User
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.util.*

class SignUpActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseFirestore

    companion object {
        const val TAG = "SignUp Activity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()
    }

    fun registration(v: View) {
        if (validateForm())
            doRegistration()
        else
            doValidateFail()
    }

    private fun validateForm(): Boolean {
        return (sign_up_surname.text.toString().trim().isNotEmpty() && sign_up_name.text.toString().trim().isNotEmpty()
                && sign_up_email.text.toString().trim().isNotEmpty() && sign_up_phone.text.toString().trim().isNotEmpty()
                && sign_up_password.text.toString().trim().isNotEmpty())
    }

    @SuppressLint("SetTextI18n")
    private fun doRegistration() {
        sign_up_email.setBackgroundResource(R.drawable.edit_text_shape)
        sign_up_password.setBackgroundResource(R.drawable.edit_text_shape)
        sign_up_error.visibility = View.GONE

        auth.createUserWithEmailAndPassword(sign_up_email.text.toString(), sign_up_password.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user: FirebaseUser = auth.currentUser!!

                    val newUser = User(
                        sign_up_surname.text.toString(),
                        sign_up_name.text.toString(),
                        sign_up_email.text.toString(),
                        sign_up_phone.text.toString(),
                        getDateFromPicker(sign_up_date_picker),
                        getGenderFromRadioButtonId(sign_up_rg_gender.checkedRadioButtonId)
                    )

                    database.collection("users").document(auth.currentUser!!.uid).set(newUser)

                    Toast.makeText(baseContext, "Successful", Toast.LENGTH_SHORT).show()
                    Timer().schedule(object : TimerTask() {
                        override fun run() {
                            NavUtils.navigateUpFromSameTask(this@SignUpActivity)
                        }
                    }, 1000)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    sign_up_error.visibility = View.GONE
                    sign_up_error.text = "Authentication failed."
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun doValidateFail() {
        sign_up_error.visibility = View.VISIBLE
        sign_up_error.text = "Fill all information please."

        if (sign_up_surname.text.toString().trim().isEmpty()) {
            sign_up_surname.setBackgroundResource(R.drawable.edit_text_shape_error)
        } else
            sign_up_surname.setBackgroundResource(R.drawable.edit_text_shape)

        if (sign_up_name.text.toString().trim().isEmpty()) {
            sign_up_name.setBackgroundResource(R.drawable.edit_text_shape_error)
        } else
            sign_up_name.setBackgroundResource(R.drawable.edit_text_shape)

        if (sign_up_email.text.toString().trim().isEmpty()) {
            sign_up_email.setBackgroundResource(R.drawable.edit_text_shape_error)
        } else
            sign_up_email.setBackgroundResource(R.drawable.edit_text_shape)

        if (sign_up_phone.text.toString().trim().isEmpty()) {
            sign_up_phone.setBackgroundResource(R.drawable.edit_text_shape_error)
        } else
            sign_up_phone.setBackgroundResource(R.drawable.edit_text_shape)

        if (sign_up_password.text.toString().trim().isEmpty()) {
            sign_up_password.setBackgroundResource(R.drawable.edit_text_shape_error)
        } else
            sign_up_password.setBackgroundResource(R.drawable.edit_text_shape)
    }

    private fun getDateFromPicker(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month
        val year = datePicker.year

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)

        return calendar.time.toString()
    }

    private fun getGenderFromRadioButtonId(id: Int): String {
        return when (id) {
            R.id.sign_up_rb_male -> "Male"
            R.id.sign_up_rb_female -> "Female"
            else -> "Custom"
        }
    }
}
