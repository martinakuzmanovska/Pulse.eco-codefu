package com.codefu.pulse_eco.events

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.codefu.pulse_eco.R


class AdditionalEventInformationDialogFragment: DialogFragment() {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"
        private const val ARG_DATE = "arg_date"
        private const val ARG_POINTS = "arg_points"

        fun newInstance(title: String, description: String, date: String, points: String): AdditionalEventInformationDialogFragment {
            val fragment = AdditionalEventInformationDialogFragment()
            val args = Bundle()
            args.putString(ARG_DESCRIPTION, description)
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DATE, date)
            args.putString(ARG_POINTS, points)

            fragment.arguments = args
            return fragment
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE) ?: "No title"
        val description = arguments?.getString(ARG_DESCRIPTION) ?: "No description"
        val cardDate = arguments?.getString(ARG_DATE) ?: "No date"
        val points = arguments?.getString(ARG_POINTS) ?: "No points"

        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog.
            // Pass null as the parent view because it's going in the dialog
            // layout.
            val view = inflater.inflate(R.layout.event_dialog_item, null)
            val titleTextView = view.findViewById<TextView>(R.id.title)
            val descriptionTextView = view.findViewById<TextView>(R.id.description)
            val dateTextView = view.findViewById<TextView>(R.id.cardDate)
            val pointsTextView = view.findViewById<TextView>(R.id.points)


            val regex = Regex("""(\d{2}-\d{2}-\d{4}) (\d{2}:\d{2})""")
            val matchResult = regex.find(cardDate)
            val dateTimeString = if (matchResult != null) {
                val (date, time) = matchResult.destructured
                "Date: $date \nTime: $time \nCity: Skopje"
            } else {
                "Invalid timestamp"
            }

            titleTextView.text = title
            descriptionTextView.text = description
            dateTextView.text = dateTimeString
            pointsTextView.text = "$points points"
            builder.setView(view)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}