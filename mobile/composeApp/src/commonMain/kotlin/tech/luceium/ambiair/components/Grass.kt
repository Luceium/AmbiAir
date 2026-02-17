import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val Grass: ImageVector
    get() {
        if (_undefined != null) {
            return _undefined!!
        }
        _undefined = ImageVector.Builder(
            name = "Grass",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1.0f,
                stroke = null,
                strokeAlpha = 1.0f,
                strokeLineWidth = 1.0f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 1.0f,
                pathFillType = PathFillType.NonZero
            ) {
                moveTo(80f, 800f)
                verticalLineToRelative(-80f)
                horizontalLineToRelative(230f)
                quadToRelative(-22f, -85f, -83.5f, -146.5f)
                reflectiveQuadTo(80f, 490f)
                quadToRelative(20f, -5f, 39.5f, -7.5f)
                reflectiveQuadTo(160f, 480f)
                quadToRelative(134f, 0f, 227f, 93f)
                reflectiveQuadToRelative(93f, 227f)
                close()
                moveToRelative(480f, 0f)
                quadToRelative(0f, -42f, -9f, -83.5f)
                reflectiveQuadTo(525f, 637f)
                quadToRelative(42f, -71f, 114.5f, -114f)
                reflectiveQuadTo(800f, 480f)
                quadToRelative(21f, 0f, 40.5f, 2.5f)
                reflectiveQuadTo(880f, 490f)
                quadToRelative(-85f, 22f, -146f, 83.5f)
                reflectiveQuadTo(650f, 720f)
                horizontalLineToRelative(230f)
                verticalLineToRelative(80f)
                close()
                moveToRelative(-80f, -239f)
                quadToRelative(0f, -65f, 24f, -122f)
                reflectiveQuadToRelative(66f, -100.5f)
                reflectiveQuadToRelative(98.5f, -69.5f)
                reflectiveQuadTo(789f, 241f)
                quadToRelative(-56f, 35f, -98f, 86f)
                reflectiveQuadToRelative(-65f, 114f)
                quadToRelative(-44f, 21f, -80.5f, 51.5f)
                reflectiveQuadTo(480f, 561f)
                moveToRelative(-73f, -75f)
                quadToRelative(-12f, -9f, -24f, -17f)
                reflectiveQuadToRelative(-25f, -16f)
                quadToRelative(0f, -6f, 1f, -12.5f)
                reflectiveQuadToRelative(1f, -12.5f)
                quadToRelative(0f, -76f, -24f, -144f)
                reflectiveQuadToRelative(-68f, -124f)
                quadToRelative(66f, 27f, 114.5f, 77.5f)
                reflectiveQuadTo(457f, 354f)
                quadToRelative(-18f, 30f, -31f, 63.5f)
                reflectiveQuadTo(407f, 486f)
            }
        }.build()
        return _undefined!!
    }

private var _undefined: ImageVector? = null
