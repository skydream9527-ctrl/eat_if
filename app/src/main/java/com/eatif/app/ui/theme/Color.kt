package com.eatif.app.ui.theme

import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFFF6B35)
val OrangeLight = Color(0xFFFF8F5C)
val OrangeDark = Color(0xFFE55A2B)
val White = Color(0xFFFFFFFF)
val GrayLight = Color(0xFFF5F5F7)
val GrayMedium = Color(0xFF86868B)
val Gray = GrayMedium
val Black = Color(0xFF1D1D1F)
val Green = Color(0xFF34C759)
val Red = Color(0xFFFF3B30)
val Gold = Color(0xFFFFD700)

val ThemeColorOrange = Color(0xFFFF6B35)
val ThemeColorGreen = Color(0xFF34C759)
val ThemeColorBlue = Color(0xFF007AFF)
val ThemeColorPurple = Color(0xFFAF52DE)
val ThemeColorPink = Color(0xFFFF2D55)
val ThemeColorRed = Color(0xFFFF3B30)
val ThemeColorTeal = Color(0xFF5AC8FA)
val ThemeColorYellow = Color(0xFFFFCC00)

data class ThemeColorOption(val key: String, val color: Color, val label: String)

val ThemeColorOptions = listOf(
    ThemeColorOption("orange", ThemeColorOrange, "活力橙"),
    ThemeColorOption("green", ThemeColorGreen, "清新绿"),
    ThemeColorOption("blue", ThemeColorBlue, "天空蓝"),
    ThemeColorOption("purple", ThemeColorPurple, "优雅紫"),
    ThemeColorOption("pink", ThemeColorPink, "甜蜜粉"),
    ThemeColorOption("red", ThemeColorRed, "热情红"),
    ThemeColorOption("teal", ThemeColorTeal, "海洋青"),
    ThemeColorOption("yellow", ThemeColorYellow, "阳光黄")
)
