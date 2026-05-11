package com.focustv.app.player

import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.focustv.app.ui.PlayerLauncher

class PlayerActivity : ComponentActivity() {
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val item = PlayerLauncher.consume()

        setContent {
            val exo = remember {
                ExoPlayer.Builder(this).build().also { p ->
                    player = p
                    if (item != null) {
                        p.setMediaItem(MediaItem.fromUri(item.streamUrl))
                        p.prepare()
                        p.playWhenReady = true
                        p.repeatMode = Player.REPEAT_MODE_OFF
                    }
                }
            }
            DisposableEffect(Unit) {
                onDispose {
                    exo.release()
                    player = null
                }
            }
            AndroidView(factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = exo
                    useController = true
                    controllerShowTimeoutMs = 3500
                    setShowFastForwardButton(true)
                    setShowRewindButton(true)
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                }
            })
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val p = player ?: return super.dispatchKeyEvent(event)
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (event.keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    finish()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_CENTER,
                KeyEvent.KEYCODE_ENTER,
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (p.isPlaying) p.pause() else p.play()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_RIGHT,
                KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
                    p.seekTo((p.currentPosition + 10_000).coerceAtMost(p.duration.takeIf { it > 0 } ?: Long.MAX_VALUE))
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT,
                KeyEvent.KEYCODE_MEDIA_REWIND -> {
                    p.seekTo((p.currentPosition - 10_000).coerceAtLeast(0))
                    return true
                }
            }
        }
        return super.dispatchKeyEvent(event)
    }
}
