package com.gematriga.nopepad

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import com.gematriga.nopepad.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var email : String
    private lateinit var password : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth



        val currentUser = auth.currentUser

        if (currentUser != null){

            val intent = Intent(this, FeedActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.userPassword.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                signInClicked(view)
                return@OnKeyListener true
            }
            false
        })

    }

    fun signInClicked(view: View) {

        email = binding.userMail.text.toString()
        password = binding.userPassword.text.toString()
        if (email.equals("") && password.equals("")){
            Toast.makeText(this,"Enter Email and Password!",Toast.LENGTH_LONG).show()
        }else{
            auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
                val intent = Intent(this@MainActivity,FeedActivity::class.java)
                startActivity(intent)
                finish()

            }.addOnFailureListener {

                Toast.makeText(this@MainActivity,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }

    }

    fun signUpClicked(view : View) {

        email = binding.userMail.text.toString()
        password = binding.userPassword.text.toString()

        if(email.equals("") && password.equals("")){
            Toast.makeText(this@MainActivity,"Enter Email and Password!", Toast.LENGTH_LONG).show()
        }else{
            println(email)
            val intent = Intent(applicationContext,SignUp::class.java)
            intent.putExtra("TransportEmail", email)
            startActivity(intent)
        }
    }
}