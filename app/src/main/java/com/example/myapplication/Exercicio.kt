package com.example.myapplication

data class Exercicio(
    val nome: String,
    val serie: String,
    val repeticoes: String,
    val descanso: String,
    val imagem: Int,
    val situacao: String = "aguardando",
    var concluido: Boolean = false
)