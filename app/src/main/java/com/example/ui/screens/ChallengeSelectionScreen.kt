package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.game.EconomyEngine
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.first
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sin

// ---- Gravity Trail palette (original art: crystal spires, aurora hills, glossy block nodes) ----
private val SkyTop = Color(0xFFFFE9F4)
private val SkyMid = Color(0xFFEFDDFF)
private val SkyBottom = Color(0xFFD7C2FA)
private val HillBack = Color(0xFFC9A9F7)
private val HillFront = Color(0xFFB48CEE)
private val TrailEdge = Color(0xFFFFF4EC)
private val TrailFill = Color(0xFFFFD5C2)
private val TrailStripe = Color(0xFFFF5FA2)

private val NodeSpacing = 108.dp
private val MapTopPad = 90.dp
private val MapBottomPad = 70.dp
private val NodeSize = 62.dp

@Composable
fun ChallengeSelectionScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayLevel: (Int) -> Unit
) {
    var selectedLevelNumber by remember(state.unlockedChallengeLevel) {
        mutableIntStateOf(state.unlockedChallengeLevel.coerceIn(1, ChallengeLevels.levels.size))
    }
    val selectedLevel = ChallengeLevels.getLevel(selectedLevelNumber)
    val selectedProgress = state.challengeProgress[selectedLevelNumber]
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3

    CascadeScreenContainer(
        maxContentWidth = 560.dp,
        backdrop = { Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(SkyTop, SkyBottom)))) },
        modifier = Modifier.testTag("challenge_selection_screen")
    ) {
        ScreenTopBar(
            title = "Challenges",
            onBack = onBack,
            backTestTag = "challenge_selection_back_button"
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCardElevated)
                    .border(1.dp, AccentGold.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "★ $totalStars/$maxStars",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp
                    ),
                    color = AccentGold
                )
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            GravityTrailMap(
                unlockedLevel = state.unlockedChallengeLevel,
                selectedLevelNumber = selectedLevelNumber,
                progress = state.challengeProgress,
                onSelect = { selectedLevelNumber = it },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                ChallengeInspectorCard(
                    level = selectedLevel,
                    progress = selectedProgress,
                    onPlay = { onPlayLevel(selectedLevel.levelNumber) }
                )
            }
        }
    }
}

private fun nodeXFraction(index: Int): Float =
    (0.5f + 0.27f * sin(index * 1.3f + 0.7f)).coerceIn(0.2f, 0.8f)

@Composable
private fun GravityTrailMap(
    unlockedLevel: Int,
    selectedLevelNumber: Int,
    progress: Map<Int, ChallengeLevelProgress>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val levels = ChallengeLevels.levels
    val n = levels.size
    val scroll = rememberScrollState()
    val mapHeight: Dp = MapTopPad + NodeSpacing * (n - 1) + MapBottomPad
    val density = LocalDensity.current

    val pulse by rememberInfiniteTransition(label = "trailPulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    // Start with the current level in view (level 1 sits at the bottom of the map).
    LaunchedEffect(unlockedLevel) {
        val max = snapshotFlow { scroll.maxValue }.first { it > 0 }
        val index = (unlockedLevel - 1).coerceIn(0, n - 1)
        val nodeYpx = with(density) { (mapHeight - MapBottomPad - NodeSpacing * index).toPx() }
        val keepAbove = with(density) { 260.dp.toPx() }
        scroll.scrollTo((nodeYpx - keepAbove).roundToInt().coerceIn(0, max))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .border(2.dp, Color.White.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
            .verticalScroll(scroll)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .height(mapHeight)
        ) {
            val widthDp = maxWidth
            val pointsDp = List(n) { i ->
                Offset(
                    x = widthDp.value * nodeXFraction(i),
                    y = (mapHeight - MapBottomPad - NodeSpacing * i).value
                )
            }

            Canvas(Modifier.fillMaxSize()) {
                drawTrailScenery(pointsDp, unlockedLevel, density.density)
            }

            // Finish trophy at the top of the trail
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .offset(x = widthDp * nodeXFraction(n - 1) - 24.dp, y = 12.dp)
                    .size(48.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFFFFE57F), Color(0xFFFFA000))))
                    .border(3.dp, Color.White, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = "Trail finish",
                    tint = Color(0xFF5B2A86),
                    modifier = Modifier.size(26.dp)
                )
            }

            levels.forEachIndexed { i, level ->
                val p = pointsDp[i]
                val unlocked = level.levelNumber <= unlockedLevel
                TrailNode(
                    level = level,
                    unlocked = unlocked,
                    isCurrent = level.levelNumber == unlockedLevel,
                    isSelected = level.levelNumber == selectedLevelNumber,
                    progress = progress[level.levelNumber],
                    pulse = pulse,
                    onClick = { if (unlocked) onSelect(level.levelNumber) },
                    modifier = Modifier.offset(
                        x = p.x.dp - NodeSize / 2 - 6.dp,
                        y = p.y.dp - NodeSize / 2 - 6.dp
                    )
                )
            }
        }
    }
}

