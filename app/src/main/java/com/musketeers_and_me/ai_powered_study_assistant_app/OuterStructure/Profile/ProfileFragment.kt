package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBDataBaseService
import com.musketeers_and_me.ai_powered_study_assistant_app.DatabaseProvider.Firebase.FBReadOperations
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.CardItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R



class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var profileAdapter: ProfileCardAdapter
    private val databaseService = FBDataBaseService()
    private var ReadOperations = FBReadOperations(databaseService)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.apply{
            updateBottomNavIcon(R.id.nav_profile, R.drawable.profile_navbar_selected) // optional dynamic nav icon
        }
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.cardRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)

        // Initialize with empty list
        profileAdapter = ProfileCardAdapter(emptyList())
        recyclerView.adapter = profileAdapter

        ReadOperations.getUserProfileStats(
            onDataReceived = { profileCards ->
                profileAdapter.updateData(profileCards)
                Log.d("ProfileFragment", "Data received: $profileCards")
            },
            onError = { error ->
                Log.d("ProfileFragment", "Error: ${error.message}")
                // Handle or log error if needed
            }
        )
    }

}

