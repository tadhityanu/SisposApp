package com.example.skripsiapp.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ListView
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsiapp.Activity.Gudang.DetailProductActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ItemProductBinding
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MonthlySoldItemAdapter(var context : Context, var listItem : MutableList<ProductModel>) : RecyclerView.Adapter<MonthlySoldItemAdapter.ListViewHolder>() {

    inner class ListViewHolder(val binding : ItemProductBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item : ProductModel){
            val fStorage = FirebaseStorage.getInstance()
            val ref = fStorage.reference

            binding.apply {
                txtItemName.text = item.itemName
                txtItemPrice.text = item.itemPrice
                txtQuantity.text = item.sold_stock.toString()
                ref.child("img_item/${item.itemImage}")
                    .downloadUrl.addOnSuccessListener { uri->
                        Picasso.get().load(uri).into(imgItem)
                    }

                if (item.itemCurrentQuantity <= 5){
                    clLayout.setBackgroundResource(R.drawable.bg_gradient_red)
                } else{
                    clLayout.setBackgroundResource(R.drawable.bg_gradient_blue)
                }

                clLayout.setOnClickListener {
                    val intent = Intent(context, DetailProductActivity::class.java)
                    intent.putExtra("item_Id", item.id)
                    intent.putExtra("item_first_quantity", item.itemFirstQuantity.toString())
                    intent.putExtra("item_image", item.itemImage)
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val view = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (listItem.isEmpty()) 0
        else listItem.size
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        listItem.get(position).let { holder.bind(it) }
    }

}