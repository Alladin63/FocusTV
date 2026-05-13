package com.alladin63.maxtv.presentation.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.alladin63.maxtv.core.data.repository.HistoryRepository
import com.alladin63.maxtv.core.data.repository.PlaylistRepository
import com.alladin63.maxtv.core.domain.model.Playlist
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class SettingsUiState(
    val playlists: List<Playlist> = emptyList(),
    val autoRefresh: Boolean = true,
    val autoRefreshHours: Int = 24,
    val clearHistoryDone: Boolean = false,
    val isLoading: Boolean = false,
    val playerBufferMs: Int = 5000,
    val preferredQuality: String = "Auto",
    val subtitlesEnabled: Boolean = false,
    val theme: String = "Sombre",
    val appVersion: String = "1.0.0",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val playlistRepository: PlaylistRepository,
    private val historyRepository: HistoryRepository,
    private val dataStore: DataStore<Preferences>,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState(isLoading = true))
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    companion object {
        val KEY_AUTO_REFRESH = booleanPreferencesKey("auto_refresh")
        val KEY_REFRESH_HOURS = intPreferencesKey("refresh_hours")
        val KEY_BUFFER_MS = intPreferencesKey("buffer_ms")
        val KEY_QUALITY = stringPreferencesKey("preferred_quality")
        val KEY_SUBTITLES = booleanPreferencesKey("subtitles_enabled")
    }

    init {
        viewModelScope.launch {
            combine(
                playlistRepository.getAllPlaylists(),
                dataStore.data,
            ) { playlists, prefs ->
                SettingsUiState(
                    playlists = playlists,
                    autoRefresh = prefs[KEY_AUTO_REFRESH] ?: true,
                    autoRefreshHours = prefs[KEY_REFRESH_HOURS] ?: 24,
                    playerBufferMs = prefs[KEY_BUFFER_MS] ?: 5000,
                    preferredQuality = prefs[KEY_QUALITY] ?: "Auto",
                    subtitlesEnabled = prefs[KEY_SUBTITLES] ?: false,
                    isLoading = false,
                )
            }.collect { _state.value = it }
        }
    }

    fun setAutoRefresh(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_AUTO_REFRESH] = enabled }
        }
    }

    fun setRefreshHours(hours: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_REFRESH_HOURS] = hours }
        }
    }

    fun setBufferMs(ms: Int) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_BUFFER_MS] = ms }
        }
    }

    fun setQuality(quality: String) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_QUALITY] = quality }
        }
    }

    fun setSubtitles(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[KEY_SUBTITLES] = enabled }
        }
    }

    fun deletePlaylist(id: Long) {
        viewModelScope.launch { playlistRepository.deletePlaylist(id) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearHistory()
            _state.update { it.copy(clearHistoryDone = true) }
        }
    }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Row(
        Modifier.fillMaxSize().background(Color(0xFF0A0A0F)).padding(0.dp)
    ) {
        // Panneau gauche — sections
        val sections = listOf("Playlists", "Lecteur", "EPG", "Avancé", "À propos")
        var selectedSection by remember { mutableStateOf(0) }

        Column(
            Modifier.width(200.dp).fillMaxHeight()
                .background(Color(0xFF12121C)).padding(vertical = 16.dp)
        ) {
            Text("⚙️  Réglages", fontSize = 18.sp, fontWeight = FontWeight.Bold,
                color = Color.White, modifier = Modifier.padding(16.dp, 0.dp, 16.dp, 16.dp))

            sections.forEachIndexed { idx, label ->
                SectionTab(label, idx == selectedSection) { selectedSection = idx }
            }
        }

        // Panneau droit — contenu
        Box(Modifier.fillMaxSize().padding(24.dp)) {
            when (selectedSection) {
                0 -> PlaylistsSection(state, viewModel)
                1 -> PlayerSection(state, viewModel)
                2 -> EpgSection(state, viewModel)
                3 -> AdvancedSection(state, viewModel)
                4 -> AboutSection(state)
            }
        }
    }
}

@Composable
private fun SectionTab(label: String, selected: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    Box(
        Modifier.fillMaxWidth().height(48.dp)
            .background(
                when {
                    selected -> Color(0xFF0D6EFD).copy(0.25f)
                    focused -> Color.White.copy(0.06f)
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = Color(0xFF0D6EFD),
            )
            .onFocusChanged { focused = it.isFocused }
            .focusable()
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(label, color = if (selected) Color(0xFF0D6EFD) else Color.White,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 15.sp)
    }
}

// ─── Sections ─────────────────────────────────────────────────────────────────

@Composable
private fun PlaylistsSection(state: SettingsUiState, vm: SettingsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Mes Playlists")

        if (state.playlists.isEmpty()) {
            InfoBox("Aucune playlist configurée.\nAjoutez-en une depuis l'écran d'accueil.")
        } else {
            state.playlists.forEach { pl ->
                PlaylistRow(pl) { vm.deletePlaylist(pl.id) }
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: Playlist, onDelete: () -> Unit) {
    var showConfirm by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(playlist.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(playlist.url, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
            Text(
                text = "Type: ${playlist.type}  •  ${if (playlist.isActive) "✅ Active" else "Inactive"}",
                color = Color.Gray, fontSize = 11.sp,
            )
        }
        if (showConfirm) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onDelete(); showConfirm = false },
                    colors = ButtonDefaults.colors(containerColor = Color(0xFFCC2222))) {
                    Text("Confirmer", fontSize = 12.sp)
                }
                OutlinedButton(onClick = { showConfirm = false }) { Text("Annuler", fontSize = 12.sp) }
            }
        } else {
            OutlinedButton(onClick = { showConfirm = true }) { Text("🗑  Supprimer", fontSize = 12.sp) }
        }
    }
}

