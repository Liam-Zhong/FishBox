package com.example.fishcodex

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WIDE_BREAKPOINT_DP = 840

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodexApp(vm: CodexViewModel) {
    val load by vm.load.collectAsStateWithLifecycle()
    val done by vm.done.collectAsStateWithLifecycle()
    val starred by vm.starred.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val filters by vm.filters.collectAsStateWithLifecycle()
    val pendingRestore by vm.pendingRestore.collectAsStateWithLifecycle()

    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    // 向下滚动时顶栏整体收起、向上滚动时立即回来（带弹簧吸附），收起后只剩鱼
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val cs = MaterialTheme.colorScheme
    val wide = LocalConfiguration.current.screenWidthDp >= WIDE_BREAKPOINT_DP

    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var showShare by rememberSaveable { mutableStateOf(false) }
    var showAppearance by rememberSaveable { mutableStateOf(false) }
    var showReset by rememberSaveable { mutableStateOf(false) }
    var showBackup by rememberSaveable { mutableStateOf(false) }
    var showAbout by rememberSaveable { mutableStateOf(false) }
    var showStats by rememberSaveable { mutableStateOf(false) }
    var filterOpen by rememberSaveable { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var searchTick by remember { mutableStateOf(0) }

    val fishList = (load as? LoadState.Ready)?.fish ?: emptyList()
    val ready = load is LoadState.Ready
    val selected = selectedId?.let { id -> fishList.firstOrNull { it.id == id } }
    val matchCount = remember(fishList, done, starred, filters) {
        fishList.count { filters.matches(it, done, starred) }
    }
    val stats = remember(fishList, done) { if (fishList.isEmpty()) null else computeStats(fishList, done) }

    /** 弹出提示条；传入 undo 时带"撤销"按钮 */
    fun notify(message: String, undo: (() -> Unit)? = null) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val result = snackbar.showSnackbar(
                message = message,
                actionLabel = if (undo != null) "撤销" else null,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) undo?.invoke()
        }
    }

    LaunchedEffect(Unit) { vm.messages.collect { notify(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> if (uri != null) vm.exportTo(uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> if (uri != null) vm.prepareRestore(uri) }

    val actions = remember(vm) {
        FilterActions(
            setStatus = vm::setStatus,
            toggleSeason = vm::toggleSeason,
            toggleTime = vm::toggleTime,
            toggleWeather = vm::toggleWeather,
            toggleLocation = vm::toggleLocation,
            toggleRod = vm::toggleRod,
            toggleGear = vm::toggleGear,
            clear = vm::clearFilters,
        )
    }

    val onSetAll: (Fish, Boolean) -> Unit = { f, collected ->
        val before = done
        vm.setAll(f, collected)
        if (!wide) selectedId = null
        notify(
            if (collected) "已将「${f.name}」的全部品质标为已收集" else "已清空「${f.name}」的收集记录",
        ) { vm.restore(before) }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = cs.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    // 点按标题区域查看收集统计
                    Column(
                        Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable(enabled = stats != null, onClickLabel = "查看收集统计") { showStats = true }
                            .padding(vertical = 4.dp),
                    ) {
                        Text("钓鱼图鉴", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (stats != null) {
                            Text(
                                "${stats.qualitiesCaught}/${stats.qualitiesTotal} 品质 · 集齐 ${stats.speciesComplete}/${stats.speciesTotal} 种",
                                style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                                color = cs.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    scrolledContainerColor = cs.surface,
                ),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { searchTick++ }, enabled = ready) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = { filterOpen = true }, enabled = ready) {
                        BadgedBox(badge = { if (filters.isActive) Badge() }) {
                            Icon(FilterListIcon, contentDescription = "筛选")
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("分享卡片") },
                                enabled = ready,
                                onClick = { menuOpen = false; showShare = true },
                            )
                            DropdownMenuItem(
                                text = { Text("设置") },
                                onClick = { menuOpen = false; showAppearance = true },
                            )
                            DropdownMenuItem(
                                text = { Text("备份与恢复") },
                                enabled = ready,
                                onClick = { menuOpen = false; showBackup = true },
                            )
                            DropdownMenuItem(
                                text = { Text("重置收集进度") },
                                enabled = ready,
                                onClick = {
                                    menuOpen = false
                                    if (done.isEmpty()) notify("当前没有收集记录") else showReset = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("关于") },
                                onClick = { menuOpen = false; showAbout = true },
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (val state = load) {
            is LoadState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is LoadState.Failed -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("数据加载失败", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    state.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = vm::reload) { Text("重试") }
            }

            is LoadState.Ready -> Row(Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    CodexContent(
                        fishList = state.fish,
                        done = done,
                        starred = starred,
                        filters = filters,
                        cardMinDp = CARD_MIN_DP[settings.cardSize.coerceIn(0, CARD_MIN_DP.size - 1)],
                        sortMode = settings.sortMode,
                        fightDescending = settings.fightDescending,
                        gameTabs = state.gameTabs,
                        padding = padding,
                        searchTick = searchTick,
                        actions = actions,
                        onQuery = vm::setQuery,
                        onEditFilters = { filterOpen = true },
                        onOpen = { selectedId = it.id },
                    )
                }
                // 大屏（平板/折叠屏展开）：详情固定显示在右侧，不再弹出面板
                if (wide) {
                    DetailPane(
                        fish = selected,
                        done = done,
                        starred = starred,
                        topPadding = padding.calculateTopPadding(),
                        onToggle = vm::toggle,
                        onSetAll = onSetAll,
                        onStar = vm::toggleStar,
                    )
                }
            }
        }
    }

    // ---- 面板与对话框 ----
    if (!wide && selected != null) {
        FishSheet(
            fish = selected,
            done = done,
            starred = starred,
            onToggle = { q -> vm.toggle(selected.id, q) },
            onSetAll = { collected -> onSetAll(selected, collected) },
            onStar = { vm.toggleStar(selected.id) },
            onDismiss = { selectedId = null },
        )
    }
    if (filterOpen && ready) {
        FilterSheet(
            filters = filters,
            matchCount = matchCount,
            total = fishList.size,
            actions = actions,
            onDismiss = { filterOpen = false },
        )
    }
    if (showStats && ready) {
        StatsSheet(
            fishList = fishList,
            done = done,
            onOpenFish = { f -> showStats = false; selectedId = f.id },
            onDismiss = { showStats = false },
        )
    }
    if (showShare && ready) ShareDialog(fishList, done) { showShare = false }
    if (showAppearance) {
        AppearanceDialog(
            settings = settings,
            onThemeMode = vm::setThemeMode,
            onDynamicColor = vm::setDynamicColor,
            onCardSize = vm::setCardSize,
            onSortMode = vm::setSortMode,
            onFightDescending = vm::setFightDescending,
            onDismiss = { showAppearance = false },
        )
    }
    if (showBackup) {
        BackupDialog(
            lastBackupAt = settings.lastBackupAt,
            onExport = {
                showBackup = false
                val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                exportLauncher.launch("fishcodex-backup-$date.json")
            },
            onImport = {
                showBackup = false
                importLauncher.launch(arrayOf("*/*"))
            },
            onDismiss = { showBackup = false },
        )
    }
    pendingRestore?.let { pending ->
        RestoreDialog(
            pending = pending,
            currentCount = done.size,
            onConfirm = {
                val beforeDone = done
                val beforeStarred = starred
                vm.confirmRestore()
                notify("已从备份恢复") { vm.restoreAll(beforeDone, beforeStarred) }
            },
            onDismiss = vm::cancelRestore,
        )
    }
    if (showReset) {
        ResetDialog(
            total = fishList.size,
            onConfirm = {
                val before = done
                vm.resetAll()
                showReset = false
                notify("已重置全部收集进度") { vm.restore(before) }
            },
            onDismiss = { showReset = false },
        )
    }
    if (showAbout) AboutDialog { showAbout = false }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FishSheet(
    fish: Fish,
    done: Set<String>,
    starred: Set<String>,
    onToggle: (Int) -> Unit,
    onSetAll: (Boolean) -> Unit,
    onStar: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            FishDetail(fish, done, fish.id in starred, onToggle, onSetAll, onStar)
            Spacer(Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}

@Composable
private fun DetailPane(
    fish: Fish?,
    done: Set<String>,
    starred: Set<String>,
    topPadding: Dp,
    onToggle: (String, Int) -> Unit,
    onSetAll: (Fish, Boolean) -> Unit,
    onStar: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.width(400.dp).fillMaxHeight().padding(top = topPadding),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        if (fish == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "选择一条鱼查看详情",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
                FishDetail(
                    fish = fish,
                    done = done,
                    starred = fish.id in starred,
                    onToggle = { q -> onToggle(fish.id, q) },
                    onSetAll = { collected -> onSetAll(fish, collected) },
                    onStar = { onStar(fish.id) },
                )
                Spacer(Modifier.height(24.dp).navigationBarsPadding())
            }
        }
    }
}

private val STATUS_LABELS = listOf("全部", "已集齐", "未集齐", "已关注")

private class FilterActions(
    val setStatus: (Int) -> Unit,
    val toggleSeason: (String) -> Unit,
    val toggleTime: (String) -> Unit,
    val toggleWeather: (String) -> Unit,
    val toggleLocation: (String) -> Unit,
    val toggleRod: (String) -> Unit,
    val toggleGear: (String) -> Unit,
    val clear: () -> Unit,
)

/**
 * 筛选面板（底部弹出）。像 Apple 的"中等高度"面板：默认只升到半屏，列表在后面保持可见并实时变化，
 * 上拉才展开到全屏。每一行是一个固定的类别，行内可以横向滑动：
 * 第一行 鱼竿 + 配件（轻型/重型），第二行 地点，其后依次是 状态、季节、时间、天气。
 * 条件随时可以勾选/取消，不需要先清除。按游戏里当前的季节、时间、天气勾选，就是"现在能钓到什么"
 * （游戏里的季节/时间/天气是游戏内状态，不是现实世界的时间，所以由玩家自己选择）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    filters: Filters,
    matchCount: Int,
    total: Int,
    actions: FilterActions,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        scrimColor = Color.Black.copy(alpha = 0.12f),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("筛选", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "显示 $matchCount / $total 种",
                        style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                TextButton(onClick = actions.clear, enabled = filters.isActive) { Text("清除全部") }
            }
            // 第一行：鱼竿 + 配件，同一行
            FilterLine {
                FilterGroupLabel("鱼竿")
                ROD_FILTER_ORDER.forEach { k ->
                    AppFilterChip(ROD_ZH[k] ?: k, k in filters.rods) { actions.toggleRod(k) }
                }
                Spacer(Modifier.width(8.dp))
                FilterGroupLabel("配件")
                GEAR_ORDER.forEach { k ->
                    AppFilterChip(GEAR_ZH[k] ?: k, k in filters.gears) { actions.toggleGear(k) }
                }
            }
            // 第二行：地点；之后每个类别各占一行
            FilterRow("地点", LOC_ORDER, { LOC_ZH[it] ?: it }, { it in filters.locations }, actions.toggleLocation)
            FilterRow("状态", STATUS_LABELS, { it }, { filters.status == STATUS_LABELS.indexOf(it) }) {
                actions.setStatus(STATUS_LABELS.indexOf(it))
            }
            FilterRow("季节", SEASON_ORDER, { SEASON_ZH[it] ?: it }, { it in filters.seasons }, actions.toggleSeason)
            FilterRow("时间", TIME_ORDER, { TIME_ZH[it] ?: it }, { it in filters.times }, actions.toggleTime)
            FilterRow("天气", WEATHER_ORDER, { WEATHER_ZH[it] ?: it }, { it in filters.weathers }, actions.toggleWeather)
            Text(
                "鱼竿按出没地点判断：池塘、冰洞用池钓，其余地点用路亚。配件即鱼的重量级：轻型 / 重型。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )
            Spacer(Modifier.height(16.dp).navigationBarsPadding())
        }
    }
}

/** 一行固定的筛选：内容可横向滑动 */
@Composable
private fun FilterLine(content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit) {
    Row(
        Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun FilterGroupLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
}

/** 一个筛选类别：标题和选项在同一行，整行可横向滑动 */
@Composable
private fun FilterRow(
    title: String,
    options: List<String>,
    label: (String) -> String,
    isSelected: (String) -> Boolean,
    onToggle: (String) -> Unit,
) {
    FilterLine {
        FilterGroupLabel(title)
        options.forEach { o -> AppFilterChip(label(o), isSelected(o)) { onToggle(o) } }
    }
}

@Composable
private fun AppFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
        } else null,
    )
}

