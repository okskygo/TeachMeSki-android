package com.teachmeski.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Placeholder until Inter font files are added under res/font (see theme task).
val InterFontFamily = FontFamily.SansSerif

val TmsTypography = Typography(
    displayLarge = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 57.sp, letterSpacing = (-0.02).sp),
    displayMedium = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 45.sp, letterSpacing = (-0.02).sp),
    displaySmall = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 36.sp, letterSpacing = (-0.02).sp),
    headlineLarge = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = InterFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)
