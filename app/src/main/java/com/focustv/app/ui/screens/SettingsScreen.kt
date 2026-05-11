package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focustv.app.core.FocusTvState
import com.focustv.app.ui.TextSoft
import com.focustv.app.ui.components.Header
import com.focustv.app.ui.components.NeonBackground
import com.focustv.app.ui.components.TvButton

@Composable
fun SettingsScreen(state: FocusTvState, onBack: () -> Unit) {
    NeonBackground {
        Column(Modifier.fillMaxSize().padding(36.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Header("Paramètres", "Optimisé Android TV / Firestick")
            Text("Thème : ${state.settings.theme}", color = Color.White)
            Text("Lecteur : ${state.settings.playerEngine}", color = TextSoft)
            Text("Fond animé VOD : ${if (state.settings.enableAnimatedVodBackground) "ON" else "OFF"}", color = TextSoft)
            Text("Cache images Coil + Media3 + DataStore actifs.", color = TextSoft)
            TvButton("Retour", Modifier.width(180.dp)) { onBack() }
        }
    }
}
