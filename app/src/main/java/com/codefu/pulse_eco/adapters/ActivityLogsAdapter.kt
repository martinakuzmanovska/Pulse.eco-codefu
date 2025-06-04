package com.codefu.pulse_eco.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.domain.models.UserActivityLog

class ActivityLogsAdapter(private var logs: List<UserActivityLog>):
    RecyclerView.Adapter<ActivityLogsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val logActivityNameTextView: TextView = itemView.findViewById(R.id.logActivityName)
        val logsDescriptionTextView: TextView = itemView.findViewById(R.id.logActivityDescription)
        val pointsTextView: TextView = itemView.findViewById(R.id.activityPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_log_item, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.dateTextView.text = log.date
        holder.logActivityNameTextView.text = log.activityName
        holder.logsDescriptionTextView.text = log.description
        holder.pointsTextView.text = log.points.toString() + "pts"
    }

    override fun getItemCount() = logs.size

    fun updateData(newLogs: List<UserActivityLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }

}