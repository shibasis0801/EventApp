package com.example.overlord.eventapp.mechanisms

import android.graphics.Bitmap
import android.os.Environment
import android.support.v7.app.AppCompatActivity
import com.example.overlord.eventapp.base.BaseActivity
import com.example.overlord.eventapp.extensions.logDebug
import com.example.overlord.eventapp.extensions.logError
import com.example.overlord.eventapp.model.Constants
import com.example.overlord.eventapp.utils.timeStamp
import id.zelory.compressor.Compressor
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File

fun AppCompatActivity.compressImage(image : File) : File {

    val destinationRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val destination = File(destinationRoot, Constants.localCompressedImages)

    // Do this in background
    val compressor = Compressor(this)
        .setDestinationDirectoryPath(destination.absolutePath)
        .setMaxWidth(1920)
        .setMaxHeight(1080)
        .setQuality(50)

    return compressor
        .compressToFile(image, "IMG_${timeStamp()}")
}

/*
class CompressionModule {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var compressor: Compressor
    private lateinit var image : File

    private val destinationRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    private val destination = File(destinationRoot, Constants.localCompressedImages)

    fun compressImage(activity: BaseActivity, image : File) : CompressionModule {
        this.image = image
        compressor = Compressor(activity)
            .setDestinationDirectoryPath(destination.absolutePath)
            .setCompressFormat(Bitmap.CompressFormat.WEBP)
            .setMaxWidth(1920)
            .setMaxHeight(1080)
            .setQuality(75)
        return this
    }

    fun addOnSuccessListener(onCompressed : (File) -> Unit = { logDebug("DefaultCompressCallback", "Default") }) {

        val disposableCompressionTask = compressor.compressToFileAsFlowable(image, "IMG_${timeStamp()}")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(onCompressed, { error -> logError("CompressionError", error) })

        compositeDisposable.add(disposableCompressionTask)
    }

    fun dispose() {
        compositeDisposable.dispose()
    }
}
*/