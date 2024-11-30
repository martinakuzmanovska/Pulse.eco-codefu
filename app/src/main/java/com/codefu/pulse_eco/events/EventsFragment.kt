package com.codefu.pulse_eco.events

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.adapters.EventCardAdapter
import com.codefu.pulse_eco.databinding.FragmentEventsBinding
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.domain.models.Event
import com.codefu.pulse_eco.domain.models.EventCardModel
import kotlinx.coroutines.launch

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!
    private lateinit var eventViewModel: EventViewModel
    private lateinit var adapter: EventCardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        eventViewModel = ViewModelProvider(
            this, EventViewModelFactory()
        )[EventViewModel::class.java]

        adapter = EventCardAdapter(requireContext(), ArrayList())
        binding.gridViewEvents.adapter = adapter


        eventViewModel.events.observe(viewLifecycleOwner) { events ->
            val eventCardModels = events.map {
                EventCardModel(it.activityName, it.date, it.imageUrl, it.points)
            }
            adapter.updateData(eventCardModels)
        }
        eventViewModel.getEvents()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
