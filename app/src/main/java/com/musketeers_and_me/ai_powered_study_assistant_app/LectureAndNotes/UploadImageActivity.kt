package com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MenuItem
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.ToolbarUtils
import android.provider.MediaStore
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition

import android.util.Log
import android.widget.EditText
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBWriteOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.Utils.Functions
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream


class UploadImageActivity : AppCompatActivity() {
    private lateinit var courseTitle: TextView
    private lateinit var courseDescription: TextView
    private lateinit var takePhotoContainer: LinearLayout
    private lateinit var uploadImageContainer: LinearLayout
    private lateinit var imagePlaceholder: ImageView
    private lateinit var extractTextButton: MaterialButton
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var image_placeholder_text: TextView
    private lateinit var saveImg: MaterialButton

    private lateinit var prev_img: ImageView
    private lateinit var next_img: ImageView

    private var courseId = ""
    private val client = OkHttpClient()
    private var images_urls: List<String> = emptyList()

    private var databaseService = FBDataBaseService()
    private var ReadOperations = FBReadOperations(databaseService)
    private var WriteOperations = FBWriteOperations(databaseService)

    private var imageFile: File? = null

    private val REQUEST_IMAGE_PICK = 100
    private lateinit var recognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_upload_image)
        // Setup Toolbar
        recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) // ✅ Correct way

        ToolbarUtils.setupToolbar(this, "Upload Image", R.drawable.back, true)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Upload Image"

        //initialize views
        saveImg = findViewById(R.id.save_button)
        courseTitle = findViewById(R.id.course_title)
        courseTitle.text = intent.getStringExtra("course_title").toString()
        courseId = intent.getStringExtra("course_id").toString()
        courseDescription = findViewById(R.id.course_description)
        takePhotoContainer = findViewById(R.id.take_photo_container)
        uploadImageContainer = findViewById(R.id.upload_image_container)
        imagePlaceholder = findViewById(R.id.image_placeholder)
        extractTextButton = findViewById(R.id.extract_text_button)
        image_placeholder_text = findViewById(R.id.image_placeholder_text)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        prev_img = findViewById(R.id.left_image)
        next_img = findViewById(R.id.right_image)

        findViewById<FrameLayout>(R.id.home_button_container).setOnClickListener {
            // Create intent for MainActivity
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        Log.d("FetchingImageURLs", "Fetching image URLs for course: $courseId")
        ReadOperations.getImageUrls(courseId, { imageUrls ->
            if (imageUrls.isNotEmpty()) {
                // Do something with the image URLs (e.g., log or display them)
                Log.d("FetchedImageURLs", imageUrls.joinToString(", "))
                images_urls = imageUrls
                Log.d("FetchedImageURLs", "${images_urls}")

            } else {
                // Handle case when no image URLs are found
                Log.d("Image URLs", "No URLs found for course: $courseId")
            }
        }, { e ->
            // Handle error fetching image URLs
            Log.e("Error fetching image URLs", e.message ?: "Unknown error")
        })

        extractTextButton.setOnClickListener{
            extractTextFromImage()

        }

        saveImg.setOnClickListener {
            val imageName = Functions.generateRandomCode()
            imageFile?.let { it1 -> uploadImageToServer(it1, imageName) }
        }


        prev_img.setOnClickListener {
            // Handle previous image action
            Toast.makeText(this, "Previous image", Toast.LENGTH_SHORT).show()
        }

        next_img.setOnClickListener {
            // Handle next image action
            Toast.makeText(this, "Next image", Toast.LENGTH_SHORT).show()
        }

        takePhotoContainer.setOnClickListener {
            Toast.makeText(this, "Taking image", Toast.LENGTH_SHORT).show()
        }

        uploadImageContainer.setOnClickListener {
            Toast.makeText(this, "uplaoding", Toast.LENGTH_SHORT).show()
            selectImageFromGallery()
        }

    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            imageUri?.let {
                try {
                    // Convert the Uri to a File
                    imageFile = getFileFromUri(it)

                    // Optionally, set the image to an ImageView
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, it)
                    imagePlaceholder.setImageBitmap(bitmap)

                    // Use the imageFile for further processing (upload, save, etc.)
                    Log.d("ImageFile", "Image file path: ${imageFile!!.absolutePath}")

                    // Now you can upload or save the image file

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Convert URI to File
    private fun getFileFromUri(uri: Uri): File {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "selected_image.jpg")  // Saving to cache directory
        val outputStream: OutputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)  // Copy the content from the InputStream to the FileOutputStream

        inputStream?.close()
        outputStream.close()

        return file  // Return the File object
    }


    private fun uploadImageToServer(imageFile: File, file_name: String) {
        if (!imageFile.exists() || imageFile.length().toInt() == 0) {
            Log.e("NewImageUploadActivity", "Image file does not exist or is empty")
            runOnUiThread {
                Toast.makeText(this, "No image file to upload", Toast.LENGTH_SHORT).show()
            }
            return
        }

        // Create a new file with the desired name (if you want to keep the original file's extension)
        val newImageFile = File(imageFile.parent, "$file_name.${imageFile.extension}")

        // Rename the original file to the new name (optional, if you want to rename it locally before uploading)
        imageFile.copyTo(newImageFile, overwrite = true)

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", newImageFile.name, newImageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())) // Use the new file name
            .build()

        val request = Request.Builder()
            .url("http://stripe-solstice-cloak.glitch.me/upload-image")
            .post(requestBody)
            .build()

        Log.d("NewImageUploadActivity", "Uploading image file: ${newImageFile.absolutePath}, size: ${newImageFile.length()} bytes")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e("NewImageUploadActivity", "Upload failed: ${e.message}")
                    Toast.makeText(this@UploadImageActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("NewImageUploadActivity", "Upload response: $responseBody, status: ${response.code}")
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val json = JSONObject(responseBody)
                        val imageUrl = json.optString("url").takeIf { it.isNotEmpty() }
                        if (imageUrl != null) {
                            Log.d("NewImageUploadActivity", "Image URL received: $imageUrl")
                            WriteOperations.saveImageUrl(courseId = courseId, imageUrl.toString())
                            runOnUiThread {
                                Toast.makeText(this@UploadImageActivity, "Image URL: $imageUrl", Toast.LENGTH_SHORT).show()
                            }
                            // Do something with the image URL, like saving it or displaying it
                        } else {
                            runOnUiThread {
                                Log.e("NewImageUploadActivity", "Image URL is missing or empty in response")
                                Toast.makeText(this@UploadImageActivity, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Log.e("NewImageUploadActivity", "Failed to parse upload response: ${e.message}")
                            Toast.makeText(this@UploadImageActivity, "Invalid server response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        Log.e("NewImageUploadActivity", "Upload failed, status: ${response.code}, message: ${response.message}")
                        Toast.makeText(this@UploadImageActivity, "Upload failed: ${response.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                response.close()
            }
        })
    }


    private fun extractTextFromImage() {
        val drawable = imagePlaceholder.drawable
        if (drawable == null) {
            image_placeholder_text.text = "Please select an image first."
            return
        }

        val bitmap = (drawable as BitmapDrawable).bitmap
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                displayExtractedText(visionText)
            }
            .addOnFailureListener { e ->
                image_placeholder_text.text = "Failed to extract text: ${e.message}"
                Log.e("TextRecognitionError", "Error: ${e.message}")
            }
    }

    private fun displayExtractedText(visionText: com.google.mlkit.vision.text.Text) {
        val extractedText = visionText.text
        image_placeholder_text.text = extractedText.ifEmpty { "No text found in image" }
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
}