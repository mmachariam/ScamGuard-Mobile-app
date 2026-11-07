package com.example.scamguard.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * A scalable shield/check logo.
 * The surrounding screen supplies the ScamGuard name for accessibility.
 */
@Composable
fun ScamGuardLogo(modifier: Modifier = Modifier) {
    val blue = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Canvas(
            modifier = Modifier
                .padding(24.dp)
                .size(64.dp)
        ) {
            // Draw using proportions so the logo adapts to its available size.
            val shield = Path().apply {
                moveTo(size.width * 0.5f, size.height * 0.06f)
                lineTo(size.width * 0.88f, size.height * 0.22f)
                lineTo(size.width * 0.83f, size.height * 0.58f)

                cubicTo(
                    size.width * 0.80f, size.height * 0.76f,
                    size.width * 0.65f, size.height * 0.89f,
                    size.width * 0.5f, size.height * 0.95f
                )

                cubicTo(
                    size.width * 0.35f, size.height * 0.89f,
                    size.width * 0.20f, size.height * 0.76f,
                    size.width * 0.17f, size.height * 0.58f
                )

                lineTo(size.width * 0.12f, size.height * 0.22f)
                close()
            }

            drawPath(
                path = shield,
                color = blue,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // The two connected lines form the check inside the shield.
            val check = Path().apply {
                moveTo(size.width * 0.32f, size.height * 0.49f)
                lineTo(size.width * 0.46f, size.height * 0.63f)
                lineTo(size.width * 0.70f, size.height * 0.37f)
            }

            drawPath(
                path = check,
                color = blue,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/** Shared card styling keeps the screens visually consistent. */
@Composable
fun ScamGuardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

/** A minimum height preserves a comfortable touch target without fixing text height. */
@Composable
fun ScamGuardPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}