package tech.luceium.ambiair.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Use system settings to choose dark or light theme.
 * Select Android builds can also choose dynamic color schemes.
 */
@Composable
actual fun getSpecialPlatformColorScheme(darkTheme: Boolean): ColorScheme? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        (if (darkTheme) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current))
    else null