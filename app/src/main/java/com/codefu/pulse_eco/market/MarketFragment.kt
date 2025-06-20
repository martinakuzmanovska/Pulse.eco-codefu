package com.codefu.pulse_eco.market

import ShopItemAdapter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.codefu.pulse_eco.databinding.FragmentMarketBinding
import com.codefu.pulse_eco.domain.factories.ShopItemModelFactory
import com.codefu.pulse_eco.domain.repositories.impl.ShopItemRepositoryImpl
import com.codefu.pulse_eco.presentation.sign_in.GoogleAuthUiClient
import com.google.android.gms.auth.api.identity.Identity
import android.util.Log

class MarketFragment : Fragment() {
    private lateinit var adapter: ShopItemAdapter
    private lateinit var viewModel: ShopItemViewModel
    private var _binding: FragmentMarketBinding? = null
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
    ): View {
        _binding = FragmentMarketBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(
            this,
            ShopItemModelFactory(ShopItemRepositoryImpl(), googleAuthUiClient)
        )[ShopItemViewModel::class.java]

        googleAuthUiClient.getSignedInUser()?.let { viewModel.setUserValue(it) }

        binding.points.text = "Your Points: " + viewModel.getUserValue()?.points.toString()

        // Initial empty adapter
        adapter = ShopItemAdapter(emptyList()) { item ->
            viewModel.redeemItem(item) // <-- Handle the click here
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        viewModel.shopItems.observe(viewLifecycleOwner) { items ->
            adapter = ShopItemAdapter(items) { item ->
                viewModel.redeemItem(item)
            }
            binding.recyclerView.adapter = adapter
        }

        viewModel.getShopItems()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

