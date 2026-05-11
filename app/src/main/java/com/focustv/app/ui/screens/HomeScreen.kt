package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focustv.app.core.FocusTvState
import com.focustv.app.core.MediaContent
import com.focustv.app.ui.components.*

@Composable
fun HomeScreen(
    state: FocusTvState,
    onOpen: (String) -> Unit,
    onItem: (MediaContent) -> Unit
) {
    val backdrop = (state.movies + state.series).firstOrNull { it.backdrop.isNotBlank() }?.backdrop.orEmpty()
    NeonBackground(backdrop) {
        Row(Modifier.fillMaxSize()) {
            SideMenu(onOpen)
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                item {
                    Header(
                        "FocusTV",
                        if (state.sources.isEmpty()) "Ajoute une source pour charger Live, VOD et Séries" else state.message
                    )
                }
                item {
                    Row(Modifier.padding(horizontal = 36.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                        TvButton("Live TV", Modifier.width(180.dp)) { onOpen("live") }
                        TvButton("Films", Modifier.width(180.dp)) { onOpen("movies") }
                        TvButton("Séries", Modifier.width(180.dp)) { onOpen("series") }
                        TvButton("Sources", Modifier.width(190.dp)) { onOpen("sources") }
                    }
                }
                item { ContentRow("Continuer / Derniers vus", state.history, onItem) }
                item { ContentRow("Films populaires", state.movies.take(30), onItem) }
                item { ContentRow("Séries", state.series.take(30), onItem) }
                item { ContentRow("Chaînes récentes", state.live.take(30), onItem) }
                item { Spacer(Modifier.height(60.dp)) }
            }
        }
    }
}

@Composable
private fun SideMenu(onOpen: (String) -> Unit) {
    Column(
        Modifier.width(226.dp).fillMaxHeight().padding(start = 18.dp, top = 34.dp, bottom = 34.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TvButton("Accueil", Modifier.fillMaxWidth()) { onOpen("home") }
        TvButton("Live", Modifier.fillMaxWidth()) { onOpen("live") }
        TvButton("Films", Modifier.fillMaxWidth()) { onOpen("movies") }
        TvButton("Séries", Modifier.fillMaxWidth()) { onOpen("series") }
        TvButton("Favoris", Modifier.fillMaxWidth()) { onOpen("favorites") }
        TvButton("Profils", Modifier.fillMaxWidth()) { onOpen("profiles") }
        TvButton("Sources", Modifier.fillMaxWidth()) { onOpen("sources") }
        TvButton("Paramètres", Modifier.fillMaxWidth()) { onOpen("settings") }
    }
}
