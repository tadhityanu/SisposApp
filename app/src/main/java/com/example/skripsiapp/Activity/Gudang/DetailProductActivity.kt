package com.example.skripsiapp.Activity.Gudang

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.example.skripsiapp.Activity.Admin.MainActivity
import com.example.skripsiapp.DataModel.CartModel
import com.example.skripsiapp.DataModel.MonthlySoldStock
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.Helper.AlarmReceiver
import com.example.skripsiapp.R
import com.example.skripsiapp.databinding.ActivityDetailProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.snapshots
import com.google.firebase.database.ktx.values
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.squareup.picasso.Picasso
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.properties.Delegates

class DetailProductActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailProductBinding
    private lateinit var productName : TextView
    private lateinit var productPrice : TextView
    private lateinit var productCurrentQuantity : TextView
    private lateinit var productSoldQuantity : TextView
    private lateinit var productDesc : TextView
    private lateinit var btnForecastStock : CardView
    private lateinit var btnGenerateQr : CardView
    private lateinit var btnEditProduct : CardView
    private lateinit var btnBack : ImageView
    private lateinit var imgItem : ImageView


    private lateinit var dbItemReference : DatabaseReference
    private lateinit var dbCartReference : DatabaseReference
    private lateinit var fStore : FirebaseFirestore
    private lateinit var fAuth : FirebaseAuth
    lateinit var bitmap : Bitmap
    private lateinit var alarmReceiver: AlarmReceiver


    private lateinit var pId :String
    private lateinit var pName :String
    private lateinit var pPrice :String
    private lateinit var pSoldQuantity :String
    private lateinit var pFirstQuantity:String
    private lateinit var pImage :String
    private lateinit var pDesc :String



    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityDetailProductBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        //initialize
        fStore = Firebase.firestore
        fAuth = Firebase.auth
        dbItemReference = FirebaseDatabase.getInstance().getReference("item_data")
        dbCartReference = FirebaseDatabase.getInstance().getReference("item_cart")

        alarmReceiver = AlarmReceiver()

        productName = binding.txtProductName
        productPrice = binding.txtProductPrice
        productCurrentQuantity = binding.txtCurrentQuantity
        productSoldQuantity = binding.txtSoldQuantity
        productDesc = binding.txtProductDesc
        btnForecastStock = binding.cvForecast
        btnGenerateQr = binding.cvGenerateQr
        btnBack = binding.icBack
        btnEditProduct = binding.cvEditProduct
        imgItem = binding.imgItem

        //Data Intent
        pId = intent.getStringExtra("item_Id").toString()
        pFirstQuantity = intent.getStringExtra("item_first_quantity").toString()
        pImage = intent.getStringExtra("item_image").toString()
        pDesc = intent.getStringExtra("item_desc").toString()



        val currentUser = fAuth.currentUser
//        if (currentUser != null){
//            checkUserLevel(currentUser.uid)
//        }
        hideSaveButton()
        setView()
        setAction()
        saveMonthlySold()

//        reminder()
    }

    override fun onStart() {
        super.onStart()
        setView()
    }

