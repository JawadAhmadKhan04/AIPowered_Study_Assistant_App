package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.util.Log
import android.widget.Toast
import android.content.Context
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.onesignal.OneSignal
import com.musketeers_and_me.ai_powered_study_assistant_app.Services.OneSignalService
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import org.json.JSONArray
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*
import com.android.volley.RequestQueue
import com.android.volley.toolbox.HurlStack
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import android.net.ConnectivityManager

class FBWriteOperations (private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()

    // Create a trust manager that does not validate certificate chains
    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
        override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
    })

    // Create a SSL socket factory with our all-trusting trust manager
    private fun getSSLSocketFactory(): SSLSocketFactory {
        try {
            val sslContext = SSLContext.getInstance("TLS")
            sslContext.init(null, trustAllCerts, SecureRandom())
            return sslContext.socketFactory
        } catch (e: Exception) {
            Log.e("SSL", "Error creating SSL socket factory", e)
            throw RuntimeException(e)
        }
    }

    // Custom Volley HurlStack that uses our SSL socket factory
    private inner class CustomHurlStack : HurlStack() {
        override fun createConnection(url: URL): HttpURLConnection {
            val connection = super.createConnection(url)
            if (connection is HttpsURLConnection) {
                connection.sslSocketFactory = getSSLSocketFactory()
                connection.hostnameVerifier = HostnameVerifier { _, _ -> true }
            }
            return connection
        }
    }

    // Function to get a custom RequestQueue that accepts self-signed certificates
    private fun getCustomRequestQueue(context: Context): RequestQueue {
        return Volley.newRequestQueue(context, CustomHurlStack())
    }

    fun saveImageUrl(courseId: String, image_url: String) {
        val courseRef = databaseService.coursesRef.child(courseId)

        // Push the new image URL to the "image_urls" array.
        val imageUrlsRef = courseRef.child("image_urls")
        imageUrlsRef.push().setValue(image_url)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Image URL saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to save image URL", e)
            }
    }


    fun updateDigest(noteId: String, content: String, updation: String) {
        // Reference to the specific note's data
        val noteRef = databaseService.notesRef.child(noteId)

        // Prepare the updated data, here we assume you generate the summary based on content
        val updatedData = mapOf<String, Any>(
            updation to content // Use content or any derived summary here
        )

        // Update only the summary field
        noteRef.updateChildren(updatedData).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Successfully updated the summary
                println("$updation updated successfully!")
                val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")
                userProfileRef.child("smartDigests").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val smartDigests = snapshot.getValue(Int::class.java) ?: 0
                        userProfileRef.child("smartDigests").setValue(smartDigests + 1)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to update quiz count", error.toException())
                    }
                })
            } else {
                // Failed to update
                println("Failed to update summary: ${task.exception?.message}")
            }
        }
    }

    fun updateNotes(
        noteId: String,
        noteContent: String,
        noteAudio: String? = null,
        type: String,
        tag: Int,
    ) {
        if (currentUserId.isEmpty()) {
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }

        val noteRef = databaseService.notesRef.child(noteId)

        val updatedData = mapOf(
            "content" to noteContent,
            "audio" to (noteAudio ?: ""),
            "type" to type.lowercase(), // e.g., "text" or "voice"
            "tag" to tag
        )

        noteRef.updateChildren(updatedData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Note updated successfully")
            }
            .addOnFailureListener { e ->
                Log.d("FBWriteOperations", "Failed to update note", e)
            }
    }

    fun saveNotes(
        courseId: String,
        noteTitle: String,
        noteContent: String,
        noteAudio: String? = null,
        type: String,
        tag: Int,
    ) {
        if (currentUserId.isEmpty()) {
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }


        val noteId = databaseService.notesRef.push().key ?: return
        val timestamp = System.currentTimeMillis()

        val noteData = mapOf(
            "courseId" to courseId,
            "title" to noteTitle,
            "content" to noteContent,
            "audio" to (noteAudio ?: ""),
            "type" to type.lowercase(), // e.g., "text" or "voice"
            "createdBy" to currentUserId,
            "createdAt" to timestamp,
            "updatedAt" to timestamp,
            "tag" to tag,
            "keyPoints" to emptyList<String>(),
            "summary" to "",
            "conceptList" to emptyList<String>(),
            "members" to mapOf(
                currentUserId to mapOf(
                    "lastModified" to timestamp
                )
            )
        )

        val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")

        databaseService.notesRef.child(noteId).setValue(noteData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Note saved successfully")
                userProfileRef.child("lectures").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLectures = snapshot.getValue(Int::class.java) ?: 0
                        userProfileRef.child("lectures").setValue(currentLectures + 1)
                        // Now you can use currentCourses safely
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to read courses count", error.toException())
                    }
                })

            }
            .addOnFailureListener { e ->
                Log.d("FBWriteOperations", "Failed to save note", e)
            }
    }


    fun bookmark_course(courseId: String, isBookmarked: Boolean) {
        if (currentUserId.isEmpty()) {
            // Handle the case where the user is not authenticated (optional)
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }

        val db = databaseService.usersRef.child(currentUserId).child("bookmarks").child(courseId)

        db.setValue(isBookmarked)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Bookmark status updated successfully")
            }
            .addOnFailureListener { e ->
                Log.d("FBWriteOperations", "Failed to update bookmark status", e)
            }
    }

    fun CreateCourse(title: String, description: String, colorInt: Int) {
        val db = databaseService.coursesRef
        val courseId = db.push().key ?: return
        val timestamp = System.currentTimeMillis()

        val courseData = mapOf(
            "title" to title,
            "description" to description,
            "createdBy" to currentUserId,
            "color" to colorInt,
            "members" to mapOf(
                currentUserId to mapOf(
                    "lastModified" to timestamp
                )
            )
        )

        Log.d("CreateCourseActivity", "Course ID: $courseId")
        Log.d("CreateCourseActivity", "Course Data: $courseData")
        val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")

        db.child(courseId).setValue(courseData)
            .addOnSuccessListener {

                userProfileRef.child("courses").addListenerForSingleValueEvent(object :
                    ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val currentCourses = snapshot.getValue(Int::class.java) ?: 0
                            userProfileRef.child("courses").setValue(currentCourses + 1)
                            // Now you can use currentCourses safely
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Firebase", "Failed to read courses count", error.toException())
                        }
                })

                Log.d("CreateCourseActivity", "Course created successfully")
            }
            .addOnFailureListener { e ->
                Log.d("CreateCourseActivity", "Failed to create course", e)
            }
    }

    fun saveProfileStats(course: Int = 0, lectures: Int = 0, smartDigests: Int = 0, quizzes: Int = 0, groups: Int = 0, timeSpent: String = "00:00:00") {
        if (currentUserId.isEmpty()) {
            // Handle the case where the user is not authenticated (optional)
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }

        // Reference to the user's profile stats in Firebase
        val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")

        // Set the values in Firebase under the profileStats node
        userProfileRef.child("courses").setValue(course)
        userProfileRef.child("lectures").setValue(lectures)
        userProfileRef.child("smartDigests").setValue(smartDigests)
        userProfileRef.child("quizzes").setValue(quizzes)
        userProfileRef.child("groups").setValue(groups)
        userProfileRef.child("timeSpent").setValue(timeSpent)
    }

    fun saveSettings(quizNotifications: Boolean, studyReminders: Boolean, addInGroups: Boolean, autoLogin: Boolean, autoSync: Boolean) {
        if (currentUserId.isEmpty()) {
            // Handle the case where the user is not authenticated (optional)
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }

        // Reference to the user's settings in Firebase
        val userSettingsRef = databaseService.usersRef.child(currentUserId).child("settings")

        // Set the values in Firebase under the settings node
        userSettingsRef.child("quizNotifications").setValue(quizNotifications)
        userSettingsRef.child("studyReminders").setValue(studyReminders)
        userSettingsRef.child("addInGroups").setValue(addInGroups)
        userSettingsRef.child("autoLogin").setValue(autoLogin)
        userSettingsRef.child("autoSync").setValue(autoSync)

    }

    fun saveUser(user: UserProfile) {
        if (currentUserId.isEmpty()) {
            Log.e("FBWriteOperations", "User is not authenticated")
            return
        }

        val userRef = databaseService.getUserRef(user.id)
        userRef.setValue(user)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "User saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to save user", e)
            }
    }

    fun saveCourse(course: Course) {
        if (currentUserId.isEmpty()) {
            Log.e("FBWriteOperations", "User is not authenticated")
            return
        }

        val courseRef = databaseService.getCourseRef(course.courseId)
        val timestamp = System.currentTimeMillis()
        
        val courseData = mapOf(
            "title" to course.title,
            "description" to course.description,
            "createdBy" to currentUserId,
            "color" to course.buttonColorResId,
            "members" to mapOf(
                currentUserId to mapOf(
                    "lastModified" to timestamp
                )
            )
        )

        courseRef.setValue(courseData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Course saved successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to save course", e)
            }
    }
    fun saveQuiz(
        noteId: String,
        title: String,
        questions: List<Map<String, Any>>,
        onSuccess: (String) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        if (currentUserId.isEmpty()) {
            Log.d("FBWriteOperations", "User is not authenticated")
            onFailure(Exception("User not authenticated"))
            return
        }
        val quizId = databaseService.quizzesRef.push().key ?: return
        val timestamp = System.currentTimeMillis()
        Log.d("FBWriteOperations", "Saving quiz with questions: $questions")
        val quizData = mapOf(
            "noteId" to noteId,
            "title" to title,
            "createdBy" to currentUserId,
            "createdAt" to timestamp,
            "feedback" to "",
            "questions" to questions.associateBy { databaseService.quizzesRef.child(quizId).child("questions").push().key ?: "" }
        )
        databaseService.quizzesRef.child(quizId).setValue(quizData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Quiz saved successfully with ID: $quizId")
                val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")
                userProfileRef.child("quizzes").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentQuizzes = snapshot.getValue(Int::class.java) ?: 0
                        userProfileRef.child("quizzes").setValue(currentQuizzes + 1)
                    }
                    override fun onCancelled(error: DatabaseError) {
                        Log.e("Firebase", "Failed to update quiz count", error.toException())
                    }
                })
                onSuccess(quizId)
            }
            .addOnFailureListener { e ->
                Log.d("FBWriteOperations", "Failed to save quiz", e)
                onFailure(e)
            }
    }

    fun updateQuizResults(
        quizId: String,
        score: Int,
        feedback: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        if (currentUserId.isEmpty()) {
            Log.d("FBWriteOperations", "User is not authenticated")
            onFailure(Exception("User not authenticated"))
            return
        }
        val quizRef = databaseService.quizzesRef.child(quizId)
        val updatedData = mapOf(
            "score" to score,
            "feedback" to feedback,
            "completedAt" to System.currentTimeMillis()
        )
        quizRef.updateChildren(updatedData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Quiz results updated successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.d("FBWriteOperations", "Failed to update quiz results", e)
                onFailure(e)
            }
    }
