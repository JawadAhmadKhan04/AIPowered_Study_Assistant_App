package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference

/**
 * Base service class for Firebase operations.
 * Handles Firebase initialization and provides common database references.
 */
class FBDataBaseService {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    
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