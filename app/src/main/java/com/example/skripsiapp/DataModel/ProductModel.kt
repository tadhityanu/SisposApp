package com.example.skripsiapp.DataModel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


class ProductModel{
        var id : String? = ""
        var itemName : String? = ""
        var itemPrice : String? = ""
        var itemFirstQuantity : Int = 0
        var itemCurrentQuantity : Int = 0
        var itemDescription : String? = ""
        var itemImage : String? = ""
        var sold_stock : Int = 0
}