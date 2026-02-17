package tech.luceium.ambiair.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Composable
@OptIn(ExperimentalResourceApi::class)
fun AmbiAirTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
//  val colorScheme = getSpecialPlatformColorScheme(darkTheme)?: when (darkTheme) {
//      true -> darkScheme
//      false -> lightScheme
//  }

//  val view = LocalView.current
//  if (!view.isInEditMode) {
//    SideEffect {
//      val window = (view.context as Activity).window
//      window.statusBarColor = colorScheme.primary.toArgb()
//      WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
//    }
//  }

  MaterialTheme(
    colorScheme = lightScheme, // colorScheme,
    typography = AmbiAirTypography(),
    content = content
  )
}
