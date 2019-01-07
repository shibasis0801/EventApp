package com.example.overlord.eventapp.main

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import com.example.overlord.eventapp.R
import com.example.overlord.eventapp.xtra.SecondActivity
import com.example.overlord.eventapp.base.BaseActivity
import com.example.overlord.eventapp.extensions.*
import com.example.overlord.eventapp.model.Guest
import com.firebase.ui.auth.AuthUI
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.CompletableFuture

class MainActivity : BaseActivity() {

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                textViewInput.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_events -> {
                textViewInput.setText(R.string.title_events)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_camera -> {
                textViewInput.setText(R.string.title_camera)

                takePhoto("Upload to Firebase")
                    .addOnSuccessListener { image ->
                        logDebug("Name: ${image.name}")
                        val compressed = compressImage(image)
                        firebaseStorage.pushImage(compressed)
                            .addOnSuccessListener { logDebug("Uploaded ${image.name}") }
                    }



                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_albums -> {
                textViewInput.setText(R.string.title_albums)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_guests -> {
                textViewInput.setText(R.string.title_guests)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


        editTextName.onTextChange { input ->
            textViewInput.text = input
            firebaseAuth?.currentUser?.email?.let {
                firestore.collection("users")
                        .add(Guest(
                                input,
                                it
                        ))
                        .addOnFailureListener { error -> logError(error.message) }
            }
        }


        buttonSignOut.setOnClickListener {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { finish() }
        }

        buttonNewActivity.setOnClickListener {
            startActivityGetResult(Intent(this, SecondActivity::class.java))
                .addOnSuccessListener { intent -> logDebug(intent.getStringExtra("Name")) }
                .addOnFailureListener { error -> logError(error.message) }
        }
    }
}