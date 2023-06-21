package com.example.skripsiapp.Activity.Gudang

import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.Activity.LoginActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.Helper.AlarmReceiver
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityMainWarehouseBinding
import com.example.skripsiapp.databinding.ItemProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

class MainWarehouseActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainWarehouseBinding
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var fAuth : FirebaseAuth
    private lateinit var btnSignOut : CardView
    private lateinit var btnToLessProduct : CardView
    private lateinit var btnToAddNewProduct : CardView
    private lateinit var btnToMonitoringProduct : CardView
    private lateinit var dbReference : DatabaseReference
    private lateinit var adapter: ItemProductAdapter
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var searchList : MutableList<ProductModel>


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainWarehouseBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        fAuth = Firebase.auth

        btnSignOut = binding.btnSignOut
        btnToLessProduct = binding.cvProduct
        btnToAddNewProduct = binding.cvAddProduct
        btnToMonitoringProduct = binding.cvMonitoring


        hideMonitoringButton()
        setAlarm()

//        showItemList()
        buttonAction()
        profileSet()
    }

    override fun onStart() {
        super.onStart()
        showItemList()
        binding.searchView.clearFocus()
    }

    private fun setAlarm(){
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) == 28){
            setRepeatingAlarm()
        }
    }

    private fun hideMonitoringButton(){
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) == 28){
            btnToMonitoringProduct.visibility = View.VISIBLE
            binding.txtMonitoring.visibility = View.VISIBLE
        } else{
            btnToMonitoringProduct.visibility = View.GONE
            binding.txtMonitoring.visibility = View.GONE
        }
    }

    private fun profileSet(){
        val currentUser = fAuth.currentUser
        if (currentUser != null){
            binding.txtUsername.text = currentUser.displayName
        }
    }

    private fun buttonAction(){
        btnSignOut.setOnClickListener {
            fAuth.signOut()
            startActivity(Intent(applicationContext, LoginActivity::class.java))
            finish()
        }
        btnToAddNewProduct.setOnClickListener {
            startActivity(Intent(applicationContext, AddNewProductActivity::class.java))
        }
        btnToLessProduct.setOnClickListener {
            startActivity(Intent(applicationContext, LessProductActivity::class.java))
        }
        btnToMonitoringProduct.setOnClickListener{
            startActivity(Intent(applicationContext, MonitoringProductActivity::class.java))
        }

    }

    private fun showItemList(){

        productList = mutableListOf()
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
        showLoading(true)
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(false)
                if (snapshot.exists()){
                    for (data in snapshot.children){
                        val item = data.getValue(ProductModel::class.java)
                        if (item != null){
                            productList.add(item)
                            productList.sortBy {
                                it.itemName
                            }
                        }
                    }
                    binding.emptyLayout.visibility = View.GONE
                    binding.rvItem.setHasFixedSize(true)
                    binding.rvItem.layoutManager = LinearLayoutManager(this@MainWarehouseActivity)
                    adapter = ItemProductAdapter(applicationContext, productList)
                    binding.rvItem.adapter = adapter

                    binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            binding.searchView.clearFocus()
                            if (searchList.isEmpty()){
                                binding.emptyLayout.visibility = View.VISIBLE
                                Toast.makeText(applicationContext, "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                            }
                            return false
                        }

                        override fun onQueryTextChange(query: String): Boolean {
                            filterList(query)
                            return false
                        }
                    })

                } else {
                    binding.emptyLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }
        })

    }

    private fun filterList(query : String?){
        if (query != null){
            searchList = mutableListOf()
            for (i in productList){
                if (i.itemName?.toLowerCase(Locale.getDefault())!!.contains(query)){
                    searchList.add(i)
                }
            }
            if (searchList.isEmpty()){
                adapter.setFilteredList(mutableListOf())
                binding.emptyLayout.visibility = View.VISIBLE
            } else{
                binding.emptyLayout.visibility = View.GONE

                adapter.setFilteredList(searchList)
            }
        }
    }

    private fun setRepeatingAlarm(){
        alarmReceiver = AlarmReceiver()
        alarmReceiver.setRepeatingAlarm(this, AlarmReceiver.TYPE_REPEATING,
            "28", "Waktunya Monitoring Stok Barang. Lakukan Monitoring Sekarang!!")
    }

    private fun showLoading(state: Boolean) {
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

}