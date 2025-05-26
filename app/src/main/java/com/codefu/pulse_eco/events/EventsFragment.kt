package com.codefu.pulse_eco.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codefu.pulse_eco.adapters.EventCardAdapter
import com.codefu.pulse_eco.databinding.FragmentEventsBinding
import com.codefu.pulse_eco.domain.factories.EventViewModelFactory
import com.codefu.pulse_eco.domain.models.EventCardModel

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
                EventCardModel(it.activityName, it.date, it.imageUrl, it.description, it.points)
            }
            adapter.updateData(eventCardModels)
        }
        eventViewModel.getEvents()

        binding.gridViewEvents.setOnItemClickListener{_, _, position, _ ->
            val selectedEvent = adapter.getItem(position)
            val dialog = selectedEvent?.let {
                AdditionalEventInformationDialogFragment
                    .newInstance(it.getCardTitleText(), it.getDescription(), it.getCardDate(), it.getPoints())
            }

            dialog?.show(parentFragmentManager, "event_dialog")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
