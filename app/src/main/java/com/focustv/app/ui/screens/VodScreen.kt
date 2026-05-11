package com.focustv.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.components.*

@Composable
fun VodScreen(
    title: String,
    items: List<MediaContent>,
    onBack: () -> Unit,
    onItem: (MediaContent) -> Unit
) {
    var heroIndex by remember { mutableStateOf(0) }
    LaunchedEffect(items.size) {
        while (items.isNotEmpty()) {
            kotlinx.coroutines.delay(5200)
            heroIndex = (heroIndex + 1) % items.size.coerceAtLeast(1)
        }
    }

    val infinite = rememberInfiniteTransition(label = "vodParallax")
    val zoom by infinite.animateFloat(
        initialValue = 1.04f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(9000), RepeatMode.Reverse),
        label = "zoom"
    )

    val hero = items.getOrNull(heroIndex)
    NeonBackground(hero?.backdrop?.ifBlank { hero.poster }.orEmpty()) {
        Box(Modifier.fillMaxSize().scale(zoom)) {}
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 60.dp)) {
            item {
                Row(Modifier.padding(horizontal = 36.dp, vertical = 24.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    TvButton("Retour", Modifier.width(150.dp)) { onBack() }
                    Header(title, "${items.size} contenus")
                }
            }
            if (items.isEmpty()) {
                item { EmptyPanel("Aucun contenu dans cette section.") }
            } else {
                val groups = items.groupBy { it.category.ifBlank { "Tous" } }.entries.take(18)
                groups.forEach { group ->
                    item { ContentRow(group.key, group.value.take(40), onItem) }
                }
            }
        }
    }
}
