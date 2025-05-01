package com.musketeers_and_me.ai_powered_study_assistant_app

import com.google.firebase.auth.FirebaseAuth

class AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

}