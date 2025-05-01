package com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase

import com.google.firebase.database.FirebaseDatabase


class FBDataBaseService {
    val database = FirebaseDatabase.getInstance()

    val usersRef = database.reference.child("users")
}