package com.codefu.pulse_eco.events

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment


class AdditionalEventInformationDialogFragment: DialogFragment() {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_DESCRIPTION = "arg_description"

        fun newInstance(title: String, description: String): AdditionalEventInformationDialogFragment {
            val fragment = AdditionalEventInformationDialogFragment()
            val args = Bundle()
            args.putString(ARG_DESCRIPTION, description)
            args.putString(ARG_TITLE, title)

            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val title = arguments?.getString(ARG_TITLE) ?: "No title"
        val description = arguments?.getString(ARG_DESCRIPTION) ?: "No description"

        return activity?.let {
            // Use the Builder class for convenient dialog construction.
            val builder = AlertDialog.Builder(it)
            builder
                .setTitle(title)
                .setMessage(description)
            // Create the AlertDialog object and return it.
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

}