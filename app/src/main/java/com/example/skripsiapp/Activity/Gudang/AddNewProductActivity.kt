package com.example.skripsiapp.Activity.Gudang

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.skripsiapp.DataModel.ProductModel
import com.example.skripsiapp.databinding.ActivityAddNewProductBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.NumberFormat
import java.util.Locale

class AddNewProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddNewProductBinding
    private lateinit var productName : EditText
    private lateinit var productPrice : EditText
    private lateinit var productStock : EditText
    private lateinit var productDesc : EditText

    private lateinit var itemId : String
    private lateinit var idFromIntent : String
    private lateinit var imgFromIntent : String

    private var getFile : File? = null
    private var product : ProductModel? = null

    companion object {
        const val CAMERA_X_RESULT = 200

        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
        private const val STOCK_QUANTITY = "stockQuantity"

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionGranted()) {
                Toast.makeText(
                    this,
                    "Tidak mendapatkan permission.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAddNewProductBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        actionBar?.hide()
        supportActionBar?.hide()

        // INITIALIZE
        productName = binding.edtProductName
        productPrice = binding.edtProductPrice
        productStock = binding.edtProductFirstStock
        productDesc = binding.edtProductDesc

        //camera permission
        if (!allPermissionGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        fieldSetting()
        hideError()
        buttonAction()

        idFromIntent = intent.getStringExtra("id").toString()
        imgFromIntent = intent.getStringExtra("image").toString()

        if (idFromIntent != null){
            getDataItem(idFromIntent)
        } else{
            getDataItem(idFromIntent)
        }
    }

    private fun getDataItem(id : String){
        val fDatabase = FirebaseDatabase.getInstance().getReference("item_data")
        fDatabase.child(id).get().addOnSuccessListener {
            if (it.exists()){
                val itemName = it.child("itemName").value.toString()
                val itemPrice = it.child("itemPrice").value.toString()
                val itemDesc = it.child("itemDescription").value.toString()
                val itemimage = it.child("itemImage").value.toString()
                val stock = "0"

                val fStorage = FirebaseStorage.getInstance()
                val ref = fStorage.reference

                ref.child("img_item/${imgFromIntent}")
                    .downloadUrl.addOnSuccessListener { uri->
                        Picasso.get().load(uri).into(binding.imgProductPreview)
                    }

                productName.setText(itemName)
                productPrice.setText(itemPrice)
                productDesc.setText(itemDesc)
                productStock.setText(stock)

            } else{
                productName.text.clear()
                productPrice.text.clear()
                productDesc.text.clear()
                productStock.text.clear()

            }
        }
    }

    private fun startCamera(){
        val intent = Intent(this, CameraActivity::class.java)
        launcherCamera.launch(intent)
    }

    @Suppress("DEPRECATION")
    private val launcherCamera = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ){
        if (it.resultCode == CAMERA_X_RESULT) {
            val myFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.data?.getSerializableExtra("picture", File::class.java)
            } else {
                @Suppress("DEPRECATION")
                it.data?.getSerializableExtra("picture")
            } as? File
            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            myFile?.let { file ->
                binding.imgProductPreview.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
            getFile = myFile
//            binding.btnSave.setOnClickListener{
//                showLoading(true)
//                uploadImage(BitmapFactory.decodeFile(getFile?.path))
//            }
        }
    }

    private fun buttonAction(){
        binding.btnCamera.setOnClickListener{
            startCamera()
        }
        binding.icBack.setOnClickListener{
            finish()
        }
    }

    private fun fieldSetting(){
        binding.btnSave.setOnClickListener {
            showLoading(true)
            when{
                productName.text.isEmpty() ->{
                    showLoading(false)
                    binding.layoutProductName.error = "Nama produk masih kosong"
                }
                productPrice.text.isEmpty() -> {
                    showLoading(false)
                    binding.layoutProductPrice.error = "Harga produk masih kosong"
                }
                productStock.text.isEmpty() -> {
                    showLoading(false)
                    binding.layoutProductFirstStock.error = "Stock produk masuk masih kosong"
                }
                productDesc.text.isEmpty() -> {
                    showLoading(false)
                    binding.layoutProductDesc.error = "Deskripsi produk harus di isi"
                }
                else ->{
                    if (getFile != null) {
                        uploadImage(BitmapFactory.decodeFile(getFile?.path))
                    } else{
                        if (idFromIntent != null){
                            showLoading(false)
                            uploadData(imgFromIntent)
                        } else{
                            showLoading(false)
                            Toast.makeText(applicationContext, "Ambil foto dulu", Toast.LENGTH_SHORT).show()
                        }
                    }

                }
            }
        }
    }

    private fun uploadImage(imgBitmap : Bitmap){
        val baos = ByteArrayOutputStream()
        val fStorage = FirebaseStorage.getInstance().reference.child("img_item/${getFile?.name}")
        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val img = baos.toByteArray()

        fStorage.putBytes(img)
            .addOnSuccessListener {
                showLoading(false)
                fStorage.downloadUrl.addOnCompleteListener { task ->
                    task.result.let { url->
                        val imageFileName = getFile?.name

                        uploadData(imageFileName)
                    }
                }
            }
    }

    private fun uploadData(imgName : String?){
        val fDatabase = FirebaseDatabase.getInstance().getReference("item_data")
        itemId = fDatabase.push().key!!

        val productItem = ProductModel()
        productItem.id = itemId
        productItem.itemName = productName.text.toString()
        productItem.itemPrice = productPrice.text.toString()
        productItem.itemFirstQuantity = productStock.text.toString().toInt()
        productItem.itemCurrentQuantity = productItem.itemFirstQuantity
        productItem.itemDescription = productDesc.text.toString()
        productItem.itemImage = imgName.toString()
        productItem.stockQuantity = productStock.text.toString().toInt()

        if (
            productName.text.isNotEmpty() &&
            productPrice.text.isNotEmpty() &&
            productStock.text.isNotEmpty() &&
            productDesc.text.isNotEmpty()
        ){
            fDatabase.child(idFromIntent).get().addOnSuccessListener {
                showLoading(false)
                if (it.exists()){
                    fDatabase.child(idFromIntent).addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.getValue(ProductModel::class.java)!!
                        val updateData : MutableMap<String, Any> = HashMap()

                        updateData["itemName"] = productName.text.toString()
                        updateData["itemPrice"] = productPrice.text.toString()
                        updateData["itemCurrentQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
                        updateData["itemFirstQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
                        updateData["itemDescription"] = productDesc.text.toString()
                        if (data.stockQuantity == 0){
                            updateData[STOCK_QUANTITY] = productStock.text.toString().toInt() + data.itemCurrentQuantity
                        } else{
                            updateData[STOCK_QUANTITY] = data.stockQuantity + productStock.text.toString().toInt()
                        }

                        if (getFile == null){
                            updateData["itemImage"] = data.itemImage.toString()
                        } else{
                            updateData["itemImage"] = imgName.toString()
                        }

                        fDatabase.child(idFromIntent).updateChildren(updateData)
                            .addOnSuccessListener {
                                showLoading(false)
                                Toast.makeText(applicationContext, "Data Berhasil di Perbaharui", Toast.LENGTH_LONG).show()
                                finish()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
                } else{
                    fDatabase.child(itemId).setValue(productItem)
                        .addOnSuccessListener {
                            showLoading(false)
                            productName.text.clear()
                            productPrice.text.clear()
                            productStock.text.clear()
                            productDesc.text.clear()
                            Toast.makeText(this, "Data Berhasil Ditambahkan", Toast.LENGTH_LONG).show()
                            finish()
                        }
                        .addOnFailureListener {
                            showLoading(false)
                            Toast.makeText(this, "Gagal Menambahkan Data", Toast.LENGTH_LONG).show()
                        }
                }
            }
//            if (idFromIntent.isEmpty()){
//                fDatabase.child(itemId).setValue(productItem)
//                    .addOnSuccessListener {
//                        productName.text.clear()
//                        productPrice.text.clear()
//                        productStock.text.clear()
//                        productDesc.text.clear()
//                        showLoading(false)
//                        Toast.makeText(this, "Data Berhasil Ditambahkan", Toast.LENGTH_LONG).show()
//                        finish()
//                    }
//                    .addOnFailureListener {
//                        showLoading(false)
//                        Toast.makeText(this, "Gagal Menambahkan Data", Toast.LENGTH_LONG).show()
//                    }
//            } else{
//                fDatabase.child(idFromIntent).addListenerForSingleValueEvent(object :
//                    ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val data = snapshot.getValue(ProductModel::class.java)!!
//                        val updateData : MutableMap<String, Any> = HashMap()
//
//                        updateData["itemName"] = productName.text.toString()
//                        updateData["itemPrice"] = productPrice.text.toString()
//                        updateData["itemCurrentQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
//                        updateData["itemFirstQuantity"] = productStock.text.toString().toInt() + data.itemCurrentQuantity
//                        updateData["itemDescription"] = productDesc.text.toString()
//                        if (getFile == null){
//                            updateData["itemImage"] = data.itemImage.toString()
//                        } else{
//                            updateData["itemImage"] = imgName.toString()
//                        }
//
//                        fDatabase.child(idFromIntent).updateChildren(updateData)
//                            .addOnSuccessListener {
//                                showLoading(false)
//                                Toast.makeText(applicationContext, "Data Berhasil di Perbaharui", Toast.LENGTH_LONG).show()
//                                finish()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//                })
//            }
        }
    }

    private fun hideError(){

        productName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductName.error = null
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductName.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        productPrice.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductPrice.error = null
            }

            override fun onTextChanged(char: CharSequence, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductPrice.error = null

                   var setPriceText =  productPrice.text.toString().trim()

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

        productStock.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductFirstStock.error = null
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductFirstStock.error = null
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

        productDesc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductDesc.error = null
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.layoutProductDesc.error = null
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