//    private fun addToCart(){
//        val dbItem = dbItemReference.child(pId)
//        val dbCart = dbCartReference.child(pId)
//
//        dbCart.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                if (snapshot.exists()){
//                    val cartModel = snapshot.getValue(CartModel::class.java)
//
//                    dbItem.get().addOnSuccessListener {
//                        showLoading(false)
//                        cartModel!!.id = pId
//                        cartModel.name = cartModel.name
//                        cartModel.price = cartModel.price
//                        cartModel.quantity = cartModel.quantity + 1
//                        cartModel.totalPrice = cartModel.price!!.toInt() * cartModel.quantity
//                        dbCart.setValue(cartModel)
//                    }
//                            .addOnSuccessListener {
//                                showLoading(false)
//                                val pModel = ProductModel()
//                                pModel.id = pId
//                                pModel.itemName = productName.text.toString()
//                                pModel.itemPrice = productPrice.text.toString()
//                                pModel.itemFirstQuantity = pFirstQuantity.toInt()
//                                pModel.itemCurrentQuantity = productCurrentQuantity.text.toString().toInt() - 1
//                                pModel.itemDescription = productDesc.text.toString()
//                                pModel.itemImage = pImage
//                                dbItem.setValue(pModel)
//
//                                Toast.makeText(this@DetailProductActivity,"Berhasil menambah keranjang", Toast.LENGTH_LONG).show()
//                                finish()
//                            }
//
//                        .addOnFailureListener {
//                            Toast.makeText(this@DetailProductActivity,"Gagal menambah keranjang", Toast.LENGTH_LONG).show()
//                        }
//                } else {
//                    val cModel = CartModel()
//                    val priceSplit = productPrice.text.split("Rp. ")
//                    val intPrice = priceSplit[1].replace(".", "")
//
//                    cModel.id = pId
//                    cModel.name =productName.text.toString()
//                    cModel.price = intPrice
//                    cModel.quantity = 1
//                    cModel.totalPrice = cModel.price.toString().toInt()
//
//                    dbCart
//                        .setValue(cModel)
//                        .addOnSuccessListener {
//
//                            val pModel = ProductModel()
//                            pModel.id = pId
//                            pModel.itemName = productName.text.toString()
//                            pModel.itemPrice = productPrice.text.toString()
//                            pModel.itemFirstQuantity = pFirstQuantity.toInt()
//                            pModel.itemCurrentQuantity = productCurrentQuantity.text.toString().toInt() - 1
//                            pModel.itemDescription = productDesc.text.toString()
//                            pModel.itemImage = pImage
//
//                            dbItem.setValue(pModel)
//                            Toast.makeText(this@DetailProductActivity,"Berhasil menambah keranjang", Toast.LENGTH_LONG).show()
//                            finish()
//                        }
//                        .addOnFailureListener{
//                            Toast.makeText(this@DetailProductActivity,"Gagal menambah keranjang", Toast.LENGTH_LONG).show()
//                        }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//            }
//        })
//    }

