package com.example.skripsiapp.Activity.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Adapter
import android.widget.Button
import android.widget.SearchView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.Activity.Gudang.LessProductActivity
import com.example.skripsiapp.Activity.Gudang.MainWarehouseActivity
import com.example.skripsiapp.Activity.LoginActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var searchList : MutableList<ProductModel>
    private lateinit var adapter : ItemProductAdapter
    private lateinit var dbReference : DatabaseReference

    private lateinit var btnSignout : CardView
    private lateinit var btnLessProduct : CardView
    private lateinit var btnCart : CardView
    private lateinit var btnQrScaner : CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        fAuth = Firebase.auth
        fStore = Firebase.firestore

        btnSignout = binding.btnSignOut
        btnLessProduct = binding.cvProduct
        btnCart = binding.cvCart
        btnQrScaner = binding.cvQrScanner


        buttonAction()
//        setProductList()
        showLoading(true)
        profilSet()
    }

    override fun onStart() {
        super.onStart()
        showLoading(true)
        setProductList()
        profilSet()
    }

    override fun onResume() {
        super.onResume()
        binding.searchView.clearFocus()
    }

    private fun profilSet(){
        val currentUser =fAuth.currentUser
        if (currentUser != null){
            binding.txtUsername.text = currentUser.displayName
        }
    }

    private fun setProductList(){
        productList = mutableListOf()
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val item = data.getValue(ProductModel::class.java)
                        if (item != null){
                            productList.add(item)
                            productList.sortBy {
                                it.itemCurrentQuantity
                            }
                        }
                    }
                    showLoading(false)
                    binding.imgEmpty.visibility = View.GONE
                    binding.rvItem.layoutManager = LinearLayoutManager(this@MainActivity)
                    binding.rvItem.setHasFixedSize(true)
                    adapter = ItemProductAdapter(applicationContext, productList)
                    binding.rvItem.adapter = adapter

                    binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            binding.searchView.clearFocus()
                            if (searchList.isEmpty()){
                                binding.imgEmpty.visibility = View.VISIBLE
                                Toast.makeText(applicationContext, "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                            }
                            return false
                        }

                        override fun onQueryTextChange(query: String): Boolean {
                            filterList(query)
                            return true
                        }
                    })

                } else{
                    binding.imgEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal Memuat Data", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun filterList(query : String?){
        if (query != null) {
            searchList = mutableListOf()
            for (i in productList) {
                if (i.itemName?.toLowerCase(Locale.getDefault())!!.contains(query)) {
                    searchList.add(i)
                }
            }
            if (searchList.isEmpty()) {
                adapter.setFilteredList(mutableListOf())
                binding.imgEmpty.visibility = View.VISIBLE
            } else {
                adapter.setFilteredList(searchList)
                binding.imgEmpty.visibility = View.GONE
            }
        }
    }

    private fun buttonAction(){
        btnSignout.setOnClickListener {
            fAuth.signOut()
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }
        btnLessProduct.setOnClickListener {
            val intent = Intent(this, LessProductActivity::class.java)
            startActivity(intent)
        }
        btnCart.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }
        btnQrScaner.setOnClickListener {
            val intent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun showLoading(state:Boolean){
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

}