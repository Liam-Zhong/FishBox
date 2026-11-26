package org.chlzhong.fishbox

import java.io.File
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** 校验 data/ 目录下的鱼类数据（它同时是打包进 App 的唯一数据源）。单元测试的工作目录是 app/，所以用 ../data */
class DataValidationTest {
    private fun read(name: String) = File("../data/$name").readText(Charsets.UTF_8)

    private fun idSet(array: JSONArray): Set<String> =
        (0 until array.length()).map { array.getJSONObject(it).getString("id") }.toSet()

    private fun strings(a: JSONArray): List<String> = (0 until a.length()).map { a.getString(it) }

    @Test fun gameOrder_coversEveryFishExactlyOnce() {
        val fishIds = JSONArray(read("fish.json")).let { idSet(it) }
        val tabs = JSONObject(read("game_order.json")).getJSONArray("tabs")
        val all = (0 until tabs.length()).flatMap { strings(tabs.getJSONObject(it).getJSONArray("fish")) }
        assertEquals("游戏内顺序里每条鱼只能出现一次", all.size, all.toSet().size)
        assertEquals("游戏内顺序必须覆盖全部鱼", fishIds, all.toSet())
    }

    @Test fun rods_andFishRods_areConsistent() {
        val rodIds = idSet(JSONArray(read("rods.json")))
        val fishIds = idSet(JSONArray(read("fish.json")))
        val fr = JSONObject(read("fish_rods.json")).getJSONObject("rods")
        for (k in fr.keys()) {
            assertTrue("$k 不是已知的鱼", k in fishIds)
            assertTrue("$k 的鱼竿 id 不在 rods.json 里", rodIds.containsAll(strings(fr.getJSONArray(k))))
        }
    }

    @Test fun everyLocation_hasARodKind() {
        val locs = JSONArray(read("locations.json"))
        val rods = idSet(JSONObject(read("equipment.json")).getJSONArray("rod_types"))
        for (i in 0 until locs.length()) {
            val l = locs.getJSONObject(i)
            val kinds = strings(l.getJSONArray("rods"))
            assertTrue("${l.getString("id")} 没有标注鱼竿类型", kinds.isNotEmpty())
            assertTrue("${l.getString("id")} 的鱼竿类型不在词表内", rods.containsAll(kinds))
        }
    }

    @Test fun fishRecords_areWellFormed() {
        val fish = JSONArray(read("fish.json"))
        val ids = idSet(fish)
        assertEquals("鱼的 id 必须唯一", fish.length(), ids.size)

        val locations = idSet(JSONArray(read("locations.json")))
        val times = idSet(JSONArray(read("times.json")))
        val weather = idSet(JSONArray(read("weather.json")))
        val seasons = idSet(JSONArray(read("seasons.json")))
        val rods = idSet(JSONObject(read("equipment.json")).getJSONArray("rod_types"))

        for (i in 0 until fish.length()) {
            val f = fish.getJSONObject(i)
            val id = f.getString("id")
            assertTrue("$id 缺少拼音", f.optString("pinyin").isNotBlank())
            assertTrue("$id 的地点不在词表内", locations.containsAll(strings(f.getJSONArray("locations"))))
            assertTrue("$id 的时间不在词表内", times.containsAll(strings(f.getJSONArray("times"))))
            assertTrue("$id 的天气不在词表内", weather.containsAll(strings(f.getJSONArray("weather"))))
            assertTrue("$id 的季节不在词表内", seasons.containsAll(strings(f.getJSONArray("seasons"))))
            assertTrue("$id 的鱼竿类型不在词表内", rods.containsAll(strings(f.getJSONArray("rods"))))
            assertTrue("$id 的地点不能为空", f.getJSONArray("locations").length() > 0)
            val prices = f.getJSONObject("sell_price_treats")
            for (q in QUALITY_IDS) assertTrue("$id 缺少 $q 价格", prices.has(q))
        }
    }
}
