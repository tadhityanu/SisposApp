package com.example.skripsiapp.Activity.Gudang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.Activity.Admin.DetailAdminActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityLessProductBinding
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class LessProductActivity : AppCompatActivity() {

    private lateinit var productList : MutableList<ProductModel>
    private lateinit var adapter: ItemProductAdapter
    private lateinit var dbReference : DatabaseReference
    private lateinit var binding : ActivityLessProductBinding
    private lateinit var fAuth : FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLessProductBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        productList = mutableListOf()
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
        fAuth = Firebase.auth

        showItemList()
        buttonAction()
    }

    private fun buttonAction(){
        binding.icBack.setOnClickListener{
            finish()
        }
    }

    private fun showItemList(){
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val item = data.getValue(ProductModel::class.java)
                        if (item != null  && item.itemCurrentQuantity <= 5){
                            productList.add(item)
                            productList.sortBy { it.itemCurrentQuantity }
                        }
                    }
                    if (productList.isNotEmpty()){
                        binding.emptyLayout.visibility = View.GONE
                        val rv = binding.rvLessItem
                        rv.setHasFixedSize(true)
                        rv.layoutManager = LinearLayoutManager(this@LessProductActivity)
                        adapter = ItemProductAdapter(applicationContext, productList)
                        rv.adapter = adapter
                        showLoading(false)

                        adapter.setOnClickCallBack(object : ItemProductAdapter.OnItemClickCallBack {
                            override fun onItemClicked(data: ProductModel) {
                                showLoading(true)

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
                                                        val intent = Intent(this@LessProductActivity, DetailAdminActivity::class.java)
                                                        intent.putExtra("item_Id", data.id)
                                                        intent.putExtra("item_first_quantity", data.itemFirstQuantity.toString())
                                                        intent.putExtra("item_image", data.itemImage)
                                                        showLoading(false)
                                                        startActivity(intent)
                                                    }
                                                    "Gudang" -> {
                                                        val intent = Intent(this@LessProductActivity, DetailProductActivity::class.java)
                                                        intent.putExtra("item_Id", data.id)
                                                        intent.putExtra("item_first_quantity", data.itemFirstQuantity.toString())
                                                        intent.putExtra("item_image", data.itemImage)
                                                        showLoading(false)
                                                        startActivity(intent)
                                                    }
                                                }
                                            }
                                        }
                                }
                            }
                        })
                    } else{
                        showLoading(false)
                        binding.emptyLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LessProductActivity, "Gagal memuat Data", Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun showLoading(state: Boolean) {
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

}