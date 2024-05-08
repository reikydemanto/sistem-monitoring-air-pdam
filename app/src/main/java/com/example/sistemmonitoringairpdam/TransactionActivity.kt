package com.example.sistemmonitoringairpdam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityMonitoringBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityTransactionBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private var _binding: ActivityTransactionBinding? = null
    private val binding get() = _binding!!

    lateinit var kamar1: Button
    lateinit var kamar2: Button
    lateinit var buttonUserProfile: ImageView
    lateinit var totalsemua: TextView
    lateinit var totalkamar1: TextView
    lateinit var totalkamar2: TextView

    val db = Firebase.firestore

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

        val collectionRef = db.collection("Record_air")

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time
        val lastDayOfMonths = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonths)
        val lastDayOfMonth = calendar.time
        val db = FirebaseFirestore.getInstance()

        collectionRef.whereNotEqualTo("hari", "NULL")
            .whereGreaterThanOrEqualTo("hari", firstDayOfMonth)
            .whereLessThanOrEqualTo("hari", lastDayOfMonth).get()
            .addOnSuccessListener { result ->
                val data = ArrayList<String>()
                for (document: QueryDocumentSnapshot in result) {
                    val field1: Double? = document.getDouble("air")
                    val field2 = document.getDouble("id_kamar")?.toString() ?: ""
                    val field3 = document.getDate("hari")
                    val record = "$field1, $field2 ,$field3"
                    data.add(record)
                }

                var totalValue = 0.0
                data.forEach { entry ->
                    val parts = entry.split(",")
                    val value = parts[0].trim().toDoubleOrNull() ?: 0.0
                    totalValue += value
                }

                val hargaPerValue = 0.15
                val totalRupiah = totalValue * hargaPerValue

                val totalsemua: TextView = findViewById(R.id.totalsemua)
                totalsemua.text = "Total Semua: Rp ${String.format("%.2f", totalRupiah)}"

                val totalPerIdKamar = mutableMapOf<String, Double>()
                data.forEach { entry ->
                    val parts = entry.split(",")
                    val value = parts[0].trim().toDoubleOrNull() ?: 0.0
                    val idKamar = parts[1].trim()

                    if (totalPerIdKamar.containsKey(idKamar)) {
                        totalPerIdKamar[idKamar] = totalPerIdKamar.getValue(idKamar) + value
                    } else {
                        totalPerIdKamar[idKamar] = value
                    }
                }

                totalPerIdKamar.forEach { (idKamar, totalValue) ->
                    totalPerIdKamar[idKamar] = totalValue * hargaPerValue
                }

                val kamar1: TextView = findViewById(R.id.totalkamar1)
                val totalKamar1 = totalPerIdKamar["1.0"] ?: 0.0
                kamar1.text = "Rp ${String.format("%.2f", totalKamar1)}"

                val kamar2: TextView = findViewById(R.id.totalkamar2)
                val totalKamar2 = totalPerIdKamar["2.0"] ?: 0.0
                kamar2.text = "Rp ${String.format("%.2f", totalKamar2)}"


            }.addOnFailureListener { exception ->
                println("Gagal mengambil data: $exception")
            }

    }

    override fun onBackPressed() {
        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
        super.onBackPressed()
        finishAffinity()
    }
}