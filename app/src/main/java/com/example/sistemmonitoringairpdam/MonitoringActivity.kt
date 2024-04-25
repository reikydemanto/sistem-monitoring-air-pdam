package com.example.sistemmonitoringairpdam

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.sistemmonitoringairpdam.databinding.ActivityMonitoringBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MonitoringActivity : AppCompatActivity() {

    val db = Firebase.firestore

    private var _binding: ActivityMonitoringBinding? = null
    private val binding get() = _binding!!

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

        val data = intent.getStringExtra("idKamar")
        val doubleValueKamar: Double = data!!.toDouble()

        val sharedPreferences = getSharedPreferences("Session", Context.MODE_PRIVATE)

//        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)
//        val doubleValueKamar: Double = idKamar.toDouble()

        _binding = ActivityMonitoringBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val calendar = Calendar.getInstance()
        val bulanSaatIni =
            calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
        val textView: TextView = findViewById(R.id.bulan)
        textView.text = bulanSaatIni

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.time
        val lastDayOfMonths = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonths)
        val lastDayOfMonth = calendar.time

        val today = calendar.time // Mendapatkan tanggal hari ini

        val dateFormat = SimpleDateFormat("dd", Locale.getDefault())
        val formattedToday = dateFormat.format(today)

        val yesterdayCalendar =
            Calendar.getInstance() // Mendapatkan instance Calendar baru untuk tanggal kemarin
        yesterdayCalendar.add(
            Calendar.DAY_OF_MONTH, -1
        ) // Mengurangi satu hari dari tanggal hari ini

        val formattedYesterday =
            dateFormat.format(yesterdayCalendar.time) // Mendapatkan tanggal kemarin

        val twoDaysAgoCalendar = Calendar.getInstance()
        twoDaysAgoCalendar.add(
            Calendar.DAY_OF_MONTH, -2
        ) // Mengurangi dua hari dari tanggal hari ini
        val twoDaysAgo = twoDaysAgoCalendar.time

        val formattedTwoDaysAgo = dateFormat.format(twoDaysAgo)

        val collectionRef = db.collection("Record_air")
        val mutableList = mutableListOf<Pair<String, Float>>()

        collectionRef.whereNotEqualTo("hari", "NULL").whereGreaterThan("hari", firstDayOfMonth)
            .whereLessThan("hari", lastDayOfMonth).get().addOnSuccessListener { result ->
                val data = ArrayList<String>()
                for (document: QueryDocumentSnapshot in result) {
                    val field1: Double? = document.getDouble("air")
                    val doubleValue: Double = field1?.toDouble() ?: 0.0
                    val field1Float: Float = doubleValue.toFloat()
                    val field2 = document.getDouble("id_kamar")
                    val field3 = document.getDate("hari")
                    val label = getDayOfMonth(field3 as Date).toString()
                    val record = "$field1, $field2 ,$field3"

                    if (field2 != null) {
                        if (field2.equals(doubleValueKamar)) {
                            mutableList.add(label to field1Float)
                            data.add(record)
                        }
                    }
                }
                binding.apply {
                    barChart.animation.duration = MonitoringActivity.animationDuration
                    barChart.animate(mutableList)
                }
                val formattedData = data.map {
                    val parts = it.split(",")
                    Data(
                        parts[0].trim().toDouble(), // nilai
                        parts[1].trim().toDouble(), // id kamar
                        SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(parts[2].trim()) // tanggal
                    )
                }

                // Filter hanya data yang terjadi pada hari Jumat
                val fridays = formattedData.filter { Calendar.getInstance().apply { time = it.date }.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY }

                // Menemukan Jumat terakhir
                val latestFriday = fridays.maxByOrNull { it.date }

                // Filter hanya data yang terjadi pada hari Jumat
                val saturday = formattedData.filter { Calendar.getInstance().apply { time = it.date }.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY }

                // Menemukan Jumat terakhir
                val latestSaturday = saturday.maxByOrNull { it.date }

                // Filter hanya data yang terjadi pada hari Jumat
                val sunday = formattedData.filter { Calendar.getInstance().apply { time = it.date }.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY }

                // Menemukan Jumat terakhir
                val latestSunday = sunday.maxByOrNull { it.date }

                val textView: TextView = findViewById(R.id.fri)
                val textView1: TextView = findViewById(R.id.sat)
                val textView2: TextView = findViewById(R.id.sun)
                if (latestFriday != null) {
                    textView.text = latestFriday.value.toString()+" Liter"
                }
                if (latestSaturday != null) {
                    textView1.text = latestSaturday.value.toString()+" Liter"
                }
                if (latestSunday != null) {
                    textView2.text = latestSunday.value.toString()+" Liter"
                }
            }.addOnFailureListener { exception ->
                println("Gagal mengambil data: $exception")
            }
    }

    companion object {
        private const val animationDuration = 1000L
    }
}