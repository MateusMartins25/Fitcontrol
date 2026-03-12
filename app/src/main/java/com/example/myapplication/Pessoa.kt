package com.example.myapplication

data class Pessoa (
    val nome: String,
    val idade: Double,
    val Altura: Double,
    val peso: Double,
    val plano: String,
    val situacao: String = "ativo"
)