package com.routewake.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.routewake.app.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteWakeTopBar(
    title: String,
    statusText: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                if (!statusText.isNullOrBlank()) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = RouteWakeAccentLime
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun RouteWakeCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val cardModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
    } else {
        modifier
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .background(backgroundColor, shape)
            .padding(16.dp)
    }

    Column(modifier = cardModifier) {
        content()
    }
}

@Composable
fun RouteWakeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    isDanger: Boolean = false,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    val buttonColors = when {
        isDanger -> ButtonDefaults.buttonColors(
            containerColor = RouteWakeErrorRed,
            contentColor = Color.White
        )
        isPrimary -> ButtonDefaults.buttonColors(
            containerColor = RouteWakeAccentLime,
            contentColor = Color(0xFF022C22)
        )
        else -> ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun RouteWakeStatusBadge(
    status: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val badgeColor = when (status.uppercase()) {
        "ACTIVE", "MONITORING" -> RouteWakeAccentLime
        "ARRIVED" -> RouteWakeAccentCyan
        "PAUSED" -> RouteWakeWarningAmber
        "ERROR" -> RouteWakeErrorRed
        else -> RouteWakeTextMutedDark
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = badgeColor.copy(alpha = 0.15f),
        border = null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor
            )
        }
    }
}