//    private fun checkUserLevel(userId : String){
//        val docReference =fStore.collection("user").document(userId)
//        docReference.get()
//            .addOnSuccessListener { docSnapshot ->
//                if (docSnapshot != null){
//                    val userAccess = docSnapshot.data?.get("accessLevel").toString()
//                    when(userAccess){
//                        "Admin" ->{
//                            binding.btnAddCart.visibility = View.VISIBLE
//                            binding.cvEditProduct.visibility = View.GONE
//                        }
//                        "Gudang" -> {
//                            binding.btnAddCart.visibility = View.GONE
//                            binding.cvEditProduct.visibility = View.VISIBLE
//                        }
//                    }
//                }
//            }
//    }

    private fun setAction(){
        btnForecastStock.setOnClickListener {
            showLoading(true)
            forecastAction()
        }
        btnGenerateQr.setOnClickListener {
            showButtomSheet()
        }
        btnBack.setOnClickListener{
            finish()
        }
        btnEditProduct.setOnClickListener {
            showLoading(true)
            val intent = Intent(this, AddNewProductActivity::class.java)
            intent.putExtra("id", pId)
            intent.putExtra("name", productName.text)
            intent.putExtra("price", productPrice.text)
            intent.putExtra("currentQuantity", productCurrentQuantity.text)
            intent.putExtra("desc", productDesc.text)
            intent.putExtra("image", pImage)
            startActivity(intent)
            finish()
        }
    }

    private fun showButtomSheet(){
        val buttomSheetDialog =BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val buttomSheetView =LayoutInflater.from(applicationContext).inflate(
            R.layout.bottom_sheet_qr_code_layout,
            findViewById(R.id.buttom_sheet_qr_code)
        )

        val id = pId
        val code = id
        val encoder =BarcodeEncoder()
        bitmap = encoder.encodeBitmap(code, BarcodeFormat.QR_CODE, 500, 500)

        val image = buttomSheetView.findViewById<ImageView>(R.id.img_qr_code)
        val header = buttomSheetView.findViewById<TextView>(R.id.txt_header)

        header.text = productName.text
        image.setImageBitmap(bitmap)

        buttomSheetDialog.setContentView(buttomSheetView)
        buttomSheetDialog.show()
    }

    private fun showBottomSheetForecast(forecast:String, mape : String){
        val buttomSheetDialog =BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val buttomSheetView =LayoutInflater.from(applicationContext).inflate(
            R.layout.buttom_sheet_forecasting_layout,
            findViewById(R.id.buttom_sheet_qr_code)
        )

        val cardForecast = buttomSheetView.findViewById<CardView>(R.id.cv_forecasting)
        val cardMape = buttomSheetView.findViewById<CardView>(R.id.cv_mape)
        val forecastResult = buttomSheetView.findViewById<TextView>(R.id.txt_forecasting)
        val mapeResult = buttomSheetView.findViewById<TextView>(R.id.txt_mape)

        cardForecast.setBackgroundResource(R.drawable.bg_white_edt_text)
        cardMape.setBackgroundResource(R.drawable.bg_white_edt_text)
        forecastResult.setText(forecast)
        mapeResult.setText("$mape %")


        buttomSheetDialog.setContentView(buttomSheetView)
        buttomSheetDialog.show()
    }

    private fun setView(){
        val fStorage = FirebaseStorage.getInstance()
        val ref = fStorage.reference

        showLoading(true)
        dbItemReference.child(pId).get().addOnSuccessListener {
            if (it.exists()){

                val stockIn = it.child("itemFirstQuantity").value.toString().toInt()
                val currentStock = it.child("itemCurrentQuantity").value.toString().toInt()
                val soldQty = stockIn - currentStock

                showLoading(false)
                productName.text = it.child("itemName").value.toString()
                productPrice.text = it.child("itemPrice").value.toString()
                productCurrentQuantity.text = it.child("itemCurrentQuantity").value.toString()
                productDesc.text = it.child("itemDescription").value.toString()
                productSoldQuantity.text = it.child("sold_stock").value.toString()
            }
        }

        ref.child("img_item/${pImage}")
            .downloadUrl.addOnSuccessListener { uri->
                Picasso.get().load(uri).into(imgItem)
            }

    }

    private fun hideSaveButton(){
        val calendar = Calendar.getInstance()
        if (calendar.get(Calendar.DAY_OF_MONTH) == 28){
            val ref = FirebaseDatabase.getInstance().getReference("item_data")
            ref.child(pId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        val data = snapshot.getValue(ProductModel::class.java)!!
                        if (data.sold_stock == 0){
                            binding.cvSaveMonthly.visibility = View.GONE
                        } else{
                            binding.cvSaveMonthly.visibility = View.VISIBLE
                        }
                    }
                                   }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        } else{
            binding.cvSaveMonthly.visibility = View.GONE
        }
    }

    private fun saveMonthlySold(){
        binding.cvSaveMonthly.setOnClickListener{
            showLoading(true)
            val ref = FirebaseDatabase.getInstance().getReference("item_data")

            ref.child(pId).get().addOnSuccessListener {
                showLoading(false)
                if (it.child("sold_stock").value != null){
                    val soldStock = it.child("sold_stock").value.toString().toInt()

                    if (it.child("monthly_sold").value == null){
                        val monthlySoldQty = MonthlySoldStock()
                        val clear = 0
                        monthlySoldQty.stockMonth1 = soldStock

                        ref.child(pId).child("monthly_sold").setValue(monthlySoldQty)
                        ref.child(pId).child("sold_stock").setValue(clear)
                        Toast.makeText(this@DetailProductActivity, "Data Penjualan Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                    } else{
                        ref.child(pId).child("monthly_sold").addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    val updateData: MutableMap<String, Any> = HashMap()
                                    val item = snapshot.getValue(MonthlySoldStock::class.java)!!

                                    updateData["stockMonth1"] = soldStock
                                    updateData["stockMonth2"] =item.stockMonth1
                                    updateData["stockMonth3"] =item.stockMonth2
                                    updateData["stockMonth4"] =item.stockMonth3
                                    updateData["stockMonth5"] =item.stockMonth4
                                    updateData["stockMonth6"] =item.stockMonth5
                                    updateData["stockMonth7"] =item.stockMonth6
                                    updateData["stockMonth8"] =item.stockMonth7
                                    updateData["stockMonth9"] =item.stockMonth8
                                    updateData["stockMonth10"] =item.stockMonth9
                                    updateData["stockMonth11"] =item.stockMonth10

                                    ref.child(pId).child("monthly_sold").updateChildren(updateData)
                                    ref.child(pId).child("sold_stock").setValue(0)
                                    Toast.makeText(this@DetailProductActivity, "Data Penjualan Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }
                        })
                    }
                } else{
                    Toast.makeText(this, "Data Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    private fun forecastAction(){
        dbItemReference.child(pId).child("monthly_sold").get().addOnSuccessListener {
            showLoading(false)
            if (it.exists()){

            val stock1 = it.child("stockMonth1").value.toString().toDouble()
            val stock2 = it.child("stockMonth2").value.toString().toDouble()
            val stock3 = it.child("stockMonth3").value.toString().toDouble()
            val stock4 = it.child("stockMonth4").value.toString().toDouble()
            val stock5 = it.child("stockMonth5").value.toString().toDouble()
            val stock6 = it.child("stockMonth6").value.toString().toDouble()
            val stock7 = it.child("stockMonth7").value.toString().toDouble()
            val stock8 = it.child("stockMonth8").value.toString().toDouble()
            val stock9 = it.child("stockMonth9").value.toString().toDouble()
            val stock10 = it.child("stockMonth10").value.toString().toDouble()
            val stock11 = it.child("stockMonth11").value.toString().toDouble()

                when{

                    stock5 == 0.0 ->{
                        Toast.makeText(applicationContext, "Data Penjualan Masih di Bawah 5 Bulan", Toast.LENGTH_SHORT).show()
                    }
                    stock5 != 0.0 && stock6 == 0.0 && stock7 == 0.0 && stock8 == 0.0 && stock9 == 0.0 && stock10 == 0.0 && stock11 == 0.0->{
                        val alpha = 0.2
                        val predict2 = stock5
                        val predict3 = (alpha * stock4) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock3) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock2) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock1) + ((1-alpha)*predict5)

                        val mape2 = (stock4 - predict2)/stock4
                        val mape3 = (stock3 - predict3)/stock3
                        val mape4 = (stock2 - predict4)/stock2
                        val mape5 = (stock1 - predict5)/stock1

                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5))/4
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict6, mape[0])

                    }
                    stock6 != 0.0 && stock7 == 0.0 && stock8 == 0.0 && stock9 == 0.0 && stock10 == 0.0 && stock11 == 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock6
                        val predict3 = (alpha * stock5) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock4) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock3) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock2) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock1) + ((1-alpha)*predict6)

                        val mape2 = (stock5 - predict2)/stock5
                        val mape3 = (stock4 - predict3)/stock4
                        val mape4 = (stock3 - predict4)/stock3
                        val mape5 = (stock2 - predict5)/stock2
                        val mape6 = (stock1 - predict6)/stock1

                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6))/5
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict7, mape[0])
                    }
                    stock7 != 0.0 && stock8 == 0.0 && stock9 == 0.0 && stock10 == 0.0 && stock11 == 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock7
                        val predict3 = (alpha * stock6) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock5) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock4) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock3) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock2) + ((1-alpha)*predict6)
                        val predict8 = (alpha * stock1) + ((1-alpha)*predict7)

                        val mape2 = (stock6 - predict2)/stock6.absoluteValue
                        val mape3 = (stock5 - predict3)/stock5
                        val mape4 = (stock4 - predict4)/stock4
                        val mape5 = (stock3 - predict5)/stock3
                        val mape6 = (stock2 - predict6)/stock2
                        val mape7 = (stock1 - predict7)/stock1

                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6) + abs(mape7))/6
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict8, mape[0])