@Composable
private fun CodexContent(
    fishList: List<Fish>,
    done: Set<String>,
    starred: Set<String>,
    filters: Filters,
    cardMinDp: Int,
    sortMode: SortMode,
    fightDescending: Boolean,
    gameTabs: List<List<String>>,
    padding: PaddingValues,
    searchTick: Int,
    actions: FilterActions,
    onQuery: (String) -> Unit,
    onEditFilters: () -> Unit,
    onOpen: (Fish) -> Unit,
) {
    // 同一类别内多选为"或"，不同类别之间为"且"
    val shown = remember(fishList, done, starred, filters) {
        fishList.filter { filters.matches(it, done, starred) }
    }
    val sections = remember(shown, sortMode, fightDescending, gameTabs) {
        buildSections(shown, sortMode, gameTabs, fightDescending)
    }
    val keys = remember(sections) { sections.map { it.key } }
    val leading = 1 + (if (filters.isActive) 1 else 0) // 搜索框 + 可能出现的"已选条件"行
    // 每个分组标题在网格中的位置，用于右侧索引跳转
    val headerIndex = remember(sections, leading) {
        var idx = leading
        val map = HashMap<String, Int>()
        sections.forEach { sec ->
            map[sec.key] = idx
            idx += 1 + sec.fish.size
        }
        map
    }
    val gridState = rememberLazyGridState()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(filters, sortMode, fightDescending) { gridState.scrollToItem(0) }
    LaunchedEffect(searchTick) {
        if (searchTick > 0) {
            gridState.animateScrollToItem(0)
            runCatching { focusRequester.requestFocus() }
        }
    }
    // "游戏内顺序"固定每行 3 条；其他排序按卡片大小自适应列数
    val columns = if (sortMode == SortMode.GAME) GridCells.Fixed(3) else GridCells.Adaptive(minSize = cardMinDp.dp)

    Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        LazyVerticalGrid(
            state = gridState,
            columns = columns,
            // 右侧留出一条窄边给索引，卡片不会被它遮住
            contentPadding = PaddingValues(
                start = 16.dp, end = 28.dp, top = 8.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "search", span = { GridItemSpan(maxLineSpan) }) {
                SearchField(filters.query, onQuery, focusRequester)
            }
            if (filters.isActive) {
                item(key = "filters", span = { GridItemSpan(maxLineSpan) }) {
                    ActiveFilterChips(filters, actions, onEditFilters)
                }
            }
            sections.forEach { sec ->
                item(key = "header_${sec.key}", span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        sec.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp, top = 8.dp).semantics { heading() },
                    )
                }
                items(sec.fish, key = { it.id }) { f -> FishCard(f, done, f.id in starred) { onOpen(f) } }
            }
            if (shown.isEmpty()) {
                item(key = "empty", span = { GridItemSpan(maxLineSpan) }) {
                    EmptyState(filters, actions.clear)
                }
            }
        }
        AlphabetIndex(keys = keys, headerIndex = headerIndex, gridState = gridState)
    }
}

