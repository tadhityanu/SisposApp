package com.example.skripsiapp.Activity.Admin

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.skripsiapp.DataModel.CartModel
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityDetailAdminBinding
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
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class DetailAdminActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailAdminBinding
    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth
    private lateinit var dbItemReference : DatabaseReference
    private lateinit var dbCartReference : DatabaseReference

    lateinit var bitmap : Bitmap

    private lateinit var pId :String
    private lateinit var pName :String
    private lateinit var pPrice :String
    private lateinit var pSoldQuantity :String
    private lateinit var pFirstQuantity:String
    private lateinit var pImage :String
    private lateinit var pDesc :String

    private lateinit var productName : TextView
    private lateinit var productPrice : TextView
    private lateinit var productCurrentQuantity : TextView
    private lateinit var productSoldQuantity : TextView
    private lateinit var productDesc : TextView
    private lateinit var btnScanner : Button
    private lateinit var btnAddCart : ImageView
    private lateinit var btnBack : ImageView
    private lateinit var imgItem : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailAdminBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        initialize()

        setView()
        buttonAction()

    }

    override fun onStart() {
        super.onStart()
        setView()
    }

    override fun onDestroy() {
        super.onDestroy()
        finish()
    }

    private fun initialize(){
        //initialize
        fStore = Firebase.firestore
        fAuth = Firebase.auth
        dbItemReference = FirebaseDatabase.getInstance().getReference("item_data")
        dbCartReference = FirebaseDatabase.getInstance().getReference("item_cart")

        pId = intent.getStringExtra("item_Id").toString()

        pFirstQuantity = intent.getStringExtra("item_first_quantity").toString()
//        pImage = intent.getStringExtra("item_image").toString()
        pImage = ""
        pDesc = intent.getStringExtra("item_desc").toString()

        productName = binding.txtProductName
        productPrice = binding.txtProductPrice
        productCurrentQuantity = binding.txtCurrentQuantity
        productSoldQuantity = binding.txtSoldQuantity
        productDesc = binding.txtProductDesc
        btnBack = binding.icBack
        imgItem = binding.imgItem
        btnScanner = binding.btnScanner
        btnAddCart = binding.btnAddCart
    }

    private fun setView(){
        val fStorage = FirebaseStorage.getInstance()
        val ref = fStorage.reference

        showLoading(true)
        dbItemReference.child(pId).get().addOnSuccessListener {
            if (it.exists()){

                val image = it.child("itemImage").value.toString()
                val firstQty = it.child("itemFirstQuantity").value.toString().toInt()
                val currentQty = it.child("itemCurrentQuantity").value.toString().toInt()

//                val soldQty = it.child("sold_stock").value.toString().toInt()

                showLoading(false)
                productName.text = it.child("itemName").value.toString()
                productPrice.text = it.child("itemPrice").value.toString()
                productCurrentQuantity.text = it.child("itemCurrentQuantity").value.toString()
                productDesc.text = it.child("itemDescription").value.toString()
                productSoldQuantity.text = (firstQty - currentQty).toString()

                ref.child("img_item/${image}").downloadUrl
                    .addOnSuccessListener { uri->
                        Picasso.get().load(uri).into(imgItem)
                    }
            }
        }



    }

    private fun buttonAction(){
        binding.icBack.setOnClickListener{
            finish()
        }
        btnScanner.setOnClickListener {
            val intent = Intent(this, QrCodeScannerActivity::class.java)
            startActivity(intent)
            Intent.FLAG_ACTIVITY_CLEAR_TOP
            finish()
        }
        btnAddCart.setOnClickListener{
            showLoading(true)
            addToCart()
        }
    }

    private fun addToCart(){
        val dbItem = dbItemReference.child(pId)
        val dbCart = dbCartReference.child(pId)

        dbCart.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val cartModel = snapshot.getValue(CartModel::class.java)
                    dbItem.get().addOnSuccessListener {
                        showLoading(false)
                        cartModel!!.id = pId
                        cartModel.name = cartModel.name
                        cartModel.price = cartModel.price
                        cartModel.quantity = cartModel.quantity + 1
                        cartModel.totalPrice = cartModel.price!!.toInt() * cartModel.quantity
                        dbCart.setValue(cartModel)
                    }
                        .addOnSuccessListener {
                            dbItem.get().addOnSuccessListener {
                                val firstQuantity = it.child("itemFirstQuantity").value.toString().toInt()
                                val image = it.child("itemImage").value.toString()

                                dbItem.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()){
                                            val updateData: MutableMap<String, Any> = HashMap()
                                            val data = snapshot.getValue(ProductModel::class.java)

                                            updateData["id"] = pId
                                            updateData["itemName"] = productName.text.toString()
                                            updateData["itemPrice"] = productPrice.text.toString()
                                            updateData["itemFirstQuantity"] = firstQuantity
                                            updateData["itemCurrentQuantity"] = productCurrentQuantity.text.toString().toInt() - 1
                                            updateData["itemDescription"] = productDesc.text.toString()
                                            updateData["itemImage"] = image

                                            dbItem.updateChildren(updateData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@DetailAdminActivity,"Berhasil menambah keranjang", Toast.LENGTH_LONG).show()
                                                    showLoading(false)
                                                    updateQuantityView()
                                                }
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@DetailAdminActivity,"Gagal menambah keranjang", Toast.LENGTH_LONG).show()
                                    }
                                })
                            }

                        }

                        .addOnFailureListener {
                            Toast.makeText(this@DetailAdminActivity,"Gagal menambah keranjang", Toast.LENGTH_LONG).show()
                        }
                } else {
                    val cModel = CartModel()
                    val priceSplit = productPrice.text.split("Rp. ")
                    val intPrice = priceSplit[1].replace(".", "")

                    cModel.id = pId
                    cModel.name =productName.text.toString()
                    cModel.price = intPrice
                    cModel.quantity = 1
                    cModel.totalPrice = cModel.price.toString().toInt()

                    dbCart
                        .setValue(cModel)
                        .addOnSuccessListener {
                            dbItem.get().addOnSuccessListener {
                                val productModel = it.child("itemFirstQuantity").value.toString().toInt()
                                val image = it.child("itemImage").value.toString()

                                dbItem.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()){
                                            val updateData: MutableMap<String, Any> = HashMap()
                                            val data = snapshot.getValue(ProductModel::class.java)

                                            updateData["id"] = pId
                                            updateData["itemName"] = productName.text.toString()
                                            updateData["itemPrice"] = productPrice.text.toString()
                                            updateData["itemFirstQuantity"] = productModel
                                            updateData["itemCurrentQuantity"] = productCurrentQuantity.text.toString().toInt() - 1
                                            updateData["itemDescription"] = productDesc.text.toString()
                                            updateData["itemImage"] = image

                                            dbItem.updateChildren(updateData)
                                                .addOnSuccessListener {
                                                    Toast.makeText(this@DetailAdminActivity,"Berhasil menambah keranjang", Toast.LENGTH_LONG).show()
                                                    showLoading(false)
                                                    updateQuantityView()
                                                }
                                        }

                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        TODO("Not yet implemented")
                                    }
                                })
                            }
                        }
                        .addOnFailureListener{
                            Toast.makeText(this@DetailAdminActivity,"Gagal menambah keranjang", Toast.LENGTH_LONG).show()
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun updateQuantityView(){
        val dbItem = dbItemReference.child(pId)

        dbItem.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val pModel = snapshot.getValue(ProductModel::class.java)!!
                    productCurrentQuantity.text = pModel.itemCurrentQuantity.toString()
                    productSoldQuantity.text = (pModel.itemFirstQuantity - pModel.itemCurrentQuantity).toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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