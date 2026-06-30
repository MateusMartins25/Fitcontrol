package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Exercicio
import com.example.myapplication.repository.ExercicioRepository
import com.example.myapplication.util.Resource
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class CadastroExercicioViewModel(
    private val repository: ExercicioRepository = ExercicioRepository()
) : ViewModel() {

    private val _salvarEvent = MutableSharedFlow<Resource<String>>()
    val salvarEvent: SharedFlow<Resource<String>> = _salvarEvent.asSharedFlow()

    private val _salvando = MutableStateFlow(false)
    val salvando: StateFlow<Boolean> = _salvando.asStateFlow()

    private val _exercicioEditando = MutableStateFlow<Exercicio?>(null)
    val exercicioEditando: StateFlow<Exercicio?> = _exercicioEditando.asStateFlow()

    private val _queryBusca = MutableStateFlow("")

    private val _resultadoBusca = MutableStateFlow<Resource<List<Exercicio>>>(Resource.Success(emptyList()))
    val resultadoBusca: StateFlow<Resource<List<Exercicio>>> = _resultadoBusca.asStateFlow()

    private val _deletarEvent = MutableSharedFlow<Resource<Unit>>()
    val deletarEvent: SharedFlow<Resource<Unit>> = _deletarEvent.asSharedFlow()

    init {
        observarBusca()
    }

    private fun observarBusca() {
        _queryBusca
            .debounce(300L)
            .filter { it.length >= 2 }
            .flatMapLatest { repository.buscarPorNome(it) }
            .onEach { _resultadoBusca.value = it }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _queryBusca.value = query
        if (query.length < 2) _resultadoBusca.value = Resource.Success(emptyList())
    }

    fun carregarExercicio(id: String) {
        viewModelScope.launch {
            when (val r = repository.buscarPorId(id)) {
                is Resource.Success -> _exercicioEditando.value = r.data
                is Resource.Error   -> _salvarEvent.emit(Resource.Error(r.message))
                else                -> Unit
            }
        }
    }

    fun salvarExercicio(exercicio: Exercicio) {
        if (_salvando.value) return
        viewModelScope.launch {
            _salvando.value = true
            _salvarEvent.emit(Resource.Loading)
            _salvarEvent.emit(repository.salvarExercicio(exercicio))
            _salvando.value = false
        }
    }

    fun deletarExercicio(id: String) {
        viewModelScope.launch {
            _deletarEvent.emit(Resource.Loading)
            _deletarEvent.emit(repository.deletarExercicio(id))
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CadastroExercicioViewModel() as T
    }
}
