package com.example.skripsiapp.CustomView

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.example.skripsiapp.R

class CartButton : AppCompatButton {

    private var enableBackground : Drawable? = null
    private var disableBackground : Drawable? = null
    private var txtColor : Int = 0

    constructor(context: Context) : super(context){
        init()
    }

    constructor(context: Context, attrs: AttributeSet) :super(context, attrs){
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr){
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        background = enableBackground
        setTextColor(txtColor)
        textSize = 14f
        gravity = Gravity.CENTER
    }

    private fun init(){
        txtColor = ContextCompat.getColor(context, R.color.white)
        enableBackground = ContextCompat.getDrawable(context, R.drawable.bg_btn_checkout)
    }

}