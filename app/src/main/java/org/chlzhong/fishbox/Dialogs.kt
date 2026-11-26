package org.chlzhong.fishbox

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 统计与筛选弹窗共用同一高度比例和 Material 3 半屏/全屏锚点。 */
internal const val APP_MODAL_BOTTOM_SHEET_HEIGHT_FRACTION = 0.9f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun rememberAppModalBottomSheetState() =
    rememberModalBottomSheetState(skipPartiallyExpanded = false)

/** 分享预览：卡片在后台线程绘制，分享时在后台线程写入缓存文件 */
@Composable
fun ShareDialog(fishList: List<Fish>, done: Set<String>, onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val colors = ShareColors(
        background = cs.surface.toArgb(),
        header = cs.primaryContainer.toArgb(),
        onHeader = cs.onPrimaryContainer.toArgb(),
        card = cs.surfaceContainerHigh.toArgb(),
        stroke = cs.outlineVariant.toArgb(),
        text = cs.onSurface.toArgb(),
        accent = cs.primary.toArgb(),
    )
    val bitmap by produceState<Bitmap?>(initialValue = null, done, colors) {
        value = withContext(Dispatchers.Default) { renderShareCard(ctx, fishList, done, colors) }
    }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享卡片") },
        text = {
            val bmp = bitmap
            if (bmp == null) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "分享卡片预览",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = bitmap != null,
                onClick = {
                    bitmap?.let { b ->
                        scope.launch {
                            val intent = withContext(Dispatchers.IO) { createShareIntent(ctx, b) }
                            try {
                                ctx.startActivity(intent)
                            } catch (e: Exception) {
                                // 没有可处理分享的应用：忽略
                            }
                        }
                    }
                },
            ) { Text("分享") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun AppearanceDialog(
    settings: AppSettings,
    onThemeMode: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onCardSize: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val supported = DYNAMIC_COLOR_SUPPORTED
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("外观与显示") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text("主题模式", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                ThemeMode.values().forEach { mode ->
                    RadioRow(mode.label, settings.themeMode == mode) { onThemeMode(mode) }
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    Modifier.fillMaxWidth()
                        .toggleable(
                            value = settings.dynamicColor && supported,
                            enabled = supported,
                            role = Role.Switch,
                            onValueChange = onDynamicColor,
                        )
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("动态取色", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (supported) "跟随系统壁纸取色" else "需要 Android 12 及以上",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(checked = settings.dynamicColor && supported, onCheckedChange = null, enabled = supported)
                }
                Spacer(Modifier.height(12.dp))
                Text("卡片大小", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                CARD_SIZE_LABELS.forEachIndexed { i, label ->
                    RadioRow(label, settings.cardSize == i) { onCardSize(i) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("完成") } },
    )
}

@Composable
fun ResetDialog(total: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("重置收集进度？") },
        text = { Text("将清空全部 $total 种鱼的收集记录（关注列表不受影响）。重置后可在提示条中立即撤销。") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text("重置") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
fun BackupDialog(lastBackupAt: Long, onExport: () -> Unit, onImport: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("备份与恢复") },
        text = {
            Column {
                Text(
                    "把收集进度和关注列表导出为一个 JSON 文件，保存到你选择的位置（本机、云盘等）；换手机或重装后可以从备份文件恢复。不需要账号，也不会上传任何数据。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (lastBackupAt > 0L) "上次备份：" + DateFormat.getDateTimeInstance().format(Date(lastBackupAt))
                    else "尚未备份",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
                    androidx.compose.material3.Button(onClick = onExport) { Text("导出备份") }
                    OutlinedButton(onClick = onImport) { Text("从备份恢复") }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

@Composable
fun RestoreDialog(pending: PendingRestore, currentCount: Int, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("恢复这份备份？") },
        text = {
            Text(
                "备份中有 ${pending.data.progress.size} 个已收集品质、${pending.data.starred.size} 条关注" +
                    (if (pending.ignored > 0) "（另有 ${pending.ignored} 项无法识别，已忽略）" else "") +
                    "。\n\n恢复会用备份替换当前的 $currentCount 个已收集品质和关注列表；恢复后可在提示条中撤销。"
            )
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("恢复") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
    )
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    val ctx = LocalContext.current
    val version = remember {
        try {
            ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("关于钓鱼图鉴") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
                if (version.isNotEmpty()) {
                    Text("版本 $version", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("《猛兽派对》（Party Animals）钓鱼收集记录工具。游戏名称、鱼类图片及相关素材的版权属于 Party Animals 所有。", style = MaterialTheme.typography.bodyMedium)
                Text("鱼类资料整理自社区资料，可能与游戏的最新版本不一致，仅供参考。", style = MaterialTheme.typography.bodyMedium)
                Text("隐私：本应用不收集、不上传任何个人数据；收集进度和设置只保存在你的设备上，备份文件由你自行保管。", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}

@Composable
private fun ProgressRing(ratio: Float, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    val animated by animateFloatAsState(ratio, tween(durationMillis = 700), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = 12.dp.toPx()
            val d = size.minDimension - stroke
            val topLeft = Offset(stroke / 2, stroke / 2)
            drawArc(cs.surfaceContainerHighest, -90f, 360f, false, topLeft, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
            if (animated > 0f) {
                drawArc(cs.primary, -90f, 360f * animated, false, topLeft, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
            }
        }
        Text("${(ratio * 100).toInt()}%", style = MaterialTheme.typography.titleLarge.merge(TabularNums))
    }
}

@Composable
private fun StatsRow(label: String, value: Int, total: Int, color: Color? = null) {
    val ratio = if (total == 0) 0f else value.toFloat() / total
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(56.dp))
        LinearProgressIndicator(
            progress = { ratio },
            color = color ?: MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
        )
        Text(
            "$value/$total",
            style = MaterialTheme.typography.labelMedium.merge(TabularNums),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp).padding(start = 8.dp),
        )
    }
}

@Composable
private fun StatsTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

/** 收集统计面板：总进度环、各品质进度、只差一档集齐的鱼、按季节/地点的完成度 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsSheet(fishList: List<Fish>, done: Set<String>, onOpenFish: (Fish) -> Unit, onDismiss: () -> Unit) {
    val stats = remember(fishList, done) { computeStats(fishList, done) }
    val ins = remember(fishList, done) { computeInsights(fishList, done) }
    val ratio = if (stats.qualitiesTotal == 0) 0f else stats.qualitiesCaught.toFloat() / stats.qualitiesTotal

    val sheetState = rememberAppModalBottomSheetState()
    val isExpanded = sheetState.currentValue == SheetValue.Expanded ||
        sheetState.targetValue == SheetValue.Expanded

    // 90% 的内容高度确保 M3 同时建立半屏和全屏锚点；半屏只显示品质概览，
    // 展开后再显示季节、地点和待完成鱼类，并允许在内容区滚动。
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight(APP_MODAL_BOTTOM_SHEET_HEIGHT_FRACTION)) {
            Column(
                Modifier.fillMaxWidth().weight(1f)
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            ) {
                Text("收集统计", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(ratio, Modifier.size(96.dp))
                    Spacer(Modifier.width(20.dp))
                    Column {
                        Text(
                            "${stats.qualitiesCaught} / ${stats.qualitiesTotal} 品质",
                            style = MaterialTheme.typography.titleMedium.merge(TabularNums),
                        )
                        Text(
                            "集齐 ${stats.speciesComplete} / ${stats.speciesTotal} 种",
                            style = MaterialTheme.typography.bodyMedium.merge(TabularNums),
                        )
                        Text(
                            "还差 ${stats.qualitiesTotal - stats.qualitiesCaught} 个品质",
                            style = MaterialTheme.typography.bodyMedium.merge(TabularNums),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                StatsTitle("各品质")
                for (q in 0 until QUALITY_COUNT) {
                    StatsRow(QUALITY_NAMES[q], stats.perQuality[q], stats.speciesTotal, QUALITY_COLORS[q])
                }

                if (isExpanded) {
                    if (ins.nearComplete.isNotEmpty()) {
                        StatsTitle("只差一档集齐")
                        ins.nearComplete.take(12).forEach { n ->
                            Row(
                                Modifier.fillMaxWidth()
                                    .clip(MaterialTheme.shapes.small)
                                    .clickable { onOpenFish(n.fish) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                FishImage(n.fish, displayQuality(n.fish, done), false, Modifier.size(40.dp))
                                Spacer(Modifier.width(12.dp))
                                Text(n.fish.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                                Text(
                                    "缺 ${QUALITY_NAMES[n.missingQuality]}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    StatsTitle("按季节")
                    ins.bySeason.forEach { StatsRow(SEASON_ZH[it.id] ?: it.id, it.collected, it.total) }
                    StatsTitle("按地点")
                    ins.byLocation.forEach { StatsRow(LOC_ZH[it.id] ?: it.id, it.collected, it.total) }
                }

                Spacer(Modifier.height(24.dp).navigationBarsPadding())
            }
        }
    }
}
