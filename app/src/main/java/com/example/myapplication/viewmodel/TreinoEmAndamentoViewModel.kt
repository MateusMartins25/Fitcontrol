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
import kotlinx.coroutines.launch

class TreinoEmAndamentoViewModel(
    private val repository: TreinoRepository = TreinoRepository()
) : ViewModel() {

    private val _treino = MutableStateFlow<Resource<Treino>>(Resource.Loading)
    val treino: StateFlow<Resource<Treino>> = _treino.asStateFlow()

    fun carregarTreino(treinoId: String) {
        viewModelScope.launch {
            _treino.value = Resource.Loading
            _treino.value = repository.buscarTreinoPorId(treinoId)
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            TreinoEmAndamentoViewModel() as T
    }
}
