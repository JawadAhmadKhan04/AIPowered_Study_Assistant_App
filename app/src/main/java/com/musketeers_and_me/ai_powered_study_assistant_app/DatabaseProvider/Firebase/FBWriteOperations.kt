package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import android.util.Log
import com.musketeers_and_me.ai_powered_study_assistant_app.AuthService
import com.google.firebase.database.FirebaseDatabase

class FBWriteOperations (private val databaseService: FBDataBaseService) {
    private val authService = AuthService()
    private val currentUserId = authService.getCurrentUserId().toString()

    fun saveSettings(quizNotifications: Boolean, studyReminders: Boolean, addInGroups: Boolean, autoLogin: Boolean, autoSync: Boolean) {
        if (currentUserId.isEmpty()) {
            // Handle the case where the user is not authenticated (optional)
            Log.d("FBWriteOperations", "User is not authenticated")
            return
        }

        Log.d("TEST", "INSIDE")

        Log.d("TEST", "User: $currentUserId")

//        // Get the values from the switches
//        val quizNotificationsValue = quizNotifications.isChecked
//        val studyRemindersValue = studyReminders.isChecked
//        val addInGroupsValue = addInGroups.isChecked
//        val autoLoginValue = autoLogin.isChecked
//        val autoSyncValue = autoSync.isChecked

        // Reference to the user's settings in Firebase
        val userSettingsRef = databaseService.usersRef.child(currentUserId).child("settings")

        // Set the values in Firebase under the settings node
        userSettingsRef.child("quizNotifications").setValue(quizNotifications)
        userSettingsRef.child("studyReminders").setValue(studyReminders)
        userSettingsRef.child("addInGroups").setValue(addInGroups)
        userSettingsRef.child("autoLogin").setValue(autoLogin)
        userSettingsRef.child("autoSync").setValue(autoSync)

    }

}