package com.example.myapplication.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ExercicioTreino(
    val exercicioId: String = "",
    val nome: String = "",
    val series: Int = 0,
    val repeticoes: Int = 0,
    val carga: Double = 0.0,
    val descanso: Int = 0,
    val observacoes: String = ""
)

data class Treino(
    @DocumentId
    val id: String = "",

    val nome: String = "",
    val grupoMuscular: String = "",
    val exercicios: List<ExercicioTreino> = emptyList(),
    val usuarioId: String = "",

    @ServerTimestamp val criadoEm: Date? = null,
    @ServerTimestamp val atualizadoEm: Date? = null
) {
    companion object {
        const val FIELD_NOME         = "nome"
        const val FIELD_USUARIO_ID   = "usuarioId"
        const val FIELD_ATUALIZADO   = "atualizadoEm"
    }

    val totalExercicios: Int get() = exercicios.size
}
