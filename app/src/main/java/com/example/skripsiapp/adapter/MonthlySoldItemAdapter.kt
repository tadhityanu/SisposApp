package com.example.skripsiapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsiapp.Activity.Gudang.DetailProductActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ItemProductBinding
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class MonthlySoldItemAdapter(var context : Context, var listItem : MutableList<ProductModel>) : RecyclerView.Adapter<MonthlySoldItemAdapter.ListViewHolder>() {


    private lateinit var onItemClickCallBack: OnItemClickCallBack

    fun setOnClickCallBack(onItemClickCallBack: OnItemClickCallBack){
        this.onItemClickCallBack = onItemClickCallBack
    }
    inner class ListViewHolder(val binding : ItemProductBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(item : ProductModel){
            val fStorage = FirebaseStorage.getInstance()
            val ref = fStorage.reference

            binding.apply {
                val name = item.itemName.toString().split(" ")
                txtItemName.text = "${name[0]} ${name[1]}..."
                txtItemPrice.text = item.itemPrice
                txtQuantity.text = item.stockQuantity.toString()
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
        listItem.get(position).let {
            holder.bind(it)
            holder.itemView.setOnClickListener {
                onItemClickCallBack.onItemClicked(listItem[holder.adapterPosition])
            }
        }
    }

    interface OnItemClickCallBack {
        fun onItemClicked (data:ProductModel)
    }

}