private fun DrawScope.drawTrailScenery(pointsDp: List<Offset>, unlockedLevel: Int, d: Float) {
    val w = size.width
    val h = size.height

    // Sky
    drawRect(Brush.verticalGradient(listOf(SkyTop, SkyMid, SkyBottom)))

    // Soft floating bubbles
    for (i in 0 until 14) {
        val bx = ((i * 83) % 100) / 100f * w
        val by = ((i * 47 + 13) % 100) / 100f * h
        drawCircle(Color.White.copy(alpha = 0.35f), radius = (6 + (i % 4) * 4) * d, center = Offset(bx, by))
    }

    // Rolling aurora hills
    fun hill(baseY: Float, amp: Float, phase: Float, color: Color) {
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, baseY)
            var x = 0f
            while (x <= w + 10f) {
                lineTo(x, baseY + amp * sin(x / w * 6.28f + phase))
                x += 12f
            }
            lineTo(w, h)
            close()
        }
        drawPath(path, color)
    }
    hill(h * 0.30f, 22f * d, 0.5f, HillBack.copy(alpha = 0.55f))
    hill(h * 0.52f, 30f * d, 2.2f, HillBack)
    hill(h * 0.74f, 34f * d, 4.0f, HillFront)

    // Crystal spires on both edges (unique scenery: faceted prisms)
    val spireColors = listOf(
        Color(0xFFFF6FB5) to Color(0xFFFF9ACD),
        Color(0xFF7C5CFF) to Color(0xFFA48CFF),
        Color(0xFF29C7E6) to Color(0xFF7FE3F5)
    )
    for (i in 0 until 9) {
        val left = i % 2 == 0
        val sx = if (left) w * (0.05f + (i % 3) * 0.05f) else w * (0.95f - (i % 3) * 0.05f)
        val sy = h * (0.06f + i * 0.105f)
        val sh = (60 + (i % 3) * 22) * d
        val sw = (22 + (i % 2) * 8) * d
        val (dark, light) = spireColors[i % spireColors.size]
        val tip = Offset(sx, sy - sh)
        val bl = Offset(sx - sw, sy)
        val br = Offset(sx + sw, sy)
        val mid = Offset(sx, sy + sw * 0.35f)
        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(bl.x, bl.y); lineTo(mid.x, mid.y); close() }, dark)
        drawPath(Path().apply { moveTo(tip.x, tip.y); lineTo(br.x, br.y); lineTo(mid.x, mid.y); close() }, light)
        drawLine(Color.White.copy(alpha = 0.7f), tip, mid, strokeWidth = 1.5f * d)
    }

    // Trail path (through node centres, smooth S-curves)
    val pts = pointsDp.map { Offset(it.x * d, it.y * d) }
    fun buildPath(from: Int, to: Int) = Path().apply {
        moveTo(pts[from].x, pts[from].y)
        for (i in from until to) {
            val a = pts[i]
            val b = pts[i + 1]
            val midY = (a.y + b.y) / 2f
            cubicTo(a.x, midY, b.x, midY, b.x, b.y)
        }
    }
    val full = buildPath(0, pts.lastIndex)
    drawPath(full, TrailEdge, style = Stroke(width = 58f * d, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(full, TrailFill, style = Stroke(width = 46f * d, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Travelled part: candy-striped ribbon
    val reached = (unlockedLevel - 1).coerceIn(0, pts.lastIndex)
    if (reached > 0) {
        val done = buildPath(0, reached)
        drawPath(done, Color.White, style = Stroke(width = 14f * d, cap = StrokeCap.Round))
        drawPath(
            done, TrailStripe,
            style = Stroke(
                width = 14f * d, cap = StrokeCap.Butt,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f * d, 16f * d))
            )
        )
    }
    // Upcoming part: dotted guide
    drawPath(
        full, Color.White.copy(alpha = 0.7f),
        style = Stroke(
            width = 5f * d, cap = StrokeCap.Round,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f * d, 14f * d))
        )
    )
}

