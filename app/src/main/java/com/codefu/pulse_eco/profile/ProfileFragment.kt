package com.codefu.pulse_eco.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codefu.pulse_eco.MainActivity
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.databinding.FragmentProfileBinding
import com.codefu.pulse_eco.home.HomeViewModel
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireActivity().applicationContext,
            oneTapClient = Identity.getSignInClient(requireActivity().applicationContext)
        )
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        googleAuthUiClient.getSignedInUser()?.let { homeViewModel.setUserValue(it) }

        val textView: TextView = binding.profileName
        homeViewModel.user.observe(viewLifecycleOwner) {
            textView.text = it?.name ?: ""
        }

        binding.logout.setOnClickListener {
            // Launch a coroutine to call suspend function
            viewLifecycleOwner.lifecycleScope.launch {
                signOut()
                (activity as? MainActivity)?.updateNavMenuVisibility()
                findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
            }
        }

        return root
    }

    private suspend fun signOut() {
        googleAuthUiClient.signOut()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
