package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentDiamond
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun PremiumPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ArcadeButtonStyle = ArcadeButtonStyle.GOLD,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    cornerRadius: Dp = 12.dp,
    fontSize: TextUnit = 16.sp,
    testTag: String? = null
) {
    GlossyArcadeButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = style,
        icon = icon,
        enabled = enabled,
        height = height,
        cornerRadius = cornerRadius,
        fontSize = fontSize,
        testTag = testTag
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = AccentCyan,
    enabled: Boolean = true,
    height: Dp = 48.dp,
    testTag: String? = null
) {
    val shape = RoundedCornerShape(14.dp)
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        border = BorderStroke(1.dp, accent.copy(alpha = if (enabled) 0.55f else 0.25f)),
        modifier = modifier
            .defaultMinSize(minHeight = height)
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) accent else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = CascadeTextStyles.ButtonLabel.copy(fontSize = 14.sp),
            color = if (enabled) TextPrimary else TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun IconActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = TextPrimary,
    borderColor: Color = GlassBorder,
    size: Dp = 48.dp,
    testTag: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 500f),
        label = "icon_action_scale"
    )
    val shape = RoundedCornerShape(14.dp)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(4.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
            .border(1.2.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = AccentCyan),
                role = Role.Button,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun PremiumCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassBorder,
    content: @Composable ColumnScope.() -> Unit
) {
    FrostedGlassCard(
        modifier = modifier,
        cornerRadius = cornerRadius,
        borderColor = borderColor,
        content = content
    )
}

@Composable
fun GoldBalance(
    amount: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    testTag: String? = null
) {
    val animatedAmount by animateIntAsState(
        targetValue = amount,
        animationSpec = tween(durationMillis = 280),
        label = "gold_balance_anim"
    )
    CurrencyPill(
        badge = { CrystalCoinBadge(size = if (compact) 16.dp else 20.dp) },
        borderAccent = AccentGold.copy(alpha = 0.55f),
        amountText = formatCompactNumber(animatedAmount),
        compact = compact,
        modifier = modifier
            .semantics { contentDescription = "$amount Gold coins" }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
fun DiamondBalance(
    amount: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    testTag: String? = null
) {
    val animatedAmount by animateIntAsState(
        targetValue = amount,
        animationSpec = tween(durationMillis = 280),
        label = "diamond_balance_anim"
    )
    CurrencyPill(
        badge = { CrystalDiamondBadge(size = if (compact) 16.dp else 20.dp) },
        borderAccent = AccentDiamond.copy(alpha = 0.55f),
        amountText = formatCompactNumber(animatedAmount),
        compact = compact,
        modifier = modifier
            .semantics { contentDescription = "$amount Diamonds" }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    )
}

@Composable
private fun CurrencyPill(
    badge: @Composable () -> Unit,
    borderAccent: Color,
    amountText: String,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 5.dp else 7.dp),
        modifier = modifier
            .defaultMinSize(minHeight = if (compact) 32.dp else 36.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
            .border(1.2.dp, borderAccent, shape)
            .padding(
                horizontal = if (compact) 9.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            )
    ) {
        badge()
        Text(
            text = amountText,
            style = CascadeTextStyles.NumericBalance.copy(
                fontSize = if (compact) 13.sp else 15.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Aligned Gold + Diamond wallet. Each pill sizes to its own content and neither value can
 * overlap the other, even at 320dp. Pass [modifier] (e.g. `Modifier.weight(1f)`) to distribute
 * space in a parent row.
 */
@Composable
fun CurrencyWallet(
    goldCoins: Int,
    diamonds: Int,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    onClick: (() -> Unit)? = null,
    goldTag: String? = null,
    diamondTag: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        GoldBalance(
            amount = goldCoins,
            compact = compact,
            testTag = goldTag,
            modifier = Modifier.weight(1f, fill = false)
        )
        DiamondBalance(
            amount = diamonds,
            compact = compact,
            testTag = diamondTag,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    accent: Color = AccentCyan,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (kicker != null) {
                Text(
                    text = kicker.uppercase(),
                    style = CascadeTextStyles.SectionKicker,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        CircularProgressIndicator(
            color = AccentCyan,
            trackColor = DividerSubtle,
            strokeWidth = 3.dp,
            modifier = Modifier.size(36.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    accent: Color = AccentGold,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    PremiumCard(
        cornerRadius = 22.dp,
        borderColor = accent.copy(alpha = 0.35f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.38f), CircleShape)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                SecondaryButton(
                    text = actionLabel,
                    onClick = onAction,
                    accent = accent
                )
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    icon: ImageVector,
    accent: Color,
    title: String,
    message: String,
    confirmLabel: String,
    confirmStyle: ArcadeButtonStyle = ArcadeButtonStyle.GOLD,
    dialogTag: String,
    confirmTag: String,
    cancelTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PremiumCard(
            cornerRadius = 18.dp,
            borderColor = GlassBorder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag(dialogTag)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PremiumPrimaryButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = ArcadeButtonStyle.SLATE,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 15.sp,
                        testTag = cancelTag,
                        modifier = Modifier.weight(1f)
                    )
                    PremiumPrimaryButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        style = confirmStyle,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 15.sp,
                        testTag = confirmTag,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun PremiumToastBanner(
    visible: Boolean,
    message: String,
    accent: Color = AccentCyan,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + slideInVertically { -it / 2 },
        exit = fadeOut(tween(180)) + slideOutVertically { -it / 2 },
        modifier = modifier
    ) {
        Surface(
            color = SurfaceCardElevated,
            shape = CircleShape,
            border = BorderStroke(1.dp, accent.copy(alpha = 0.65f)),
            shadowElevation = 8.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumModalSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = SurfaceCard,
        contentColor = TextPrimary,
        shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        content = content
    )
}

fun formatCompactNumber(value: Int): String = when {
    value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000f)
    value >= 10_000 -> String.format(Locale.US, "%.1fK", value / 1_000f)
    else -> value.toString()
}
