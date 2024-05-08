package com.example.sistemmonitoringairpdam

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityLupaPasswordBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityResetPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class ResetPasswordActivity : AppCompatActivity() {

    private var _binding: ActivityResetPasswordBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    lateinit var kirim: Button
    lateinit var editTextPassword: EditText
    lateinit var editTextConfirmPassword: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val idDocument = intent.getStringExtra("documentId")
        println(idDocument)

        if (idDocument == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        editTextPassword = findViewById(R.id.editTextPassword)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)

        kirim = findViewById(R.id.buttonKirim)
        kirim.setOnClickListener {
            if (editTextPassword.text.toString() != editTextConfirmPassword.text.toString()) {
                Toast.makeText(applicationContext, "Password Tidak Sama", Toast.LENGTH_SHORT).show()
            } else {
                val docRef = db.collection("User_air").document(idDocument.toString())
                val updates = hashMapOf(
                    "password" to editTextConfirmPassword.text.toString(),
                )
                docRef.update(updates as Map<String, Any>)
                    .addOnSuccessListener {
                        Toast.makeText(applicationContext, "Password Sudah Berubah ",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(applicationContext, "Gagal memperbarui Password ",Toast.LENGTH_SHORT).show()
                    }

            }
        }
    }
}