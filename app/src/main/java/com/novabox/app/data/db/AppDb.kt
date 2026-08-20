package com.novabox.app.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AppDbHelper private constructor(ctx: Context) :
    SQLiteOpenHelper(ctx.applicationContext, "novabox.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE history(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                vod_id TEXT, source_id TEXT, name TEXT, pic TEXT,
                play_from TEXT, play_index INTEGER DEFAULT 0,
                play_pos INTEGER DEFAULT 0,
                updated_at INTEGER)"""
        )
        db.execSQL(
            """CREATE TABLE favorite(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                vod_id TEXT, source_id TEXT, name TEXT, pic TEXT,
                updated_at INTEGER,
                UNIQUE(vod_id, source_id))"""
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {}

    // --- History ---
    fun addHistory(
        vodId: String, sourceId: String, name: String, pic: String,
        playFrom: String, playIndex: Int, playPos: Long
    ) {
        val db = writableDatabase
        db.delete("history", "vod_id=? AND source_id=?", arrayOf(vodId, sourceId))
        val cv = ContentValues().apply {
            put("vod_id", vodId); put("source_id", sourceId); put("name", name)
            put("pic", pic); put("play_from", playFrom); put("play_index", playIndex)
            put("play_pos", playPos); put("updated_at", System.currentTimeMillis())
        }
        db.insert("history", null, cv)
    }

    fun updateHistoryPosition(vodId: String, sourceId: String, playPos: Long) {
        val cv = ContentValues().apply {
            put("play_pos", playPos); put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("history", cv, "vod_id=? AND source_id=?", arrayOf(vodId, sourceId))
    }

    fun getHistory(limit: Int = 50): List<Map<String, Any?>> {
        val list = mutableListOf<Map<String, Any?>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM history ORDER BY updated_at DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        while (c.moveToNext()) list.add(cursorToMap(c))
        c.close()
        return list
    }

    fun removeHistory(vodId: String, sourceId: String) {
        writableDatabase.delete("history", "vod_id=? AND source_id=?", arrayOf(vodId, sourceId))
    }

    fun clearHistory() = writableDatabase.delete("history", null, null)

    // --- Favorite ---
    fun addFavorite(vodId: String, sourceId: String, name: String, pic: String) {
        val cv = ContentValues().apply {
            put("vod_id", vodId); put("source_id", sourceId); put("name", name)
            put("pic", pic); put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.insertWithOnConflict("favorite", null, cv, SQLiteDatabase.CONFLICT_IGNORE)
    }

    fun removeFavorite(vodId: String, sourceId: String) {
        writableDatabase.delete("favorite", "vod_id=? AND source_id=?", arrayOf(vodId, sourceId))
    }

    fun isFavorite(vodId: String, sourceId: String): Boolean {
        val c = readableDatabase.rawQuery(
            "SELECT 1 FROM favorite WHERE vod_id=? AND source_id=?",
            arrayOf(vodId, sourceId)
        )
        val r = c.moveToFirst()
        c.close()
        return r
    }

    fun getFavorites(limit: Int = 200): List<Map<String, Any?>> {
        val list = mutableListOf<Map<String, Any?>>()
        val c = readableDatabase.rawQuery(
            "SELECT * FROM favorite ORDER BY updated_at DESC LIMIT ?",
            arrayOf(limit.toString())
        )
        while (c.moveToNext()) list.add(cursorToMap(c))
        c.close()
        return list
    }

    fun clearFavorites() = writableDatabase.delete("favorite", null, null)

    private fun cursorToMap(c: Cursor): Map<String, Any?> = mapOf(
        "vod_id" to c.getString(c.getColumnIndexOrThrow("vod_id")),
        "source_id" to c.getString(c.getColumnIndexOrThrow("source_id")),
        "name" to c.getString(c.getColumnIndexOrThrow("name")),
        "pic" to c.getString(c.getColumnIndexOrThrow("pic")),
        "play_from" to safeStr(c, "play_from"),
        "play_index" to c.getInt(c.getColumnIndexOrThrow("play_index")),
        "play_pos" to c.getLong(c.getColumnIndexOrThrow("play_pos")),
        "updated_at" to c.getLong(c.getColumnIndexOrThrow("updated_at"))
    )

    private fun safeStr(c: Cursor, col: String): String {
        val idx = c.getColumnIndex(col)
        return if (idx >= 0) c.getString(idx) ?: "" else ""
    }

    companion object {
        @Volatile
        private var INSTANCE: AppDbHelper? = null

        fun get(ctx: Context): AppDbHelper =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDbHelper(ctx).also { INSTANCE = it }
            }
    }
}
