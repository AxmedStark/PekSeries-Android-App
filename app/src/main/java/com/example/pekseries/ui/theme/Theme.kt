package com.example.pekseries.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Настраиваем цветовую схему для ТЕМНОЙ темы (основная)
private val DarkColorScheme = darkColorScheme(
    primary = PekRed,            // Кнопки, активные элементы
    secondary = PekRed,          // Вторичные элементы
    tertiary = PekGray,
    background = PekDarkBg,      // Фон всего экрана
    surface = PekCardBg,         // Фон карточек и меню
    onPrimary = Color.White,     // Текст на красной кнопке
    onBackground = PekWhite,     // Текст на темном фоне
    onSurface = PekWhite         // Текст на карточках
)

// Светлая тема (на всякий случай, но цвета тоже темные для стиля)
private val LightColorScheme = lightColorScheme(
    primary = PekRed,
    secondary = PekRed,
    tertiary = PekGray,
    background = PekDarkBg,
    surface = PekCardBg,
    onPrimary = Color.White,
    onBackground = PekWhite,
    onSurface = PekWhite
)

@Composable
fun PekSeriesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color доступен на Android 12+.
    // Мы ставим false, чтобы сохранить твой фирменный красный стиль.
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> DarkColorScheme // Всегда используем темную схему, даже если телефон в светлом режиме
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Красим статус-бар (верхнюю полоску) в цвет фона
            window.statusBarColor = colorScheme.background.toArgb()
            // Делаем иконки статус-бара светлыми
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}