package com.mobileapps.matchmock

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRegister.setOnClickListener {
            createUser()
        }

        tvGoToLogin.setOnClickListener {
            Log.d("MainActivity", "Try to show Login screen")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        btnPicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
    }

    var selectedPicUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {

            selectedPicUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPicUri)

            btnPictureRounded.setImageBitmap(bitmap)

            btnPicture.alpha = 0f

//            val bitmapDrawable = BitmapDrawable(bitmap)
//            btnPicture.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun createUser() {
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password: $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill the e-mail and password fields", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("User", "Successfully created user wih ID: ${it.result?.user?.uid}")
                Toast.makeText(this, "Successfully created user. Thank you.", Toast.LENGTH_SHORT).show()

                uploadPic()
            }
            .addOnFailureListener {
                Log.d("User", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadPic() {

        if (selectedPicUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPicUri!!)
            .addOnSuccessListener {
                Log.d("User", "Successfully uploaded image: ${it.metadata?.path}")

                saveUserToDatabase(it.toString())

            }

    }

    private fun saveUserToDatabase(profilePicUrl: String) {

        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, etUsername.text.toString(), profilePicUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("User", "User saved to Firebase database")
            }
    }
}

class User(
    val uid: String, val username: String, val profilePicUrl: String

)