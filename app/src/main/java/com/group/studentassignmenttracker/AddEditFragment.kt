package com.group.studentassignmenttracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.group.studentassignmenttracker.databinding.FragmentAddEditBinding
import java.util.*

class AddEditFragment : Fragment() {

    private var _binding: FragmentAddEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var dbHelper: DatabaseHelper
    private var assignmentId: Int = -1
    private var existingAssignment: Assignment? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = DatabaseHelper(requireContext())

        // Get assignment ID from arguments (if editing)
        assignmentId = arguments?.getInt("assignmentId", -1) ?: -1

        if (assignmentId != -1) {
            // Load existing data for editing
            existingAssignment = dbHelper.getAllAssignments().find { it.id == assignmentId }
            existingAssignment?.let { populateFields(it) }
        }

        // Date picker
        binding.etDueDate.setOnClickListener { showDatePicker() }

        // Save button
        binding.btnSave.setOnClickListener { saveAssignment() }
    }

    private fun populateFields(assignment: Assignment) {
        binding.etTitle.setText(assignment.title)
        binding.etCourseUnit.setText(assignment.courseUnit)
        binding.etDescription.setText(assignment.description)
        binding.etDueDate.setText(assignment.dueDate)
        when (assignment.priority) {
            "High" -> binding.radioHigh.isChecked = true
            "Medium" -> binding.radioMedium.isChecked = true
            "Low" -> binding.radioLow.isChecked = true
        }
        binding.btnSave.text = "Update Assignment"
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                binding.etDueDate.setText(date)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveAssignment() {
        val title = binding.etTitle.text.toString().trim()
        val courseUnit = binding.etCourseUnit.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val dueDate = binding.etDueDate.text.toString().trim()

        // Validation
        if (title.isEmpty()) { binding.etTitle.error = "Title is required"; return }
        if (courseUnit.isEmpty()) { binding.etCourseUnit.error = "Course unit is required"; return }
        if (dueDate.isEmpty()) { Toast.makeText(requireContext(), "Please select a due date", Toast.LENGTH_SHORT).show(); return }

        val priority = when (binding.radioGroupPriority.checkedRadioButtonId) {
            R.id.radioMedium -> "Medium"
            R.id.radioLow -> "Low"
            else -> "High"
        }

        val assignment = Assignment(
            id = if (assignmentId != -1) assignmentId else 0,
            title = title,
            courseUnit = courseUnit,
            description = description,
            dueDate = dueDate,
            priority = priority,
            status = existingAssignment?.status ?: "Pending"
        )

        if (assignmentId != -1) {
            dbHelper.updateAssignment(assignment)
            Toast.makeText(requireContext(), "Assignment updated!", Toast.LENGTH_SHORT).show()
        } else {
            dbHelper.insertAssignment(assignment)
            // Schedule notification for this assignment
            NotificationHelper.scheduleNotification(requireContext(), assignment)
            Toast.makeText(requireContext(), "Assignment saved!", Toast.LENGTH_SHORT).show()
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}