package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile

class FBWriteOperations (private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()

    fun saveNotes(
        courseId: String,
        noteTitle: String,
        noteContent: String,
        noteAudio: String? = null,
        type: String,
        tag: Int
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

        databaseService.notesRef.child(noteId).setValue(noteData)
            .addOnSuccessListener {
                Log.d("FBWriteOperations", "Note saved successfully")
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
}