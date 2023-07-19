package com.example.skripsiapp.Activity

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.skripsiapp.Activity.Admin.MainActivity
import com.example.skripsiapp.Activity.Gudang.MainWarehouseActivity
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var fAuth : FirebaseAuth
    private lateinit var fStore : FirebaseFirestore

    private lateinit var txtRegister : TextView
    private lateinit var edtEmail : EditText
    private lateinit var edtPass : EditText
    private lateinit var btnLogin : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        fAuth = Firebase.auth
        fStore = Firebase.firestore

        txtRegister = binding.txtRegister
        edtEmail = binding.edtEmail
        edtPass = binding.edtPassword
        btnLogin = binding.btnLogin

        val currentID = fAuth.currentUser
        if (currentID != null){
            checkUserLevel(currentID.uid)
        }

        toRegister()
        buttonAction()
    }

    private fun buttonAction(){
        btnLogin.setOnClickListener {
            showLoading(true)
            checkField()
        }
    }

    private fun loginAction(){
        val email = edtEmail.text.toString()
        val pass = edtPass.text.toString()
        fAuth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                checkUserLevel(authResult.user!!.uid)
                showLoading(false)
            }
            .addOnFailureListener {
                showLoading(false)
                when(it.localizedMessage){
                    "There is no user record corresponding to this identifier. The user may have been deleted." ->{
                        Toast.makeText(this, "Akun Tidak Tersedia", Toast.LENGTH_LONG).show()
                    }
                    else->{
                        Toast.makeText(this, "Email atau Password salah", Toast.LENGTH_LONG).show()
                    }
                }
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

    private fun checkField(){
        val email = edtEmail.text.toString()
        val pass = edtPass.text.toString()
        when{
            email.isEmpty() -> {
                showLoading(false)
                edtEmail.error = "Email masih kosong"
            }
            pass.isEmpty() -> {
                showLoading(false)
                binding.layoutEdtPassword.error = "Password tidak boleh kosong"
            }
            else -> loginAction()
        }
    }

    private fun showLoading(state: Boolean) {
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

    private fun toRegister(){
        txtRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}