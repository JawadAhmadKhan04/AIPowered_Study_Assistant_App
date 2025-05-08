package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.*

class NewVoiceNoteActivity : AppCompatActivity() {
    private lateinit var contentLayout: LinearLayout
    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var noteTitle: EditText
    private lateinit var recordingTime: TextView
    private lateinit var recordButton: MaterialButton
    private lateinit var saveButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingThread: Thread? = null
    private lateinit var pcmFile: File
    private lateinit var wavFile: File
    private val sampleRate = 44100
    private val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    private var audioUrl: String? = null
    private var transcription: String? = null
    private val client = OkHttpClient()
    private val fbWriteOperations = FBWriteOperations(FBDataBaseService())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_voice_note)
        ToolbarUtils.setupToolbar(this, "New Voice Note", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "New Voice Note"

        // Check authentication
        if (FirebaseAuth.getInstance().currentUser == null) {
            Log.e("NewVoiceNoteActivity", "User not authenticated")
            Toast.makeText(this, "Please sign in", Toast.LENGTH_SHORT).show()
            // TODO: Redirect to login activity
            finish()
            return
        }

        contentLayout = findViewById(R.id.content_layout)
        courseTitle = findViewById(R.id.course_title)
        courseTitle.text = intent.getStringExtra("course_title") ?: "Unknown Course"
        courseDescription = findViewById(R.id.course_description)
        noteTitle = findViewById(R.id.note_title)
        recordingTime = findViewById(R.id.recording_time)
        recordButton = findViewById(R.id.record_button)
        saveButton = findViewById(R.id.save_button)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        checkPermissions()

        recordButton.setOnClickListener {
            if (!isRecording) {
                startRecording()
                recordButton.text = "Stop Recording"
            } else {
                stopRecording()
                recordButton.text = "Start Recording"
            }
        }

        saveButton.setOnClickListener {
            if (noteTitle.text.isEmpty()) {
                Toast.makeText(this, "Please enter a note title", Toast.LENGTH_SHORT).show()
            } else if (audioUrl == null || !wavFile.exists()) {
                Toast.makeText(this, "Please record audio", Toast.LENGTH_SHORT).show()
                Log.e(
                    "NewVoiceNoteActivity",
                    "Audio URL: $audioUrl, WAV file exists: ${wavFile.exists()}"
                )
            } else {
                val courseId = intent.getStringExtra("course_id") ?: ""
                if (courseId.isEmpty()) {
                    Toast.makeText(this, "Invalid course ID", Toast.LENGTH_SHORT).show()
                    Log.e("NewVoiceNoteActivity", "Course ID is empty")
                }
                else {
                    Log.d("NewVoiceNoteActivity", "Saving to Firebase: courseId=$courseId, title=${noteTitle.text}, audioUrl=$audioUrl, transcription=$transcription")
                    fbWriteOperations.saveNotes(courseId, noteTitle.text.toString(), transcription ?: "", audioUrl ?: "", "voice", 0)
//                    val intent = Intent(this, VoiceNoteActivity::class.java)
//                    intent.putExtra("course_title", courseTitle.text.toString())
//                    intent.putExtra("note_title", noteTitle.text.toString())
//                    intent.putExtra("audio_url", audioUrl)
//                    intent.putExtra("transcription", transcription)
//                    startActivity(intent)
                    finish()
                }
          }

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }
    }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET)
        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()
        if (neededPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, neededPermissions, 200)
        } else {
            Log.d("NewVoiceNoteActivity", "All permissions granted")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            Log.d("NewVoiceNoteActivity", "Permissions granted")
        } else {
            Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            checkPermissions()
            return
        }

        pcmFile = File(cacheDir, "temp_audio_${System.currentTimeMillis()}.pcm")
        wavFile = File(cacheDir, "audio_${System.currentTimeMillis()}.wav")
        Log.d("NewVoiceNoteActivity", "Starting recording, PCM file: ${pcmFile.absolutePath}, WAV file: ${wavFile.absolutePath}")

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        try {
            audioRecord?.startRecording()
            isRecording = true
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("NewVoiceNoteActivity", "Failed to start recording: ${e.message}")
            Toast.makeText(this, "Failed to start recording", Toast.LENGTH_SHORT).show()
            return
        }

        recordingThread = Thread {
            FileOutputStream(pcmFile).use { output ->
                val buffer = ByteArray(bufferSize)
                while (isRecording) {
                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        output.write(buffer, 0, read)
                    }
                }
            }
            Log.d("NewVoiceNoteActivity", "PCM file size: ${pcmFile.length()} bytes")
            convertPcmToWav(pcmFile, wavFile)
            Log.d("NewVoiceNoteActivity", "WAV file size: ${wavFile.length()} bytes, exists: ${wavFile.exists()}")
            pcmFile.delete()
            runOnUiThread { uploadToServer() }
        }
        recordingThread?.start()
    }

    private fun stopRecording() {
        isRecording = false
        try {
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            recordingThread = null
            Toast.makeText(this, "Recording stopped", Toast.LENGTH_SHORT).show()
            Log.d("NewVoiceNoteActivity", "Recording stopped")
        } catch (e: Exception) {
            Log.e("NewVoiceNoteActivity", "Failed to stop recording: ${e.message}")
            Toast.makeText(this, "Failed to stop recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToServer() {
        if (!wavFile.exists() || wavFile.length().toInt() == 0) {
            Log.e("NewVoiceNoteActivity", "WAV file does not exist or is empty")
            runOnUiThread {
                Toast.makeText(this, "No audio file to upload", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("audio", wavFile.name, wavFile.asRequestBody("audio/wav".toMediaTypeOrNull()))
            .build()

        val request = Request.Builder()
            .url("http://stripe-solstice-cloak.glitch.me/upload-audio")
            .post(requestBody)
            .build()

        Log.d("NewVoiceNoteActivity", "Uploading WAV file: ${wavFile.absolutePath}, size: ${wavFile.length()} bytes")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NewVoiceNoteActivity", "Upload failed: ${e.message}")
                    Toast.makeText(this@NewVoiceNoteActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("NewVoiceNoteActivity", "Upload response: $responseBody, status: ${response.code}")
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        audioUrl = json.optString("url").takeIf { it.isNotEmpty() }
                        if (audioUrl != null) {
                            Log.d("NewVoiceNoteActivity", "Audio URL received: $audioUrl")
                            runOnUiThread {
                                Toast.makeText(this@NewVoiceNoteActivity, "Audio URL: $audioUrl", Toast.LENGTH_SHORT).show()
                            }
                            fetchTranscription(audioUrl!!)
                        } else {
                            runOnUiThread {
                                Log.e("NewVoiceNoteActivity", "Audio URL is missing or empty in response")
                                Toast.makeText(this@NewVoiceNoteActivity, "Failed to get audio URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Log.e("NewVoiceNoteActivity", "Failed to parse upload response: ${e.message}")
                            Toast.makeText(this@NewVoiceNoteActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("NewVoiceNoteActivity", "Upload failed, status: ${response.code}, message: ${response.message}")
                        Toast.makeText(this@NewVoiceNoteActivity, "Upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }

    private fun fetchTranscription(audioUrl: String) {
        val json = JSONObject().put("url", audioUrl)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("http://stripe-solstice-cloak.glitch.me/speech-to-text-url")
            .post(requestBody)
            .build()

        Log.d("NewVoiceNoteActivity", "Fetching transcription for URL: $audioUrl")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NewVoiceNoteActivity", "Transcription failed: ${e.message}")
                    Toast.makeText(this@NewVoiceNoteActivity, "Transcription failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("NewVoiceNoteActivity", "Transcription response: $responseBody, status: ${response.code}")
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        transcription = json.optString("text").takeIf { it != "Could not understand audio" && it.isNotEmpty() }
                        runOnUiThread {
                            Log.d("NewVoiceNoteActivity", "Transcription received: $transcription")
                            Toast.makeText(
                                this@NewVoiceNoteActivity,
                                if (transcription != null) "Transcription: $transcription" else "No transcription available",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Log.e("NewVoiceNoteActivity", "Failed to parse transcription response: ${e.message}")
                            Toast.makeText(this@NewVoiceNoteActivity, "Invalid transcription response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("NewVoiceNoteActivity", "Transcription failed, status: ${response.code}, message: ${response.message}")
                        Toast.makeText(this@NewVoiceNoteActivity, "Transcription failed", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }

    private fun convertPcmToWav(pcmFile: File, wavFile: File) {
        if (!pcmFile.exists() || pcmFile.length().toInt() == 0) {
            Log.e("NewVoiceNoteActivity", "PCM file does not exist or is empty")
            return
        }
        val pcmData = pcmFile.readBytes()
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val byteRate = 16 * sampleRate * 1 / 8

        val header = ByteArray(44)
        writeWavHeader(header, totalAudioLen, totalDataLen, sampleRate, 1, byteRate)

        try {
            FileOutputStream(wavFile).use { out ->
                out.write(header)
                out.write(pcmData)
            }
            Log.d("NewVoiceNoteActivity", "Converted PCM to WAV, WAV file size: ${wavFile.length()} bytes")
        } catch (e: IOException) {
            Log.e("NewVoiceNoteActivity", "Failed to convert PCM to WAV: ${e.message}")
        }
    }

    private fun writeWavHeader(
        header: ByteArray,
        audioLen: Int,
        dataLen: Int,
        sampleRate: Int,
        channels: Int,
        byteRate: Int
    ) {
        val intToBytes = { value: Int -> byteArrayOf(
            (value and 0xff).toByte(),
            (value shr 8 and 0xff).toByte(),
            (value shr 16 and 0xff).toByte(),
            (value shr 24 and 0xff).toByte()
        ) }
        val shortToBytes = { value: Short -> byteArrayOf(
            (value.toInt() and 0xff).toByte(),
            (value.toInt() shr 8 and 0xff).toByte()
        ) }

        val headerData = ByteArrayOutputStream().apply {
            write("RIFF".toByteArray())
            write(intToBytes(dataLen))
            write("WAVE".toByteArray())
            write("fmt ".toByteArray())
            write(intToBytes(16)) // Subchunk1Size
            write(shortToBytes(1)) // AudioFormat
            write(shortToBytes(channels.toShort()))
            write(intToBytes(sampleRate))
            write(intToBytes(byteRate))
            write(shortToBytes((channels * 16 / 8).toShort()))
            write(shortToBytes(16)) // BitsPerSample
            write("data".toByteArray())
            write(intToBytes(audioLen))
        }.toByteArray()

        header.copyFrom(headerData)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isRecording) {
            stopRecording()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioRecord?.release()
        audioRecord = null
        if (::pcmFile.isInitialized) pcmFile.delete()
        if (::wavFile.isInitialized) wavFile.delete()
        Log.d("NewVoiceNoteActivity", "Cleaned up temporary files")
    }
}

private fun ByteArray.copyFrom(source: ByteArray) {
    for (i in source.indices) {
        this[i] = source[i]
    }
}