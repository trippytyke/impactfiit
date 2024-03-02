package com.uol.impactfiit
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.Entry
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference


class ProgressFragment:Fragment(R.layout.fragment_progress) {

    private lateinit var lineChart: LineChart
    private lateinit var barChart: BarChart
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ============== OPEN SCOPE ============
        lineChart = view.findViewById(R.id.lineChart)
        barChart = view.findViewById(R.id.barChart)
        // **************************************************

        // ============= FIREBASE ===========
        val uid = Firebase.auth.currentUser?.uid
        val db = Firebase.firestore
        val usersDocRef = db.collection("users").document(uid!!)
        val caloriesDocRef = db.collection("calorieLog")
            .document(uid)
            .collection("dailyIntake")
        // **************************************************

        // =========== GET DATA FROM FIREBASE AND DRAW CHARTS ==========
        fetchWeightsAndDrawGraph(lineChart, usersDocRef)
        fetchCaloriesAndDrawChart(barChart, caloriesDocRef)
        // ***************************************************
    }


    private fun fetchCaloriesAndDrawChart(barChart: BarChart, doc: CollectionReference) {
        val dates : MutableList<String> = mutableListOf()
        val calories : MutableList<Int> = mutableListOf()

        doc.get()
            .addOnSuccessListener { query ->
                if (query != null) {
                    for (document in query.documents) {
                        val date = document.id
                        dates.add(date)

                        val totalCalories = document.get("totalCalories") as Long
                        calories.add(totalCalories.toInt())
                    }

                    if (dates.size == calories.size) {
                        drawBarChart(barChart, dates, calories)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the operation
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }


    private fun drawBarChart(barChart: BarChart, dates: List<String>, calories: List<Int>) {
        val entriesCollection = mutableListOf<BarEntry>()

        dates.forEachIndexed { index, date ->
            entriesCollection.add(BarEntry(index.toFloat(), calories[index].toFloat()))
        }

        val orangeColour = context?.let { ContextCompat.getColor(it, R.color.colorName) } as Int
        val barDataSet = BarDataSet(entriesCollection, "calories")
        barDataSet.color = orangeColour
        barDataSet.valueTextColor = Color.WHITE

        val dataSets = mutableListOf<IBarDataSet>()
        dataSets.add(barDataSet)

        val barData = BarData(dataSets)
        barData.barWidth = 0.2f

        barChart.data = barData
        barChart.setFitBars(true)

        val xAxis = barChart.xAxis
//        xAxis.valueFormatter = IndexAxisValueFormatter(dates)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val index = value.toInt()

                if (index >= 0 && index < dates.size) {
                    val dateParts = dates[index].split(" ")
                    return "${dateParts[0]} \n${dateParts[1]}\n"
                }
                return ""
            }
        }
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.labelCount = dates.size
        xAxis.setAvoidFirstLastClipping(true)
        xAxis.textColor = Color.WHITE
        xAxis.gridColor = Color.TRANSPARENT

        val leftAxis = barChart.axisLeft
        leftAxis.textColor = Color.WHITE
        leftAxis.gridColor = Color.TRANSPARENT

        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f

        barChart.invalidate()
    }

    private fun fetchWeightsAndDrawGraph(lineChart: LineChart, doc: DocumentReference) {
        var spacing = 0.0f

        doc.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot != null && documentSnapshot.exists()) {

                    val entriesCollection = mutableListOf<Entry>()

                    //code below gets the fields weightHistory & dateHistory. This field is an array
                    val weightHistory = documentSnapshot.get("weightHistory") as List<Float>
                    val dateHistory = documentSnapshot.get("weightHistoryTime") as List<String>

                    //get weight goal
                    val targetWeight = documentSnapshot.get("targetWeight") as String

                    if (weightHistory != null && dateHistory != null) {

                        var currDate = LocalDate.parse(dateHistory[0])

                        for (i in weightHistory.indices) {

                            if (currDate == LocalDate.parse(dateHistory[i])) {
                                // Should only execute for first entry
                                entriesCollection.add(Entry(spacing, weightHistory[i]))

                            } else {
                                // This block executes for all but the first entry
                                // currDate should be older than (<) dateHistory[i]

                                // Get and compare the days between currDate and dateHistory[i]
                                val dateInIteration = LocalDate.parse(dateHistory[i])
                                val dateDiff = ChronoUnit.DAYS.between(currDate, dateInIteration)

                                // Increment spacing by the difference between both dates
                                // For a more accurate estimation of the weight changes
                                // i.e 1 unit in the graph's x-axis is 1 day
                                spacing += dateDiff

                                entriesCollection.add(Entry(spacing, weightHistory[i]))

                                // Update current date to dateHistory[i] for the comparison
                                // for the next iteration
                                currDate = LocalDate.parse(dateHistory[i])
                            }

                        }

                        // Create a LineDataSet object to hold the data set
                        val dataSet = LineDataSet(entriesCollection, "Weight")
                        dataSet.color = Color.WHITE

                        val orangeColour = context?.let { ContextCompat.getColor(it, R.color.colorName) } as Int
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
                        setLineChartStyle(lineChart)
                        // ******************************************************************

                        // ============= ADD LINE TO DISPLAY TARGET LINE ===============
                        drawTargetWeight(lineChart, orangeColour, targetWeight.toFloat())
                        // ******************************************************************

                    } else {
                        // Handle case where fields are null due to bad import or empty data
                        Log.d(ContentValues.TAG, "Unable to get weightHistory and dateHistory")
                    }
                } else {
                    // Handle case where document doesn't exist
                    Log.d(ContentValues.TAG, "Unable to get document")
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred during the operation
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    private fun setLineChartStyle(lineChart: LineChart) {
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
    }
    private fun drawTargetWeight(lineChart: LineChart, colour: Int, weight: Float) {
        val targetWeightLine = LimitLine(weight, "Target weight")
        targetWeightLine.lineColor = colour
        targetWeightLine.label = "Target weight"
        targetWeightLine.textColor = Color.WHITE
        lineChart.axisLeft.addLimitLine(targetWeightLine)
        lineChart.legend.isEnabled = false
    }
}