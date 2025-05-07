package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.ContactsContract
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.NoteItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class FBReadOperations(private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()
    private val listeners = mutableListOf<ValueEventListener>()

    fun getNotes(courseId: String, callback: (List<NoteItem>, List<NoteItem>) -> Unit) {
        val notesRef = databaseService.notesRef
        val query = notesRef.orderByChild("courseId").equalTo(courseId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val textNotesList = mutableListOf<NoteItem>()
                val voiceNotesList = mutableListOf<NoteItem>()

                for (noteSnapshot in snapshot.children) {
                    val note = noteSnapshot.getValue(NoteItem::class.java)
                    if (note != null) {
                        // Map the note to a NoteItem
                        val noteItem = NoteItem(
                            title = note.title,
                            createdAt = note.createdAt,
                            age = "${System.currentTimeMillis() - note.createdAt} ms ago", // For simplicity, showing time since creation in ms
                            type = if (note.type == "text") "text" else "voice"
                        )

                        if (note.type == "text") {
                            textNotesList.add(noteItem)
                        } else if (note.type == "voice") {
                            voiceNotesList.add(noteItem)
                        }
                    }
                }

                callback(textNotesList, voiceNotesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FBReadOperations", "Error fetching notes: ${error.message}")
            }
        })
    }


    fun getBookmarkedCourses(userId: String, onCoursesFetched: (List<Course>) -> Unit){
        val coursesRef = databaseService.coursesRef
        val bookmarksRef = databaseService.usersRef.child(userId).child("bookmarks")

        bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookmarkSnapshot: DataSnapshot) {
                val bookmarkedCourseIds = mutableSetOf<String>()
                for (bookmark in bookmarkSnapshot.children) {
                    val isBookmarked = bookmark.getValue(Boolean::class.java) ?: false
                    if (isBookmarked) {
                        bookmarkedCourseIds.add(bookmark.key ?: continue)
                    }
                }

                coursesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courseList = mutableListOf<Course>()

                        for (courseSnapshot in snapshot.children) {
                            val courseId = courseSnapshot.key ?: continue
                            val createdBy = courseSnapshot.child("createdBy").getValue(String::class.java)
                            val members = courseSnapshot.child("members").children

                            if (createdBy == userId || members.any { it.key == userId }) {
                                val title = courseSnapshot.child("title").getValue(String::class.java) ?: ""
                                val description = courseSnapshot.child("description").getValue(String::class.java) ?: ""
                                val noteCount = courseSnapshot.child("noteCount").getValue(Int::class.java) ?: 0
                                val daysAgo = courseSnapshot.child("daysAgo").getValue(Int::class.java) ?: 0
                                val color = courseSnapshot.child("color").getValue(Int::class.java) ?: 0

                                val course = Course(
                                    title = title,
                                    noteCount = noteCount,
                                    daysAgo = daysAgo,
                                    buttonColorResId = color,
                                    bookmarked = courseId in bookmarkedCourseIds,
                                    courseId = courseId,
                                    description = description
                                )
                                if (course.bookmarked) {
                                    courseList.add(course)
                                }

                            }
                        }

                        onCoursesFetched(courseList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ReadOperations", "Failed to fetch courses", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReadOperations", "Failed to fetch bookmarks", error.toException())
            }
        })
    }

    fun getCourses(userId: String, bookmarked: Boolean, onCoursesFetched: (MutableList<Course>) -> Unit) {
        val coursesRef = databaseService.coursesRef
        val bookmarksRef = databaseService.usersRef.child(userId).child("bookmarks")

        bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookmarkSnapshot: DataSnapshot) {
                val bookmarkedCourseIds = mutableSetOf<String>()
                for (bookmark in bookmarkSnapshot.children) {
                    val isBookmarked = bookmark.getValue(Boolean::class.java) ?: false
                    if (isBookmarked) {
                        bookmarkedCourseIds.add(bookmark.key ?: continue)
                    }
                }

                coursesRef.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courseList = mutableListOf<Course>()

                        for (courseSnapshot in snapshot.children) {
                            val courseId = courseSnapshot.key ?: continue
                            val createdBy = courseSnapshot.child("createdBy").getValue(String::class.java)
                            val members = courseSnapshot.child("members").children

                            if (createdBy == userId || members.any { it.key == userId }) {
                                val title = courseSnapshot.child("title").getValue(String::class.java) ?: ""
                                val description = courseSnapshot.child("description").getValue(String::class.java) ?: ""
                                val noteCount = courseSnapshot.child("noteCount").getValue(Int::class.java) ?: 0
                                val daysAgo = courseSnapshot.child("daysAgo").getValue(Int::class.java) ?: 0
                                val color = courseSnapshot.child("color").getValue(Int::class.java) ?: 0

                                val course = Course(
                                    title = title,
                                    noteCount = noteCount,
                                    daysAgo = daysAgo,
                                    buttonColorResId = color,
                                    bookmarked = courseId in bookmarkedCourseIds,
                                    courseId = courseId,
                                    description = description
                                )
                                if (bookmarked && course.bookmarked) {
                                    courseList.add(course)
                                }
                                else if (!bookmarked){
                                    courseList.add(course)
                                }

                            }
                        }

                        onCoursesFetched(courseList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ReadOperations", "Failed to fetch courses", error.toException())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReadOperations", "Failed to fetch bookmarks", error.toException())
            }
        })
    }


    fun autoLoginAllowed(activity: Activity, onResult: (Boolean) -> Unit) {
        if (currentUserId.isEmpty()) {
            // Handle the case where the user is not authenticated (optional)
            onResult(false)
        }
        val settingsRef = databaseService.usersRef.child(currentUserId).child("settings")

        settingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("CommitPrefEdits")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val autoLogin = dataSnapshot.child("autoLogin").getValue(Boolean::class.java) ?: false

                onResult(autoLogin) // Pass the result to the callback
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                onResult(false) // Or you can choose to pass an error value instead
            }
        })
    }


    fun getSettings(onDataReceived: (Boolean, Boolean, Boolean, Boolean, Boolean) -> Unit, onError: (DatabaseError) -> Unit) {
        val settingsRef = databaseService.usersRef.child(currentUserId).child("settings")

        settingsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val quizNotifications = dataSnapshot.child("quizNotifications").getValue(Boolean::class.java) ?: false
                val studyReminders = dataSnapshot.child("studyReminders").getValue(Boolean::class.java) ?: false
                val addInGroups = dataSnapshot.child("addInGroups").getValue(Boolean::class.java) ?: false
                val autoLogin = dataSnapshot.child("autoLogin").getValue(Boolean::class.java) ?: false
                val autoSync = dataSnapshot.child("autoSync").getValue(Boolean::class.java) ?: false

                // Pass the values to the onDataReceived callback
                onDataReceived(quizNotifications, studyReminders, addInGroups, autoLogin, autoSync)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onError(databaseError) // Pass the error to the callback
            }
        })
    }

    fun getUserProfileStats(onDataReceived: (List<CardItem>) -> Unit, onError: (DatabaseError) -> Unit) {
//        val userId = currentUserId

        val profileRef = databaseService.usersRef.child(currentUserId).child("profile")


        profileRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = snapshot.child("courses").getValue(Int::class.java) ?: 0
                val lectures = snapshot.child("lectures").getValue(Int::class.java) ?: 0
                val smartDigests = snapshot.child("smartDigests").getValue(Int::class.java) ?: 0
                val quizzes = snapshot.child("quizzes").getValue(Int::class.java) ?: 0
                val groups = snapshot.child("groups").getValue(Int::class.java) ?: 0
                val timeSpent = snapshot.child("timeSpent").getValue(String::class.java) ?: "00:00:00"

                val profileCards = listOf(
                    CardItem("Courses", R.drawable.courses, courses.toString()),
                    CardItem("Lectures", R.drawable.lectures, lectures.toString()),
                    CardItem("Smart Digest", R.drawable.smart_digest, smartDigests.toString()),
                    CardItem("Quiz Created", R.drawable.quiz, quizzes.toString()),
                    CardItem("Groups", R.drawable.group_study, groups.toString()),
                    CardItem("Time Spent", R.drawable.clock, timeSpent)
                )

                onDataReceived(profileCards)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error)
            }
        })
    }

    fun listenForUserChanges(onUserChanged: (UserProfile) -> Unit) {
        val userRef = databaseService.getUserRef(currentUserId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserProfile::class.java)
                if (user != null) {
                    onUserChanged(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error listening for user changes", error.toException())
            }
        }
        userRef.addValueEventListener(listener)
        listeners.add(listener)
    }

    fun listenForCourseChanges(onCourseChanged: (Course) -> Unit) {
        val coursesRef = databaseService.coursesRef
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(Course::class.java)
                    if (course != null) {
                        onCourseChanged(course)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error listening for course changes", error.toException())
            }
        }
        coursesRef.addValueEventListener(listener)
        listeners.add(listener)
    }

    fun getUser(userId: String, onUserFetched: (UserProfile?) -> Unit) {
        val userRef = databaseService.getUserRef(userId)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserProfile::class.java)
                onUserFetched(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error fetching user", error.toException())
                onUserFetched(null)
            }
        })
    }

    fun getUserCourses(userId: String, onCoursesFetched: (List<Course>) -> Unit) {
        val coursesRef = databaseService.coursesRef
        coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val courses = mutableListOf<Course>()
                for (courseSnapshot in snapshot.children) {
                    val course = courseSnapshot.getValue(Course::class.java)
                    if (course != null) {
                        courses.add(course)
                    }
                }
                onCoursesFetched(courses)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error fetching user courses", error.toException())
                onCoursesFetched(emptyList())
            }
        })
    }
