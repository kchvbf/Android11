package com.example.factapp

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class FactWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    private val url = "https://catfact.ninja/facts?limit=7"

    @WorkerThread
    override fun doWork(): Result {

        Thread.sleep(6666)

        var facts: List<Fact> = emptyList()

        try {

            val url = URL(url)

            val conn = url.openConnection() as HttpURLConnection

            conn.connectTimeout = 10000

            conn.requestMethod = "GET"

            val response = conn.inputStream.bufferedReader().readText()
            conn.disconnect()

            val json = Gson().fromJson(response, Map::class.java)

            facts = (json["data"] as List<Map<String, Any>>).map {
                Fact(
                    fact = it["fact"] as String,
                    length = (it["length"] as Double).toInt()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val outputData = Data.Builder()
            .putString("Facts", Gson().toJson(facts))
            .build()

        return Result.success(outputData)
    }
}