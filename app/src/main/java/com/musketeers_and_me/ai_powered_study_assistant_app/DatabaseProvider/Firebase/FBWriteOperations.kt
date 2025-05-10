package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage

class FBWriteOperations (private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()

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
        val code = generateGroupCode() // You'll need to implement this

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
                Log.d("FBWriteOperations", "Study group created successfully")
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

        // Find group by code
        databaseService.studyGroupsRef.orderByChild("code").equalTo(groupCode)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.children.iterator().hasNext()) {
                        val groupSnapshot = snapshot.children.iterator().next()
                        val groupId = groupSnapshot.key ?: return

                        // Add user to members
                        val timestamp = System.currentTimeMillis()
                        val memberData = mapOf(
                            "role" to "member",
                            "joinedAt" to timestamp
                        )

                        databaseService.studyGroupsRef.child(groupId)
                            .child("members")
                            .child(currentUserId)
                            .setValue(memberData)
                            .addOnSuccessListener {
                                Log.d("FBWriteOperations", "Joined study group successfully")
                                // Update user's group count
                                updateUserGroupCount(1)
                                onComplete(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e("FBWriteOperations", "Failed to join study group", e)
                                onComplete(false)
                            }
                    } else {
                        Log.e("FBWriteOperations", "Group not found with code: $groupCode")
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

        val messageData = mapOf(
            "senderId" to message.senderId,
            "senderName" to message.senderName,
            "content" to message.content,
            "timestamp" to message.timestamp,
            "messageType" to message.messageType.name,
            "noteId" to message.noteId,
            "noteType" to message.noteType
        )

        databaseService.groupChatsRef.child(groupId)
            .child("messages")
            .child(messageId)
            .setValue(messageData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Message sent successfully")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("FBWriteOperations", "Failed to send message", e)
                onComplete(false)
            }
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
        // Generate a random 6-character alphanumeric code
        val allowedChars = ('A'..'Z') + ('0'..'9')
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }
}