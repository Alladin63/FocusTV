package com.focustv.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.focustv.app.core.FocusTvState
import com.focustv.app.core.IptvSource
import com.focustv.app.core.SourceDetector
import com.focustv.app.ui.PanelBg
import com.focustv.app.ui.TextSoft
import com.focustv.app.ui.components.Header
import com.focustv.app.ui.components.NeonBackground
import com.focustv.app.ui.components.TvButton

@Composable
fun SourcesScreen(
    state: FocusTvState,
    onBack: () -> Unit,
    onSave: (IptvSource) -> Unit,
    onDelete: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var mac by remember { mutableStateOf("") }

    NeonBackground {
        LazyColumn(Modifier.fillMaxSize().padding(start = 36.dp, end = 36.dp), contentPadding = PaddingValues(bottom = 60.dp)) {
            item { Header("Sources IPTV", "M3U/M3U8 • Xtream Codes • Portail Stalker • multi-serveurs") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                    SourceField("Nom de la source", name, { name = it }, Modifier.weight(1f))
                    SourceField("URL / serveur / portail", url, { url = it }, Modifier.weight(2f))
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                    SourceField("Utilisateur Xtream", username, { username = it }, Modifier.weight(1f))
                    SourceField("Mot de passe Xtream", password, { password = it }, Modifier.weight(1f))
                    SourceField("MAC Stalker", mac, { mac = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TvButton("Enregistrer + charger", Modifier.width(260.dp)) {
                        val type = SourceDetector.detect(url, username, password)
                        onSave(
                            IptvSource(
                                name = name.ifBlank { "Source ${state.sources.size + 1}" },
                                type = type,
                                url = url.trim(),
                                username = username.trim(),
                                password = password.trim(),
                                portalMac = mac.trim()
                            )
                        )
                        name = ""; url = ""; username = ""; password = ""; mac = ""
                    }
                    TvButton("Rafraîchir", Modifier.width(190.dp)) { onRefresh() }
                    TvButton("Retour", Modifier.width(160.dp)) { onBack() }
                }
                Spacer(Modifier.height(28.dp))
                Text("Sources enregistrées", color = Color.White)
            }

            items(state.sources, key = { it.id }) { src ->
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Column(Modifier.weight(1f).padding(18.dp)) {
                        Text(src.name, color = Color.White)
                        Text("${src.type}  •  ${src.url}", color = TextSoft, maxLines = 1)
                    }
                    TvButton("Supprimer", Modifier.width(180.dp)) { onDelete(src.id) }
                }
            }
        }
    }
}

@Composable
private fun SourceField(label: String, value: String, onValue: (String) -> Unit, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValue,
        modifier = modifier.height(72.dp),
        label = { Text(label) },
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = PanelBg,
            unfocusedContainerColor = PanelBg,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedLabelColor = Color.White,
            unfocusedLabelColor = TextSoft,
            cursorColor = Color.White
        )
    )
}
