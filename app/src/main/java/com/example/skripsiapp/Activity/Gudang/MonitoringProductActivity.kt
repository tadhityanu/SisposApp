package com.example.skripsiapp.Activity.Gudang

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityMonitoringProductBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MonitoringProductActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMonitoringProductBinding
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var adapter: ItemProductAdapter
    private lateinit var dbReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMonitoringProductBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        productList = mutableListOf()
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")

        showLoading(true)
        buttonAction()
//        showItemList()
    }

    override fun onStart() {
        super.onStart()
        showItemList()
    }

    override fun onPause() {
        super.onPause()
        productList.clear()
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
                        if (item != null && item.stockQuantity != 0){
                            productList.add(item)
                        }
                    }
                    if (productList.isNotEmpty()){
                        binding.emptyLayout.visibility = View.GONE
                        val rv = binding.rvMonitoringStock
                        rv.setHasFixedSize(true)
                        rv.layoutManager = LinearLayoutManager(this@MonitoringProductActivity)
                        adapter = ItemProductAdapter(applicationContext, productList)
                        rv.adapter = adapter
                        showLoading(false)

                        adapter.setOnClickCallBack(object : ItemProductAdapter.OnItemClickCallBack {
                            override fun onItemClicked(data: ProductModel) {
                                showLoading(true)
                                val intent = Intent(this@MonitoringProductActivity, DetailProductActivity::class.java)
                                intent.putExtra("item_Id", data.id)
                                intent.putExtra("item_first_quantity", data.itemFirstQuantity.toString())
                                intent.putExtra("item_image", data.itemImage)
                                startActivity(intent)
                            }
                        })

                    } else{
                        showLoading(false)
                        binding.emptyLayout.visibility = View.VISIBLE
                        binding.rvMonitoringStock.visibility = View.GONE
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MonitoringProductActivity, "Gagal memuat Data", Toast.LENGTH_LONG).show()
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