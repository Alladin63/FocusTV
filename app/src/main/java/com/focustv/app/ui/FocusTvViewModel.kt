package com.focustv.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focustv.app.core.FocusTvState
import com.focustv.app.core.IptvSource
import com.focustv.app.core.MediaContent
import com.focustv.app.services.FocusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FocusTvViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = FocusRepository(app)
    private val _state = MutableStateFlow(FocusTvState(loading = true))
    val state: StateFlow<FocusTvState> = _state

    init {
        viewModelScope.launch {
            val initial = repo.initialState()
            _state.value = initial.copy(loading = true, message = "Chargement des sources…")
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val current = _state.value
            _state.value = current.copy(loading = true, message = "Synchronisation Live / VOD / Séries…")
            val data = repo.reloadAll(current.sources)
            _state.value = _state.value.copy(
                live = data.first,
                movies = data.second,
                series = data.third,
                loading = false,
                message = if (data.first.isEmpty() && data.second.isEmpty() && data.third.isEmpty()) "Aucun contenu chargé. Ajoute une source." else "FocusTV prêt"
            )
        }
    }

    fun saveSource(source: IptvSource) {
        viewModelScope.launch {
            val sources = repo.saveSource(source)
            _state.value = _state.value.copy(sources = sources, message = "Source enregistrée")
            refresh()
        }
    }

    fun deleteSource(id: String) {
        viewModelScope.launch {
            val sources = repo.deleteSource(id)
            _state.value = _state.value.copy(sources = sources, message = "Source supprimée")
            refresh()
        }
    }

    fun toggleFavorite(item: MediaContent) {
        viewModelScope.launch {
            val fav = repo.toggleFavorite(item.id)
            _state.value = _state.value.copy(favorites = fav)
        }
    }

    fun markWatched(item: MediaContent) {
        viewModelScope.launch { repo.addHistory(item) }
    }
}
