package com.musketeers_and_me.ai_powered_study_assistant_app.QuizCenter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.musketeers_and_me.ai_powered_study_assistant_app.R

class QuizResultsAdapter(private val quizResults: List<QuizResult>) :
    RecyclerView.Adapter<QuizResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.quizTitleTextView)
        val questionCountTextView: TextView = view.findViewById(R.id.questionCountTextView)
        val scoreTextView: TextView = view.findViewById(R.id.scoreTextView)
        val rootView: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quiz_result, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val quizResult = quizResults[position]
        holder.titleTextView.text = quizResult.title
        holder.questionCountTextView.text = "Number of Questions: ${quizResult.questionCount}"
        holder.scoreTextView.text = "Score: ${quizResult.score}%"

        holder.rootView.setOnClickListener {
            val dialog = AllQuizResultsDialog(holder.rootView.context, quizResult)
            dialog.show()
        }
    }

    override fun getItemCount() = quizResults.size
} 