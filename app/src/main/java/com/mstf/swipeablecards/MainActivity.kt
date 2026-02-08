package com.mstf.swipeablecards

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.mstf.swipeablecards.ui.theme.SwipeableCardsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SwipeableCardsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DeckStackedCards(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DeckStackedCards(modifier: Modifier = Modifier) {
    val cardCount = 4
    val topPeek = 18.dp
    val scaleStep = 0.04f
    val alphaStep = 0.12f

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            for (index in cardCount - 1 downTo 0) {
                val scale = 1f - (index * scaleStep)
                val alpha = 1f - (index * alphaStep)

                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(450.dp)
                        .offset(y = -topPeek * index)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        },
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = (cardCount - index).times(3).dp
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Card ${index + 1}")
                    }
                }
            }
        }
    }
}