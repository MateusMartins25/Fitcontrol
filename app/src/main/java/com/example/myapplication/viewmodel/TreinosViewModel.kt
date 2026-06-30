package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Treino
import com.example.myapplication.repository.TreinoRepository
import com.example.myapplication.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TreinosViewModel(
    private val repository: TreinoRepository = TreinoRepository()
) : ViewModel() {

    private val _treinos = MutableStateFlow<Resource<List<Treino>>>(Resource.Loading)
    val treinos: StateFlow<Resource<List<Treino>>> = _treinos.asStateFlow()

    init {
        carregarTreinos()
    }

    private fun carregarTreinos() {
        repository.listarTreinos()
            .onEach { _treinos.value = it }
            .launchIn(viewModelScope)
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreinosViewModel() as T
    }
}
