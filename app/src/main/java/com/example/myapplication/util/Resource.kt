package com.example.myapplication.util

/**
 * Wrapper genérico para resultados de operações assíncronas.
 * Substitui callbacks aninhados e simplifica o tratamento de erro na UI.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()

    val isSuccess get() = this is Success
    val isError   get() = this is Error
    val isLoading get() = this is Loading
}
