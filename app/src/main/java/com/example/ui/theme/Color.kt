package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Primary Real Estate Palette
val Navy900 = Color(0xFF0F172A)
val Navy800 = Color(0xFF1E293B)
val Navy700 = Color(0xFF334155)
val Navy600 = Color(0xFF475569)
val Blue600 = Color(0xFF2563EB)
val Blue700 = Color(0xFF1D4ED8)
val Blue50 = Color(0xFFEFF6FF)

// Secondary Emerald Palette
val Emerald700 = Color(0xFF047857)
val Emerald600 = Color(0xFF059669)
val Emerald500 = Color(0xFF10B981)
val Emerald50 = Color(0xFFECFDF5)

// Accent Warm Amber / Gold
val Amber600 = Color(0xFFD97706)
val Amber500 = Color(0xFFF59E0B)
val Amber50 = Color(0xFFFFFBEB)

// Neutrals
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)

// Functional Status Colors
val SuccessGreen = Color(0xFF16A34A)
val WarningAmber = Color(0xFFEA580C)
val ErrorRed = Color(0xFFDC2626)

object DesignColors {
    val Primary = Blue600
    val PrimaryDark = Navy900
    val PrimaryLight = Blue50
    val PrimaryContainer = Color(0xFFDBEAFE)

    val Secondary = Emerald600
    val SecondaryDark = Emerald700
    val SecondaryContainer = Emerald50

    val Accent = Amber600
    val AccentContainer = Amber50

    val Success = SuccessGreen
    val SuccessContainer = Color(0xFFDCFCE7)

    val Warning = WarningAmber
    val WarningContainer = Color(0xFFFFEDD5)

    val Error = ErrorRed
    val ErrorContainer = Color(0xFFFEE2E2)
}

object Spacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
}

object CornerRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
    val pill = 999.dp
}

object Elevation {
    val none = 0.dp
    val low = 1.dp
    val medium = 3.dp
    val high = 6.dp
    val overlay = 12.dp
}
