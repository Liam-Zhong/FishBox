package com.example.fishcodex

import androidx.compose.animation.animateColorAsState
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage

fun isComplete(fish: Fish, done: Set<String>): Boolean =
    (0 until QUALITY_COUNT).all { key(fish.id, it) in done }

fun collectedCount(fish: Fish, done: Set<String>): Int =
    (0 until QUALITY_COUNT).count { key(fish.id, it) in done }

/** 卡片上展示"已收集的最高品质"的图片；一个都没有时展示标准品质 */
fun displayQuality(fish: Fish, done: Set<String>): Int =
    (QUALITY_COUNT - 1 downTo 0).firstOrNull { key(fish.id, it) in done } ?: 0

private fun Color.toGray(): Color {
    val l = 0.299f * red + 0.587f * green + 0.114f * blue
    return Color(l, l, l, 1f).copy(alpha = 0.75f)
}

/** 本地没有图片时的占位鱼（Canvas 绘制） */
@Composable
fun FishIcon(fish: Fish, colored: Boolean, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val body = if (colored) fish.body else fish.body.toGray()
        val fin = if (colored) fish.fin else fish.fin.toGray()
        val w = size.width
        val h = size.height
        val cx = w * 0.5f
        val cy = h * 0.5f
        val bw = w * 0.62f
        val bh = h * when (fish.shape) { 1 -> 0.30f; 2 -> 0.74f; else -> 0.52f }

        val tail = Path().apply {
            moveTo(cx - bw / 2 + 6f, cy)
            lineTo(w * 0.06f, cy - bh * 0.6f)
            lineTo(w * 0.06f, cy + bh * 0.6f)
            close()
        }
        drawPath(tail, fin)
        val dorsal = Path().apply {
            moveTo(cx - bw * 0.2f, cy - bh / 2 + 3f)
            lineTo(cx, cy - bh * 0.85f)
            lineTo(cx + bw * 0.2f, cy - bh / 2 + 3f)
            close()
        }
        drawPath(dorsal, fin)
        drawOval(body, Offset(cx - bw / 2, cy - bh / 2), Size(bw, bh))
        drawOval(Color.White.copy(alpha = 0.25f), Offset(cx - bw * 0.35f, cy + bh * 0.05f), Size(bw * 0.7f, bh * 0.28f))
        drawCircle(Color.White, bh * 0.11f, Offset(cx + bw * 0.28f, cy - bh * 0.1f))
        drawCircle(Color.Black, bh * 0.055f, Offset(cx + bw * 0.30f, cy - bh * 0.1f))
    }
}

private val GrayFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })

/** 鱼图：优先本地资源；未集齐时灰度显示；加载失败或没有图片时退回占位鱼 */
@Composable
fun FishImage(fish: Fish, quality: Int, colored: Boolean, modifier: Modifier = Modifier) {
    SubcomposeAsyncImage(
        model = fishImageModel(fish.id, quality),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = if (colored) null else GrayFilter,
        alpha = if (colored) 1f else 0.8f,
        loading = { FishIcon(fish, colored, Modifier.fillMaxSize()) },
        error = { FishIcon(fish, colored, Modifier.fillMaxSize()) },
    )
}

