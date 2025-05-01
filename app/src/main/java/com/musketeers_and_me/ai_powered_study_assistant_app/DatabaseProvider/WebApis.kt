package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider
import android.content.Context
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException
import org.json.JSONObject

class WebApis {
    private var base_url = "http://192.168.100.54:5000/"

    fun testServer(context: Context, callback: (String?) -> Unit) {
        val url = base_url + "test" // Replace <YOUR_IP> with your Flask server's IP

        val okHttpClient = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WebApis", "Error: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("WebApis", "Response: $responseBody")
                    callback(responseBody)
                } else {
                    Log.d("WebApis", "Error: ${response.message}")
                    callback(null)
                }
            }
        })
    }


    fun getSummary(context: Context, text: String, callback: (String?) -> Unit) {
        val url = base_url + "summarize"

        val okHttpClient = OkHttpClient()


        val jsonBody = JSONObject()
        jsonBody.put("text", text)
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonBody.toString())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")  // Ensure this header is set
            .build()



        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure (you can invoke callback with null or error message)
                Log.d("WebApis", "Error: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                // Get the response body
                val responseBody = response.body?.string()

                if (response.isSuccessful) {
                    // Pass the result to the callback
                    Log.d("WebApis", "Response: $responseBody")
                    callback(responseBody)
                } else {
                    // Handle error response, pass null or an error message to callback
                    Log.d("WebApis", "Error: ${response.message}")
                    callback(null)
                }
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