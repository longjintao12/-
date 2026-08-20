package com.novabox.app.net

import com.novabox.app.data.model.*
import org.json.JSONObject

object ApiParser {

    fun parseCategories(json: String): List<Category> {
        val list = mutableListOf<Category>()
        runCatching {
            val data = JSONObject(json).getJSONObject("data")
            val cls = data.optJSONObject("class") ?: return list
            val keys = cls.keys()
            while (keys.hasNext()) {
                val id = keys.next()
                list.add(Category(id, cls.optString(id)))
            }
        }
        return list
    }

    fun parseVodList(json: String, sourceId: String, sourceName: String): List<VodSummary> {
        val out = mutableListOf<VodSummary>()
        runCatching {
            val data = JSONObject(json).getJSONObject("data")
            val arr = data.optJSONArray("list") ?: return out
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                out.add(
                    VodSummary(
                        sourceId = sourceId,
                        sourceName = sourceName,
                        vodId = o.optString("vod_id"),
                        vodName = o.optString("vod_name"),
                        vodPic = o.optString("vod_pic"),
                        vodRemarks = o.optString("vod_remarks"),
                        vodYear = o.optString("vod_year"),
                        vodType = o.optString("type_name")
                    )
                )
            }
        }
        return out
    }

    fun parseDetail(json: String, sourceId: String): VodDetail? {
        return runCatching {
            val data = JSONObject(json).getJSONObject("data")
            val arr = data.optJSONArray("list")
            if (arr == null || arr.length() == 0) return null
            val o = arr.getJSONObject(0)
            val playFrom = o.optString("vod_play_from").split("$$$").filter { it.isNotBlank() }
            val playUrl = o.optString("vod_play_url").split("$$$").filter { it.isNotBlank() }
            VodDetail(
                sourceId = sourceId,
                vodId = o.optString("vod_id"),
                vodName = o.optString("vod_name"),
                vodPic = o.optString("vod_pic"),
                vodActor = o.optString("vod_actor"),
                vodDirector = o.optString("vod_director"),
                vodContent = o.optString("vod_content")
                    .replace("<br>", "\n")
                    .replace(Regex("<[^>]+>"), ""),
                vodRemarks = o.optString("vod_remarks"),
                vodYear = o.optString("vod_year"),
                vodArea = o.optString("vod_area"),
                vodLang = o.optString("vod_lang"),
                vodPlayFrom = playFrom,
                vodPlayUrl = playUrl
            )
        }.getOrNull()
    }

    fun parseEpisodes(vodPlayUrl: String): List<PlayUrl> {
        return vodPlayUrl.split("#").mapNotNull { seg ->
            if (seg.isBlank()) null
            else {
                val parts = seg.split("$", limit = 2)
                if (parts.size == 2) PlayUrl(parts[0], parts[1])
                else PlayUrl(seg, seg)
            }
        }
    }
}
