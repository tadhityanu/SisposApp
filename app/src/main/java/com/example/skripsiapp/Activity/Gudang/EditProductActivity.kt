package com.example.skripsiapp.Activity.Gudang

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityEditProductBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class EditProductActivity : AppCompatActivity() {

    private lateinit var binding : ActivityEditProductBinding

    private lateinit var productName : EditText
    private lateinit var productPrice : EditText
    private lateinit var productStock : EditText
    private lateinit var productDesc : EditText
    private lateinit var btnUpdate : Button

    private lateinit var dbReference : DatabaseReference
    private lateinit var itemId : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        //initialize
        productName = binding.edtProductName
        productPrice = binding.edtProductPrice
        productStock = binding.edtProductFirstStock
        productDesc = binding.edtProductDesc
        btnUpdate = binding.btnSave

        setView()
        btnUpdate.setOnClickListener {
            showLoading(true)
            updateData()
//            saveSoldQuantity()
        }
        fieldSet()

    }

    private fun setView(){
        itemId = intent.getStringExtra("id").toString()
        val pName = intent.getStringExtra("name").toString()
        val pPrice = intent.getStringExtra("price").toString()
        val pDesc = intent.getStringExtra("desc").toString()


        productName.setText(pName)
        productPrice.setText(pPrice)
        productDesc.setText(pDesc)
    }

    private fun updateData(){
        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
        val pStock = intent.getStringExtra("currentQuantity").toString()
        val pImage = intent.getStringExtra("image").toString()

        dbReference.child(itemId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val data = snapshot.getValue(ProductModel::class.java)!!
                    val updateData : MutableMap<String, Any> = HashMap()

                    updateData["itemName"] = productName.text.toString()
                    updateData["itemPrice"] = productPrice.text.toString()
                    updateData["itemCurrentQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
                    updateData["itemFirstQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
                    updateData["itemDescription"] = productDesc.text.toString()

                    dbReference.child(itemId).updateChildren(updateData)
                        .addOnSuccessListener {
                            showLoading(false)
                            Toast.makeText(this@EditProductActivity, "Data Berhasil di Perbaharui", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this@EditProductActivity, "Data Gagal di Perbaharui", Toast.LENGTH_LONG).show()
                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

//        val pModel = ProductModel()
//        pModel.id = itemId
//        pModel.itemName = productName.text.toString()
//        pModel.itemPrice = productPrice.text.toString()
//        pModel.itemFirstQuantity = productStock.text.toString().toInt() + pStock.toInt()
//        pModel.itemCurrentQuantity = pModel.itemFirstQuantity
//        pModel.itemDescription = productDesc.text.toString()
//        pModel.itemImage = pImage
//        showLoading(true)
//
//        saveSoldQuantity()
//        dbReference.child(itemId).setValue(pModel)
//            .addOnSuccessListener {
//                productName.text.clear()
//                productPrice.text.clear()
//                productStock.text.clear()
//                productDesc.text.clear()
//                showLoading(false)
//                Toast.makeText(this, "Data Berhasil di Perbaharui", Toast.LENGTH_LONG).show()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Data Gagal di Perbaharui", Toast.LENGTH_LONG).show()
//            }
    }

//    private fun saveSoldQuantity(){
//        dbReference = FirebaseDatabase.getInstance().getReference("item_data")
//        val id = itemId
//
//        dbReference.child(id).get().addOnSuccessListener {
//            val itemIn = it.child("itemFirstQuantity").value.toString().toInt()
//            val currentItem = it.child("itemCurrentQuantity").value.toString().toInt()
//            val soldStock = itemIn - currentItem
//
//            if (it.child("sold_stock").value != null){
//                if (it.child("sold_stock").value.toString().toInt() == 0){
//                    dbReference.child(id).child("sold_stock")
//                        .setValue(soldStock)
//                } else {
//                    dbReference.child(id).child("sold_stock")
//                        .setValue(it.child("sold_stock").value.toString().toInt() + soldStock)
//                }
//            } else{
//                dbReference.child(id).child("sold_stock")
//                    .setValue(soldStock)
//            }
//        }
//    }

    private fun fieldSet(){
        productPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductPrice.error = null
            }

            override fun onTextChanged(char: CharSequence, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductPrice.error = null

                var setPriceText = productPrice.text.toString().trim()

                if (char.toString() == setPriceText){
                    productPrice.removeTextChangedListener(this)
                    val cleanString = char.toString().replace("[Rp. ]".toRegex(), "")
                    if (cleanString.isNotEmpty()){
                        val formated = NumberFormat.getCurrencyInstance(Locale("IND", "ID")).format((cleanString.toDouble()))
                        val split = formated.split(",")
                        val length = split[0].length
                        val currency = split[0].substring(0,2)+". "+split[0].substring(2,length)
                        setPriceText = currency
                    } else{
                        setPriceText = ""
                    }
                    productPrice.setText(setPriceText)
                    productPrice.setSelection(setPriceText.length)
                    productPrice.addTextChangedListener(this)
                }



            }

            override fun afterTextChanged(p0: Editable?) {

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