@Composable
private fun TrailNode(
    level: ChallengeLevel,
    unlocked: Boolean,
    isCurrent: Boolean,
    isSelected: Boolean,
    progress: ChallengeLevelProgress?,
    pulse: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed = progress?.completed == true
    val stars = (progress?.stars ?: 0).coerceIn(0, 3)
    val fill = when {
        !unlocked -> Brush.verticalGradient(listOf(Color(0xFFD9D0EA), Color(0xFFB4A8CE)))
        completed -> Brush.verticalGradient(listOf(Color(0xFF5DE0B0), Color(0xFF12A57A)))
        else -> Brush.verticalGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF8F00)))
    }
    val scale = when {
        isCurrent && !completed -> pulse
        isSelected -> 1.1f
        else -> 1f
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(6.dp)
            .testTag("challenge_level_card_${level.levelNumber}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(NodeSize)
                .scale(scale)
                .shadow(if (unlocked) 6.dp else 0.dp, CircleShape)
                .clip(CircleShape)
                .background(fill)
                .border(
                    if (isSelected) 4.dp else 3.dp,
                    if (isSelected) Color(0xFF5B2A86) else Color.White,
                    CircleShape
                )
                .clickable(enabled = unlocked, onClick = onClick)
        ) {
            // gloss
            Box(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 4.dp)
                    .size(width = NodeSize * 0.55f, height = NodeSize * 0.22f)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.4f))
            )
            if (!unlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked level ${level.levelNumber}",
                    tint = Color(0xFF6F6291),
                    modifier = Modifier.size(26.dp)
                )
            } else {
                Text(
                    text = "${level.levelNumber}",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    ),
                    color = Color.White
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
            for (idx in 1..3) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (idx <= stars) Color(0xFFFFC107) else Color.White.copy(alpha = if (unlocked) 0.85f else 0.45f),
                    modifier = Modifier
                        .size(if (idx == 2) 17.dp else 13.dp)
                        .offset(y = if (idx == 2) (-2).dp else 0.dp)
                )
            }
        }
    }
}

@Composable
private fun ChallengeInspectorCard(
    level: ChallengeLevel,
    progress: ChallengeLevelProgress?,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewBoard = remember(level.levelNumber) { level.buildBoard() }
    val twoStarTarget = ceil(level.targetScore * 1.15).toInt()
    val threeStarTarget = ceil(level.targetScore * 1.4).toInt()
    val (rewardGold, rewardDiamonds) = EconomyEngine.calculateChallengeWinReward(3)

    PremiumCard(
        cornerRadius = 22.dp,
        borderColor = AccentGold.copy(alpha = 0.45f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("challenge_target_summary_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoardPreview(
                    board = previewBoard,
                    modifier = Modifier
                        .widthIn(max = 78.dp)
                        .fillMaxWidth(0.24f)
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "LEVEL ${level.levelNumber}",
                        style = CascadeTextStyles.SectionKicker,
                        color = AccentGold
                    )
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = level.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InspectorStatChip("Target", "${level.targetScore}", Modifier.weight(1f))
                InspectorStatChip("Moves", "${level.moveLimit}", Modifier.weight(1f))
                InspectorStatChip("Best", "${progress?.bestScore ?: 0}", Modifier.weight(1f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("★ ${level.targetScore}", style = CascadeTextStyles.NumericBalance.copy(fontSize = 11.sp), color = TextSecondary)
                Text("★★ $twoStarTarget / 1 move left", style = CascadeTextStyles.NumericBalance.copy(fontSize = 10.sp), color = Color(0xFF1D7AFC), maxLines = 1)
                Text("★★★ $threeStarTarget / 3 left", style = CascadeTextStyles.NumericBalance.copy(fontSize = 10.sp), color = AccentGold, maxLines = 1)
            }
            Text(
                text = "More stars = finish faster or score higher. 3★ reward: $rewardGold Gold · $rewardDiamonds Gems",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            PremiumPrimaryButton(
                text = "Play Level ${level.levelNumber}",
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlay,
                height = 50.dp,
                cornerRadius = 15.dp,
                fontSize = 16.sp,
                testTag = "challenge_play_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InspectorStatChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BoardWellBg)
            .border(1.dp, GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 7.dp, horizontal = 6.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            color = TextPrimary
        )
    }
}
