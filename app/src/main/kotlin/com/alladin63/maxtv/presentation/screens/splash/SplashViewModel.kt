package com.alladin63.maxtv.presentation.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alladin63.maxtv.core.data.local.dao.PlaylistDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashState { Loading, HasPlaylist, NoPlaylist }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        viewModelScope.launch {
            delay(1200) // Animation splash
            val active = playlistDao.getActivePlaylist().first()
            _state.value = if (active != null) SplashState.HasPlaylist else SplashState.NoPlaylist
        }
    }
}
