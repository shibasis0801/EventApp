package com.phoenixoverlord.eventapp.intro

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.bumptech.glide.Glide
import com.phoenixoverlord.eventapp.R
import com.phoenixoverlord.eventapp.base.BaseActivity
import com.phoenixoverlord.eventapp.extensions.*
import com.phoenixoverlord.eventapp.extensions.Firebase.auth
import com.phoenixoverlord.eventapp.extensions.Firebase.firestore
import com.phoenixoverlord.eventapp.extensions.Firebase.storage
import com.phoenixoverlord.eventapp.main.MainActivity
import com.phoenixoverlord.eventapp.mechanisms.compressImage

import com.phoenixoverlord.eventapp.model.User
import com.phoenixoverlord.eventapp.utils.uniqueName

import com.firebase.ui.auth.AuthUI
import java.util.*
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.lang.Error

class LoginActivity : BaseActivity() {

    private var user = User()
    private var compressedImage : File? = null

    private fun createPhoneLoginIntent(): Intent {
        return AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(
                Arrays.asList(
                    AuthUI.IdpConfig.PhoneBuilder().build()
                )
            )
            .setIsSmartLockEnabled(true)
            .build()
    }

    private fun startApp() {
        snackbar("Successful Sign In")
        finishAndStart(MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        setUpViews()

        if (auth.currentUser != null) {
            startApp()
        } else {

            buttonSignin.setOnClickListener {
                startActivityGetResult(createPhoneLoginIntent())
                    .addOnSuccessListener {
                        val userID = auth.currentUser?.uid
                        firestore.document("users/$userID")
                            .addSnapshotListener { documentSnapshot, _ ->
                                if (documentSnapshot != null && documentSnapshot.exists())
                                    startApp()
                                else {
                                    auth.signOut()
                                    toastError("You have to register first")
                                }
                            }
                    }
            }

            buttonLogin.setOnClickListener {

                if(user.name == "") {
                    snackbar("Please enter your name!")
                } else {
                    startActivityGetResult(
                        createPhoneLoginIntent()

                    ).addOnSuccessListener {

                        user.phoneno = auth.currentUser!!.phoneNumber.toString()
                        user.ID = auth.currentUser!!.uid
                        compressedImage?.let {image ->
                            //ToDo create saveUser like savePost in firebase extensions

                            storage.pushImage(image, user.profile_photo)
                                .addOnSuccessListener {

                                    logDebug("Uploaded ${image.name}")
                                }

                            firestore.collection("users")
                                .document(auth.currentUser!!.uid)
                                .set(user)
                                .addOnSuccessListener { documentReference ->

                                    logDebug("DocumentSnapshot added with ID: " + auth.currentUser!!.uid)
                                    startApp()
                                }
                                .addOnFailureListener { e ->

                                    logError("Error adding document", e)
                                }
                        }
                    }
                        .addOnFailureListener { error, intent ->
                            logError(error)
                            val response = IdpResponse.fromResultIntent(intent)

                            if (response == null) {
                                val message = "Sign In Cancelled"
                                logError(Error(message))
                                snackbar(message)
                            } else if (response.error!!.errorCode == ErrorCodes.NO_NETWORK) {
                                val message = "No Internet Connection"
                                logError(Error(message))
                                snackbar(message)
                            }
                        }
                }
            }
        }
    }

    fun setupSpinner(spinner : Spinner, textArrayResID : Int, onItemSelected : (String) -> Unit) {
        ArrayAdapter.createFromResource(
            this,
            textArrayResID,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                logDebug("Nothing Selected")
            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                onItemSelected(parent?.getItemAtPosition(position) as String)
            }
        }
    }

    private fun setUpViews() {

        userName.onTextChange { text -> user.name = text }

        bride.setOnClickListener {
            groom.setImageResource(R.drawable.male_bw)
            bride.setImageResource(R.drawable.female)
            user.wedding_side = "Bride"
        }

        groom.setOnClickListener {
            bride.setImageResource(R.drawable.female_bw)
            groom.setImageResource(R.drawable.male)
            user.wedding_side = "Groom"
        }

        user.relation = "Friend"
        setupSpinner(userRelation, R.array.relations) { selected -> user.relation = selected }

        uploadProfilePhoto.setOnClickListener {
            withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).execute {
                takePhoto("Add Profile Photo")
                    .addOnSuccessListener { images ->

                        val image = images[0]

                        logDebug("Profile photo name: ${image.name}")

                        user.profile_photo = uniqueName()

                        compressedImage = compressImage(image, user.profile_photo)

                        loadImage(userProfilePhoto, image)
                    }
            }
        }

    }

}
