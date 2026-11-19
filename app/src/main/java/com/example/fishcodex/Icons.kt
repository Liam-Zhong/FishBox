package com.example.fishcodex

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/** Material "filter_list" 图标（core 图标集里没有，这里自绘，避免引入体积很大的 extended 图标库） */
val FilterListIcon: ImageVector by lazy {
    materialIcon(name = "Filled.FilterList") {
        materialPath {
            moveTo(10.0f, 18.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(2.0f)
            close()
            moveTo(3.0f, 6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineToRelative(18.0f)
            verticalLineTo(6.0f)
            horizontalLineTo(3.0f)
            close()
            moveTo(6.0f, 13.0f)
            horizontalLineToRelative(12.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(2.0f)
            close()
        }
    }
}
