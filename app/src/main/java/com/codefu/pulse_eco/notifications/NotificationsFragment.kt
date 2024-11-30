package com.codefu.pulse_eco.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.databinding.FragmentNotificationsBinding
import com.codefu.pulse_eco.domain.factories.UserActivityLogViewModelFactory
import com.codefu.pulse_eco.logs.UserActivityLogViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity


class NotificationsFragment : Fragment() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        val logsViewModel = ViewModelProvider(this, UserActivityLogViewModelFactory(requireContext(). applicationContext))[UserActivityLogViewModel::class.java]

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val userId = googleAuthUiClient.getSignedInUser()?.userId

        Log.d("USER",userId?: "no user")

        val textView: TextView = binding.textNotifications
        logsViewModel.logs.observe(viewLifecycleOwner) {
            logs ->
            if (logs.isNullOrEmpty()){
                textView.text = "No logs"
            }
            else {
                textView.text =
                logs.joinToString("\n") { log ->
                    "Log description: ${log.description}, Log Points: ${log.activityName}"
            }}
        }

        if (userId != null) {
            logsViewModel.getLogs(userId)
        }

//        notificationsViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}