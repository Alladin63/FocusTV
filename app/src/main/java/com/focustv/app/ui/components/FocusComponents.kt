package com.focustv.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.DeepBg
import com.focustv.app.ui.ElectricBlue
import com.focustv.app.ui.NeonViolet
import com.focustv.app.ui.PanelBg
import com.focustv.app.ui.TextSoft

@Composable
fun NeonBackground(backdrop: String = "", content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.fillMaxSize().background(DeepBg)) {
        if (backdrop.isNotBlank()) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().blur(18.dp).scale(1.08f),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.horizontalGradient(
                    listOf(DeepBg.copy(alpha = 0.98f), DeepBg.copy(alpha = 0.78f), DeepBg.copy(alpha = 0.96f))
                )
            )
        )
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(NeonViolet.copy(alpha = 0.18f), Color.Transparent),
                    radius = 1200f
                )
            )
        )
        content()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TvButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.07f else 1f, label = "btnScale")
    val bg by animateColorAsState(if (focused) ElectricBlue else PanelBg, label = "btnBg")
    Button(
        onClick = onClick,
        modifier = modifier
            .height(62.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable(),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = if (focused) Color.Black else Color.White),
        border = BorderStroke(if (focused) 2.dp else 1.dp, if (focused) Color.White else NeonViolet.copy(alpha = 0.45f))
    ) {
        Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.basicMarquee())
    }
}

@Composable
fun PosterCard(item: MediaContent, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.09f else 1f, label = "posterScale")
    val border = if (focused) ElectricBlue else Color.Transparent

    Card(
        onClick = onClick,
        modifier = modifier
            .width(156.dp)
            .height(238.dp)
            .padding(6.dp)
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .border(2.dp, border, RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10162E)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(if (focused) 14.dp else 4.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.poster.ifBlank { item.logo },
                contentDescription = item.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))))
                    .padding(10.dp)
            ) {
                Text(item.title, color = Color.White, fontSize = 14.sp, maxLines = 2, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun ContentRow(title: String, itemsList: List<MediaContent>, onItem: (MediaContent) -> Unit) {
    if (itemsList.isEmpty()) return
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 36.dp, bottom = 4.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 30.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(itemsList, key = { it.id }) { item ->
                PosterCard(item = item) { onItem(item) }
            }
        }
    }
}

@Composable
fun EmptyPanel(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = TextSoft, fontSize = 24.sp)
    }
}

@Composable
fun Header(title: String, subtitle: String = "") {
    Column(Modifier.padding(start = 36.dp, top = 26.dp, bottom = 12.dp)) {
        Text(title, color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Black)
        if (subtitle.isNotBlank()) Text(subtitle, color = TextSoft, fontSize = 16.sp)
    }
}