@Composable
fun QualityDots(fish: Fish, done: Set<String>, size: Dp = 7.dp) {
    // 已收集：品质色实心；未收集：统一灰色空心圈
    val gray = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (q in 0 until QUALITY_COUNT) {
            val on = key(fish.id, q) in done
            Box(
                Modifier.size(size).clip(CircleShape)
                    .then(
                        if (on) Modifier.background(QUALITY_COLORS[q])
                        else Modifier.border(1.dp, gray, CircleShape)
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FishCard(fish: Fish, done: Set<String>, starred: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val complete = isComplete(fish, done)
    val count = collectedCount(fish, done)
    val container by animateColorAsState(
        targetValue = if (complete) cs.primaryContainer else cs.surfaceContainer,
        label = "cardContainer",
    )
    val content = if (complete) cs.onPrimaryContainer else cs.onSurfaceVariant
    val description = "${fish.name}，已收集 $count/$QUALITY_COUNT 个品质" +
        (if (complete) "，已集齐" else "") + (if (starred) "，已关注" else "")

    Box {
        Card(
            onClick = onClick,
            shape = MaterialTheme.shapes.medium,
            colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
            modifier = Modifier.semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            },
        ) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                FishImage(fish, displayQuality(fish, done), complete, Modifier.fillMaxWidth().aspectRatio(1f))
                Spacer(Modifier.height(4.dp))
                Text(
                    fish.name,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(6.dp))
                QualityDots(fish, done)
            }
        }
        if (starred) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = cs.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(6.dp).size(16.dp),
            )
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("$label：", style = MaterialTheme.typography.labelLarge)
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * 鱼的详情内容：手机上放在底部面板里，大屏（≥840dp）放在右侧的详情栏里。
 * 5 个品质的名称统一为 标准/非凡/稀有/史诗/传奇；个别鱼的专属品质名放在可展开的"品质别名与说明"里。
 */
@Composable
fun FishDetail(
    fish: Fish,
    done: Set<String>,
    starred: Boolean,
    onToggle: (Int) -> Unit,
    onSetAll: (Boolean) -> Unit,
    onStar: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val complete = isComplete(fish, done)
    val count = collectedCount(fish, done)

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FishImage(fish, displayQuality(fish, done), complete, Modifier.size(96.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(fish.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f, fill = false))
                    if (fish.isNew) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "新",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onPrimary,
                            modifier = Modifier.clip(CircleShape).background(cs.primary).padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
                Text(
                    "已收集 $count / $QUALITY_COUNT · 搏斗 Lv.${fish.fight}",
                    style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                    color = cs.onSurfaceVariant,
                )
            }
            IconButton(onClick = onStar) {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = if (starred) "取消关注" else "关注",
                    tint = if (starred) cs.primary else cs.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(fish.description, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            for (q in 0 until QUALITY_COUNT) {
                val on = key(fish.id, q) in done
                val c = QUALITY_COLORS[q]
                Column(
                    Modifier
                        .clip(MaterialTheme.shapes.small)
                        .toggleable(value = on, role = Role.Checkbox, onValueChange = { onToggle(q) })
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        Modifier.size(46.dp).clip(CircleShape)
                            .border(2.dp, if (on) c else c.copy(alpha = 0.35f), CircleShape)
                            .padding(3.dp)
                    ) { FishImage(fish, q, on, Modifier.fillMaxSize()) }
                    Text(QUALITY_NAMES[q], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                    Text(
                        "${fish.prices[q]} 干",
                        style = MaterialTheme.typography.labelSmall.merge(TabularNums),
                        color = cs.onSurfaceVariant,
                    )
                }
            }
        }
        Text(
            "点按圆圈切换收集状态；数字为出售价（小鱼干）",
            style = MaterialTheme.typography.labelSmall,
            color = cs.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )

        val names = fish.rarityNames
        if (names != null) {
            var open by rememberSaveable(fish.id) { mutableStateOf(false) }
            TextButton(onClick = { open = !open }) { Text(if (open) "收起品质别名" else "品质别名与说明") }
            if (open) {
                for (q in 0 until QUALITY_COUNT) {
                    Text("${QUALITY_NAMES[q]} · ${names[q]}", style = MaterialTheme.typography.labelLarge)
                    val desc = fish.rarityDescs?.getOrNull(q)
                    if (!desc.isNullOrBlank()) {
                        Text(
                            desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 6.dp),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        InfoLine("地点", fish.locations.joinToString("、") { LOC_ZH[it] ?: it })
        InfoLine("时间", fish.times.joinToString("、") { TIME_ZH[it] ?: it })
        InfoLine("天气", fish.weather.joinToString("、") { WEATHER_ZH[it] ?: it })
        InfoLine("季节", fish.seasons.joinToString("、") { SEASON_ZH[it] ?: it })
        InfoLine(
            "鱼竿",
            ROD_FILTER_ORDER.filter { it in fish.rodKinds }.joinToString("/") { ROD_ZH[it] ?: it } +
                when (fish.gear) { "light" -> "（轻型）"; "heavy" -> "（重型）"; else -> "" }
        )
        InfoLine("搏斗难度", "${fish.fight} / 6")
        

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onSetAll(!complete) }) { Text(if (complete) "清空" else "全部收集") }
            if (fish.detailUrl.isNotBlank()) {
                OutlinedButton(
                    onClick = {
                        try {
                            ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fish.detailUrl)))
                        } catch (e: Exception) {
                            // 没有可打开链接的应用：忽略
                        }
                    },
                ) { Text("来源页面") }
            }
        }
    }
}
