package com.example.myapplication.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de Exercício espelhado no Firestore.
 * Coleção: "exercicios"
 */
data class Exercicio(
    @DocumentId
    val id: String = "",

    val nome: String = "",
    val gruposMusculares: List<String> = emptyList(),   // ex: ["Peito", "Tríceps"]
    val equipamento: String = "",                        // "Barra" | "Halter" | "Máquina" | "Livre"
    val series: Int = 0,
    val repeticoes: Int = 0,
    val carga: Double = 0.0,
    val descanso: Int = 0,                               // segundos
    val dificuldade: String = DIFICULDADE_INICIANTE,    // "Iniciante" | "Médio" | "Avançado"
    val instrucoes: String = "",
    val ativo: Boolean = true,

    @ServerTimestamp val criadoEm: Date? = null,
    @ServerTimestamp val atualizadoEm: Date? = null
) {
    companion object {
        const val DIFICULDADE_INICIANTE = "Iniciante"
        const val DIFICULDADE_MEDIO     = "Médio"
        const val DIFICULDADE_AVANCADO  = "Avançado"

        const val EQUIP_BARRA   = "Barra"
        const val EQUIP_HALTER  = "Halter"
        const val EQUIP_MAQUINA = "Máquina"
        const val EQUIP_LIVRE   = "Livre"

        const val FIELD_NOME        = "nome"
        const val FIELD_DIFICULDADE = "dificuldade"
        const val FIELD_ATIVO       = "ativo"
        const val FIELD_ATUALIZADO  = "atualizadoEm"
    }

    fun toUpdateMap(): Map<String, Any?> = mapOf(
        "nome"             to nome,
        "gruposMusculares" to gruposMusculares,
        "equipamento"      to equipamento,
        "series"           to series,
        "repeticoes"       to repeticoes,
        "carga"            to carga,
        "descanso"         to descanso,
        "dificuldade"      to dificuldade,
        "instrucoes"       to instrucoes,
        "ativo"            to ativo,
        FIELD_ATUALIZADO   to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}
