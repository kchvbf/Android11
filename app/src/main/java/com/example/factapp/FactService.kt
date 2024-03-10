package com.example.factapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.google.gson.Gson
import java.net.HttpURLConnection
import java.net.URL

class FactService : Service() {


    private val url = "https://catfact.ninja/facts?limit=7"

    private val broadcastIntent = Intent("com.example.factapp")

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        loadFacts()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun loadFacts() {
        Thread {

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
            broadcastIntent.putExtra("Facts", Gson().toJson(facts))
            sendBroadcast(broadcastIntent)
        }.start()
    }


}