@Composable
private fun SearchField(query: String, onQuery: (String) -> Unit, focusRequester: FocusRequester) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQuery,
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        placeholder = { Text("搜索名称或拼音") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQuery("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "清除搜索")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = cs.surfaceContainerHigh,
            unfocusedContainerColor = cs.surfaceContainerHigh,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
    )
}

/** 已选条件：每个条件一个可移除的芯片（点一下就取消），最前面的"筛选"芯片再次打开面板继续添加 */
@Composable
private fun ActiveFilterChips(filters: Filters, actions: FilterActions, onEdit: () -> Unit) {
    val chips = buildList<Pair<String, () -> Unit>> {
        if (filters.status in 1..3) add(STATUS_LABELS[filters.status] to { actions.setStatus(0) })
        SEASON_ORDER.filter { it in filters.seasons }.forEach { k -> add((SEASON_ZH[k] ?: k) to { actions.toggleSeason(k) }) }
        TIME_ORDER.filter { it in filters.times }.forEach { k -> add((TIME_ZH[k] ?: k) to { actions.toggleTime(k) }) }
        WEATHER_ORDER.filter { it in filters.weathers }.forEach { k -> add((WEATHER_ZH[k] ?: k) to { actions.toggleWeather(k) }) }
        LOC_ORDER.filter { it in filters.locations }.forEach { k -> add((LOC_ZH[k] ?: k) to { actions.toggleLocation(k) }) }
        ROD_FILTER_ORDER.filter { it in filters.rods }.forEach { k -> add((ROD_ZH[k] ?: k) to { actions.toggleRod(k) }) }
        GEAR_ORDER.filter { it in filters.gears }.forEach { k -> add((GEAR_ZH[k] ?: k) to { actions.toggleGear(k) }) }
    }
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = onEdit,
            label = { Text("筛选") },
            leadingIcon = { Icon(FilterListIcon, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) },
        )
        chips.forEach { (label, remove) ->
            InputChip(
                selected = true,
                onClick = remove,
                label = { Text(label) },
                trailingIcon = {
                    Icon(Icons.Filled.Close, contentDescription = "移除$label", modifier = Modifier.size(InputChipDefaults.IconSize))
                },
            )
        }
        TextButton(onClick = actions.clear) { Text("清除") }
    }
}

