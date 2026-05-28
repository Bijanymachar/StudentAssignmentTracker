package com.group.studentassignmenttracker

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.group.studentassignmenttracker.databinding.FragmentAssignmentListBinding

class AssignmentListFragment : Fragment() {

    private var _binding: FragmentAssignmentListBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: AssignmentAdapter
    private var currentFilter = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAssignmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Setup RecyclerView
        adapter = AssignmentAdapter(
            mutableListOf(),
            onEdit = { assignment ->
                val bundle = Bundle().apply { putInt("assignmentId", assignment.id) }
                findNavController().navigate(R.id.action_list_to_edit, bundle)
            },
            onDelete = { assignment ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Assignment")
                    .setMessage("Are you sure you want to delete '${assignment.title}'?")
                    .setPositiveButton("Delete") { _, _ ->
                        dbHelper.deleteAssignment(assignment.id)
                        loadAssignments()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onComplete = { assignment ->
                dbHelper.markAsCompleted(assignment.id)
                loadAssignments()
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        // FAB
        binding.fabAdd.setOnClickListener {
            findNavController().navigate(R.id.action_list_to_add)
        }

        // Search
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isEmpty()) loadAssignments()
                else adapter.updateData(dbHelper.searchAssignments(query))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Filter chips
        binding.chipAll.setOnClickListener { currentFilter = "All"; loadAssignments() }
        binding.chipPending.setOnClickListener { currentFilter = "Pending"; loadAssignments() }
        binding.chipCompleted.setOnClickListener { currentFilter = "Completed"; loadAssignments() }
        binding.chipOverdue.setOnClickListener { currentFilter = "Overdue"; loadAssignments() }

        loadAssignments()
    }

    private fun loadAssignments() {
        val all = dbHelper.getAllAssignments()
        val filtered = if (currentFilter == "All") all
        else all.filter { it.status == currentFilter }
        adapter.updateData(filtered)
    }

    override fun onResume() {
        super.onResume()
        loadAssignments()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}