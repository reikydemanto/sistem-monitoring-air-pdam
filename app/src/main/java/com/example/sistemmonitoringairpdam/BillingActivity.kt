package com.example.sistemmonitoringairpdam

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityBillingBinding
import com.example.sistemmonitoringairpdam.databinding.ActivityTransactionBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BillingActivity : AppCompatActivity() {

    private var _binding: ActivityBillingBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    fun getDayOfMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date // Setel tanggal pada objek Calendar
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    data class Data(val value: Double, val roomId: Double, val date: Date)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityBillingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)

        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)

        val collectionRef = db.collection("Record_air")
        val mutableList = mutableListOf<Pair<String, Float>>()

        collectionRef.whereEqualTo("id_kamar", idKamar).get()
            .addOnSuccessListener { result ->
                val data = ArrayList<String>()
                for (document: QueryDocumentSnapshot in result) {
                    val field1: Double? = document.getDouble("air")
                    val field2 = document.getDouble("id_kamar")
                    val field3 = document.getDate("hari")
                    val record = "$field1, $field2 ,$field3"
                    data.add(record)
                }
                // Mengubah data ke dalam format yang lebih mudah diproses
                val formattedData = data.map {
                    val parts = it.split(",")
                    Data(
                        parts[0].trim().toDouble(), // nilai
                        parts[1].trim().toDouble(), // id kamar
                        SimpleDateFormat(
                            "EEE MMM dd HH:mm:ss zzz yyyy",
                            Locale.US
                        ).parse(parts[2].trim()) // tanggal
                    )
                }

                // Kelompokkan data berdasarkan bulan
                val groupedData = formattedData.groupBy { getMonthName(it.date) }

                // Hitung jumlah nilai (value) untuk setiap bulan
                val result = mutableMapOf<String, Double>()
                groupedData.forEach { (month, dataList) ->
                    val totalValue = dataList.sumByDouble { it.value }
                    result[month] = totalValue
                }

                // Menampilkan hasil
                result.forEach { (month, totalValue) ->
                    val formattedTotalValue = String.format("%.2f", totalValue)
                    println("$month: Total Nilai = $formattedTotalValue")
                }

                result.forEach { (month, totalValue) ->
                    val formattedTotalValue = String.format("%.2f", totalValue)
                    // Menggunakan nilai bulan dari Calendar.MONTH (dimulai dari 0)
                    val monthNumber = getMonthNumber(month)
                    val floatValue = formattedTotalValue.replace(",", ".").toFloat()
                    mutableList.add("$monthNumber" to floatValue)
                }

                binding.apply {
                    barChart.animation.duration = BillingActivity.animationDuration
                    barChart.animate(mutableList)
                }

            }.addOnFailureListener { exception ->
                println("Gagal mengambil data: $exception")
            }
    }

    fun getMonthNumber(month: String): Int {
        val dateFormat = SimpleDateFormat("MMMM", Locale.US)
        val date = dateFormat.parse(month)
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.MONTH) + 1 // Ditambah 1 karena indeks bulan dimulai dari 0
    }

    fun getMonthName(date: Date): String {
        val dateFormat = SimpleDateFormat("MMMM", Locale.US)
        return dateFormat.format(date)
    }

    companion object {
        private const val animationDuration = 1000L
    }
}