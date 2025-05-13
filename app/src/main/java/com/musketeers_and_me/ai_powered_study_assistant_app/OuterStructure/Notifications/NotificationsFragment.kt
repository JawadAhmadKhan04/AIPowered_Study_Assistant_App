package com.musketeers_and_me.ai_powered_study_assistant_app.OuterStructure.Notifications

import NotificationsAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.MainActivity
import com.musketeers_and_me.ai_powered_study_assistant_app.Models.NotificationItem
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class NotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notifications, container, false)

        (activity as? MainActivity)?.apply{
            updateBottomNavIcon(R.id.nav_noti, R.drawable.notification_navbar_selected) // optional dynamic nav icon
        }



        recyclerView = view.findViewById(R.id.notificationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sampleNotifications = listOf(
            NotificationItem("Heading of noti", "Notification1\nThis is 1"),
        )

        adapter = NotificationsAdapter(sampleNotifications.toMutableList())
        recyclerView.adapter = adapter

        return view
    }
}
