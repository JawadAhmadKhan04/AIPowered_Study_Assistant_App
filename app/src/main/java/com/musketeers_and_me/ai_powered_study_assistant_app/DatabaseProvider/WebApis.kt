package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider
import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import org.json.JSONObject

class WebApis {
    private var base_url = "http://192.168.100.54:5000/"

    fun getSummary(context: Context, text: String, callback: (String?) -> Unit) {
        val url = base_url + "summarize/"

        val okHttpClient = OkHttpClient()

        // Create a POST request body with the text
        val json = """
        {
            "text": "$text"
        }
    """.trimIndent()

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaTypeOrNull(), json
        )

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WebApis", "Error: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // Extract "summary" from JSON if needed
                callback(responseBody)
            }
        })
    }

//
//    fun getSummary(context: Context, text: String, callback: (String?) -> Unit) {
//        val url = base_url + "summarize"  // Replace with your actual IP
//
//        val queue = Volley.newRequestQueue(context)
//        val jsonBody = JSONObject()
//        jsonBody.put("text", text)
//
//        val request = JsonObjectRequest(
//            Request.Method.POST, url, jsonBody,
//            { response ->
//                val summary = response.optString("summary")
//                callback(summary)
//            },
//            { error ->
//                error.printStackTrace()
//                Log.d("WebApis", "Error: ${error.message}")
//                callback(null)
//            }
//        )
//
//        queue.add(request)
//    }

}