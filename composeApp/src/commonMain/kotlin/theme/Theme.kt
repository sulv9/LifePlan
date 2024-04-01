package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LifePlanLightColors = lightColorScheme()

private val LifePlanDarkColors = darkColorScheme()

@Composable
fun LifePlanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) LifePlanDarkColors else LifePlanLightColors,
        shapes = LifePlanShapes,
        typography = LifePlanTypography,
        content = content
    )
}