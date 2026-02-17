package tech.luceium.ambiair.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

/**
 * Use system settings to choose dark or light theme.
 * Select Android builds can also choose dynamic color schemes.
 */
@Composable
actual fun getSpecialPlatformColorScheme(darkTheme: Boolean): ColorScheme? = null