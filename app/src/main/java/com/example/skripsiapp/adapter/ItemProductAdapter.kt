package com.example.skripsiapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.skripsiapp.Activity.Admin.DetailAdminActivity
import com.example.skripsiapp.Activity.Gudang.DetailProductActivity
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ItemProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ItemProductAdapter(var context : Context, var listItem : MutableList<ProductModel>):RecyclerView.Adapter<ItemProductAdapter.ListViewHolder>() {

    private lateinit var onItemClickCallBack: OnItemClickCallBack

    fun setOnClickCallBack(onItemClickCallBack: OnItemClickCallBack){
        this.onItemClickCallBack = onItemClickCallBack
    }
    fun setFilteredList(list : MutableList<ProductModel>){
        this.listItem = list
        notifyDataSetChanged()
    }

    private lateinit var fAuth : FirebaseAuth

    inner class ListViewHolder(val binding: ItemProductBinding) : RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(item : ProductModel){
            val fStorage = FirebaseStorage.getInstance()
            fAuth = Firebase.auth
            val ref = fStorage.reference

            binding.apply {
                val name = item.itemName.toString().split(" ")
                txtItemName.text = item.itemName
                txtItemPrice.text = item.itemPrice
                txtQuantity.text = item.itemCurrentQuantity.toString()
                ref.child("img_item/${item.itemImage}")
                    .downloadUrl.addOnSuccessListener { uri->
                        Picasso.get().load(uri).into(imgItem)
                    }

                if (item.itemCurrentQuantity <= 5){
                    clLayout.setBackgroundResource(R.drawable.bg_gradient_red)
                } else{
                    clLayout.setBackgroundResource(R.drawable.bg_gradient_blue)
                }

//                clLayout.setOnClickListener {
//                    onItemClickCallBack.onItemClicked(listItem[position])
//                    val currentUser =fAuth.currentUser
//                    if (currentUser != null){
//                        val fStore = FirebaseFirestore.getInstance()
//                        val docReference =fStore.collection("user").document(currentUser.uid)
//                        docReference.get()
//                            .addOnSuccessListener { docSnapshot ->
//                                if (docSnapshot != null){
//                                    val userAccess = docSnapshot.data?.get("accessLevel").toString()
//                                    when(userAccess){
//                                        "Admin" ->{
//                                            val intent = Intent(itemView.context, DetailAdminActivity::class.java)
//                                            intent.putExtra("item_Id", item.id)
//                                            intent.putExtra("item_first_quantity", item.itemFirstQuantity.toString())
//                                            intent.putExtra("item_image", item.itemImage)
//                                            itemView.context.startActivity(intent)
//                                        }
//                                        "Gudang" -> {
//                                            val intent = Intent(itemView.context, DetailProductActivity::class.java)
//                                            intent.putExtra("item_Id", item.id)
//                                            intent.putExtra("item_first_quantity", item.itemFirstQuantity.toString())
//                                            intent.putExtra("item_image", item.itemImage)
//                                            itemView.context.startActivity(intent)
//                                        }
//                                    }
//                                }
//                            }
//                    }
////---------------------------------------------------------------------------------------------------------------------//
//                }
            }

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemProductAdapter.ListViewHolder {
        val view = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemProductAdapter.ListViewHolder, position: Int) {
        listItem.get(position).let {
            holder.bind(it)
        }
        holder.itemView.setOnClickListener {
            onItemClickCallBack.onItemClicked(listItem[holder.adapterPosition])
        }
    }

    override fun getItemCount(): Int {
        return if (listItem.isEmpty()) 0
        else listItem.size
    }

    interface OnItemClickCallBack {
        fun onItemClicked (data:ProductModel)
    }

}