package com.novabox.app.data.model

import org.json.JSONObject

data class Source(
    val id: String,
    val name: String,
    val api: String,
    val key: String = "",
    val enabled: Boolean = true,
    val order: Int = 0
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("name", name)
        put("api", api)
        put("key", key)
        put("enabled", enabled)
        put("order", order)
    }

    companion object {
        fun fromJson(o: JSONObject) = Source(
            id = o.optString("id"),
            name = o.optString("name"),
            api = o.optString("api"),
            key = o.optString("key"),
            enabled = o.optBoolean("enabled", true),
            order = o.optInt("order")
        )
    }
}

data class VodSummary(
    val sourceId: String,
    val sourceName: String,
    val vodId: String,
    val vodName: String,
    val vodPic: String,
    val vodRemarks: String = "",
    val vodYear: String = "",
    val vodType: String = ""
)

data class VodDetail(
    val sourceId: String,
    val vodId: String,
    val vodName: String,
    val vodPic: String,
    val vodActor: String = "",
    val vodDirector: String = "",
    val vodContent: String = "",
    val vodRemarks: String = "",
    val vodYear: String = "",
    val vodArea: String = "",
    val vodLang: String = "",
    val vodPlayFrom: List<String> = emptyList(),
    val vodPlayUrl: List<String> = emptyList()
)

data class PlayUrl(val name: String, val url: String)

data class Category(val id: String, val name: String)

sealed class HomeItem {
    data class Banner(val items: List<VodSummary>) : HomeItem()
    data class Cats(val items: List<Category>) : HomeItem()
    data class Row(val title: String, val items: List<VodSummary>) : HomeItem()
}
