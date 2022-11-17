package com.gematriga.nopepad

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.gematriga.nopepad.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val tEmail : String? = intent.getStringExtra("TransportEmail")
        binding.userMailReg.setText(tEmail)
        println(tEmail)


        binding.userPasswordReg.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                signUpRegClicked(view)
                return@OnKeyListener true
            }
            false
        })

        auth = Firebase.auth
    }

    fun signUpRegClicked(view : View){

        val email = binding.userMailReg.text.toString()
        val password = binding.userPasswordReg.text.toString()

        if (email.isEmpty() && password.isEmpty()){
            Toast.makeText(this,"Enter email and password!", Toast.LENGTH_LONG).show()
        }else{

            auth.createUserWithEmailAndPassword(email,password).addOnSuccessListener {

                val intent = Intent(this@SignUp,FeedActivity::class.java)
                startActivity(intent)
                finish()

            }.addOnFailureListener {
                Toast.makeText(this@SignUp,it.localizedMessage,Toast.LENGTH_LONG).show()
            }

        }
    }
}