package com.android.dmribeiro87.kotlinmesseger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.android.dmribeiro87.kotlinmesseger.messages.LatestMessageActivity
import com.android.dmribeiro87.kotlinmesseger.R
import com.android.dmribeiro87.kotlinmesseger.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private val GALERY: Int = 0
    private val CAMERA: Int = 1
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar!!.hide()

        register_button.setOnClickListener {
            performRegister()
        }
        already_have_account_textview.setOnClickListener {
            intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            Log.d(TAG, "Try to select photo!!")
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, GALERY)

        }


    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALERY && resultCode == Activity.RESULT_OK && data != null){
            //check what image was selected....
            Log.d(TAG, "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            selectphoto_imageview_register.setImageBitmap(bitmap)

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }


    }


    private fun performRegister(){

        val email = email_et_register.text.toString()
        val password = password_et_register.text.toString()

        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_LONG).show()
            return
        }

        //Firebase Authentication to create an user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                uploadToFirebaseStorage()

                //If is successfuly
                Log.d(TAG, "Success ${it.result!!.user.uid}")
            }.addOnFailureListener {
                Log.d(TAG, "Fail to cheate user: ${it.message}")
                Toast.makeText(this, "Fail to create user", Toast.LENGTH_LONG).show()

            }
    }

    private fun uploadToFirebaseStorage(){
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d(TAG, "Success uploaded image: ${it.metadata?.path}")
                //Show File Location
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d(TAG, "File Location $it")

                    saveUserToFirebaseDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed upload Photo", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(
            uid,
            username_et_register.text.toString(),
            profileImageUrl
        )

        ref.setValue(user).addOnSuccessListener {
            Log.d(TAG, "Finally we save user to Firebase Database")
            val intent  = Intent(this, LatestMessageActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

        }.addOnFailureListener{
            Log.d(TAG, "Failed to set value to Firebase Database: ${it.message}")
            Toast.makeText(this, "Failed to create user", Toast.LENGTH_LONG).show()
        }

    }




}
