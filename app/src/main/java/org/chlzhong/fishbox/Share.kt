package org.chlzhong.fishbox

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class TopCatch(val fish: Fish, val quality: Int)

data class ShareStats(
    val speciesTotal: Int,
    val speciesAny: Int,
    val speciesComplete: Int,
    val qualitiesCaught: Int,
    val qualitiesTotal: Int,
    val perQuality: List<Int>,
    val top: TopCatch?,
)

/** 统计：已收集的品质数、各品质数量，以及"已收集的鱼里搏斗等级最高的那条 + 它的最高品质"。
 *  并列时依次比较：该鱼已收集的最高品质、该品质的出售价。 */
fun computeStats(fishList: List<Fish>, done: Set<String>): ShareStats {
    val per = List(QUALITY_COUNT) { q -> fishList.count { key(it.id, q) in done } }
    val caught = fishList.mapNotNull { f ->
        val q = (QUALITY_COUNT - 1 downTo 0).firstOrNull { key(f.id, it) in done }
        if (q == null) null else TopCatch(f, q)
    }
    val top = caught.maxWithOrNull(
        compareBy<TopCatch>({ it.fish.fight }, { it.quality }, { it.fish.prices[it.quality] })
    )
    return ShareStats(
        speciesTotal = fishList.size,
        speciesAny = caught.size,
        speciesComplete = fishList.count { f -> (0 until QUALITY_COUNT).all { key(f.id, it) in done } },
        qualitiesCaught = per.sum(),
        qualitiesTotal = fishList.size * QUALITY_COUNT,
        perQuality = per,
        top = top,
    )
}

/** 分享卡片使用的颜色（取自当前主题，所以浅色/深色/动态取色下卡片风格一致） */
data class ShareColors(
    val background: Int,
    val header: Int,
    val onHeader: Int,
    val card: Int,
    val stroke: Int,
    val text: Int,
    val accent: Int,
)

private fun paint(size: Float, color: Int, tf: Typeface?, bold: Boolean, align: Paint.Align) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        textAlign = align
        typeface = Typeface.create(tf ?: Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

private fun Canvas.text(
    s: String, x: Float, y: Float, size: Float, color: Int, tf: Typeface?,
    bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT,
) = drawText(s, x, y, paint(size, color, tf, bold, align))

private fun Canvas.box(l: Float, t: Float, r: Float, b: Float, cols: ShareColors) {
    drawRoundRect(RectF(l, t, r, b), 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cols.card })
    drawRoundRect(RectF(l, t, r, b), 36f, 36f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = cols.stroke; style = Paint.Style.STROKE; strokeWidth = 4f
    })
}

private fun Canvas.bar(l: Float, t: Float, r: Float, b: Float, ratio: Float, fill: Int, trackColor: Int) {
    val track = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = trackColor; alpha = 40 }
    drawRoundRect(RectF(l, t, r, b), (b - t) / 2, (b - t) / 2, track)
    if (ratio > 0f) {
        val w = (r - l) * ratio.coerceIn(0f, 1f)
        drawRoundRect(RectF(l, t, l + maxOf(w, b - t), b), (b - t) / 2, (b - t) / 2,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = fill })
    }
}

private fun loadBundledBitmap(ctx: Context, id: String, q: Int): Bitmap? = try {
    ctx.assets.open("fish_images/${id}_${QUALITY_IDS[q]}.webp").use { BitmapFactory.decodeStream(it) }
} catch (e: Exception) {
    null
}

