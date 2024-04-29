package platform

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun Modifier.coloredShadow(
    color: Color = Color.Black,
    borderRadius: Dp = 0.dp,
    blurRadius: Float = 0F,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    spread: Float = 0f,
    modifier: Modifier = Modifier,
) : Modifier