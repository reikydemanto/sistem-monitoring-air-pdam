package com.example.sistemmonitoringairpdam

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.sistemmonitoringairpdam.databinding.ActivityBillingBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.firestore
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class BillingActivity : AppCompatActivity() {

    private var _binding: ActivityBillingBinding? = null
    private val binding get() = _binding!!

    val db = Firebase.firestore

    lateinit var monthly: CardView
    lateinit var weekly: CardView
    lateinit var daily: CardView

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
        val valueReceived = intent.getIntExtra("var", 0)

        val idKamar: Int = sharedPreferences.getInt("idKamar", 0)

        val collectionRef = db.collection("Record_air")

        val mutableListBulan = mutableListOf<Pair<String, Float>>()
        val mutableListHari = mutableListOf<Pair<String, Float>>()
        val mutableListMinggu = mutableListOf<Pair<String, Float>>()

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

                val calendar = Calendar.getInstance()
                val firstDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, 1) }.time
                val lastDayOfMonth = calendar.apply { set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH)) }.time

// Mendapatkan bulan pertama yang ada dalam data
                val firstMonth =
                    formattedData.minByOrNull { it.date }?.let { getMonthNumber(it.date) }
// Mendapatkan bulan saat ini
                val currentMonth =
                    Calendar.getInstance().get(Calendar.MONTH) + 1 // Mendapatkan bulan saat ini

                val monthlyGroupedData = formattedData.groupBy { getMonthNumber(it.date) }
                val dailyGroupedDataThisMonth = formattedData
                    .filter { it.date in firstDayOfMonth..lastDayOfMonth }
                    .groupBy { getDateKey(it.date) }
                val weeklyGroupedDataThisMonth = formattedData
                    .filter { it.date in firstDayOfMonth..lastDayOfMonth }
                    .groupBy { getWeekInMonth(it.date) }

                val monthlyResult = mutableMapOf<String, Double>()
                val dailyResultThisMonth = mutableMapOf<String, Double>()
                val weeklyResultThisMonth = mutableMapOf<Int, Double>()

// Menginisialisasi nilai total untuk setiap bulan yang mungkin ada dalam rentang data
                for (i in 1..currentMonth) {
                    val month = if (firstMonth != null && i == 1) firstMonth else getMonthNumber(
                        getDateFromMonthAndYear(i, Calendar.getInstance().get(Calendar.YEAR))
                    )
                    monthlyResult[month] = 0.0
                }

// Menghitung total nilai per bulan
                monthlyGroupedData.forEach { (month, dataList) ->
                    val totalValue = dataList.sumByDouble { it.value }
                    monthlyResult[month] = totalValue
                }

// Menghitung total nilai per hari
                dailyGroupedDataThisMonth.forEach { (date, dataList) ->
                    val totalValue = dataList.sumByDouble { it.value }
                    dailyResultThisMonth[date] = totalValue
                }

// Menghitung total nilai per minggu
                weeklyGroupedDataThisMonth.forEach { (week, dataList) ->
                    val totalValue = dataList.sumByDouble { it.value }
                    weeklyResultThisMonth[week.toInt()] = totalValue
                }

                val sortedMonthlyResult = monthlyResult.toList().sortedBy { it.first.toInt() }
                val sortedDailyResultThisMonth = dailyResultThisMonth.toList().sortedBy { it.first }
                val sortedWeeklyResultThisMonth = weeklyResultThisMonth.toList().sortedBy { it.first }

                sortedMonthlyResult.forEach { (month, totalValue) ->
                    val formattedTotalValue =
                        String.format("%.2f", totalValue).replace(",", ".").toFloat()
                    mutableListBulan.add(month to formattedTotalValue)
                }

                sortedDailyResultThisMonth.forEach { (date, totalValue) ->
                    val formattedDate = SimpleDateFormat("dd", Locale.US).format(SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date))
                    val formattedTotalValue = String.format("%.2f", totalValue).replace(",", ".").toFloat()
                    mutableListHari.add(formattedDate to formattedTotalValue)
                }


                sortedWeeklyResultThisMonth.forEach { (week, totalValue) ->
                    val formattedTotalValue = String.format("%.2f", totalValue).replace(",", ".").toFloat()
                    mutableListMinggu.add("Week $week" to formattedTotalValue)
                }

                binding.apply {
                    barChart.animation.duration = BillingActivity.animationDuration
                    when (valueReceived) {
                        1 -> {
                            barChart.animate(mutableListHari)
                            labelbawah.text = "Tanggal"
                        }
                        2 -> {
                            barChart.animate(mutableListMinggu)
                            labelbawah.text = "Minggu"
                        }
                        3 -> {
                            barChart.animate(mutableListBulan)
                            labelbawah.text = "Bulan"
                        }
                        else -> {
                            // Jika nilai yang diterima tidak sesuai dengan 1, 2, atau 3, maka animasikan dengan mutableListBulan
                            barChart.animate(mutableListBulan)
                        }
                    }
                }


            }.addOnFailureListener { exception ->
                println("Gagal mengambil data: $exception")
            }

        daily = findViewById<CardView>(R.id.dailybutton)
        daily.setOnClickListener {
            val valueToSend = 1
            val intent = Intent(this, BillingActivity::class.java)
            intent.putExtra("var", valueToSend)
            startActivity(intent)
        }
        weekly = findViewById<CardView>(R.id.weekly)
        weekly.setOnClickListener {
            val valueToSend = 2
            val intent = Intent(this, BillingActivity::class.java)
            intent.putExtra("var", valueToSend)
            startActivity(intent)
        }
        monthly = findViewById<CardView>(R.id.monthly)
        monthly.setOnClickListener {
            val valueToSend = 3
            val intent = Intent(this, BillingActivity::class.java)
            intent.putExtra("var", valueToSend)
            startActivity(intent)
        }

    }

    fun getWeekInMonth(date: Date): Int {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return calendar.get(Calendar.WEEK_OF_MONTH)
    }

    fun getMonthName(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val monthNumber = calendar.get(Calendar.MONTH)
        val monthNames = DateFormatSymbols().months
        return monthNames[monthNumber]
    }

    fun getMonthNumber(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        return (calendar.get(Calendar.MONTH) + 1).toString()
    }

    fun getDateKey(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    fun getDateFromMonthAndYear(month: Int, year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1) // Bulan dimulai dari 0
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set tanggal ke 1 untuk menghindari kebingungan
        return calendar.time
    }

    override fun onBackPressed() {
        // Jika tombol back ditekan di halaman utama, keluar dari aplikasi
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }


    companion object {
        private const val animationDuration = 1000L
    }
}
