package theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.Shape

val LifePlanShapes = Shapes()

val Shapes.full: Shape
    get() = RoundedCornerShape(percent = 100)

val Shapes.none: Shape
    get() = RoundedCornerShape(size = 0F)