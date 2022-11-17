package com.gematriga.nopepad

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gematriga.nopepad.databinding.ActivityUploadBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class UploadActivity : AppCompatActivity() {

    private lateinit var binding : ActivityUploadBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        firestore = Firebase.firestore

        binding.backButtonForCreateFrg.setOnClickListener {
            val intent = Intent(this@UploadActivity,FeedActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onBackPressed() {
        try {
            val title = binding.titleEditText.text.toString()
            val note = binding.noteEditText.text.toString()

            val postMap = hashMapOf<String, Any>()
            postMap.put("email", auth.currentUser!!.email!!)
            postMap.put("title", title)
            postMap.put("note", note)
            postMap.put("date", com.google.firebase.Timestamp.now())

            if (title.isNotEmpty() || note.isNotEmpty()){
                firestore.collection("Notes").add(postMap).addOnSuccessListener {

                    Toast.makeText(this@UploadActivity,"Your note has been saved.", Toast.LENGTH_SHORT).show()
                    finish()

                }.addOnFailureListener {

                    Toast.makeText(this@UploadActivity,it.localizedMessage,Toast.LENGTH_LONG).show()

                }
            }else{
                finish()
            }

        }catch (e: Exception){
            println(e.localizedMessage)
        }

    }

}