package com.example.skripsiapp.Activity.Gudang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.adapter.MonthlySoldItemAdapter
import com.example.skripsiapp.databinding.ActivityListMonthlySoldItemBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ListMonthlySoldItemActivity : AppCompatActivity() {

    private lateinit var binding : ActivityListMonthlySoldItemBinding
    private lateinit var adapter : MonthlySoldItemAdapter
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var dbReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityListMonthlySoldItemBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
        productList = mutableListOf()

        showLoading(true)
        showItemList()

    }

    private fun showItemList(){
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val item = data.getValue(ProductModel::class.java)
                        if (item != null && item.sold_stock > 0){
                            productList.add(item)
                        }
                    }
                    if (productList.isNotEmpty()){
                        binding.emptyLayout.visibility = View.GONE
                        val rv = binding.rvLessItem
                        rv.setHasFixedSize(true)
                        rv.layoutManager = LinearLayoutManager(this@ListMonthlySoldItemActivity)
                        adapter = MonthlySoldItemAdapter(applicationContext, productList)
                        rv.adapter = adapter
                        showLoading(false)
                    }else{
                        showLoading(false)
                        binding.emptyLayout.visibility = View.VISIBLE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListMonthlySoldItemActivity, "Gagal memuat Data", Toast.LENGTH_LONG).show()
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