/** 用 Android Canvas 直接画一张 1080×1500 的分享卡片 */
fun renderShareCard(ctx: Context, fishList: List<Fish>, done: Set<String>, cols: ShareColors): Bitmap {
    val w = 1080
    val h = 1500
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmp)
    val tf = loadAppTypeface(ctx)
    val st = computeStats(fishList, done)
    c.drawColor(cols.background)

    // 头部
    c.drawRect(0f, 0f, w.toFloat(), 230f, Paint().apply { color = cols.header })
    c.text("钓鱼图鉴", 60f, 112f, 68f, cols.onHeader, tf, true)
    c.text("我的钓鱼收集", 60f, 180f, 36f, cols.onHeader, tf)
    c.text(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        w - 60f, 180f, 32f, cols.onHeader, tf, align = Paint.Align.RIGHT)

    // 卡片 1：总体进度
    c.box(40f, 270f, 1040f, 640f, cols)
    c.text("已钓到品质", 80f, 340f, 38f, cols.text, tf)
    val big = "${st.qualitiesCaught}"
    c.text(big, 80f, 490f, 150f, cols.text, tf, true)
    val bigW = paint(150f, cols.text, tf, true, Paint.Align.LEFT).measureText(big)
    c.text("/ ${st.qualitiesTotal} 品质", 80f + bigW + 24f, 490f, 48f, cols.text, tf)
    val ratio = if (st.qualitiesTotal == 0) 0f else st.qualitiesCaught.toFloat() / st.qualitiesTotal
    c.text("${(ratio * 100).toInt()}%", 1000f, 340f, 56f, cols.accent, tf, true, Paint.Align.RIGHT)
    c.bar(80f, 530f, 1000f, 556f, ratio, cols.accent, cols.text)
    c.text("集齐 ${st.speciesComplete}/${st.speciesTotal} 种 · 已钓到 ${st.speciesAny} 种", 80f, 612f, 34f, cols.text, tf)

    // 卡片 2：各品质
    c.box(40f, 670f, 1040f, 1030f, cols)
    c.text("各品质收集", 80f, 730f, 38f, cols.text, tf)
    for (i in 0 until QUALITY_COUNT) {
        val y = 795f + i * 50f
        val col = QUALITY_COLORS[i].toArgb()
        c.drawCircle(100f, y - 12f, 14f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = col })
        c.text(QUALITY_NAMES[i], 135f, y, 32f, cols.text, tf)
        val r = if (st.speciesTotal == 0) 0f else st.perQuality[i].toFloat() / st.speciesTotal
        c.bar(280f, y - 26f, 870f, y - 4f, r, col, cols.text)
        c.text("${st.perQuality[i]}/${st.speciesTotal}", 1000f, y, 32f, cols.text, tf, align = Paint.Align.RIGHT)
    }

    // 卡片 3：当前最高搏斗等级的鱼
    c.box(40f, 1060f, 1040f, 1420f, cols)
    c.text("当前最高搏斗等级", 80f, 1120f, 38f, cols.text, tf)
    val top = st.top
    if (top == null) {
        c.text("还没有收集记录", w / 2f, 1280f, 44f, cols.text, tf, align = Paint.Align.CENTER)
    } else {
        val dst = RectF(80f, 1150f, 320f, 1390f)
        val img = loadBundledBitmap(ctx, top.fish.id, top.quality)
        if (img != null) {
            val s = minOf(dst.width() / img.width, dst.height() / img.height)
            val iw = img.width * s
            val ih = img.height * s
            val d = RectF(dst.centerX() - iw / 2, dst.centerY() - ih / 2, dst.centerX() + iw / 2, dst.centerY() + ih / 2)
            c.drawBitmap(img, null, d, Paint(Paint.FILTER_BITMAP_FLAG))
        } else {
            c.text("暂无本地图片", dst.centerX(), dst.centerY(), 28f, cols.text, tf, align = Paint.Align.CENTER)
        }
        c.text(top.fish.name, 360f, 1235f, 60f, cols.text, tf, true)
        c.text("搏斗等级 Lv.${top.fish.fight}/6", 360f, 1298f, 38f, cols.text, tf)
        c.text("最高品质 ", 360f, 1356f, 38f, cols.text, tf)
        val lw = paint(38f, cols.text, tf, false, Paint.Align.LEFT).measureText("最高品质 ")
        c.text(QUALITY_NAMES[top.quality], 360f + lw, 1356f, 38f, QUALITY_COLORS[top.quality].toArgb(), tf, true)
    }

    c.text("钓鱼图鉴 · 个人收集记录", w / 2f, 1468f, 28f, cols.text, tf, align = Paint.Align.CENTER)
    return bmp
}

/** 写入缓存文件并构造分享 Intent（含磁盘 IO，请在后台线程调用） */
fun createShareIntent(ctx: Context, bmp: Bitmap): Intent {
    val dir = File(ctx.cacheDir, "shared").apply { mkdirs() }
    val f = File(dir, "fish_card.png")
    f.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", f)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    return Intent.createChooser(send, "分享我的钓鱼图鉴")
}
