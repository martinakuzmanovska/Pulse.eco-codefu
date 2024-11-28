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
import com.codefu.pulse_eco.domain.repositories.ActivityRepository


class HomeFragment : Fragment() {

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

        //val factory = ActivityViewModelFactory()
        //val activityViewModel = ViewModelProvider(this, factory)[ActivityViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


//        activityViewModel.activities.observe(viewLifecycleOwner) { activities ->
//            if (activities.isNullOrEmpty()) {
//                textView.text = "No activities found."
//            } else {
//                textView.text =  activities.joinToString("\n") { it.toString() }
//                Log.d("INFO", activities.joinToString("\n") { it.toString() })
//            }
//        }
//        activityViewModel.getActivities()

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}