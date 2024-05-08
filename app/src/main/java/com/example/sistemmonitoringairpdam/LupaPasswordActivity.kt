package com.example.sistemmonitoringairpdam

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityBillingBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityLupaPasswordBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LupaPasswordActivity : AppCompatActivity() {

    private var _binding: ActivityLupaPasswordBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    lateinit var kirim: Button
    lateinit var editTextUsername: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityLupaPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        kirim = findViewById(R.id.buttonSend)
        kirim.setOnClickListener {
            db.collection("User_air")
                .whereEqualTo("username", editTextUsername.text.toString()).get()
                .addOnSuccessListener { result ->
                    if (!result.isEmpty) {
                        val document = result.documents[0]
                        // Ambil ID dokumen
                        val documentId = document.id
                        // Ambil nilai dari bidang id_user jika diperlukan
                        val idUser = document.getDouble("id_user")

                        val intent = Intent(this, ResetPasswordActivity::class.java)
                        intent.putExtra("documentId", documentId)
                        // Anda dapat juga mengirim nilai id_user jika diperlukan
                        intent.putExtra("idUser", idUser)
                        println(documentId)
                        startActivity(intent)
                    } else {
                        // Tampilkan pesan jika tidak ada dokumen yang ditemukan
                        Toast.makeText(
                            applicationContext,
                            "Username tidak ditemukan",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }
        }
    }
}