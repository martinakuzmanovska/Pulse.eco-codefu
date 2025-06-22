package com.codefu.pulse_eco.logs

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codefu.pulse_eco.QrCodeActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.adapters.ActivityLogsAdapter
import com.codefu.pulse_eco.databinding.FragmentActivityLogsBinding
import com.codefu.pulse_eco.domain.factories.UserActivityLogViewModelFactory
import com.codefu.pulse_eco.domain.models.UserActivityLog
import com.codefu.pulse_eco.domain.repositories.impl.UserActivityLogRepositoryImpl
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity


class ActivityLogsFragment : Fragment() {

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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
        googleAuthUiClient.getSignedInUser()?.let { viewModel.setUserValue(it) }

        val headerProfileTitle = binding.includeHeader.profileTitle
        viewModel.user.observe(viewLifecycleOwner) {
            headerProfileTitle.text = viewModel.user.value?.name.toString()
        }

        viewModel.logs.observe(viewLifecycleOwner) { logs ->
            val model = logs.map {
                UserActivityLog(it.userId, it.activityName, it.date, it.description, it.points)

            }
            val points = logs.sumOf { it.points ?: 0 }
            adapter.updateData(model)

           val pointsTextView = view?.findViewById<TextView>(R.id.points)
            pointsTextView?.text = points.toString()
        }

        if (userId != null) {
            viewModel.getLogs(userId)
        }

        return binding.root
    }

}