package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

/**
 * Base service class for Firebase operations.
 * Handles Firebase initialization and provides common database references.
 */
class FBDataBaseService(val context: Context? = null) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
    // Server configuration
    val ip_address: String = "http://172.16.58.62/studysmartai/" // Using HTTP for local testing - change to HTTPS for production
    
    // Database references
    val usersRef: DatabaseReference = database.getReference("users")
    val coursesRef: DatabaseReference = database.getReference("courses")
    val notesRef: DatabaseReference = database.getReference("notes")
    val studyGroupsRef: DatabaseReference = database.getReference("studyGroups")
    val groupChatsRef: DatabaseReference = database.getReference("groupChats")

    val quizzesRef: DatabaseReference = database.getReference("quizzes")
    // User-specific references
    fun getUserRef(userId: String): DatabaseReference = usersRef.child(userId)
    fun getUserCoursesRef(userId: String): DatabaseReference = usersRef.child(userId).child("courses")
    
    // Course-specific references
    fun getCourseRef(courseId: String): DatabaseReference = coursesRef.child(courseId)
    fun getCourseMembersRef(courseId: String): DatabaseReference = coursesRef.child(courseId).child("members")

    // Group-specific references
    fun getGroupRef(groupId: String): DatabaseReference = studyGroupsRef.child(groupId)
    fun getGroupMembersRef(groupId: String): DatabaseReference = studyGroupsRef.child(groupId).child("members")
    fun getGroupChatRef(groupId: String): DatabaseReference = groupChatsRef.child(groupId)
}