@Composable
private fun PlayerSection(state: SettingsUiState, vm: SettingsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Lecteur vidéo")

        SettingToggle(
            label = "Sous-titres automatiques",
            description = "Activer les sous-titres si disponibles",
            checked = state.subtitlesEnabled,
            onCheckedChange = vm::setSubtitles,
        )

        SettingDropdown(
            label = "Qualité préférée",
            description = "Résolution de lecture par défaut",
            selected = state.preferredQuality,
            options = listOf("Auto", "1080p", "720p", "480p", "360p"),
            onSelect = vm::setQuality,
        )

        SettingDropdown(
            label = "Buffer lecteur",
            description = "Temps de mise en tampon (plus = moins de coupures)",
            selected = "${state.playerBufferMs / 1000}s",
            options = listOf("2s", "5s", "10s", "15s", "30s"),
            onSelect = { vm.setBufferMs(it.removeSuffix("s").toInt() * 1000) },
        )
    }
}

@Composable
private fun EpgSection(state: SettingsUiState, vm: SettingsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Guide TV (EPG)")

        SettingToggle(
            label = "Mise à jour automatique",
            description = "Actualiser le guide TV en arrière-plan",
            checked = state.autoRefresh,
            onCheckedChange = vm::setAutoRefresh,
        )

        if (state.autoRefresh) {
            SettingDropdown(
                label = "Fréquence de mise à jour",
                description = "Intervalle entre les actualisations EPG",
                selected = "${state.autoRefreshHours}h",
                options = listOf("6h", "12h", "24h", "48h"),
                onSelect = { vm.setRefreshHours(it.removeSuffix("h").toInt()) },
            )
        }

        InfoBox("L'URL EPG est configurée dans la playlist.\nFormat XMLTV compatible.")
    }
}

@Composable
private fun AdvancedSection(state: SettingsUiState, vm: SettingsViewModel) {
    var clearDone by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Avancé")

        SettingAction(
            label = "Effacer l'historique",
            description = "Supprimer l'historique de visionnage",
            buttonLabel = if (clearDone) "✅ Effacé" else "Effacer",
            buttonColor = if (clearDone) Color(0xFF2E7D32) else Color(0xFFCC2222),
            onClick = { vm.clearHistory(); clearDone = true },
        )

        InfoBox("ℹ️  Les données sont stockées localement sur l'appareil.\nAucune donnée n'est envoyée vers des serveurs externes.")
    }
}

@Composable
private fun AboutSection(state: SettingsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("À propos")
        InfoBox("""
🎬 MaX-TV v${state.appVersion}
Application IPTV native pour Android TV et Fire TV.

🛠️  Stack technique:
• Kotlin 2.0 + Coroutines + Flow
• Jetpack Compose for TV
• Media3 / ExoPlayer
• Room + DataStore
• Hilt (injection de dépendances)
• Coil 3 (chargement d'images)

📦  Formats supportés:
• M3U / M3U8
• Xtream Codes API
• EPG XMLTV
• HLS, DASH, RTSP

Développé par Alladin63
GitHub: github.com/Alladin63/MaX-TV
        """.trimIndent())
    }
}

// ─── Composants réutilisables ─────────────────────────────────────────────────

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 20.sp, fontWeight = FontWeight.Bold,
        color = Color(0xFF0D6EFD), modifier = Modifier.padding(bottom = 4.dp))
    Spacer(Modifier.height(4.dp).fillMaxWidth().background(Color(0xFF0D6EFD).copy(0.3f)))
}

@Composable
private fun InfoBox(text: String) {
    Box(
        Modifier.fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(0.08f), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(text, color = Color.LightGray, fontSize = 13.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun SettingToggle(
    label: String, description: String,
    checked: Boolean, onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 16.dp)) {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingDropdown(
    label: String, description: String,
    selected: String, options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text(selected, color = Color(0xFF0D6EFD))
                Text("  ▾", color = Color.Gray)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF1A1A2E))) {
                options.forEach { opt ->
                    DropdownMenuItem(
                        text = { Text(opt, color = if (opt == selected) Color(0xFF0D6EFD) else Color.White) },
                        onClick = { onSelect(opt); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingAction(
    label: String, description: String,
    buttonLabel: String, buttonColor: Color,
    onClick: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth()
            .background(Color(0xFF1A1A2E), RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(description, color = Color.Gray, fontSize = 12.sp)
        }
        Button(onClick = onClick,
            colors = ButtonDefaults.colors(containerColor = buttonColor)) {
            Text(buttonLabel, fontSize = 13.sp)
        }
    }
}
