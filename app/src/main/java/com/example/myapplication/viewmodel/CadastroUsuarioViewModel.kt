package com.example.myapplication.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Usuario
import com.example.myapplication.repository.UsuarioRepository
import com.example.myapplication.util.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CadastroUsuarioViewModel(
    private val repository: UsuarioRepository = UsuarioRepository()
) : ViewModel() {

    private val _salvarEvent = MutableSharedFlow<Resource<String>>()
    val salvarEvent: SharedFlow<Resource<String>> = _salvarEvent.asSharedFlow()

    private val _salvando = MutableStateFlow(false)
    val salvando: StateFlow<Boolean> = _salvando.asStateFlow()

    private val _usuarioEditando = MutableStateFlow<Usuario?>(null)
    val usuarioEditando: StateFlow<Usuario?> = _usuarioEditando.asStateFlow()

    private val _deletarEvent = MutableSharedFlow<Resource<Unit>>()
    val deletarEvent: SharedFlow<Resource<Unit>> = _deletarEvent.asSharedFlow()

    fun carregarUsuario(usuarioId: String) {
        viewModelScope.launch {
            when (val r = repository.buscarUsuarioPorId(usuarioId)) {
                is Resource.Success -> _usuarioEditando.value = r.data
                is Resource.Error   -> _salvarEvent.emit(Resource.Error(r.message))
                else                -> Unit
            }
        }
    }

    fun salvarUsuario(usuario: Usuario, fotoUri: Uri? = null) {
        if (_salvando.value) return
        viewModelScope.launch {
            _salvando.value = true
            _salvarEvent.emit(Resource.Loading)
            val result = repository.salvarUsuario(usuario, fotoUri)
            _salvarEvent.emit(result)
            _salvando.value = false
        }
    }

    fun deletarUsuario(usuarioId: String) {
        viewModelScope.launch {
            _deletarEvent.emit(Resource.Loading)
            _deletarEvent.emit(repository.deletarUsuario(usuarioId))
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CadastroUsuarioViewModel() as T
    }
}
