package com.example.myapplication.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Produto
import com.example.myapplication.repository.ProdutoRepository
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
class CadastroProdutoViewModel(
    private val repository: ProdutoRepository = ProdutoRepository()
) : ViewModel() {

    private val _salvarEvent = MutableSharedFlow<Resource<String>>()
    val salvarEvent: SharedFlow<Resource<String>> = _salvarEvent.asSharedFlow()

    private val _salvando = MutableStateFlow(false)
    val salvando: StateFlow<Boolean> = _salvando.asStateFlow()

    private val _produtoEditando = MutableStateFlow<Produto?>(null)
    val produtoEditando: StateFlow<Produto?> = _produtoEditando.asStateFlow()

    private val _queryBusca = MutableStateFlow("")

    private val _resultadoBusca = MutableStateFlow<Resource<List<Produto>>>(Resource.Success(emptyList()))
    val resultadoBusca: StateFlow<Resource<List<Produto>>> = _resultadoBusca.asStateFlow()

    private val _deletarEvent = MutableSharedFlow<Resource<Unit>>()
    val deletarEvent: SharedFlow<Resource<Unit>> = _deletarEvent.asSharedFlow()

    init {
        observarBusca()
    }

    private fun observarBusca() {
        _queryBusca
            .debounce(300L)
            .filter { it.length >= 2 }
            .flatMapLatest { query ->
                repository.buscarPorNome(query)
            }
            .onEach { _resultadoBusca.value = it }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(query: String) {
        _queryBusca.value = query
        if (query.length < 2) {
            _resultadoBusca.value = Resource.Success(emptyList())
        }
    }

    fun carregarProduto(produtoId: String) {
        viewModelScope.launch {
            when (val result = repository.buscarProdutoPorId(produtoId)) {
                is Resource.Success -> _produtoEditando.value = result.data
                is Resource.Error   -> _salvarEvent.emit(Resource.Error(result.message))
                else -> Unit
            }
        }
    }

    fun salvarProduto(produto: Produto, imagemUri: Uri? = null) {
        if (_salvando.value) return

        viewModelScope.launch {
            _salvando.value = true
            _salvarEvent.emit(Resource.Loading)

            val result = repository.salvarProduto(produto, imagemUri)
            _salvarEvent.emit(result)

            _salvando.value = false
        }
    }

    fun deletarProduto(produtoId: String) {
        viewModelScope.launch {
            _deletarEvent.emit(Resource.Loading)
            val result = repository.deletarProduto(produtoId)
            _deletarEvent.emit(result)
        }
    }

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CadastroProdutoViewModel() as T
        }
    }
}
