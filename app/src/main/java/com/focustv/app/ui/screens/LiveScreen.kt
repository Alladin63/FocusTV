package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.TextSoft
import com.focustv.app.ui.components.Header
import com.focustv.app.ui.components.NeonBackground
import com.focustv.app.ui.components.TvButton

@Composable
fun LiveScreen(
    items: List<MediaContent>,
    onBack: () -> Unit,
    onPlay: (MediaContent) -> Unit,
    onDetails: (MediaContent) -> Unit
) {
    val categories = remember(items) { listOf("Tous") + items.map { it.category }.distinct().filter { it.isNotBlank() } }
    var category by remember { mutableStateOf("Tous") }
    val filtered = remember(items, category) { if (category == "Tous") items else items.filter { it.category == category } }

    NeonBackground {
        Row(Modifier.fillMaxSize().padding(28.dp)) {
            LazyColumn(Modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item { TvButton("Retour", Modifier.fillMaxWidth()) { onBack() } }
                items(categories.take(80)) { cat ->
                    TvButton(cat, Modifier.fillMaxWidth()) { category = cat }
                }
            }
            Spacer(Modifier.width(26.dp))
            Column(Modifier.weight(1f)) {
                Header("Live TV", "${filtered.size} chaînes • $category")
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { channel ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TvButton(channel.title, Modifier.weight(1f)) { onPlay(channel) }
                            TvButton("Infos", Modifier.width(140.dp)) { onDetails(channel) }
                        }
                    }
                }
            }
            Spacer(Modifier.width(22.dp))
            Column(Modifier.width(330.dp).padding(top = 80.dp)) {
                Text("EPG", color = Color.White)
                Text("L’EPG XMLTV est chargé par le service EPG. Les programmes peuvent être associés par tvg-id / nom de chaîne.", color = TextSoft)
            }
        }
    }
}
