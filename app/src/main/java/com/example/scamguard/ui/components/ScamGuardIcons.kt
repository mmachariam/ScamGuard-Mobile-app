package com.example.scamguard.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Shared outline icons drawn on a 24 × 24 viewport.
 * Material Icon supplies their size, tint and accessibility description.
 */
object ScamGuardIcons {

    val Home = outlineIcon("Home") {
        moveTo(3f, 10f)
        lineTo(12f, 3f)
        lineTo(21f, 10f)

        moveTo(5f, 9f)
        lineTo(5f, 21f)
        lineTo(10f, 21f)
        lineTo(10f, 15f)
        lineTo(14f, 15f)
        lineTo(14f, 21f)
        lineTo(19f, 21f)
        lineTo(19f, 9f)
    }

    val Check = outlineIcon("Check") {
        moveTo(5f, 4f)
        lineTo(19f, 4f)
        quadTo(21f, 4f, 21f, 6f)
        lineTo(21f, 16f)
        quadTo(21f, 18f, 19f, 18f)
        lineTo(9f, 18f)
        lineTo(4f, 21f)
        lineTo(4f, 18f)
        quadTo(3f, 18f, 3f, 16f)
        lineTo(3f, 6f)
        quadTo(3f, 4f, 5f, 4f)
        close()

        moveTo(7f, 9f)
        lineTo(17f, 9f)

        moveTo(7f, 13f)
        lineTo(14f, 13f)
    }

    val History = outlineIcon("History") {
        moveTo(4f, 8f)
        curveTo(6f, 3f, 13f, 1f, 18f, 5f)
        curveTo(23f, 9f, 22f, 17f, 17f, 20f)
        curveTo(12f, 23f, 6f, 21f, 4f, 17f)

        moveTo(4f, 3f)
        lineTo(4f, 8f)
        lineTo(9f, 8f)

        moveTo(12f, 7f)
        lineTo(12f, 12f)
        lineTo(16f, 14f)
    }

    val Settings = outlineIcon("Settings") {
        // Three adjustment sliders keep this icon readable at small sizes.
        moveTo(4f, 6f)
        lineTo(8f, 6f)
        moveTo(12f, 6f)
        lineTo(20f, 6f)

        moveTo(4f, 12f)
        lineTo(14f, 12f)
        moveTo(18f, 12f)
        lineTo(20f, 12f)

        moveTo(4f, 18f)
        lineTo(6f, 18f)
        moveTo(10f, 18f)
        lineTo(20f, 18f)

        moveTo(8f, 4f)
        lineTo(12f, 4f)
        lineTo(12f, 8f)
        lineTo(8f, 8f)
        close()

        moveTo(14f, 10f)
        lineTo(18f, 10f)
        lineTo(18f, 14f)
        lineTo(14f, 14f)
        close()

        moveTo(6f, 16f)
        lineTo(10f, 16f)
        lineTo(10f, 20f)
        lineTo(6f, 20f)
        close()
    }
}

/** All icons share the same stroke weight and rounded line endings. */
private fun outlineIcon(
    name: String,
    drawing: PathBuilder.() -> Unit
): ImageVector {
    return ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            drawing()
        }
    }.build()
}