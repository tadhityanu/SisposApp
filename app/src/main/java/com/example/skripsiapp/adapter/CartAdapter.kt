package com.example.skripsiapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsiapp.DataModel.CartModel
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.databinding.ItemCartBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    var context:Context,
    var cartList :MutableList<CartModel>
    ) : RecyclerView.Adapter<CartAdapter.ListViewHolder>(){

        inner class ListViewHolder(val binding : ItemCartBinding):RecyclerView.ViewHolder(binding.root){
            fun bind(data : CartModel){
                binding.apply {
                    txtItemName.text = data.name

                    val localeId = Locale("in", "ID")
                    val rupiahFormat = NumberFormat.getCurrencyInstance(localeId)
                    val rupiahPrice = rupiahFormat.format(data.price?.toDouble())
                    val price = rupiahPrice.split(",")
                    txtPriceName.text  = price[0]

                    btnAdd.setOnClickListener {
                        addItem(binding, cartList[position])
                        updateDbItemMin(cartList[position])
                    }

                    btnMin.setOnClickListener {
                        minItem(binding, cartList[position])
                        updateDbItemAdd(cartList[position])
                    }

                    txtCartQuantity.text = data.quantity.toString()

                    FirebaseDatabase.getInstance().getReference("item_cart").child(data.id!!).addValueEventListener(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    if (data.quantity < 1){
                                        binding.root.removeAllViews()
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                }

            }
        }

    private fun updateFirebase(cartModel : CartModel){
        FirebaseDatabase.getInstance()
            .getReference("item_cart")
            .child(cartModel.id!!)
            .setValue(cartModel)
    }

    private fun removeItemValue(cartModel: CartModel){
        FirebaseDatabase.getInstance()
            .getReference("item_cart")
            .child(cartModel.id!!)
            .removeValue()
    }

    private fun addItem(binding : ItemCartBinding, cartItem : CartModel){
        val dbBarang = FirebaseDatabase.getInstance()
            .getReference("item_data")
        dbBarang.child(cartItem.id!!).get().addOnSuccessListener {
            if (it.child("itemCurrentQuantity").value.toString().toInt() > 0 ){
                cartItem.quantity = cartItem.quantity + 1
                cartItem.totalPrice = cartItem.price!!.toInt() * cartItem.quantity
                binding.txtCartQuantity.text = cartItem.quantity.toString()
                updateFirebase(cartItem)
            } else{
                Toast.makeText(context, "Stock Barang Sudah Habis", Toast.LENGTH_LONG).show()
            }
        }

    }
    private fun minItem(binding : ItemCartBinding, cartItem : CartModel){
        if (cartItem.quantity > 0){
            cartItem.quantity = cartItem.quantity - 1
            cartItem.totalPrice = cartItem.price!!.toInt() * cartItem.quantity
            binding.txtCartQuantity.text =cartItem.quantity.toString()
            updateFirebase(cartItem)
        } else if (cartItem.quantity == 0){
            removeItemValue(cartItem)
            binding.root.removeAllViews()
        }
    }

    private fun updateDbItemMin(cartModel : CartModel){
        val dbBarang = FirebaseDatabase.getInstance()
            .getReference("item_data")
        dbBarang.child(cartModel.id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val updateData: MutableMap<String, Any> = HashMap()
                    val barangModel = snapshot.getValue(ProductModel::class.java)!!

                    if (barangModel.itemCurrentQuantity > 0){
                        updateData["itemCurrentQuantity"] = barangModel.itemCurrentQuantity - 1
//                        updateData["sold_stock"] = barangModel.monthlyStockQuantity + 1
                        dbBarang.child(cartModel.id!!)
                            .updateChildren(updateData)
                    } else{
                        Toast.makeText(context, "Stock Barang Sudah Habis", Toast.LENGTH_LONG).show()
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateDbItemAdd(cartModel : CartModel){
        val dbBarang = FirebaseDatabase.getInstance()
            .getReference("item_data")
        dbBarang.child(cartModel.id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    if (cartModel.quantity >= 0){
                        val updateData: MutableMap<String, Any> = HashMap()
                        val barangModel = snapshot.getValue(ProductModel::class.java)!!
                        updateData["itemCurrentQuantity"] = barangModel.itemCurrentQuantity + 1
//                        updateData["sold_stock"] = barangModel.monthlyStockQuantity - 1
                        dbBarang.child(cartModel.id!!)
                            .updateChildren(updateData)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view =ItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (cartList.isEmpty()) 0
        else cartList.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        cartList.get(position).let {
            holder.bind(it)
        }
    }
}