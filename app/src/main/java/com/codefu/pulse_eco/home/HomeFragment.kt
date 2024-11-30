package com.codefu.pulse_eco.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.activities.ActivityViewModel
import com.codefu.pulse_eco.databinding.FragmentHomeBinding
import com.codefu.pulse_eco.domain.factories.ActivityViewModelFactory
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.domain.repositories.ActivityRepository
import com.codefu.pulse_eco.events.EventViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity

class HomeFragment : Fragment() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        googleAuthUiClient.getSignedInUser()?.let { homeViewModel.setUserValue(it) }

        val textView: TextView = binding.userFullName
        homeViewModel.user.observe(viewLifecycleOwner) {
            textView.text =  homeViewModel.user.value?.name.toString()
        }

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}