package com.alladin63.maxtv.presentation.screens.epg

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.*
import coil3.compose.AsyncImage
import com.alladin63.maxtv.core.domain.model.Channel
import com.alladin63.maxtv.core.domain.model.EpgProgram
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.data.repository.EpgRepository
import com.alladin63.maxtv.core.data.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── ViewModel ───────────────────────────────────────────────────────────────

data class EpgUiState(
    val channels: List<Channel> = emptyList(),
    val programs: Map<String, List<EpgProgram>> = emptyMap(),
    val currentTimeMs: Long = System.currentTimeMillis(),
    val isLoading: Boolean = false,
    val selectedChannel: Channel? = null,
    val selectedProgram: EpgProgram? = null,
)

@HiltViewModel
class EpgViewModel @Inject constructor(
    private val epgRepository: EpgRepository,
    private val contentRepository: ContentRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EpgUiState(isLoading = true))
    val state: StateFlow<EpgUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            contentRepository.getChannels().collect { channels ->
                _state.update { it.copy(channels = channels, isLoading = false) }
            }
        }
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000)
                _state.update { it.copy(currentTimeMs = System.currentTimeMillis()) }
            }
        }
    }

    fun selectProgram(channel: Channel, program: EpgProgram) {
        _state.update { it.copy(selectedChannel = channel, selectedProgram = program) }
    }

    fun dismissDetail() { _state.update { it.copy(selectedProgram = null) } }
}

// ─── Screen ───────────────────────────────────────────────────────────────────

private val CELL_HEIGHT = 72.dp
private val CHANNEL_COL_WIDTH = 180.dp
private val HOUR_WIDTH = 320.dp
private val TIME_BAR_HEIGHT = 40.dp

@Composable
fun EpgScreen(
    onChannelClick: (String, String) -> Unit,
    onBack: () -> Unit = {},
    viewModel: EpgViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val hScroll = rememberScrollState()
    val timeFmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val now = state.currentTimeMs

    val gridStartMs = remember(now) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        cal.timeInMillis - 30 * 60_000L
    }

    Box(Modifier.fillMaxSize().background(Color(0xFF0A0A0F))) {
        if (state.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
            return@Box
        }

        Column(Modifier.fillMaxSize()) {
            Text("📺  Guide TV", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = Color.White, modifier = Modifier.padding(16.dp, 8.dp))

            Row(Modifier.fillMaxSize()) {
                // Colonne gauche — noms chaînes
                Column(Modifier.width(CHANNEL_COL_WIDTH).fillMaxHeight()) {
                    Spacer(Modifier.height(TIME_BAR_HEIGHT))
                    LazyColumn {
                        items(state.channels) { ch ->
                            ChannelCell(ch, state.selectedChannel?.id == ch.id)
                        }
                    }
                }

                // Grille scrollable horizontalement
                Column(Modifier.fillMaxSize().horizontalScroll(hScroll)) {
                    val totalW = HOUR_WIDTH * 5
                    TimeBar(gridStartMs, now, HOUR_WIDTH.value, totalW.value, timeFmt)
                    LazyColumn {
                        items(state.channels) { ch ->
                            EpgRow(
                                programs = state.programs[ch.id.toString()] ?: emptyList(),
                                gridStartMs = gridStartMs,
                                nowMs = now,
                                hourWidthDp = HOUR_WIDTH.value,
                                totalWidthDp = totalW.value,
                                onProgramClick = { prog -> viewModel.selectProgram(ch, prog) },
                            )
                        }
                    }
                }
            }
        }

        // Ligne rouge "maintenant"
        val nowOffsetDp = CHANNEL_COL_WIDTH.value +
                ((now - gridStartMs) / 3_600_000f) * HOUR_WIDTH.value
        Box(Modifier.offset(x = nowOffsetDp.dp, y = TIME_BAR_HEIGHT + 8.dp)
            .width(2.dp).fillMaxHeight().background(Color(0xFFFF4444).copy(alpha = 0.8f)))

        // Overlay détail programme
        state.selectedProgram?.let { prog ->
            ProgramDetail(prog,
                onDismiss = viewModel::dismissDetail,
                onPlay = {
                    state.selectedChannel?.let { onChannelClick(it.streamUrl, it.name) }
                    viewModel.dismissDetail()
                }
            )
        }
    }
}

