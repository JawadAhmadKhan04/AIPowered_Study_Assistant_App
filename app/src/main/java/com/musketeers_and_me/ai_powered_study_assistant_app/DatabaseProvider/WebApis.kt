package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider
import android.content.Context
import android.util.Log
import okhttp3.*
import java.io.IOException

class WebApis {
    private var base_url = "http://192.168.1.8:5000/" // ALSO ADD IP IN NETWORK_SECURITY_CONFIG.XML

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

    fun getKeyPts(context: Context, text: String, topic: String, callback: (String?) -> Unit) {

        val url = base_url + "key_points"

        val okHttpClient = OkHttpClient()

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

    fun getConceptList(context: Context, text: String, topic: String, callback: (String?) -> Unit) {
        val url = base_url + "concept_list"

        val okHttpClient = OkHttpClient()

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
    fun generateQuiz(context: Context, text: String, topic: String, questionCount: Int, callback: (String?) -> Unit) {
        val url = base_url + "quiz"
        val okHttpClient = OkHttpClient()
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("text", text)
            .addFormDataPart("context", topic)
            .addFormDataPart("question_count", questionCount.toString())
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d("WebApis", "Quiz generation error: ${e.message}")
                callback(null)
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful) {
                    Log.d("WebApis", "Quiz response: $responseBody")
                    callback(responseBody)
                } else {
                    Log.d("WebApis", "Quiz error: ${response.message}")
                    callback(null)
                }
            }
        })
    }


}