/**
 * 右侧字母索引（像"通讯录"）：平时完全隐藏、不占用任何布局空间；
 * 一滚动列表就淡入，停下约 1.2 秒后淡出；淡入期间可以按住字母条上下滑动快速跳转，
 * 滑动时旁边出现当前字母的气泡，并有轻微触感反馈。隐藏时不拦截任何触摸，不会误触。
 * 无障碍：字母分组标题都标成了"标题"，TalkBack 可以按标题导航，所以字母条本身不暴露给读屏。
 */
@Composable
private fun AlphabetIndex(
    keys: List<String>,
    headerIndex: Map<String, Int>,
    gridState: LazyGridState,
) {
    if (keys.size < 2) return
    val cs = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val scrolling = gridState.isScrollInProgress
    var dragging by remember { mutableStateOf(false) }
    var show by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<String?>(null) }
    val scrollable = gridState.canScrollForward || gridState.canScrollBackward

    LaunchedEffect(scrolling, dragging) {
        if (scrolling || dragging) {
            show = true
        } else {
            delay(1200)
            show = false
        }
    }
    val currentLetter by remember(keys, headerIndex) {
        derivedStateOf {
            val first = gridState.firstVisibleItemIndex
            keys.lastOrNull { (headerIndex[it] ?: 0) <= first } ?: keys.first()
        }
    }
    val visible = show && scrollable
    val indexAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "indexAlpha",
    )

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val stripH = maxHeight * 0.7f
        val stripTop = (maxHeight - stripH) / 2

        val gesture = if (visible) {
            Modifier.pointerInput(keys) {
                val pick: (Float) -> Unit = { y ->
                    val idx = ((y / size.height) * keys.size).toInt().coerceIn(0, keys.size - 1)
                    val l = keys[idx]
                    if (l != selected) {
                        selected = l
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        headerIndex[l]?.let { target -> scope.launch { gridState.scrollToItem(target) } }
                    }
                }
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    dragging = true
                    pick(down.position.y)
                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.first()
                        pressed = change.pressed
                        if (pressed) {
                            pick(change.position.y)
                            change.consume()
                        }
                    }
                    dragging = false
                    selected = null
                }
            }
        } else {
            Modifier
        }

        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .width(28.dp)
                .height(stripH)
                .graphicsLayer { this.alpha = indexAlpha }
                .clip(RoundedCornerShape(14.dp))
                .background(if (dragging) cs.surfaceContainerHigh else Color.Transparent)
                .then(gesture)
                .clearAndSetSemantics { },
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val highlight = selected ?: currentLetter
            keys.forEach { l ->
                Text(
                    l,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = if (l == highlight) FontWeight.Bold else FontWeight.Medium,
                    color = if (l == highlight) cs.primary else cs.onSurfaceVariant,
                )
            }
        }

        val sel = selected
        if (dragging && sel != null) {
            val idx = keys.indexOf(sel).coerceAtLeast(0)
            val cy = stripTop + stripH * ((idx + 0.5f) / keys.size)
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-44).dp, y = cy - 28.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(cs.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    sel,
                    style = MaterialTheme.typography.headlineSmall,
                    color = cs.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun EmptyState(filters: Filters, onClear: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            when {
                filters.query.isNotBlank() -> "没有找到「${filters.query.trim()}」"
                filters.activeCount > 0 -> "没有符合条件的鱼"
                filters.status == 1 -> "还没有集齐的鱼"
                filters.status == 2 -> "所有鱼都已集齐"
                filters.status == 3 -> "还没有关注的鱼，在详情里点星标即可关注"
                else -> "没有符合条件的鱼"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        if (filters.isActive) {
            Text(
                "试试放宽筛选条件",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onClear) { Text("清除筛选") }
        }
    }
}
