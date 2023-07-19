package com.example.skripsiapp.Helper

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Build
import com.example.skripsiapp.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val FILENAME_FORMAT = "dd-MM-yyyy-HH-mm-ss"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun rotateBitmap(bitmap:Bitmap, isBackCamera:Boolean) :Bitmap{
    val matrix = Matrix()
    return if (isBackCamera){
        matrix.postRotate(0f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f,1f,bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}


fun createFile(application:Application) : File{
    val mediaDir = application.externalMediaDirs.firstOrNull().let {
        File(it, application.resources.getString(R.string.app_name)).apply { mkdirs() }
    }

    val outputDirectory = if (
        mediaDir != null && mediaDir.exists()
    ) mediaDir else application.filesDir


    val time = Calendar.getInstance().timeInMillis
    val formater = SimpleDateFormat(FILENAME_FORMAT)
    val current = formater.format(time)

    return File(outputDirectory, "$current.jpg")
}