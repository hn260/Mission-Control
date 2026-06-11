package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.1f),
    onClick: (() -> Unit)? = null,
    testTag: String = "glass_card",
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = modifier
        .testTag(testTag)
        .shadow(
            elevation = 4.dp,
            shape = RoundedCornerShape(16.dp),
            clip = false,
            ambientColor = Color.White.copy(alpha = 0.02f),
            spotColor = Color.Black
        )
        .border(1.dp, borderColor, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(
            Brush.verticalGradient(
                colors = listOf(
                    GlassCardColor.copy(alpha = 0.8f),
                    GlassCardColor.copy(alpha = 0.4f)
                )
            )
        )

    if (onClick != null) {
        Column(
            modifier = cardModifier.clickable(onClick = onClick),
            content = content
        )
    } else {
        Column(
            modifier = cardModifier.padding(12.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "Online", "APPROVED", "Completed", "AUTO_APPROVE" -> StatusGreen.copy(alpha = 0.15f) to StatusGreen
        "Offline", "REJECTED", "Failed" -> StatusRed.copy(alpha = 0.15f) to StatusRed
        "Busy", "Planning", "Researching", "Building", "Testing", "Reviewing", "REQUIRE_APPROVAL" -> StatusAmber.copy(alpha = 0.15f) to StatusAmber
        "Waiting Approval" -> AccentPurple.copy(alpha = 0.15f) to AccentPurple
        else -> SpaceMuted.copy(alpha = 0.15f) to SpaceGlow
    }

    Box(
        modifier = modifier
            .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(50.dp))
            .background(bgColor, RoundedCornerShape(50.dp))
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = textColor,
            style = Typography.labelSmall.copy(letterSpacing = 1.sp)
        )
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isMonospace: Boolean = true,
    valueColor: Color = PrimaryCyan
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = Typography.bodyMedium,
            color = SpaceMuted
        )
        Text(
            text = value,
            style = if (isMonospace) Typography.labelMedium else Typography.titleMedium,
            color = valueColor
        )
    }
}

@Composable
fun NeonDivider(
    modifier: Modifier = Modifier,
    color: Color = PrimaryCyan.copy(alpha = 0.15f)
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color,
                        Color.Transparent
                    )
                )
            )
    )
}
