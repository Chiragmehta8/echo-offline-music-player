package com.example.soundboard

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "FocusSounds.db"
        private const val DATABASE_VERSION = 2
        const val TABLE_NAME = "focus_tracks"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_RES_ID = "res_id"
        const val COLUMN_PLAY_COUNT = "play_count"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY, "
                + "$COLUMN_TITLE TEXT, "
                + "$COLUMN_RES_ID INTEGER, "
                + "$COLUMN_PLAY_COUNT INTEGER DEFAULT 0)")
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Seed initial sound data (IDs 1, 2, 3, 4)
    fun initializeDefaultSounds(soundMap: List<Triple<Int, String, Int>>) {
        val db = this.writableDatabase
        for ((id, title, resId) in soundMap) {
            val values = ContentValues().apply {
                put(COLUMN_ID, id)
                put(COLUMN_TITLE, title)
                put(COLUMN_RES_ID, resId)
                put(COLUMN_PLAY_COUNT, 0)
            }
            db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE)
        }
        db.close()
    }

    // Get formatted string list for ListView
    fun getAllSoundsFormatted(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_ID, $COLUMN_TITLE, $COLUMN_PLAY_COUNT FROM $TABLE_NAME ORDER BY $COLUMN_ID ASC", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val count = cursor.getInt(2)
                list.add("ID $id: $title (Played $count times)")
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    // Fetch sound resource details by sound ID
    fun getSoundById(id: Int): Pair<String, Int>? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $COLUMN_TITLE, $COLUMN_RES_ID FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var result: Pair<String, Int>? = null
        if (cursor.moveToFirst()) {
            val title = cursor.getString(0)
            val resId = cursor.getInt(1)
            result = Pair(title, resId)
        }
        cursor.close()
        db.close()
        return result
    }

    // Increment play count (UPDATE)
    fun incrementPlayCount(id: Int): Int {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_NAME SET $COLUMN_PLAY_COUNT = $COLUMN_PLAY_COUNT + 1 WHERE $COLUMN_ID = ?", arrayOf(id.toString()))

        val cursor = db.rawQuery("SELECT $COLUMN_PLAY_COUNT FROM $TABLE_NAME WHERE $COLUMN_ID = ?", arrayOf(id.toString()))
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return count
    }

    // Rename a sound by ID (UPDATE)
    fun renameSound(id: Int, newTitle: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, newTitle)
        }
        val rowsAffected = db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
        db.close()
        return rowsAffected > 0
    }
}