package com.group.studentassignmenttracker

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "assignments.db"
        const val DATABASE_VERSION = 1

        // Table
        const val TABLE_ASSIGNMENTS = "assignments"

        // Columns
        const val COL_ID = "id"
        const val COL_TITLE = "title"
        const val COL_COURSE_UNIT = "course_unit"
        const val COL_DESCRIPTION = "description"
        const val COL_DUE_DATE = "due_date"
        const val COL_PRIORITY = "priority"
        const val COL_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_ASSIGNMENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_COURSE_UNIT TEXT NOT NULL,
                $COL_DESCRIPTION TEXT,
                $COL_DUE_DATE TEXT NOT NULL,
                $COL_PRIORITY TEXT NOT NULL,
                $COL_STATUS TEXT NOT NULL DEFAULT 'Pending'
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ASSIGNMENTS")
        onCreate(db)
    }

    // ── INSERT ──────────────────────────────────────────────
    fun insertAssignment(assignment: Assignment): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, assignment.title)
            put(COL_COURSE_UNIT, assignment.courseUnit)
            put(COL_DESCRIPTION, assignment.description)
            put(COL_DUE_DATE, assignment.dueDate)
            put(COL_PRIORITY, assignment.priority)
            put(COL_STATUS, assignment.status)
        }
        return db.insert(TABLE_ASSIGNMENTS, null, values)
    }

    // ── GET ALL ─────────────────────────────────────────────
    fun getAllAssignments(): List<Assignment> {
        val list = mutableListOf<Assignment>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ASSIGNMENTS, null, null, null, null, null,
            "$COL_DUE_DATE ASC"
        )
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Assignment(
                        id = getInt(getColumnIndexOrThrow(COL_ID)),
                        title = getString(getColumnIndexOrThrow(COL_TITLE)),
                        courseUnit = getString(getColumnIndexOrThrow(COL_COURSE_UNIT)),
                        description = getString(getColumnIndexOrThrow(COL_DESCRIPTION)),
                        dueDate = getString(getColumnIndexOrThrow(COL_DUE_DATE)),
                        priority = getString(getColumnIndexOrThrow(COL_PRIORITY)),
                        status = getString(getColumnIndexOrThrow(COL_STATUS))
                    )
                )
            }
            close()
        }
        return list
    }

    // ── UPDATE ──────────────────────────────────────────────
    fun updateAssignment(assignment: Assignment): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, assignment.title)
            put(COL_COURSE_UNIT, assignment.courseUnit)
            put(COL_DESCRIPTION, assignment.description)
            put(COL_DUE_DATE, assignment.dueDate)
            put(COL_PRIORITY, assignment.priority)
            put(COL_STATUS, assignment.status)
        }
        return db.update(TABLE_ASSIGNMENTS, values, "$COL_ID=?", arrayOf(assignment.id.toString()))
    }

    // ── DELETE ──────────────────────────────────────────────
    fun deleteAssignment(id: Int): Int {
        val db = writableDatabase
        return db.delete(TABLE_ASSIGNMENTS, "$COL_ID=?", arrayOf(id.toString()))
    }

    // ── MARK COMPLETE ────────────────────────────────────────
    fun markAsCompleted(id: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_STATUS, "Completed") }
        return db.update(TABLE_ASSIGNMENTS, values, "$COL_ID=?", arrayOf(id.toString()))
    }

    // ── DASHBOARD STATS ──────────────────────────────────────
    fun getTotalCount(): Int = getCountByStatus(null)
    fun getPendingCount(): Int = getCountByStatus("Pending")
    fun getCompletedCount(): Int = getCountByStatus("Completed")
    fun getOverdueCount(): Int = getCountByStatus("Overdue")

    private fun getCountByStatus(status: String?): Int {
        val db = readableDatabase
        val query = if (status == null)
            "SELECT COUNT(*) FROM $TABLE_ASSIGNMENTS"
        else
            "SELECT COUNT(*) FROM $TABLE_ASSIGNMENTS WHERE $COL_STATUS='$status'"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()
        return count
    }

    // ── SEARCH & FILTER ──────────────────────────────────────
    fun searchAssignments(query: String): List<Assignment> {
        val list = mutableListOf<Assignment>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_ASSIGNMENTS, null,
            "$COL_TITLE LIKE ? OR $COL_COURSE_UNIT LIKE ?",
            arrayOf("%$query%", "%$query%"),
            null, null, "$COL_DUE_DATE ASC"
        )
        with(cursor) {
            while (moveToNext()) {
                list.add(
                    Assignment(
                        id = getInt(getColumnIndexOrThrow(COL_ID)),
                        title = getString(getColumnIndexOrThrow(COL_TITLE)),
                        courseUnit = getString(getColumnIndexOrThrow(COL_COURSE_UNIT)),
                        description = getString(getColumnIndexOrThrow(COL_DESCRIPTION)),
                        dueDate = getString(getColumnIndexOrThrow(COL_DUE_DATE)),
                        priority = getString(getColumnIndexOrThrow(COL_PRIORITY)),
                        status = getString(getColumnIndexOrThrow(COL_STATUS))
                    )
                )
            }
            close()
        }
        return list
    }

    // ── UPDATE OVERDUE ────────────────────────────────────────
    fun updateOverdueAssignments(todayDate: String) {
        val db = writableDatabase
        val values = ContentValues().apply { put(COL_STATUS, "Overdue") }
        db.update(
            TABLE_ASSIGNMENTS, values,
            "$COL_DUE_DATE < ? AND $COL_STATUS = 'Pending'",
            arrayOf(todayDate)
        )
    }
}