package com.novabox.app.data.repo

import android.content.Context
import com.novabox.app.data.model.Source
import org.json.JSONArray

object SourceRepo {
    private lateinit var ctx: Context
    private const val KEY = "sources_json"

    fun init(c: Context) {
        ctx = c.applicationContext
    }

    private val prefs get() = ctx.getSharedPreferences("novabox_sources", Context.MODE_PRIVATE)

    fun load(): MutableList<Source> {
        val raw = prefs.getString(KEY, "") ?: ""
        val list = mutableListOf<Source>()
        if (raw.isNotBlank()) {
            try {
                val arr = JSONArray(raw)
                for (i in 0 until arr.length()) {
                    list.add(Source.fromJson(arr.getJSONObject(i)))
                }
            } catch (_: Exception) {}
        }
        list.sortBy { it.order }
        return list
    }

    fun save(list: List<Source>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    fun defaultSources(): List<Source> = emptyList()
}
