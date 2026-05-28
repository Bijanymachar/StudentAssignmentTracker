package com.group.studentassignmenttracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.studentassignmenttracker.databinding.FragmentDashboardBinding
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Show today's date
        val sdf = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault())
        binding.tvDate.text = sdf.format(Date())

        // Update overdue assignments automatically
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        dbHelper.updateOverdueAssignments(today)

        // Load stats
        refreshStats()

        // Navigation buttons
        binding.btnViewAssignments.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_list)
        }

        binding.btnAddAssignment.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_add)
        }
    }

    private fun refreshStats() {
        binding.tvTotal.text = dbHelper.getTotalCount().toString()
        binding.tvPending.text = dbHelper.getPendingCount().toString()
        binding.tvCompleted.text = dbHelper.getCompletedCount().toString()
        binding.tvOverdue.text = dbHelper.getOverdueCount().toString()
    }

    override fun onResume() {
        super.onResume()
        refreshStats() // Refresh every time user returns to dashboard
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}