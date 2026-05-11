package com.focustv.app.ui

import android.content.Context
import android.content.Intent
import com.focustv.app.core.MediaContent
import com.focustv.app.player.PlayerActivity

object PlayerLauncher {
    private var pending: MediaContent? = null

    fun open(context: Context, item: MediaContent) {
        pending = item
        context.startActivity(Intent(context, PlayerActivity::class.java))
    }

    fun consume(): MediaContent? {
        val p = pending
        pending = null
        return p
    }
}
