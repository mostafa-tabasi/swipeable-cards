package com.mstf.swipeablecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mstf.swipeablecards.ui.theme.SwipeableCardsTheme
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwipeableCardsTheme {
                // State for sliders
                var topPeek by remember { mutableFloatStateOf(18f) }        // in dp
                var scaleStep by remember { mutableFloatStateOf(0.06f) }
                var alphaStep by remember { mutableFloatStateOf(0.15f) }
                var maxVisible by remember { mutableFloatStateOf(3f) }      // will convert to Int

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        SwipeableDeck(
                            modifier = Modifier.padding(innerPadding),
                            topPeek = topPeek.dp,
                            scaleStep = scaleStep,
                            alphaStep = alphaStep,
                            maxVisible = maxVisible.toInt(),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sliders
                        Text("Top Peek: ${topPeek.toInt()} dp")
                        Slider(
                            value = topPeek,
                            onValueChange = { topPeek = it },
                            valueRange = 0f..50f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scale Step: ${"%.2f".format(scaleStep)}")
                        Slider(
                            value = scaleStep,
                            onValueChange = { scaleStep = it },
                            valueRange = 0.01f..0.2f
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Max Visible: ${maxVisible.toInt()}")
                        Slider(
                            value = maxVisible,
                            onValueChange = { maxVisible = it },
                            valueRange = 1f..6f,
                            steps = 5
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Alpha Step: ${"%.2f".format(alphaStep)}")
                        Slider(
                            value = alphaStep,
                            onValueChange = { alphaStep = it },
                            valueRange = 0.01f..0.5f
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SwipeableDeck(
    modifier: Modifier = Modifier,
    topPeek: Dp,
    scaleStep: Float,
    alphaStep: Float,
    maxVisible: Int
) {
    val dismissThreshold = 120.dp
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val cards = remember {
        mutableStateListOf<Color>().apply {
            repeat(6) {
                add(
                    Color.hsv(
                        hue = Random.nextFloat() * 360f,
                        saturation = 0.6f,
                        value = 0.9f
                    )
                )
            }
        }
    }

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(top = 150.dp),
        contentAlignment = Alignment.TopCenter // Move deck to top
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp) // Optional padding from top
                .height(300.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            val visible = cards.take(maxVisible)

            visible.reversed().forEachIndexed { reversedIndex, cardColor ->
                val cardIndex = cards.indexOf(cardColor)
                val isTop = cardIndex == 0

                val animatedScale by animateFloatAsState(
                    targetValue = 1f - cardIndex * scaleStep,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
                val animatedAlpha by animateFloatAsState(
                    targetValue = 1f - cardIndex * alphaStep,
                    animationSpec = tween(250)
                )
                val animatedYOffset by animateDpAsState(
                    targetValue = -topPeek * cardIndex,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(450.dp)
                        .offset {
                            IntOffset(
                                x = if (isTop) offsetX.value.roundToInt() else 0,
                                y = if (isTop) offsetY.value.roundToInt()
                                else with(density) { animatedYOffset.roundToPx() }
                            )
                        }
                        .graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                            alpha = animatedAlpha
                            if (isTop) rotationZ = offsetX.value / 20f
                        }
                        .then(
                            if (isTop)
                                Modifier.pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            coroutineScope.launch {
                                                offsetX.snapTo(offsetX.value + dragAmount.x)
                                                offsetY.snapTo(offsetY.value + dragAmount.y)
                                            }
                                        },
                                        onDragEnd = {
                                            val thresholdPx =
                                                with(density) { dismissThreshold.toPx() }
                                            if (abs(offsetX.value) > thresholdPx) {
                                                val targetX =
                                                    if (offsetX.value > 0) 1500f else -1500f
                                                coroutineScope.launch {
                                                    offsetX.animateTo(targetX, tween(300))
                                                }.invokeOnCompletion {
                                                    cards.removeFirst()
                                                    coroutineScope.launch {
                                                        offsetX.snapTo(0f)
                                                        offsetY.snapTo(0f)
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    offsetX.animateTo(0f, spring())
                                                    offsetY.animateTo(0f, spring())
                                                }
                                            }
                                        }
                                    )
                                }
                            else Modifier
                        ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = (maxVisible - cardIndex).times(5).dp
                    ),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {}
            }
        }
    }
}