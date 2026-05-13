package com.alladin63.maxtv.core.service

import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MaxTvPlaybackService : MediaSessionService() {
    override fun onGetSession(controllerInfo: androidx.media3.session.MediaSession.ControllerInfo) = null
}
