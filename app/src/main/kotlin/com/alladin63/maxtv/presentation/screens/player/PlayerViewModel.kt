package com.alladin63.maxtv.presentation.screens.player

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.okhttp.OkHttpDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = true,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val error: String? = null,
    val volume: Float = 1f
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient
) : ViewModel() {

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var progressJob: Job? = null
    private var currentContentId: Long = 0L
    private var currentType: String = "VOD"

    val player: ExoPlayer = ExoPlayer.Builder(context).build().also { player ->
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _state.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressTracking() else progressJob?.cancel()
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                _state.update {
                    it.copy(
                        isBuffering = playbackState == Player.STATE_BUFFERING,
                        duration = player.duration.coerceAtLeast(0L)
                    )
                }
                if (playbackState == Player.STATE_READY) {
                    _state.update { it.copy(error = null) }
                }
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                Timber.e(error, "Player error")
                _state.update { it.copy(error = "Erreur de lecture: ${error.message}", isBuffering = false) }
            }
        })
        player.prepare()
        player.playWhenReady = true
    }

    fun initPlayer(url: String, title: String, type: String, contentId: Long) {
        currentContentId = contentId
        currentType = type
        _state.update { PlayerState(isBuffering = true) }

        try {
            val dataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
            val mediaItem = MediaItem.fromUri(url)

            if (url.contains(".m3u8") || type == "LIVE") {
                val hlsSource = HlsMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)
                player.setMediaSource(hlsSource)
            } else {
                player.setMediaItem(mediaItem)
            }
            player.prepare()
            player.playWhenReady = true
        } catch (e: Exception) {
            Timber.e(e, "Error initializing player")
            _state.update { it.copy(error = "Impossible de démarrer la lecture", isBuffering = false) }
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) player.pause() else player.play()
    }

    fun seekForward() {
        player.seekTo((player.currentPosition + 10_000).coerceAtMost(player.duration))
    }

    fun seekBackward() {
        player.seekTo((player.currentPosition - 10_000).coerceAtLeast(0))
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    fun increaseVolume() {
        val newVol = (player.volume + 0.1f).coerceAtMost(1f)
        player.volume = newVol
        _state.update { it.copy(volume = newVol) }
    }

    fun decreaseVolume() {
        val newVol = (player.volume - 0.1f).coerceAtLeast(0f)
        player.volume = newVol
        _state.update { it.copy(volume = newVol) }
    }

    fun retry() {
        val url = player.currentMediaItem?.localConfiguration?.uri?.toString() ?: return
        initPlayer(url, "", currentType, currentContentId)
    }

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                _state.update {
                    it.copy(
                        currentPosition = player.currentPosition,
                        duration = player.duration.coerceAtLeast(0L)
                    )
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        progressJob?.cancel()
        player.release()
    }
}
