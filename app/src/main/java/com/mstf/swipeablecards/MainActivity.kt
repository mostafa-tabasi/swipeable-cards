package com.mstf.swipeablecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SwipeableDeck(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SwipeableDeck(modifier: Modifier = Modifier) {
    val visibleCards = 4
    val topPeek = 18.dp
    val scaleStep = 0.04f
    val alphaStep = 0.12f
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
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // IMPORTANT: draw from bottom → top
            cards
                .take(visibleCards)
                .reversed()
                .forEachIndexed { reversedIndex, color ->

                    val index = visibleCards - 1 - reversedIndex
                    val isTop = index == 0

                    val scale by animateFloatAsState(
                        targetValue = 1f - index * scaleStep,
                        label = "scale"
                    )

                    val alpha by animateFloatAsState(
                        targetValue = 1f - index * alphaStep,
                        label = "alpha"
                    )

                    val yOffset by animateDpAsState(
                        targetValue = -topPeek * index,
                        label = "yOffset"
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(210.dp)
                            .offset {
                                IntOffset(
                                    x = if (isTop) offsetX.value.roundToInt() else 0,
                                    y = if (isTop)
                                        offsetY.value.roundToInt()
                                    else
                                        with(density) { yOffset.roundToPx() }
                                )
                            }
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                                if (isTop) {
                                    rotationZ = offsetX.value / 20f
                                }
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
                            defaultElevation = (visibleCards - index).times(5).dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = color)
                    ) {}
                }
        }
    }
}