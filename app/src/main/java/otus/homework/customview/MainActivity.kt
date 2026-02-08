package otus.homework.customview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.gson.Gson

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pieChartView = findViewById<PieChartView>(R.id.pieChartView)

        val jsonString = loadJsonFromRaw()
        val transactions = parseTransactions(jsonString)
        val categoriesData = groupTransactionsByCategory(transactions)

        pieChartView.setData(categoriesData)
        pieChartView.setOnSectorClickListener(object : PieChartView.OnSectorClickListener {
            override fun onSectorClick(category: String) {
                Toast.makeText(
                    this@MainActivity,
                    "Выбрана категория: $category",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun loadJsonFromRaw(): String {
        return try {
            resources.openRawResource(R.raw.payload).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    private fun parseTransactions(jsonString: String): List<Transaction> {
        return try {
            Gson().fromJson(jsonString, Array<Transaction>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun groupTransactionsByCategory(transactions: List<Transaction>): List<CategoryData> {
        val categoriesMap = mutableMapOf<String, Double>()

        transactions.forEach { transaction ->
            val currentAmount = categoriesMap[transaction.category] ?: 0.0
            categoriesMap[transaction.category] = currentAmount + transaction.amount
        }

        val colors = arrayOf(
            ContextCompat.getColor(this, android.R.color.holo_red_dark),
            ContextCompat.getColor(this, android.R.color.holo_blue_dark),
            ContextCompat.getColor(this, android.R.color.holo_green_dark),
            ContextCompat.getColor(this, android.R.color.holo_orange_dark),
            ContextCompat.getColor(this, android.R.color.holo_purple),
            ContextCompat.getColor(this, android.R.color.darker_gray),
            ContextCompat.getColor(this, android.R.color.holo_blue_light),
            ContextCompat.getColor(this, android.R.color.holo_green_light),
            ContextCompat.getColor(this, android.R.color.holo_orange_light),
            ContextCompat.getColor(this, android.R.color.holo_red_light),
            ContextCompat.getColor(this, R.color.colorPrimary),
            ContextCompat.getColor(this, R.color.colorAccent)
        )

        return categoriesMap.entries.mapIndexed { index, (category, amount) ->
            CategoryData(
                category = category,
                amount = amount,
                color = colors[index % colors.size]
            )
        }.sortedByDescending { it.amount }
    }
}