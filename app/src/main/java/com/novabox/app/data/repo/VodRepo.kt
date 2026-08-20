package com.novabox.app.data.repo

import com.novabox.app.data.model.Category
import com.novabox.app.data.model.Source
import com.novabox.app.data.model.VodDetail
import com.novabox.app.data.model.VodSummary
import com.novabox.app.net.ApiClient
import com.novabox.app.net.ApiParser
import java.net.URLEncoder

object VodRepo {

    private fun buildUrl(source: Source, params: Map<String, String>): String {
        val api = source.api.trim().trimEnd('/')
        val q = mutableMapOf<String, String>()
        if (source.key.isNotBlank()) q["token"] = source.key
        q.putAll(params)
        val qs = q.entries.joinToString("&") {
            "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        return "$api?$qs"
    }

    suspend fun fetchHome(source: Source): Pair<List<Category>, List<VodSummary>> {
        val json = ApiClient.getString(buildUrl(source, mapOf("ac" to "list")))
        val cats = ApiParser.parseCategories(json)
        var list = ApiParser.parseVodList(json, source.id, source.name)
        if (list.isEmpty()) {
            try {
                val j2 = ApiClient.getString(buildUrl(source, mapOf("ac" to "detail", "pg" to "1")))
                list = ApiParser.parseVodList(j2, source.id, source.name)
            } catch (_: Exception) {}
        }
        return cats to list
    }

    suspend fun fetchCategoryList(source: Source, catId: String, page: Int): List<VodSummary> {
        val params = mutableMapOf("ac" to "detail", "pg" to page.toString())
        if (catId.isNotBlank()) params["t"] = catId
        val json = ApiClient.getString(buildUrl(source, params))
        return ApiParser.parseVodList(json, source.id, source.name)
    }

    suspend fun fetchDetail(source: Source, vodId: String): VodDetail? {
        val json = ApiClient.getString(buildUrl(source, mapOf("ac" to "detail", "ids" to vodId)))
        return ApiParser.parseDetail(json, source.id)
    }

    suspend fun search(source: Source, wd: String): List<VodSummary> {
        var r = ApiClient.getString(buildUrl(source, mapOf("ac" to "detail", "wd" to wd)))
        var list = ApiParser.parseVodList(r, source.id, source.name)
        if (list.isEmpty()) {
            r = ApiClient.getString(buildUrl(source, mapOf("ac" to "search", "wd" to wd)))
            list = ApiParser.parseVodList(r, source.id, source.name)
        }
        return list
    }
}