@Composable
private fun ChannelCell(channel: Channel, selected: Boolean) {
    Row(
        Modifier.width(180.dp).height(CELL_HEIGHT)
            .background(if (selected) Color(0xFF0D6EFD).copy(0.2f) else Color.Transparent)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AsyncImage(channel.logoUrl, null,
            Modifier.size(36.dp).clip(RoundedCornerShape(4.dp)))
        Text(channel.name, color = Color.White, fontSize = 13.sp,
            maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TimeBar(
    gridStartMs: Long, nowMs: Long,
    hourWidthDp: Float, totalWidthDp: Float,
    timeFmt: SimpleDateFormat,
) {
    Box(Modifier.width(totalWidthDp.dp).height(TIME_BAR_HEIGHT)
        .background(Color(0xFF1A1A2E))) {
        val startHour = gridStartMs / 3_600_000L
        for (i in 0..5) {
            val hourMs = (startHour + i) * 3_600_000L
            val x = ((hourMs - gridStartMs) / 3_600_000f) * hourWidthDp
            Text(timeFmt.format(Date(hourMs)), color = Color.Gray, fontSize = 12.sp,
                modifier = Modifier.offset(x = (x + 4).dp).align(Alignment.CenterStart))
        }
    }
}

@Composable
private fun EpgRow(
    programs: List<EpgProgram>,
    gridStartMs: Long, nowMs: Long,
    hourWidthDp: Float, totalWidthDp: Float,
    onProgramClick: (EpgProgram) -> Unit,
) {
    Box(Modifier.width(totalWidthDp.dp).height(CELL_HEIGHT)
        .background(Color(0xFF12121C))
        .border(0.5.dp, Color.White.copy(0.05f))) {

        if (programs.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(2.dp)
                .background(Color(0xFF1E1E30), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.CenterStart) {
                Text("Aucune info", color = Color.Gray, fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp))
            }
        } else {
            programs.forEach { prog ->
                val x = ((prog.startTime - gridStartMs) / 3_600_000f) * hourWidthDp
                val w = (((prog.endTime - prog.startTime) / 3_600_000f) * hourWidthDp - 4)
                    .coerceAtLeast(20f)
                val isNow = prog.startTime <= nowMs && nowMs < prog.endTime
                var focused by remember { mutableStateOf(false) }

                Box(
                    Modifier.offset(x = (x + 2).dp, y = 2.dp)
                        .width(w.dp).height(CELL_HEIGHT - 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isNow && focused -> Color(0xFF0D6EFD)
                                isNow -> Color(0xFF0D6EFD).copy(0.65f)
                                focused -> Color(0xFF2A2A4A)
                                else -> Color(0xFF1E1E30)
                            }
                        )
                        .border(if (focused) 2.dp else 0.dp, Color(0xFF0D6EFD),
                            RoundedCornerShape(4.dp))
                        .onFocusChanged { focused = it.isFocused }
                        .focusable()
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Column {
                        Text(prog.title, color = Color.White, fontSize = 13.sp,
                            fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (w > 120) {
                            val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                            Text("${fmt.format(Date(prog.startTime))}–${fmt.format(Date(prog.endTime))}",
                                color = Color.Gray, fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ProgramDetail(
    program: EpgProgram,
    onDismiss: () -> Unit,
    onPlay: () -> Unit,
) {
    val fmt = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    Box(Modifier.fillMaxSize().background(Color.Black.copy(0.75f)),
        contentAlignment = Alignment.Center) {
        Column(
            Modifier.width(500.dp)
                .background(Color(0xFF1A1A2E), RoundedCornerShape(12.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(program.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("${fmt.format(Date(program.startTime))} — ${fmt.format(Date(program.endTime))}",
                fontSize = 14.sp, color = Color(0xFF0D6EFD))
            if (program.description.isNotBlank()) {
                Text(program.description, fontSize = 14.sp, color = Color.LightGray,
                    maxLines = 5, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onPlay) { Text("▶  Regarder") }
                OutlinedButton(onClick = onDismiss) { Text("Fermer") }
            }
        }
    }
}
