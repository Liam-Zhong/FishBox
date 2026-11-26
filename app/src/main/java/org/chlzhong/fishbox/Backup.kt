package org.chlzhong.fishbox

import org.json.JSONArray
import org.json.JSONObject

data class BackupData(val progress: Set<String>, val starred: Set<String>)

/** ignored：备份里无法识别（鱼不存在或格式不对）而被忽略的条目数 */
data class RestoreResult(val data: BackupData, val ignored: Int)

/** 备份文件的编码/解码（纯逻辑，便于单元测试）。恢复前会逐条校验。 */
object Backup {
    const val FORMAT = "fishbox-backup"
    const val VERSION = 1
    private val KEY = Regex("^[A-Za-z0-9_]+:[0-4]$")

    fun encode(progress: Set<String>, starred: Set<String>, exportedAt: Long): String =
        JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("exportedAt", exportedAt)
            .put("progress", JSONArray(progress.sorted()))
            .put("starred", JSONArray(starred.sorted()))
            .toString(2)

    fun decode(text: String, fishIds: Set<String>): Result<RestoreResult> = runCatching {
        val o = JSONObject(text)
        require(o.optString("format") == FORMAT) { "不是 FishCodex 备份文件" }
        require(o.optInt("version", -1) in 1..VERSION) { "备份文件版本不受支持" }
        var ignored = 0
        val progress = HashSet<String>()
        val pa = o.optJSONArray("progress") ?: JSONArray()
        for (i in 0 until pa.length()) {
            val k = pa.optString(i, "")
            if (KEY.matches(k) && k.substringBefore(':') in fishIds) progress += k else ignored++
        }
        val starred = HashSet<String>()
        val sa = o.optJSONArray("starred") ?: JSONArray()
        for (i in 0 until sa.length()) {
            val id = sa.optString(i, "")
            if (id in fishIds) starred += id else ignored++
        }
        RestoreResult(BackupData(progress, starred), ignored)
    }
}
