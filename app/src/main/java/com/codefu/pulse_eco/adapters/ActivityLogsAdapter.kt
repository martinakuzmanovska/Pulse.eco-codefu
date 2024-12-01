package com.codefu.pulse_eco.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codefu.pulse_eco.QrCodeActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.domain.models.UserActivityLog
import kotlinx.coroutines.NonDisposableHandle.parent

class ActivityLogsAdapter(private var logs: List<UserActivityLog>):
    RecyclerView.Adapter<ActivityLogsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.date)
        val logsTextView: TextView = itemView.findViewById(R.id.activityLog)
        val pointsTextView: TextView = itemView.findViewById(R.id.activityPoints)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_log_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.dateTextView.text = log.date
        holder.logsTextView.text = log.description
        holder.pointsTextView.text = log.points.toString()


    }

    override fun getItemCount() = logs.size

    fun updateData(newLogs: List<UserActivityLog>) {
        logs = newLogs
        notifyDataSetChanged()
    }

}