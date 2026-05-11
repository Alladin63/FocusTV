package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focustv.app.core.FocusTvState
import com.focustv.app.ui.components.Header
import com.focustv.app.ui.components.NeonBackground
import com.focustv.app.ui.components.TvButton

@Composable
fun ProfilesScreen(state: FocusTvState, onBack: () -> Unit) {
    NeonBackground {
        Column(Modifier.fillMaxSize().padding(36.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Header("Profils", "Multi-profils + verrouillage parental")
            state.profiles.forEach { profile ->
                TvButton(profile.name + if (profile.locked) " 🔒" else "", Modifier.width(320.dp)) { }
            }
            Text("La structure de profils est persistante. Ajoute ici les écrans PIN/édition si besoin.", color = Color.White)
            TvButton("Retour", Modifier.width(180.dp)) { onBack() }
        }
    }
}
