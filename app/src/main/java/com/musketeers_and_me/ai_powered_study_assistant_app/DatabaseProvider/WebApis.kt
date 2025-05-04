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
    private var base_url = "http://192.168.100.69:5000/"

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


    fun getSummary(context: Context, text: String, topic: String, callback: (String?) -> Unit) {
        val url = base_url + "summarize"

        val okHttpClient = OkHttpClient()

// Build form-data body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("text", text)
            .addFormDataPart("context", topic) // include context if needed
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build() // No need to add "Content-Type" manually for form-data

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


}