//    fun getVoiceNotes(courseId: String, onNotesFetched: (List<NoteItem>) -> Unit) {
//        val notesRef = databaseService.getNotesRef()
//        notesRef.orderByChild("courseId").equalTo(courseId).addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val notes = mutableListOf<NoteItem>()
//                for (noteSnapshot in snapshot.children) {
//                    val type = noteSnapshot.child("type").getValue(String::class.java)
//                    if (type == "voice") {
//                        val title = noteSnapshot.child("title").getValue(String::class.java) ?: ""
//                        val createdAt = noteSnapshot.child("createdAt").getValue(Long::class.java) ?: 0
//                        val age = calculateAge(createdAt)
//                        notes.add(NoteItem(title, age, NoteType.VOICE))
//                    }
//                }
//                onNotesFetched(notes)
//            }
//            override fun onCancelled(error: DatabaseError) {
//                Log.e("FBReadOperations", "Failed to fetch voice notes", error.toException())
//                onNotesFetched(emptyList())
//            }
//        })
//    }

    private fun calculateAge(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> "$days days ago"
            hours > 0 -> "$hours hours ago"
            minutes > 0 -> "$minutes minutes ago"
            else -> "Just now"
        }
    }

    fun removeAllListeners() {
        listeners.forEach { listener ->
            databaseService.usersRef.removeEventListener(listener)
            databaseService.coursesRef.removeEventListener(listener)
        }
        listeners.clear()
    }
}