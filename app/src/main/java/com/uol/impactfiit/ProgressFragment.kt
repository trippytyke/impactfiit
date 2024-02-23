package com.uol.impactfiit
import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import android.graphics.Color.WHITE
import androidx.core.content.ContextCompat


class ProgressFragment:Fragment(R.layout.fragment_progress) {

    private lateinit var lineChart: LineChart
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ============== OPEN SCOPE ============
        lateinit var weights: List<Float>
        lateinit var dates: List<Float>
        var spacing = 0.0f
        lineChart = view.findViewById(R.id.chart)
        // **************************************************

        // ============= FIREBASE ===========
        val uid = Firebase.auth.currentUser?.uid
        val db = Firebase.firestore
        val docRef = db.collection("users").document(uid!!)
        // **************************************************

        // =========== GET DATA FROM FIREBASE ==========
        docRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {

                    val entriesCollection = mutableListOf<Entry>()

                    //code below gets the fields weightHistory & dateHistory. This field is an array
                    val weightHistory = documentSnapshot.get("weightHistory") as List<Float>
                    val dateHistory = documentSnapshot.get("weightHistoryTime") as List<String>

                    if (weightHistory != null && dateHistory != null) {

                        var currDate = LocalDate.parse(dateHistory[0])

                        for (i in weightHistory.indices) {

                            if (currDate == LocalDate.parse(dateHistory[i])) {
                                // Should only execute for first entry
                                println("HARITH iteration")

                                entriesCollection.add(Entry(spacing, weightHistory[i]))

                            } else {
                                // This block executes for all but the first entry
                                // currDate should be older than (<) dateHistory[i]

                                // Get and compare the days between currDate and dateHistory[i]
                                val dateInIteration = LocalDate.parse(dateHistory[i])
                                val dateDiff = ChronoUnit.DAYS.between(currDate, dateInIteration)

                                // ============= LAZY TEMP ERROR CHECKING ==============
                                println("HARITH else DATE DIFF " + dateDiff)
                                println("HARITH else CURRDATE " + currDate)
                                println("HARITH else DATEINITERATION " + dateInIteration)
                                println("HARITH else INDEX " + i)
                                println("                                                   HARITH")
                                // ******************************************

                                // Increment spacing by the difference between both dates
                                // For a more accurate estimation of the weight changes
                                // i.e 1 unit in the graph's x-axis is 1 day
                                spacing += dateDiff

                                entriesCollection.add(Entry(spacing, weightHistory[i]))

                                // Update current date to dateHistory[i] for the comparison
                                // for the next iteration
                                currDate = LocalDate.parse(dateHistory[i])
                                println("Local Date 2 = " + dateInIteration)
                            }

                        }

                        println("Today's date is" + LocalDate.now())

                        // Create a LineDataSet object to hold the data set
                        val dataSet = LineDataSet(entriesCollection, "")
                        dataSet.color = Color.WHITE

                        val orangeColour = context?.let { ContextCompat.getColor(it, R.color.colorName) }
                        if (orangeColour != null) {
                            dataSet.setCircleColor(orangeColour)
                        }

                        dataSet.setDrawCircleHole(false)
                        dataSet.valueTextColor = Color.WHITE
                        dataSet.valueTextSize = 10f

                        // Create a LineData object and add the LineDataSet to it
                        val lineData = LineData(dataSet)

                        // Set the LineData object to the LineChart
                        lineChart.data = lineData

                        // Refresh the chart to display the new data
                        lineChart.invalidate()

                        // ========================== LineChart styles ============================
                        lineChart.apply {
                            xAxis.apply {
                                setDrawGridLines(false)
                                axisLineColor = Color.WHITE
                                textColor = Color.WHITE
                            }

                            axisLeft.apply {
                                setDrawGridLines(false)
                                axisLineColor = Color.WHITE
                                textColor = Color.WHITE
                            }

                            axisRight.apply {
                                setDrawGridLines(false)
                            }
                        }

                        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
                        lineChart.description.isEnabled = false
                        lineChart.axisRight.isEnabled = false
                        // ******************************************************************

                    } else {
                        // Handle case where fields are null due to bad import or empty data
                    }
                } else {
                    // Handle case where document doesn't exist
            }
        }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the operation
                Log.d(ContentValues.TAG, "get failed with ", exception)
        }
        // **************************************************


        // ============== LINE CHART ================

        // Create a list of Entry objects representing data points
        val entries = mutableListOf<Entry>()
        entries.add(Entry(0f, 10f))  // Example data point at x = 0, y = 10
        entries.add(Entry(1f, 20f))  // Example data point at x = 1, y = 20
        entries.add(Entry(2f, 15f))  // Example data point at x = 2, y = 15
        // Add more data points as needed

        // Provide x-axis labels for each data point
//        val labels = listOf("Label 1", "Label 2", "Label 3") // Replace with your actual labels
//        lineChart.xAxis.valueFormatter = object : ValueFormatter() {
//            override fun getFormattedValue(value: Float): String {
//                val index = value.toInt()
//                return if (index >= 0 && index < labels.size) labels[index] else ""
//            }
//        }

//        // Create a LineDataSet object to hold the data set
//        val dataSet = LineDataSet(entriesCollection, "Test Data")
//
//        // Create a LineData object and add the LineDataSet to it
//        val lineData = LineData(dataSet)
//
//        // Set the LineData object to your LineChart
//        lineChart.data = lineData
//
//        // Refresh the chart to display the new data
//        lineChart.invalidate()
        // **************************************************



//        val logoutButton = view.findViewById<AppCompatButton>(R.id.logoutBtn)
//
//        logoutButton.setOnClickListener {
//            val intent = Intent(activity, LoginActivity::class.java)
//            Firebase.auth.signOut()
//            startActivity(intent)
//        }
    }
}