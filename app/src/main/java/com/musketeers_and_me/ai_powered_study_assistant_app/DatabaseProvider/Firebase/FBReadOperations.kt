package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.annotation.SuppressLint
import android.app.Activity
import android.provider.ContactsContract
import android.util.Log
import com.google.firebase.database.DataSnapshot
import android.widget.Toast
import android.content.Context
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.musketeers_and_me.ai_powered_study_assistant_app.LectureAndNotes.NoteItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Course
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.Question
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.GroupMessage
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.StudyGroup
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.UserProfile
import com.musketeers_and_me.ai_powered_study_assistant_app.R
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.MessageType

class FBReadOperations(private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()
    private val listeners = mutableListOf<ValueEventListener>()

    fun getImageUrls(courseId: String, onSuccess: (MutableList<String>) -> Unit, onFailure: (Exception) -> Unit) {
        val courseRef = databaseService.coursesRef.child(courseId)

        courseRef.child("image_urls").get().addOnSuccessListener { snapshot ->
            val imageUrlsMap = snapshot.getValue(object : GenericTypeIndicator<Map<String, String>>() {}) ?: emptyMap()

            val imageUrls = imageUrlsMap.values.toMutableList()

            onSuccess(imageUrls)
        }.addOnFailureListener { e ->
            onFailure(e)
        }
    }


    fun getDigest(noteId: String, callback: (content: String, audio: String, type: String, summary: String, tag: Int, keyPoints: String, conceptList: String) -> Unit) {
        // Reference to the specific note's data
        val noteRef = databaseService.notesRef.child(noteId)

        // Add a listener to get the data of the specific note's fields
        noteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Extract fields directly from the snapshot
                val data = snapshot.child("content").getValue(String::class.java)?: ""
                val audio = snapshot.child("audio").getValue(String::class.java)?: ""
                val type = snapshot.child("type").getValue(String::class.java)?: ""
                val summary = snapshot.child("summary").getValue(String::class.java) ?: ""
                val tag = snapshot.child("tag").getValue(Int::class.java) ?: 0
                val keyPoints = snapshot.child("keyPoints").getValue(String::class.java) ?: ""
                val conceptList = snapshot.child("conceptList").getValue(String::class.java) ?: ""

                // Pass the extracted values to the callback
                callback(data, audio, type, summary, tag, keyPoints, conceptList)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error case
                println("Error retrieving note digest: ${error.message}")
                // Return empty strings if there is an error
                callback("", "", "", "", 0, "", "")
            }
        })
    }

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
                            type = if (note.type == "text") "text" else "voice",
                            note_id = noteSnapshot.key ?: "",
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

    fun getAllNotes(callback: (List<NoteItem>) -> Unit) {
        val notesRef = databaseService.notesRef
        val query = notesRef.orderByChild("createdBy").equalTo(currentUserId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notesList = mutableListOf<NoteItem>()

                for (noteSnapshot in snapshot.children) {
                    val note = noteSnapshot.getValue(NoteItem::class.java)
                    if (note != null) {
                        // Map the note to a NoteItem
                        val noteItem = NoteItem(
                            title = note.title,
                            createdAt = note.createdAt,
                            age = "${System.currentTimeMillis() - note.createdAt} ms ago",
                            type = if (note.type == "text") "text" else "voice",
                            note_id = noteSnapshot.key ?: "",
                        )
                        notesList.add(noteItem)
                    }
                }

                callback(notesList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("FBReadOperations", "Error fetching all notes: ${error.message}")
                callback(emptyList())
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
                    val courseId = courseSnapshot.key ?: continue
                    val createdBy = courseSnapshot.child("createdBy").getValue(String::class.java)
                    val members = courseSnapshot.child("members").children

                    // Only process courses where current user is creator or member
                    if (createdBy == currentUserId || members.any { it.key == currentUserId }) {
                        val title = courseSnapshot.child("title").getValue(String::class.java) ?: ""
                        val description = courseSnapshot.child("description").getValue(String::class.java) ?: ""
                        val color = courseSnapshot.child("color").getValue(Int::class.java) ?: R.color.red
                        val lastModified = members.find { it.key == currentUserId }
                            ?.child("lastModified")
                            ?.getValue(Long::class.java) ?: System.currentTimeMillis()

                        val course = Course(
                            title = title,
                            noteCount = 0, // Will be updated when notes are added
                            daysAgo = calculateDaysAgo(lastModified),
                            buttonColorResId = color,
                            bookmarked = false, // Will be updated from bookmarks
                            courseId = courseId,
                            description = description
                        )
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
        val bookmarksRef = databaseService.usersRef.child(userId).child("bookmarks")

        // First get bookmarks
        bookmarksRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(bookmarkSnapshot: DataSnapshot) {
                val bookmarkedCourseIds = mutableSetOf<String>()
                for (bookmark in bookmarkSnapshot.children) {
                    val isBookmarked = bookmark.getValue(Boolean::class.java) ?: false
                    if (isBookmarked) {
                        bookmarkedCourseIds.add(bookmark.key ?: continue)
                    }
                }

                // Then get courses
                coursesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val courses = mutableListOf<Course>()
                        for (courseSnapshot in snapshot.children) {
                            val courseId = courseSnapshot.key ?: continue
                            val createdBy = courseSnapshot.child("createdBy").getValue(String::class.java)
                            val members = courseSnapshot.child("members").children

                            if (createdBy == userId || members.any { it.key == userId }) {
                                val title = courseSnapshot.child("title").getValue(String::class.java) ?: ""
                                val description = courseSnapshot.child("description").getValue(String::class.java) ?: ""
                                val color = courseSnapshot.child("color").getValue(Int::class.java) ?: R.color.red
                                val lastModified = members.find { it.key == userId }
                                    ?.child("lastModified")
                                    ?.getValue(Long::class.java) ?: System.currentTimeMillis()

                                val course = Course(
                                    title = title,
                                    noteCount = 0, // Will be updated when notes are added
                                    daysAgo = calculateDaysAgo(lastModified),
                                    buttonColorResId = color,
                                    bookmarked = courseId in bookmarkedCourseIds,
                                    courseId = courseId,
                                    description = description
                                )
                                courses.add(course)
                            }
                        }
                        onCoursesFetched(courses)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FBReadOperations", "Error fetching courses", error.toException())
                        onCoursesFetched(emptyList())
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error fetching bookmarks", error.toException())
                onCoursesFetched(emptyList())
            }
        })
    }

    // Group Study Operations
    fun getStudyGroups(onGroupsFetched: (List<StudyGroup>) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBReadOperations", "User is not authenticated")
            onGroupsFetched(emptyList())
            return
        }

        databaseService.studyGroupsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val groups = mutableListOf<StudyGroup>()
                for (groupSnapshot in snapshot.children) {
                    val groupId = groupSnapshot.key ?: continue
                    val members = groupSnapshot.child("members")

                    // Only include groups where user is a member
                    if (members.hasChild(currentUserId)) {
                        val name = groupSnapshot.child("name").getValue(String::class.java) ?: ""
                        val description = groupSnapshot.child("description").getValue(String::class.java) ?: ""
                        val createdBy = groupSnapshot.child("createdBy").getValue(String::class.java) ?: ""
                        val createdAt = groupSnapshot.child("createdAt").getValue(Long::class.java) ?: 0
                        val code = groupSnapshot.child("code").getValue(String::class.java) ?: ""

                        val memberCount = members.childrenCount.toInt()
                        val userRole = members.child(currentUserId).child("role").getValue(String::class.java) ?: "member"

                        val group = StudyGroup(
                            id = groupId,
                            name = name,
                            description = description,
                            createdBy = createdBy,
                            createdAt = createdAt,
                            code = code,
                            memberCount = memberCount,
                            userRole = userRole
                        )
                        groups.add(group)
                    }
                }
                onGroupsFetched(groups)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FBReadOperations", "Error fetching study groups", error.toException())
                onGroupsFetched(emptyList())
            }
        })
    }

    fun getGroupMessages(groupId: String, onMessagesFetched: (List<GroupMessage>) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBReadOperations", "User is not authenticated")
            onMessagesFetched(emptyList())
            return
        }

        databaseService.groupChatsRef.child(groupId)
            .child("messages")
            .orderByChild("timestamp")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<GroupMessage>()
                    for (messageSnapshot in snapshot.children) {
                        val messageId = messageSnapshot.key ?: continue
                        val senderId = messageSnapshot.child("senderId").getValue(String::class.java) ?: ""
                        val content = messageSnapshot.child("content").getValue(String::class.java) ?: ""
                        val timestamp = messageSnapshot.child("timestamp").getValue(Long::class.java) ?: 0
                        val messageType = messageSnapshot.child("messageType").getValue(String::class.java) ?: "REGULAR"
                        val noteId = messageSnapshot.child("noteId").getValue(String::class.java) ?: ""
                        val noteType = messageSnapshot.child("noteType").getValue(String::class.java) ?: ""
                        val senderName = messageSnapshot.child("senderName").getValue(String::class.java) ?: "Unknown User"

                        // Create message object with isCurrentUser set correctly
                        val message = GroupMessage(
                            id = messageId,
                            groupId = groupId,
                            senderId = senderId,
                            senderName = senderName,
                            content = content,
                            timestamp = timestamp,
                            isCurrentUser = senderId == currentUserId && currentUserId.isNotEmpty(),
                            messageType = MessageType.valueOf(messageType),
                            noteId = noteId,
                            noteType = noteType
                        )
                        messages.add(message)
                    }

                    // Sort messages by timestamp
                    messages.sortBy { it.timestamp }
                    onMessagesFetched(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FBReadOperations", "Error fetching group messages", error.toException())
                    onMessagesFetched(emptyList())
                }
            })
    }

    fun getGroupDetails(groupId: String, onGroupFetched: (StudyGroup?) -> Unit) {
        if (currentUserId.isEmpty()) {
            Log.e("FBReadOperations", "User is not authenticated")
            onGroupFetched(null)
            return
        }

        databaseService.studyGroupsRef.child(groupId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onGroupFetched(null)
                        return
                    }

                    val name = snapshot.child("name").getValue(String::class.java) ?: ""
                    val description = snapshot.child("description").getValue(String::class.java) ?: ""
                    val createdBy = snapshot.child("createdBy").getValue(String::class.java) ?: ""
                    val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: 0
                    val code = snapshot.child("code").getValue(String::class.java) ?: ""

                    val members = snapshot.child("members")
                    val memberCount = members.childrenCount.toInt()
                    val userRole = members.child(currentUserId).child("role").getValue(String::class.java) ?: "member"

                    val group = StudyGroup(
                        id = groupId,
                        name = name,
                        description = description,
                        createdBy = createdBy,
                        createdAt = createdAt,
                        code = code,
                        memberCount = memberCount,
                        userRole = userRole
                    )
                    onGroupFetched(group)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FBReadOperations", "Error fetching group details", error.toException())
                    onGroupFetched(null)
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
// In FBReadOperations.kt
fun getQuizQuestions(quizId: String, context: Context, callback: (List<Question>, List<String>) -> Unit) {
    databaseService.quizzesRef.child(quizId).child("questions").get()
        .addOnSuccessListener { snapshot ->
            val questions = mutableListOf<Question>()
            val questionKeys = mutableListOf<String>()
            snapshot.children.forEach { child ->
                try {
                    val questionText = child.child("question").getValue(String::class.java) ?: ""
                    val optionsSnapshot = child.child("options")
                    Log.d("FBReadOperations", "Raw options for question ${child.key}: $optionsSnapshot")
                    val options = optionsSnapshot.children.associate { opt ->
                        val key = opt.key ?: "unknown"
                        val value = opt.getValue(String::class.java) ?: ""
                        Log.d("FBReadOperations", "Option key: $key, value: $value")
                        key to value
                    }
                    Log.d("FBReadOperations", "Parsed options: $options")
                    val correctAnswer = child.child("correctAnswer").getValue(String::class.java) ?: ""
                    val explanation = child.child("explanation").getValue(String::class.java) ?: ""
                    Log.d("FBReadOperations", "Explanation for question ${child.key}: $explanation")
                    val isAttempted = child.child("isAttempted").getValue(Boolean::class.java) ?: false
                    val isCorrect = child.child("isCorrect").getValue(Boolean::class.java) ?: false
                    val selectedAnswer = child.child("selectedAnswer").getValue(String::class.java) ?: ""
                    val question = Question(
                        questionText,
                        options,
                        correctAnswer,
                        explanation,
                        isAttempted,
                        isCorrect,
                        selectedAnswer
                    )
                    questions.add(question)
                    questionKeys.add(child.key ?: "question_${questions.size}")
                } catch (e: Exception) {
                    Toast.makeText(context, "Error parsing question ${child.key}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            callback(questions, questionKeys)
        }
        .addOnFailureListener { e ->
            callback(emptyList(), emptyList())
        }
}
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

    private fun calculateDaysAgo(timestamp: Long): Int {
        val currentTime = System.currentTimeMillis()
        val diffInMillis = currentTime - timestamp
        return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
    }

    fun removeAllListeners() {
        listeners.forEach { listener ->
            databaseService.usersRef.removeEventListener(listener)
            databaseService.coursesRef.removeEventListener(listener)
        }
        listeners.clear()
    }

    fun getUserDetails(userId: String, onUserFetched: (UserProfile?) -> Unit) {
        if (userId.isEmpty()) {
            Log.e("FBReadOperations", "Invalid user ID")
            onUserFetched(null)
            return
        }

        databaseService.usersRef.child(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        onUserFetched(null)
                        return
                    }

                    val username = snapshot.child("username").getValue(String::class.java) ?: ""
                    val email = snapshot.child("email").getValue(String::class.java) ?: ""
                    val createdAt = snapshot.child("createdAt").getValue(Long::class.java) ?: System.currentTimeMillis()
                    val lastLogin = snapshot.child("lastLogin").getValue(Long::class.java) ?: System.currentTimeMillis()

                    val user = UserProfile(
                        id = userId,
                        email = email,
                        username = username,
                        createdAt = createdAt,
                        lastLogin = lastLogin
                    )
                    onUserFetched(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FBReadOperations", "Error fetching user details", error.toException())
                    onUserFetched(null)
                }
            })
    }
}