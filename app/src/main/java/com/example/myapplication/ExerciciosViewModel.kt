package com.example.myapplication

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ExerciciosViewModel : ViewModel() {
    val listaExercicios = MutableLiveData<MutableList<Exercicio>>(mutableListOf())
    val treinoAmanha = MutableLiveData<MutableList<Exercicio>>()
}