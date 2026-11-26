package org.chlzhong.fishbox

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.vector.ImageVector
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
    var fabOpen by rememberSaveable { mutableStateOf(false) } // 右下角筛选按钮是否展开
    var facet by rememberSaveable { mutableStateOf<String?>(null) } // 当前打开的筛选类别面板
    var menuOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    // 搜索：点顶栏的放大镜后，顶栏变成搜索框；取消时清空搜索词
    var searching by rememberSaveable { mutableStateOf(false) }
    val searchFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val closeSearch: () -> Unit = {
        searching = false
        vm.setQuery("")
        focusManager.clearFocus()
    }
    BackHandler(enabled = searching) { closeSearch() }
    LaunchedEffect(searching) {
        if (searching) {
            delay(80)
            runCatching { searchFocus.requestFocus() }
        }
    }

    val fishList = (load as? LoadState.Ready)?.fish ?: emptyList()
    val gameTabs = (load as? LoadState.Ready)?.gameTabs ?: emptyList()
    // 详情卡片左右滑动的顺序：打开时按列表当前的显示顺序固定下来，
    // 这样滑动过程中改了收集状态（比如在"未集齐"筛选下集齐了一条）也不会让页面错位
    var pagerIds by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    val visibleOrder = remember(fishList, done, starred, filters, settings.sortMode, settings.fightDescending, gameTabs) {
        pagerOrder(
            buildSections(
                fishList.filter { filters.matches(it, done, starred) },
                settings.sortMode, gameTabs, settings.fightDescending,
            ),
        )
    }
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

    // 外层铺满整个窗口：筛选菜单的遮罩要盖住顶栏、状态栏和导航栏，而不只是鱼卡片区域
    Box(Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = cs.background,
        snackbarHost = { SnackbarHost(snackbar, Modifier.padding(bottom = 72.dp)) }, // 让出右下角筛选按钮的位置
        topBar = {
            if (searching) {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = closeSearch) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "退出搜索")
                        }
                    },
                    title = { SearchTopField(filters.query, vm::setQuery, searchFocus) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = cs.surface,
                        scrolledContainerColor = cs.surface,
                    ),
                    scrollBehavior = scrollBehavior,
                )
            } else {
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
                            // 做成带箭头的色块，一眼能看出可以点开
                            Row(
                                Modifier
                                    .padding(top = 2.dp)
                                    .clip(CircleShape)
                                    .background(cs.secondaryContainer)
                                    .padding(start = 10.dp, end = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "${stats.qualitiesCaught}/${stats.qualitiesTotal} 品质 · 集齐 ${stats.speciesComplete}/${stats.speciesTotal} 种",
                                    style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                                    color = cs.onSecondaryContainer,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = cs.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cs.surface,
                    scrolledContainerColor = cs.surface,
                ),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = { searching = true }, enabled = ready) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    Box {
                        IconButton(onClick = { sortOpen = true }, enabled = ready) {
                            Icon(SortIcon, contentDescription = "排序")
                        }
                        DropdownMenu(expanded = sortOpen, onDismissRequest = { sortOpen = false }) {
                            SortMode.values().forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(m.label) },
                                    leadingIcon = {
                                        if (m == settings.sortMode) Icon(Icons.Filled.Check, contentDescription = null)
                                    },
                                    onClick = { vm.setSortMode(m); sortOpen = false },
                                )
                            }
                            if (settings.sortMode == SortMode.FIGHT) {
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("倒序（由难到易）") },
                                    leadingIcon = {
                                        if (settings.fightDescending) Icon(Icons.Filled.Check, contentDescription = null)
                                    },
                                    onClick = { vm.setFightDescending(!settings.fightDescending); sortOpen = false },
                                )
                            }
                        }
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.Settings, contentDescription = "设置")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("外观与显示") },
                                onClick = { menuOpen = false; showAppearance = true },
                            )
                            DropdownMenuItem(
                                text = { Text("分享卡片") },
                                enabled = ready,
                                onClick = { menuOpen = false; showShare = true },
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
            }
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
                        actions = actions,
                        onEditFilters = { fabOpen = true },
                        onOpen = { selectedId = it.id; pagerIds = visibleOrder },
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

    if (ready) {
        FilterFabMenu(
            open = fabOpen,
            onOpenChange = { fabOpen = it },
            filters = filters,
            onFacet = { key -> facet = key; fabOpen = false },
            onClearAll = { actions.clear(); fabOpen = false },
            // 大屏时右侧固定着 400dp 的详情栏，按钮要躲开它
            endInset = if (wide) DETAIL_PANE_WIDTH else 0.dp,
            modifier = Modifier.fillMaxSize(),
        )
    }
    }

    // ---- 面板与对话框 ----
    if (!wide && selected != null) {
        FishSheet(
            ids = pagerIds,
            initialId = selected.id,
            fishById = remember(fishList) { fishList.associateBy { it.id } },
            done = done,
            starred = starred,
            onPage = { id -> selectedId = id },
            onToggle = vm::toggle,
            onSetAll = onSetAll,
            onStar = vm::toggleStar,
            onDismiss = { selectedId = null },
        )
    }
    facet?.let { key ->
        if (ready) {
            FacetSheet(
                facet = key,
                filters = filters,
                matchCount = matchCount,
                actions = actions,
                onDismiss = { facet = null },
            )
        }
    }
    if (showStats && ready) {
        StatsSheet(
            fishList = fishList,
            done = done,
            onOpenFish = { f -> showStats = false; pagerIds = emptyList(); selectedId = f.id },
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
            onDismiss = { showAppearance = false },
        )
    }
    if (showBackup) {
        BackupDialog(
            lastBackupAt = settings.lastBackupAt,
            onExport = {
                showBackup = false
                val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
                exportLauncher.launch("fishbox-backup-$date.json")
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

/**
 * 手机上的详情底部面板。可左右滑动切换到"下一条 / 上一条"：顺序就是列表里当前看到的顺序
 * （当前的筛选、搜索、排序结果，先行后列）。HorizontalPager 的滑动由系统的帧调度驱动，
 * 在 90/120Hz 屏幕上会按高刷新率渲染（MainActivity 里还会请求设备的最高刷新率）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FishSheet(
    ids: List<String>,
    initialId: String,
    fishById: Map<String, Fish>,
    done: Set<String>,
    starred: Set<String>,
    onPage: (String) -> Unit,
    onToggle: (String, Int) -> Unit,
    onSetAll: (Fish, Boolean) -> Unit,
    onStar: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    // 重启进程后恢复时没有保存顺序，只显示当前这一条
    val pages = remember(ids) { if (initialId in ids) ids else listOf(initialId) }
    val pagerState = rememberPagerState(initialPage = pages.indexOf(initialId).coerceAtLeast(0)) { pages.size }
    LaunchedEffect(pagerState, pages) {
        snapshotFlow { pagerState.settledPage }.collect { page -> pages.getOrNull(page)?.let(onPage) }
    }
    // 将操作按钮固定在面板底部：内容较长时只滚动详情，不会把“全部收集”和“来源页面”挤出屏幕。
    // 移除页码百分比指示器，并略微加高详情区，让原有鱼类信息有更多显示空间。
    val pagerHeight = (LocalConfiguration.current.screenHeightDp * 0.70f).dp
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(pagerHeight),
            pageSpacing = 16.dp,
            beyondViewportPageCount = 1,
            key = { pages[it] },
        ) { page ->
            val fish = fishById[pages[page]]
            if (fish != null) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
                    FishDetail(
                        fish = fish,
                        done = done,
                        starred = fish.id in starred,
                        onToggle = { q -> onToggle(fish.id, q) },
                        onSetAll = { collected -> onSetAll(fish, collected) },
                        onStar = { onStar(fish.id) },
                        showActions = false,
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        val currentFish = pages.getOrNull(pagerState.currentPage)?.let { fishById[it] }
        if (currentFish != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            FishDetailActions(
                fish = currentFish,
                done = done,
                onSetAll = { collected -> onSetAll(currentFish, collected) },
                modifier = Modifier.fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
            )
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
        modifier = Modifier.width(DETAIL_PANE_WIDTH).fillMaxHeight().padding(top = topPadding),
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

/** 大屏时右侧详情栏的宽度 */
private val DETAIL_PANE_WIDTH = 400.dp

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipFlow(content: @Composable () -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) { content() }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
    )
}

/**
 * 一个筛选类别。iconCategory 不为空且该类别的每个选项都有网站图标时，用“图标瓦片”（图标在上、文字在下，
 * 选中时填充容器色并加描边）；只要有一个选项缺图标，就整类退回文字芯片（有图标的芯片前置图标），
 * 避免同一行里瓦片和芯片高矮不一。
 */
@Composable
private fun FilterSection(
    title: String?,
    options: List<String>,
    label: (String) -> String,
    isSelected: (String) -> Boolean,
    iconCategory: String?,
    onToggle: (String) -> Unit,
) {
    val icons = iconCategory?.let { cat -> options.associateWith { uiIconModel(cat, it) } } ?: emptyMap()
    val allIcons = icons.isNotEmpty() && icons.values.all { it != null }
    Column {
        title?.let { SectionLabel(it) }
        ChipFlow {
            options.forEach { o ->
                val icon = icons[o]
                if (allIcons && icon != null) {
                    OptionTile(label(o), icon, isSelected(o)) { onToggle(o) }
                } else {
                    AppFilterChip(label(o), isSelected(o), icon) { onToggle(o) }
                }
            }
        }
    }
}

/** 带网站图标的选项瓦片：图标在上、文字在下，选中时填充容器色并加描边 */
@Composable
private fun OptionTile(label: String, iconModel: String, selected: Boolean, onToggle: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    Column(
        Modifier
            .width(76.dp)
            .clip(shape)
            .background(if (selected) cs.primaryContainer else cs.surfaceContainerHigh)
            .border(1.5.dp, if (selected) cs.primary else Color.Transparent, shape)
            .toggleable(value = selected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OptionIcon(iconModel, 32.dp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) cs.onPrimaryContainer else cs.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 选项芯片（M3 FilterChip，约 40dp 高）。有网站图标时前置显示该图标，选中状态靠容器填充色表现；
 * 没有图标时，选中显示对勾（M3 标准做法）。
 */
@Composable
private fun AppFilterChip(label: String, selected: Boolean, iconModel: String? = null, onClick: () -> Unit) {
    val leading: (@Composable () -> Unit)? = when {
        iconModel != null -> { { OptionIcon(iconModel) } }
        selected -> { { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } }
        else -> null
    }
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        modifier = Modifier.height(40.dp),
        leadingIcon = leading,
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
    actions: FilterActions,
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
    val leading = if (filters.isActive) 1 else 0 // 可能出现的"已选条件"行
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
    LaunchedEffect(filters, sortMode, fightDescending) { gridState.scrollToItem(0) }
    // "游戏内顺序"固定每行 3 条；其他排序按卡片大小自适应列数
    val columns = if (sortMode == SortMode.GAME) GridCells.Fixed(3) else GridCells.Adaptive(minSize = cardMinDp.dp)

    Box(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
        LazyVerticalGrid(
            state = gridState,
            columns = columns,
            // 右侧留出一条窄边给索引，卡片不会被它遮住
            contentPadding = PaddingValues(
                start = 16.dp, end = 28.dp, top = 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp, // 留出右下角筛选按钮的位置
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
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

/** 顶栏里的搜索框（点放大镜后替换标题区域）：无胶囊底色的窄输入行，搜索词实时生效 */
@Composable
private fun SearchTopField(query: String, onQuery: (String) -> Unit, focusRequester: FocusRequester) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = query,
            onValueChange = onQuery,
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = cs.onSurface),
            cursorBrush = SolidColor(cs.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            decorationBox = { inner ->
                Box(Modifier.heightIn(min = 40.dp), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text("搜索名称或拼音", style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)
                    }
                    inner()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQuery("") }) {
                Icon(Icons.Filled.Close, contentDescription = "清除搜索词")
            }
        }
    }
}

/** 已选条件：每个条件一个可移除的芯片（点一下就取消），最前面的"筛选"芯片展开右下角的筛选菜单继续添加 */
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

// ---------------------------------------------------------------------------------------------
// 右下角筛选按钮（类似 Aegis / AntennaPod 的展开式悬浮按钮）
//
// 条件有 8 类、几十个选项，一次摆在一张大面板里太重。这里的做法是"一次只看一类"（Google Drive /
// Google 搜索结果里的筛选条也是这样处理多维度筛选的）：
//   1. 按钮展开后是 4 个类别胶囊，已选的类别高亮并带数量，一眼知道哪里设了条件；
//   2. 点某个类别，只弹出这一类的小面板，勾选立刻生效，标题里实时显示"显示 N 种"；
//   3. 收起时按钮角标显示已选条件总数；展开后若有条件，最上面多一个"清除全部"。
// 类别按拇指的远近排列：最常用的"状态"（未集齐 / 已关注）紧挨着按钮。
// ---------------------------------------------------------------------------------------------

private const val FACET_STATUS = "status"
private const val FACET_GEAR = "gear"
private const val FACET_LOCATION = "loc"
private const val FACET_CONDITION = "cond"

/** 从上到下的显示顺序 */
private val FACETS = listOf(
    FACET_CONDITION to "季节 · 时间 · 天气",
    FACET_LOCATION to "地点",
    FACET_GEAR to "鱼竿与配件",
    FACET_STATUS to "状态",
)

private fun Filters.facetCount(facet: String): Int = when (facet) {
    FACET_STATUS -> if (status != 0) 1 else 0
    FACET_GEAR -> rods.size + gears.size
    FACET_LOCATION -> locations.size
    FACET_CONDITION -> seasons.size + times.size + weathers.size
    else -> 0
}

private const val CLEAR_KEY = "__clear"

/**
 * 筛选悬浮按钮 + 展开菜单。
 *
 * 展开：类别胶囊从按钮处依次“弹”出（离按钮近的先出来，带一点点回弹），收起时快速依次缩回。
 * 两种用法：
 *  - 轻点按钮：展开 / 收起，再点胶囊。
 *  - 长按按钮：展开后手指不用抬起，直接滑向某个胶囊（它会放大高亮并有轻微震动反馈），
 *    松手就打开它；松手时不在任何胶囊上，菜单就保持展开，当作普通展开继续用。（类似 iOS 的长按快捷菜单）
 * 整个手势在按钮自己的指针处理里完成，展开菜单引起的重组不会打断它，所以是连续的，不需要重新触摸。
 */
@Composable
private fun FilterFabMenu(
    open: Boolean,
    onOpenChange: (Boolean) -> Unit,
    filters: Filters,
    onFacet: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    endInset: Dp = 0.dp,
) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val total = FACETS.sumOf { filters.facetCount(it.first) }
    BackHandler(enabled = open) { onOpenChange(false) }

    // 手势里要用最新的值，但又不能让手势因为它们变化而重启
    val openNow by rememberUpdatedState(open)
    val onOpenChangeNow by rememberUpdatedState(onOpenChange)
    val onFacetNow by rememberUpdatedState(onFacet)
    val onClearAllNow by rememberUpdatedState(onClearAll)

    val bounds = remember { mutableMapOf<String, Rect>() } // 各胶囊在屏幕上的位置
    var fabCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var hovered by remember { mutableStateOf<String?>(null) }
    var pressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "fabPress",
    )

    Box(modifier) {
        AnimatedVisibility(visible = open, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize()
                    .background(cs.scrim.copy(alpha = 0.32f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                        onOpenChange(false)
                    },
            )
        }
        Column(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 16.dp + endInset, bottom = 16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(horizontalAlignment = Alignment.End) {
                val count = FACETS.size + if (total > 0) 1 else 0
                if (total > 0) {
                    PopIn(open, order = FACETS.size, count = count) {
                        DisposableEffect(Unit) { onDispose { bounds.remove(CLEAR_KEY) } }
                        FacetPill(
                            "清除全部", 0, false, hovered == CLEAR_KEY,
                            Modifier.onGloballyPositioned { bounds[CLEAR_KEY] = it.boundsInRoot() },
                            onClearAll,
                        )
                    }
                }
                FACETS.forEachIndexed { i, (key, label) ->
                    val n = filters.facetCount(key)
                    PopIn(open, order = FACETS.lastIndex - i, count = count) { // 离按钮越近越先出现
                        DisposableEffect(key) { onDispose { bounds.remove(key) } }
                        FacetPill(
                            label, n, n > 0, hovered == key,
                            Modifier.onGloballyPositioned { bounds[key] = it.boundsInRoot() },
                        ) { onFacet(key) }
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .graphicsLayer { scaleX = pressScale; scaleY = pressScale }
                    .onGloballyPositioned { fabCoords = it }
                    .semantics {
                        role = Role.Button
                        contentDescription = if (open) "收起筛选" else "筛选"
                        onClick { onOpenChange(!open); true }
                    }
                    .pointerInput(Unit) {
                        val vSlop = 5.dp.toPx()   // 胶囊之间的空隙一并算进去，手指划过时不会“掉空”
                        val hSlop = 56.dp.toPx()  // 手指偏左一点也算选中（右手拇指的自然轨迹）
                        fun pillAt(p: Offset): String? = bounds.entries.firstOrNull { (_, r) ->
                            p.y >= r.top - vSlop && p.y <= r.bottom + vSlop && p.x >= r.left - hSlop
                        }?.key
                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            pressed = true
                            val longPress = awaitLongPressOrCancellation(down.id)
                            if (longPress == null) {
                                // 没等到长按：手指抬起就是一次轻点
                                val ch = currentEvent.changes.firstOrNull { it.id == down.id }
                                pressed = false
                                if (ch != null && !ch.pressed) onOpenChangeNow(!openNow)
                                return@awaitEachGesture
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenChangeNow(true)
                            hovered = null
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Main)
                                val ch = event.changes.firstOrNull { it.id == down.id } ?: break
                                ch.consume()
                                val root = fabCoords?.takeIf { it.isAttached }?.localToRoot(ch.position)
                                val now = root?.let(::pillAt)
                                if (now != hovered) {
                                    hovered = now
                                    if (now != null) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                if (!ch.pressed) break
                            }
                            pressed = false
                            val target = hovered
                            hovered = null
                            // 没选中任何胶囊（target 为空）：菜单保持展开，当作普通展开继续用
                            target?.let { if (it == CLEAR_KEY) onClearAllNow() else onFacetNow(it) }
                        }
                    },
                shape = MaterialTheme.shapes.large,
                color = if (open) cs.tertiaryContainer else cs.primaryContainer,
                contentColor = if (open) cs.onTertiaryContainer else cs.onPrimaryContainer,
                shadowElevation = 6.dp,
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Crossfade(targetState = open, label = "fabIcon") { isOpen ->
                        if (isOpen) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                        } else {
                            BadgedBox(badge = { if (total > 0) Badge { Text("$total") } }) {
                                Icon(FilterListIcon, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 展开菜单里每个胶囊的出现 / 消失动画：从按钮的方向（右下）缩放、上移并淡入，
 * 带一点回弹，靠近按钮的先出现（order 越小越先）；收起时反向、更快。
 */
@Composable
private fun PopIn(visible: Boolean, order: Int, count: Int, content: @Composable () -> Unit) {
    val p = remember { Animatable(0f) }
    LaunchedEffect(visible) {
        if (visible) {
            delay(order * 30L)
            p.animateTo(1f, spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow))
        } else {
            delay((count - 1 - order) * 18L)
            p.animateTo(0f, tween(durationMillis = 140))
        }
    }
    if (visible || p.value > 0.001f) {
        Box(
            Modifier
                .padding(top = 10.dp) // 间距放在每一项里，没出现的项不占位
                .graphicsLayer {
                    val v = p.value
                    alpha = v.coerceIn(0f, 1f)
                    val sc = 0.5f + 0.5f * v
                    scaleX = sc
                    scaleY = sc
                    translationY = (1f - v) * 28.dp.toPx()
                    transformOrigin = TransformOrigin(1f, 1f)
                },
        ) { content() }
    }
}

/** 展开菜单里的一个类别胶囊；已选的类别用容器色高亮并显示数量，长按滑选经过时放大并变成主色 */
@Composable
private fun FacetPill(
    label: String,
    count: Int,
    active: Boolean,
    hovered: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue = if (hovered) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "pillScale",
    )
    val container by animateColorAsState(
        when {
            hovered -> cs.primary
            active -> cs.primaryContainer
            else -> cs.surfaceContainerHigh
        },
        label = "pillColor",
    )
    val content = if (hovered) cs.onPrimary else if (active) cs.onPrimaryContainer else cs.onSurface
    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
            transformOrigin = TransformOrigin(1f, 0.5f) // 向左放大，不超出屏幕右边
        },
        shape = CircleShape,
        color = container,
        contentColor = content,
        shadowElevation = if (hovered) 8.dp else 3.dp,
    ) {
        Row(
            Modifier.heightIn(min = 48.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.labelLarge)
            if (count > 0) {
                Spacer(Modifier.width(8.dp))
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                    color = if (hovered) cs.primary else cs.onPrimary,
                    modifier = Modifier.clip(CircleShape)
                        .background(if (hovered) cs.onPrimary else cs.primary)
                        .padding(horizontal = 7.dp, vertical = 1.dp),
                )
            }
        }
    }
}

/** 单个类别的小面板：勾选立即生效，标题处实时显示结果数量 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FacetSheet(
    facet: String,
    filters: Filters,
    matchCount: Int,
    actions: FilterActions,
    onDismiss: () -> Unit,
) {
    val cs = MaterialTheme.colorScheme
    val title = FACETS.firstOrNull { it.first == facet }?.second ?: "筛选"
    val count = filters.facetCount(facet)
    val clear: () -> Unit = {
        when (facet) {
            FACET_STATUS -> actions.setStatus(0)
            FACET_GEAR -> {
                filters.rods.toList().forEach(actions.toggleRod)
                filters.gears.toList().forEach(actions.toggleGear)
            }
            FACET_LOCATION -> filters.locations.toList().forEach(actions.toggleLocation)
            FACET_CONDITION -> {
                filters.seasons.toList().forEach(actions.toggleSeason)
                filters.times.toList().forEach(actions.toggleTime)
                filters.weathers.toList().forEach(actions.toggleWeather)
            }
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        scrimColor = Color.Black.copy(alpha = 0.12f),
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "显示 $matchCount 种",
                        style = MaterialTheme.typography.labelMedium.merge(TabularNums),
                        color = cs.onSurfaceVariant,
                    )
                }
                TextButton(onClick = clear, enabled = count > 0) { Text("清除") }
                Button(onClick = onDismiss) { Text("完成") }
            }
            when (facet) {
                FACET_STATUS -> {
                    Spacer(Modifier.height(12.dp))
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        STATUS_LABELS.forEachIndexed { i, label ->
                            SegmentedButton(
                                selected = filters.status == i,
                                onClick = { actions.setStatus(i) },
                                shape = SegmentedButtonDefaults.itemShape(i, STATUS_LABELS.size),
                                icon = {},
                            ) { Text(label, maxLines = 1) }
                        }
                    }
                }
                FACET_GEAR -> {
                    SectionLabel("鱼竿")
                    MultiChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ROD_FILTER_ORDER.forEachIndexed { i, k ->
                            val on = k in filters.rods
                            SegmentedButton(
                                checked = on,
                                onCheckedChange = { actions.toggleRod(k) },
                                shape = SegmentedButtonDefaults.itemShape(i, ROD_FILTER_ORDER.size),
                            ) { Text(ROD_ZH[k] ?: k, maxLines = 1) }
                        }
                    }
                    SectionLabel("配件")
                    MultiChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        GEAR_ORDER.forEachIndexed { i, k ->
                            val on = k in filters.gears
                            val iconModel = uiIconModel("gear", k)
                            SegmentedButton(
                                checked = on,
                                onCheckedChange = { actions.toggleGear(k) },
                                shape = SegmentedButtonDefaults.itemShape(i, GEAR_ORDER.size),
                                icon = { if (iconModel != null) OptionIcon(iconModel) else SegmentedButtonDefaults.Icon(on) },
                            ) { Text(GEAR_ZH[k] ?: k, maxLines = 1) }
                        }
                    }
                }
                FACET_LOCATION -> {
                    Spacer(Modifier.height(8.dp))
                    FilterSection(null, LOC_ORDER, { LOC_ZH[it] ?: it }, { it in filters.locations }, "location", actions.toggleLocation)
                }
                FACET_CONDITION -> {
                    FilterSection("季节", SEASON_ORDER, { SEASON_ZH[it] ?: it }, { it in filters.seasons }, "season", actions.toggleSeason)
                    Spacer(Modifier.height(8.dp))
                    FilterSection("时间", TIME_ORDER, { TIME_ZH[it] ?: it }, { it in filters.times }, "time", actions.toggleTime)
                    Spacer(Modifier.height(8.dp))
                    FilterSection("天气", WEATHER_ORDER, { WEATHER_ZH[it] ?: it }, { it in filters.weathers }, "weather", actions.toggleWeather)
                    Text(
                        "按游戏里当前的季节、时间、天气勾选，就是\"现在能钓到什么\"。",
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp).navigationBarsPadding())
        }
    }
}
