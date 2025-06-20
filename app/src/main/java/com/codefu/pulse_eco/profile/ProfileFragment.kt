package com.codefu.pulse_eco.profile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    private lateinit var homeViewModel: HomeViewModel

    private var userId: String? = null  // You need to set this properly, e.g. from signed-in user or arguments

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Get signed in user from GoogleAuthUiClient and set to ViewModel
        val signedInUser = googleAuthUiClient.getSignedInUser()
        if (signedInUser != null) {
            homeViewModel.setUserValue(signedInUser)
            userId = signedInUser.userId // Assuming UserData has id property
        }

        // Fetch full user data from Firebase if userId is known
        userId?.let {
            homeViewModel.fetchUserData(it)
        }

        // Observe LiveData for user to update UI
        homeViewModel.user.observe(viewLifecycleOwner) { userData ->
            binding.profileName.text = userData?.name ?: ""
            binding.pointsText.text = userData?.points?.toString() ?: "0"
        }

        // Purchase History button click shows dialog with current user itemIds
        binding.purchaseHistoryText.setOnClickListener {
            showPurchaseHistoryDialog()
        }

        binding.logout.setOnClickListener {
            // Launch a coroutine to call suspend function
            viewLifecycleOwner.lifecycleScope.launch {
                signOut()
                (activity as? MainActivity)?.updateNavMenuVisibility()
                findNavController().navigate(R.id.navigation_home)

            }
        }

        return root
    }

    private fun showPurchaseHistoryDialog() {
        // Use the latest user data from ViewModel
        val user = homeViewModel.user.value
        val itemIds = user?.itemIds ?: emptyList()

        val message = if (itemIds.isEmpty()) {
            "No purchase history available."
        } else {
            itemIds.joinToString(separator = "\n")
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Purchase History")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private suspend fun signOut() {
        googleAuthUiClient.signOut()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
