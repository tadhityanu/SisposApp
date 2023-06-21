package com.example.skripsiapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.example.skripsiapp.Activity.Admin.MainActivity
import com.example.skripsiapp.Activity.Gudang.MainWarehouseActivity
import com.example.skripsiapp.Activity.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SplashScreen : AppCompatActivity() {

    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        fAuth = Firebase.auth
        fStore = Firebase.firestore

        profileSet()
    }

    private fun profileSet(){
        val currentUser = fAuth.currentUser
        if (currentUser == null){
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        } else{
            checkUserLevel(currentUser.uid)
        }
    }
    private fun checkUserLevel(userId : String){
        val docReference =fStore.collection("user").document(userId)
        docReference.get()
            .addOnSuccessListener { docSnapshot ->
                if (docSnapshot != null){
                    val userAccess = docSnapshot.data?.get("accessLevel").toString()
                    when(userAccess){
                        "Admin" ->{
                            startActivity(Intent(applicationContext, MainActivity::class.java))
                            finish()
                        }
                        "Gudang" -> {
                            startActivity(Intent(applicationContext, MainWarehouseActivity::class.java))
                            finish()
                        }
                    }
                }
            }
    }
}