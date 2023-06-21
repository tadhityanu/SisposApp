package com.example.skripsiapp.Activity.Admin

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.skripsiapp.Activity.Gudang.DetailProductActivity
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityQrCodeScannerBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class QrCodeScannerActivity : AppCompatActivity() {

    private lateinit var binding : ActivityQrCodeScannerBinding
    private lateinit var codeScanner : CodeScanner
    private lateinit var fAuth : FirebaseAuth
    private lateinit var dbItem : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityQrCodeScannerBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        fAuth = Firebase.auth
        dbItem = Firebase.database

        setupPermissions()
        codeScanner()

    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, binding.scn)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    val text = it.text.split(";")
                    val id = text[0]
                    dbItem.getReference("item_data").child(id).get()
                        .addOnSuccessListener { db ->
                            if (db.exists()){
                                val intent = Intent(this@QrCodeScannerActivity, DetailAdminActivity::class.java)
                                intent.putExtra("item_Id", id)
                                startActivity(intent)
                                finish()
                            } else{
                                Toast.makeText(this@QrCodeScannerActivity, "Barang Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        }
//                    val img = text[1]
//                    checkUserLevel(id, img)
//                    intent.putExtra("item_image", img)

                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "codeScanner: ${it.message}")
                }
            }

            binding.scn.setOnClickListener {
                codeScanner.startPreview()
            }

        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun checkUserLevel(id : String, img:String){
        val currentUser =fAuth.currentUser
        if (currentUser != null){
            val fStore = FirebaseFirestore.getInstance()
            val docReference =fStore.collection("user").document(currentUser.uid)
            docReference.get()
                .addOnSuccessListener { docSnapshot ->
                    if (docSnapshot != null){
                        val userAccess = docSnapshot.data?.get("accessLevel").toString()
                        when(userAccess){
                            "Admin" ->{
                                val intent = Intent(this@QrCodeScannerActivity, DetailAdminActivity::class.java)
                                intent.putExtra("item_Id", id)
                                intent.putExtra("item_image", img)
                                startActivity(intent)
                            }
                            "Gudang" -> {
                                val intent = Intent(this@QrCodeScannerActivity, DetailProductActivity::class.java)
                                intent.putExtra("item_Id", id)
                                intent.putExtra("item_image", img)
                                startActivity(intent)
                            }
                        }
                    }
                }
        }
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQ
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQ -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        this,
                        "You need the camera permission to use this app",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val CAMERA_REQ = 101
    }

}