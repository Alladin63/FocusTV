package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.TextSoft
import com.focustv.app.ui.components.NeonBackground
import com.focustv.app.ui.components.TvButton

@Composable
fun DetailsScreen(
    item: MediaContent?,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onPlay: () -> Unit,
    onFavorite: () -> Unit
) {
    if (item == null) {
        NeonBackground { TvButton("Retour", Modifier.padding(40.dp).width(160.dp)) { onBack() } }
        return
    }

    NeonBackground(item.backdrop.ifBlank { item.poster }) {
        Row(Modifier.fillMaxSize().padding(50.dp)) {
            AsyncImage(
                model = item.poster.ifBlank { item.logo },
                contentDescription = item.title,
                modifier = Modifier.width(250.dp).height(380.dp)
            )
            Spacer(Modifier.width(38.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(10.dp))
                Text(listOf(item.year, item.genre, item.duration, item.rating).filter { it.isNotBlank() }.joinToString(" • "), color = TextSoft, fontSize = 18.sp)
                Spacer(Modifier.height(22.dp))
                Text(item.description.ifBlank { "Aucun synopsis disponible pour le moment." }, color = Color.White, fontSize = 20.sp, lineHeight = 28.sp, maxLines = 8)
                if (item.cast.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Casting : ${item.cast}", color = TextSoft, fontSize = 18.sp)
                }
                Spacer(Modifier.height(32.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                    TvButton("Lecture", Modifier.width(180.dp)) { onPlay() }
                    TvButton(if (isFavorite) "Retirer favori" else "Ajouter favori", Modifier.width(240.dp)) { onFavorite() }
                    TvButton("Retour", Modifier.width(160.dp)) { onBack() }
                }
            }
        }
    }
}
