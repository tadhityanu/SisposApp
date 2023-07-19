package com.example.skripsiapp.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.lang.Exception

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var fAuth:FirebaseAuth
    private lateinit var fStore:FirebaseFirestore

    //component
    private lateinit var edtRegisterUsername : EditText
    private lateinit var edtRegisterEmail : EditText
    private lateinit var edtRegisterPass : EditText
    private lateinit var edtRegisterConfPass : EditText
    private lateinit var rgAccessLevel : RadioGroup
    private lateinit var rbAdmin : RadioButton
    private lateinit var rbGudang : RadioButton
    private lateinit var btnRegister : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        fAuth = Firebase.auth
        fStore = Firebase.firestore

        edtRegisterUsername = binding.edtRegisterUsername
        edtRegisterEmail = binding.edtRegisterEmail
        edtRegisterPass = binding.edtRegisterPassword
        edtRegisterConfPass = binding.edtRegisterConfirmPassword
        rgAccessLevel = binding.rgAccessLevel
        btnRegister = binding.btnRegister

        buttonAction()
        layoutSetting()

    }

    private fun buttonAction(){
        btnRegister.setOnClickListener {
            showLoading(true)
            checkField()
        }
    }

    private fun registerAction(){
        val username = edtRegisterUsername.text.toString()
        val email = edtRegisterEmail.text.toString()
        val pass = edtRegisterPass.text.toString()
        fAuth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { authResult ->
                showLoading(false)
                val userId =FirebaseAuth.getInstance().currentUser!!.uid

                val radioId = rgAccessLevel.checkedRadioButtonId
                val userAccessLevel = findViewById<RadioButton>(radioId)

                val userData = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "password" to pass,
                    "accessLevel" to userAccessLevel.text.toString()
                )

                fStore.collection("user").document(userId).set(userData)

                val userProfileUpdate = userProfileChangeRequest {
                    displayName = edtRegisterUsername.text.toString()
                }
                val user = authResult.user
                user?.updateProfile(userProfileUpdate)

                Toast.makeText(this, "Akun berhasil dibuat", Toast.LENGTH_LONG).show()
                fAuth.signOut()
                finish()
            }


            .addOnFailureListener { error ->
//                Toast.makeText(this, error.localizedMessage., Toast.LENGTH_SHORT).show()
                showLoading(false)

                when(error.localizedMessage){
                    "The email address is already in use by another account." ->{
                        Toast.makeText(applicationContext, "Email Sudah Terpakai", Toast.LENGTH_SHORT).show()
                    }
                    "The email address is badly formatted." ->{
                        Toast.makeText(applicationContext, "Penulisan Email Salah", Toast.LENGTH_SHORT).show()
                    }
                    "A network error (such as timeout, interrupted connection or unreachable host) has occurred."->{
                        Toast.makeText(applicationContext, "Koneksi Gagal", Toast.LENGTH_SHORT).show()
                    }
                }

            }
    }

    private fun checkField(){
        when{
            edtRegisterUsername.text.isEmpty() -> binding.layoutEdtRegisterUsername.error = "Username tidak boleh kosong"
            edtRegisterEmail.text.isEmpty() -> binding.layoutEdtRegisterEmail.error = "Email tidak boleh kosong"
            edtRegisterPass.text.isEmpty() -> binding.layoutEdtRegisterPassword.error = "Password tidak boleh kosong"
            edtRegisterConfPass.text.isEmpty() -> binding.layoutEdtRegisterConfirmPassword.error = "Lakukan konfirmasi password terlebih dahulu"

            edtRegisterPass.text.length < 6 -> binding.layoutEdtRegisterPassword.error = "Password harus lebih dari 6 karakter"
            !edtRegisterConfPass.text.toString().equals(edtRegisterPass.text.toString()) -> binding.layoutEdtRegisterConfirmPassword.error = "Password yang dimasukan berbeda"
            rgAccessLevel.checkedRadioButtonId == -1 -> Toast.makeText(this, "Pilih Jenis Akun", Toast.LENGTH_LONG).show()
            else -> registerAction()
        }
    }

    private fun layoutSetting(){
        edtRegisterUsername.addTextChangedListener (object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        edtRegisterEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        edtRegisterPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        edtRegisterConfPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                hideError()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
    }

    private fun hideError(){
        val usernameLayout = binding.layoutEdtRegisterUsername
        val emailLayout = binding.layoutEdtRegisterEmail
        val passLayout = binding.layoutEdtRegisterPassword
        val confPassLayout = binding.layoutEdtRegisterConfirmPassword

        usernameLayout.error = null
        emailLayout.error = null
        passLayout.error = null
        confPassLayout.error = null
    }

    companion object{
        private const val EMAIL_USED_CAUTION = "The email address is already in use by another account"
    }

    private fun showLoading(state: Boolean) {
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

}