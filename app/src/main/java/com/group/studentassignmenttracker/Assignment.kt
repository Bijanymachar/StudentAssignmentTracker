package com.group.studentassignmenttracker

data class Assignment(
    val id: Int = 0,
    val title: String,
    val courseUnit: String,
    val description: String,
    val dueDate: String,          // stored as "YYYY-MM-DD"
    val priority: String,         // "High", "Medium", "Low"
    val status: String = "Pending" // "Pending", "Completed", "Overdue"
)