//                    binding.txtMape.text = mape[0] + "%"
//                    binding.txtPredict.text = predict8.toString()
                    }
                    stock8 != 0.0  && stock9 == 0.0 && stock10 == 0.0 && stock11 == 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock8
                        val predict3 = (alpha * stock7) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock6) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock5) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock4) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock3) + ((1-alpha)*predict6)
                        val predict8 = (alpha * stock2) + ((1-alpha)*predict7)
                        val predict9 = (alpha * stock1) + ((1-alpha)*predict8)

                        val mape2 = (stock7 - predict2)/stock7.absoluteValue
                        val mape3 = (stock6 - predict3)/stock6
                        val mape4 = (stock5 - predict4)/stock5
                        val mape5 = (stock4 - predict5)/stock4
                        val mape6 = (stock3 - predict6)/stock3
                        val mape7 = (stock2 - predict7)/stock2
                        val mape8 = (stock1 - predict8)/stock1


                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6) + abs(mape7) + abs(mape8))/7
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict9, mape[0])

                    }
                    stock9 != 0.0 && stock10 == 0.0 && stock11 == 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock9
                        val predict3 = (alpha * stock8) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock7) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock6) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock5) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock4) + ((1-alpha)*predict6)
                        val predict8 = (alpha * stock3) + ((1-alpha)*predict7)
                        val predict9 = (alpha * stock2) + ((1-alpha)*predict8)
                        val predict10 = (alpha * stock1) + ((1-alpha)*predict9)

                        val mape2 = (stock8 - predict2)/stock8
                        val mape3 = (stock7 - predict3)/stock7
                        val mape4 = (stock6 - predict4)/stock6
                        val mape5 = (stock5 - predict5)/stock5
                        val mape6 = (stock4 - predict6)/stock4
                        val mape7 = (stock3 - predict7)/stock3
                        val mape8 = (stock2 - predict8)/stock2
                        val mape9 = (stock1 - predict9)/stock1

                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6) + abs(mape7) + abs(mape8) + abs(mape9))/8
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict10, mape[0])

                    }
                    stock10 != 0.0 &&  stock11 == 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock10
                        val predict3 = (alpha * stock9) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock8) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock7) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock6) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock5) + ((1-alpha)*predict6)
                        val predict8 = (alpha * stock4) + ((1-alpha)*predict7)
                        val predict9 = (alpha * stock3) + ((1-alpha)*predict8)
                        val predict10 = (alpha * stock2) + ((1-alpha)*predict9)
                        val predict11 = (alpha * stock1) + ((1-alpha)*predict10)

                        val mape2 = (stock9 - predict2)/stock9
                        val mape3 = (stock8 - predict3)/stock8
                        val mape4 = (stock7 - predict4)/stock7
                        val mape5 = (stock6 - predict5)/stock6
                        val mape6 = (stock5 - predict6)/stock5
                        val mape7 = (stock4 - predict7)/stock4
                        val mape8 = (stock3 - predict8)/stock3
                        val mape9 = (stock2 - predict9)/stock2
                        val mape10 = (stock1 - predict10)/stock1

                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6) + abs(mape7) + abs(mape8) + abs(mape9) + abs(mape10))/9
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict11, mape[0])
                    }
                    stock11 != 0.0 ->{
                        val alpha = 0.2
                        val predict2 = stock11
                        val predict3 = (alpha * stock10) + ((1-alpha)*predict2)
                        val predict4 = (alpha * stock9) + ((1-alpha)*predict3)
                        val predict5 = (alpha * stock8) + ((1-alpha)*predict4)
                        val predict6 = (alpha * stock7) + ((1-alpha)*predict5)
                        val predict7 = (alpha * stock6) + ((1-alpha)*predict6)
                        val predict8 = (alpha * stock5) + ((1-alpha)*predict7)
                        val predict9 = (alpha * stock4) + ((1-alpha)*predict8)
                        val predict10 = (alpha * stock3) + ((1-alpha)*predict9)
                        val predict11 = (alpha * stock2) + ((1-alpha)*predict10)
                        val predict12 = (alpha * stock1) + ((1-alpha)*predict11)

                        val mape2 = (stock10 - predict2)/stock10
                        val mape3 = (stock9 - predict3)/stock9
                        val mape4 = (stock8 - predict4)/stock8
                        val mape5 = (stock7 - predict5)/stock7
                        val mape6 = (stock6 - predict6)/stock6
                        val mape7 = (stock5 - predict7)/stock5
                        val mape8 = (stock4 - predict8)/stock4
                        val mape9 = (stock3 - predict9)/stock3
                        val mape10 = (stock2 - predict10)/stock2
                        val mape11 = (stock1 - predict11)/stock1


                        val averageMape = (abs(mape2) + abs(mape3) + abs(mape4) + abs(mape5) + abs(mape6) + abs(mape7) + abs(mape8) + abs(mape9) + abs(mape10) + abs(mape11))/10
                        val mapeValue = averageMape*100
                        val mape = mapeValue.toString().split(".")

                        absoluteForecastResult(predict12, mape[0])
                    }
                }
            }else{
                showLoading(false)
                Toast.makeText(applicationContext, "Data Penjualan Masih di Bawah 5 Bulan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun absoluteForecastResult(number : Double, mape : String){
        val decimal = BigDecimal(number).setScale(1, RoundingMode.HALF_EVEN)
        val mapeValue = 100 - mape.toInt()
        showBottomSheetForecast(
            decimal.toString(),
            mapeValue.toString()
        )
    }

    private fun showLoading(state: Boolean) {
        if(state) {
            binding.pbMain.visibility = View.VISIBLE
        } else {
            binding.pbMain.visibility = View.GONE
        }
    }
}