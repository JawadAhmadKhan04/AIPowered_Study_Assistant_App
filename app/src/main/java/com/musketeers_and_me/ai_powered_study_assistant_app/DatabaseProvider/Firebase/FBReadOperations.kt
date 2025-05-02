package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class FBReadOperations(private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()

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

}