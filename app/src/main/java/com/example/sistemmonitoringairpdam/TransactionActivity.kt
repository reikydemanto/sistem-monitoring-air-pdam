package com.example.sistemmonitoringairpdam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityMonitoringBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityTransactionBinding

class TransactionActivity : AppCompatActivity() {

    private var _binding: ActivityTransactionBinding? = null
    private val binding get() = _binding!!

    lateinit var kamar1: Button
    lateinit var kamar2: Button
    lateinit var buttonUserProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()

        buttonUserProfile = findViewById<ImageView>(R.id.userPorifle)
        buttonUserProfile.setOnClickListener {
            editor.clear()
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        kamar1 = findViewById<Button>(R.id.kamar1)
        kamar1.setOnClickListener {
            val intent = Intent(this, MonitoringActivity::class.java)
            intent.putExtra("idKamar", "1")
            startActivity(intent)
        }

        kamar2 = findViewById<Button>(R.id.kamar2)
        kamar2.setOnClickListener {
            val intent = Intent(this, MonitoringActivity::class.java)
            intent.putExtra("idKamar", "2")
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
        super.onBackPressed()
        finishAffinity()
    }
}