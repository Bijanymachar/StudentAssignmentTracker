package com.group.studentassignmenttracker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.group.studentassignmenttracker.databinding.ItemAssignmentBinding

class AssignmentAdapter(
    private var assignments: MutableList<Assignment>,
    private val onEdit: (Assignment) -> Unit,
    private val onDelete: (Assignment) -> Unit,
    private val onComplete: (Assignment) -> Unit
) : RecyclerView.Adapter<AssignmentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemAssignmentBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAssignmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val assignment = assignments[position]
        with(holder.binding) {
            tvTitle.text = assignment.title
            tvCourseUnit.text = assignment.courseUnit
            tvDueDate.text = assignment.dueDate
            tvStatus.text = assignment.status

            // Priority colour bar
            priorityBar.setBackgroundColor(
                when (assignment.priority) {
                    "High" -> Color.parseColor("#F44336")
                    "Medium" -> Color.parseColor("#FF9800")
                    else -> Color.parseColor("#4CAF50")
                }
            )

            // Status badge colour
            val statusColor = when (assignment.status) {
                "Completed" -> "#4CAF50"
                "Overdue" -> "#F44336"
                else -> "#FF9800"
            }
            tvStatus.setBackgroundColor(Color.parseColor(statusColor))

            btnEdit.setOnClickListener { onEdit(assignment) }
            btnDelete.setOnClickListener { onDelete(assignment) }
            root.setOnLongClickListener {
                if (assignment.status != "Completed") onComplete(assignment)
                true
            }
        }
    }

    override fun getItemCount() = assignments.size

    fun updateData(newList: List<Assignment>) {
        assignments.clear()
        assignments.addAll(newList)
        notifyDataSetChanged()
    }
}