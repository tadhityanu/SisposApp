package com.example.skripsiapp.Activity.Gudang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.Data
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityLessProductBinding
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LessProductActivity : AppCompatActivity() {

    private lateinit var productList : MutableList<ProductModel>
    private lateinit var adapter: ItemProductAdapter
    private lateinit var dbReference : DatabaseReference
    private lateinit var binding : ActivityLessProductBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLessProductBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        productList = mutableListOf()
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")

        showLoading(true)
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