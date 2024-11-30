package com.codefu.pulse_eco.logs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codefu.pulse_eco.QrCodeActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.activities.ActivityViewModel
import com.codefu.pulse_eco.adapters.ActivityLogsAdapter
import com.codefu.pulse_eco.adapters.EventCardAdapter
import com.codefu.pulse_eco.databinding.FragmentActivityLogsBinding
import com.codefu.pulse_eco.databinding.FragmentEventsBinding
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.domain.factories.UserActivityLogViewModelFactory
import com.codefu.pulse_eco.domain.models.EventCardModel
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.codefu.pulse_eco.events.EventViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_REF
import com.codefu.pulse_eco.utils.Constants.ACTIVITY_USER_LOGS
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.FirebaseDatabase


class ActivityLogsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ActivityLogsAdapter
    private lateinit var viewModel: UserActivityLogViewModel
    private var _binding: FragmentActivityLogsBinding? = null
    private val binding get() = _binding!!

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentActivityLogsBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            this, UserActivityLogViewModelFactory(UserActivityLogRepositoryImpl(requireContext(). applicationContext))
        )[UserActivityLogViewModel::class.java]

        adapter = ActivityLogsAdapter( ArrayList())
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
        val button:ImageButton= _binding!!.fabAdd
        button.setOnClickListener{

            val intent = Intent(requireContext(), QrCodeActivity::class.java)
            startActivity(intent)

        }



        val userId = googleAuthUiClient.getSignedInUser()?.userId

        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            val model = logs.map {
                UserActivityLog(it.userId, it.activityName, it.date, it.description, it.points)
            }
            adapter.updateData(model)
        }

        if (userId != null) {
            viewModel.getLogs(userId)
        }

        return binding.root
    }


}