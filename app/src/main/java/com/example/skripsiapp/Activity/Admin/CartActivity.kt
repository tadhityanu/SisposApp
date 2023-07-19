package com.example.skripsiapp.Activity.Admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.size
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsiapp.DataModel.CartModel
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.adapter.CartAdapter
import com.example.skripsiapp.adapter.ItemProductAdapter
import com.example.skripsiapp.databinding.ActivityCartBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class CartActivity : AppCompatActivity() {

    private lateinit var binding : ActivityCartBinding
    private lateinit var dbReference : DatabaseReference
    private lateinit var dbItem : DatabaseReference
    private lateinit var cartList : MutableList<CartModel>
    private lateinit var productList : MutableList<ProductModel>
    private lateinit var pModel : ProductModel
    private lateinit var cModel : CartModel
    private lateinit var adapter : CartAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityCartBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        //initialize
        dbReference = FirebaseDatabase.getInstance().getReference("item_cart")
        dbItem = FirebaseDatabase.getInstance().getReference("item_data")
        cartList = mutableListOf()
        productList = mutableListOf()
        pModel = ProductModel()
        cModel = CartModel()

        getCartList()
        buttonAction()
    }

    override fun onDestroy() {
        super.onDestroy()
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (item in snapshot.children){
                        val cData = item.getValue(CartModel::class.java)
                        if (cData!!.quantity < 1){
                            dbReference.child(cData.id!!).removeValue()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun buttonAction(){
        binding.icBack.setOnClickListener{
            finish()
        }
        binding.btnCheckout.setOnClickListener{
            showCheckoutDialog()
        }
        binding.cvToScanner.setOnClickListener{
            val intent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(intent)

        }
    }

    private fun showCheckoutDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Transaksi Produk")
        builder.setMessage("Apakah Anda Yakin Melakukan Transaksi?")

        builder.setPositiveButton("Bayar"){dialog, which->
            checkoutAction()
            Toast.makeText(this, "Transaksi Berhasil di Lakukan", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Batal"){dialog, which ->
            Toast.makeText(this, "Transaksi Batal di Lakukan", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()

    }

    private fun checkoutAction(){
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    dbReference.removeValue()
                    binding.rvItem.visibility = View.GONE
                    binding.emptyLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun getCartList(){
        showLoading(true)
        dbReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (item in snapshot.children){
                        val cData = item.getValue(CartModel::class.java)
                            cData?.id = item.key
                            cartList.add(cData!!)
                    }
                    showLoading(false)
                    binding.rvItem.setHasFixedSize(true)
                    binding.rvItem.layoutManager = LinearLayoutManager(this@CartActivity)
                    adapter = CartAdapter(applicationContext, cartList)
                    binding.rvItem.adapter = adapter

                    countTotalPrice(cartList)
                }else{
                    showLoading(false)
                    binding.emptyLayout.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                showLoading(false)
            }
        })
    }

    private fun countTotalPrice(cart : List<CartModel>){
        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                showLoading(true)
                var sum = 0
                if (snapshot.exists()){
                    for (cartItem in cart){
                        sum += cartItem.totalPrice
                    }
                } else{
                    sum
                }
                val localeId = Locale("in", "ID")
                val rupiahFormat = NumberFormat.getCurrencyInstance(localeId)
                val rupiahPrice = rupiahFormat.format(sum.toDouble())
                val splitprice = rupiahPrice.split(",")
                showLoading(false)
                binding.txtTotalPrice.text = splitprice[0]

            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun showLoading(state:Boolean){
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }

}