//    fun saveVoiceNote(courseId: String, title: String, audioUrl: String, transcription: String) {
//        if (currentUserId.isEmpty()) {
//            Log.e("FBWriteOperations", "User is not authenticated")
//            return
//        }
//        val notesRef = databaseService.getNotesRef()
//        val noteId = notesRef.push().key ?: return
//        val timestamp = System.currentTimeMillis()
//        val noteData = mapOf(
//            "courseId" to courseId,
//            "title" to title,
//            "content" to transcription,
//            "audio" to audioUrl,
//            "type" to "voice",
//            "createdBy" to currentUserId,
//            "createdAt" to timestamp,
//            "updatedAt" to timestamp,
//            "members" to mapOf(
//                currentUserId to mapOf(
//                    "lastModified" to timestamp
//                )
//            )
//        )
//        notesRef.child(noteId).setValue(noteData)
//            .addOnSuccessListener {
//                Log.d("FBWriteOperations", "Voice note saved successfully")
//                val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")
//                userProfileRef.child("lectures").addListenerForSingleValueEvent(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val currentLectures = snapshot.getValue(Int::class.java) ?: 0
//                        userProfileRef.child("lectures").setValue(currentLectures + 1)
//                    }
//                    override fun onCancelled(error: DatabaseError) {
//                        Log.e("Firebase", "Failed to read lectures count", error.toException())
//                    }
//                })
//            }
//            .addOnFailureListener { e ->
//                Log.e("FBWriteOperations", "Failed to save voice note", e)
//            }
//    }

    // Group Study Operations
    fun createStudyGroup(name: String, description: String, onComplete: (String?) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBWriteOperations", "User is not authenticated")
            onComplete(null)
            return
        }

        val groupId = databaseService.studyGroupsRef.push().key ?: return
        val timestamp = System.currentTimeMillis()
        val code = generateGroupCode()
        
        Log.d("FBWriteOperations", "Creating study group with name: $name, code: $code")

        val groupData = mapOf(
            "name" to name,
            "description" to description,
            "createdBy" to currentUserId,
            "createdAt" to timestamp,
            "code" to code,
            "members" to mapOf(
                currentUserId to mapOf(
                    "role" to "admin",
                    "joinedAt" to timestamp
                )
            )
        )

        databaseService.studyGroupsRef.child(groupId).setValue(groupData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Study group created successfully with ID: $groupId, code: $code")
                // Update user's group count
                updateUserGroupCount(1)
                onComplete(groupId)
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to create study group", e)
                onComplete(null)
            }
    }

    fun joinStudyGroup(groupCode: String, onComplete: (Boolean) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBWriteOperations", "User is not authenticated")
            onComplete(false)
            return
        }

        // Normalize the code (trim and uppercase)
        val normalizedCode = groupCode.trim().uppercase()
        Log.d("FBWriteOperations", "Attempting to join group with code: $normalizedCode")

        // First, get all groups
        databaseService.studyGroupsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("FBWriteOperations", "Retrieved ${snapshot.childrenCount} groups")
                
                // Find the group with matching code
                var foundGroupId: String? = null
                var foundGroupName: String? = null
                
                for (groupSnapshot in snapshot.children) {
                    val code = groupSnapshot.child("code").getValue(String::class.java)
                    val name = groupSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    
                    Log.d("FBWriteOperations", "Checking group: $name (${groupSnapshot.key}), code: $code")
                    
                    if (code == normalizedCode) {
                        foundGroupId = groupSnapshot.key
                        foundGroupName = name
                        Log.d("FBWriteOperations", "Found matching group: $foundGroupName (ID: $foundGroupId)")
                        break
                    }
                }
                
                if (foundGroupId == null) {
                    Log.e("FBWriteOperations", "No group found with code: $normalizedCode")
                    onComplete(false)
                    return
                }
                
                // Check if user is already a member
                val membersRef = snapshot.child(foundGroupId).child("members")
                if (membersRef.hasChild(currentUserId)) {
                    Log.d("FBWriteOperations", "User is already a member of this group")
                    onComplete(true)
                    return
                }
                
                // Add user to group members
                val timestamp = System.currentTimeMillis()
                val memberData = mapOf(
                    "role" to "member",
                    "joinedAt" to timestamp
                )
                
                databaseService.studyGroupsRef.child(foundGroupId)
                    .child("members")
                    .child(currentUserId)
                    .setValue(memberData)
                    .addOnSuccessListener {
                        Log.d("FBWriteOperations", "Successfully joined group: $foundGroupName")
                        updateUserGroupCount(1)
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FBWriteOperations", "Failed to join group", e)
                        onComplete(false)
                    }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("FBWriteOperations", "Error finding group", error.toException())
                onComplete(false)
            }
        })
    }

    fun sendGroupMessage(groupId: String, message: GroupMessage, onComplete: (Boolean) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBWriteOperations", "User is not authenticated")
            onComplete(false)
            return
        }

        val messageId = databaseService.groupChatsRef.child(groupId).child("messages").push().key ?: return
        val timestamp = System.currentTimeMillis()
        
        // Get network status from a connectivity manager
        val connectivityManager = databaseService.context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        val isOffline = networkInfo?.isConnected != true
        
        // Create a complete message with the generated ID
        val completeMessage = message.copy(
            id = messageId,
            // Add a flag to indicate if this message was created while offline
            // This will be used to determine if a notification should be sent when synced
            wasOffline = isOffline
        )

        val messageData = mapOf(
            "id" to messageId,
            "senderId" to completeMessage.senderId,
            "senderName" to completeMessage.senderName,
            "content" to completeMessage.content,
            "timestamp" to completeMessage.timestamp,
            "messageType" to completeMessage.messageType.name,
            "noteId" to completeMessage.noteId,
            "noteType" to completeMessage.noteType,
            "wasOffline" to isOffline  // Add this field to the Firebase data
        )

        databaseService.groupChatsRef.child(groupId)
            .child("messages")
            .child(messageId)
            .setValue(messageData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Message sent successfully")
                
                // Only send notification if we're online
                if (!isOffline) {
                    // Send notification to group members
                    sendGroupMessageNotification(groupId, completeMessage)
                } else {
                    Log.d("FBWriteOperations", "Device is offline, notification will be sent when synced")
                }
                
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to send message", e)
                onComplete(false)
            }
    }
    
    /**
     * Sends notifications to all members of a group when a new message is sent
     * Uses a PHP backend to handle the notification delivery
     * @param groupId The ID of the group
     * @param message The message that was sent
     */
    fun sendGroupMessageNotification(groupId: String, message: GroupMessage) {
        Log.d("NotificationDebug", "Preparing to send notifications for group message")
        Log.d("NotificationDebug", "Message sender ID: ${message.senderId}")
        Log.d("NotificationDebug", "Current user ID: $currentUserId")
        
        // Force sending notification regardless of sender - for testing purposes
        // Comment this section out when not testing
        /*if (message.senderId == currentUserId) {
            Log.d("NotificationDebug", "Not sending notification to sender")
            return
        }*/
        
        // Check for context
        val context = databaseService.context
        if (context == null) {
            Log.e("NotificationDebug", "Cannot send notification: context is null")
            return
        }
        
        Log.d("NotificationDebug", "Getting group info for group: $groupId")
        
        // Get the group info to include in notification
        databaseService.getGroupRef(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(groupSnapshot: DataSnapshot) {
                if (!groupSnapshot.exists()) {
                    Log.e("NotificationDebug", "Group not found: $groupId")
                    return
                }
                
                val groupName = groupSnapshot.child("name").getValue(String::class.java) ?: "Group Chat"
                Log.d("NotificationDebug", "Group name: $groupName")
                
                // Get all members of the group
                val membersRef = groupSnapshot.child("members")
                Log.d("NotificationDebug", "Total members in group: ${membersRef.childrenCount}")
                
                // For each member, send notification through the backend
                for (memberSnapshot in membersRef.children) {
                    val memberId = memberSnapshot.key ?: continue
                    Log.d("NotificationDebug", "Checking member: $memberId")
                    
                    // Skip the sender - using message.senderId for comparison instead of currentUserId
                    if (memberId == message.senderId) {
                        Log.d("NotificationDebug", "Skipping sender: $memberId")
                        continue
                    }
                    
                    Log.d("NotificationDebug", "Sending notification to member: $memberId")
                    
                    // Send notification to this member
                    sendGroupMessageToUser(
                        context,
                        memberId,
                        groupName,
                        "${message.senderName}: ${message.content}",
                        groupId,
                        message
                    )
                }
            }
            
            override fun onCancelled(error: DatabaseError) {
                Log.e("NotificationDebug", "Error getting group details", error.toException())
            }
        })
    }
    
    /**
     * Sends a notification to a specific user through the PHP backend
     * Following exactly the structure of the sample function
     */
    fun sendGroupMessageToUser(
        context: Context,
        recipientUserId: String,
        title: String,
        body: String,
        groupId: String,
        message: GroupMessage
    ) {
        Log.d("NotificationDebug", "Attempting to send notification")
        Log.d("NotificationDebug", "Recipient User ID: $recipientUserId")
        Log.d("NotificationDebug", "Message from: ${message.senderName} (${message.senderId})")
        Log.d("NotificationDebug", "Message content: ${message.content}")
        
        // Get sender's ID for verification
        val sharedPref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val senderId = sharedPref.getString("user_id", message.senderId)
        Log.d("NotificationDebug", "Sender User ID from SharedPrefs: $senderId")

        val url = databaseService.ip_address + "send_notification.php"
        Log.d("NotificationDebug", "Notification URL: $url")
        
        // Use custom request queue that accepts self-signed certificates
        val requestQueue = getCustomRequestQueue(context)
        
        val params = HashMap<String, String>()
        // Required parameters 
        params["recipientUserId"] = recipientUserId
        params["title"] = title
        params["body"] = body
        params["sender_id"] = senderId ?: message.senderId
        params["type"] = "group_message"
        
        // Group specific parameters
        params["groupId"] = groupId
        params["messageId"] = message.id
        params["senderName"] = message.senderName
        params["messageType"] = message.messageType.name
        params["timestamp"] = message.timestamp.toString()
        
        // Add optional deep link for the app to open the correct group chat
        params["url"] = "studysmart://group/$groupId"
        
        // Add optional Android specific parameters for better notification display
        params["android_channel_id"] = "group_messages"
        params["android_group"] = groupId 
        params["badge_count"] = "1"

        Log.d("NotificationDebug", "Request parameters: $params")
        Log.d("NotificationDebug", "IMPORTANT - Verify that your PHP script (send_notification.php) exists at: $url")
        Log.d("NotificationDebug", "IMPORTANT - Verify that your PHP script has the correct OneSignal REST API key configured")

        val request = object : StringRequest(
            Request.Method.POST, 
            url,
            { response ->
                try {
                    Log.d("NotificationDebug", "Raw response: $response")
                    try {
                        val jsonResponse = JSONObject(response)
                        
                        if (jsonResponse.has("id")) {
                            // Success
                            Log.d("NotificationDebug", "Notification sent successfully: ${jsonResponse.getString("id")}")
                        } else if (jsonResponse.has("errors")) {
                            // Error
                            val errors = jsonResponse.getJSONArray("errors")
                            Log.e("NotificationDebug", "Error sending notification: $errors")
                        } else {
                            // Unknown response format
                            Log.e("NotificationDebug", "Unexpected response format: $jsonResponse")
                        }
                    } catch (e: Exception) {
                        // Response might not be JSON
                        Log.e("NotificationDebug", "Error parsing response as JSON. Raw response: $response")
                        Log.e("NotificationDebug", "This may indicate your PHP script is returning HTML instead of JSON")
                        e.printStackTrace()
                    }
                } catch (e: Exception) {
                    Log.e("NotificationDebug", "Error handling notification response: ${e.message}")
                    Log.e("NotificationDebug", "Raw response causing error: $response")
                    e.printStackTrace()
                }
            },
            { error ->
                Log.e("NotificationDebug", "Network error: ${error.message}")
                error.printStackTrace()
                
                // Get more details about the error
                val statusCode = error.networkResponse?.statusCode
                val errorData = error.networkResponse?.data?.let { String(it) } ?: "No data"
                Log.e("NotificationDebug", "Error status code: $statusCode")
                Log.e("NotificationDebug", "Error data: $errorData")
                
                if (statusCode == 404) {
                    Log.e("NotificationDebug", "ERROR: Your PHP script was not found (404). Verify the URL: $url")
                } else if (statusCode == 500) {
                    Log.e("NotificationDebug", "ERROR: Your PHP script encountered a server error (500). Check the server logs.")
                } else if (statusCode == null) {
                    Log.e("NotificationDebug", "ERROR: Could not connect to server. Verify that XAMPP is running and the URL is correct.")
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> = params
        }

        // Add a tag for easy identification in Volley debug logs
        request.tag = "group_notification"
        Log.d("NotificationDebug", "Adding request to queue")
        
        // Add the request to the queue
        requestQueue.add(request)
    }

    private fun updateUserGroupCount(increment: Int) {
        val userProfileRef = databaseService.usersRef.child(currentUserId).child("profile")
        userProfileRef.child("groups").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentGroups = snapshot.getValue(Int::class.java) ?: 0
                userProfileRef.child("groups").setValue(currentGroups + increment)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBWriteOperations", "Failed to update group count", error.toException())
            }
        })
    }

    private fun generateGroupCode(): String {
        // Generate a random 6-character alphanumeric code (uppercase only)
        val allowedChars = ('A'..'Z') + ('0'..'9')
        val code = (1..6)
            .map { allowedChars.random() }
            .joinToString("")
        
        Log.d("FBWriteOperations", "Generated group code: $code")
        return code
    }
}