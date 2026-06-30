package com.example.myapplication.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Modelo de Usuário espelhado no Firestore.
 *
 * Coleção: "usuarios"
 */
data class Usuario(
    @DocumentId
    val id: String = "",

    val nome: String = "",
    val telefone: String = "",
    val email: String = "",
    val dataNascimento: String = "",   // armazenado como "dd/MM/yyyy"
    val administrador: Boolean = false,
    val fotoUrl: String = "",

    @ServerTimestamp
    val criadoEm: Date? = null,

    @ServerTimestamp
    val atualizadoEm: Date? = null
) {
    companion object {
        const val FIELD_NOME           = "nome"
        const val FIELD_EMAIL          = "email"
        const val FIELD_TELEFONE       = "telefone"
        const val FIELD_ADMINISTRADOR  = "administrador"
        const val FIELD_ATUALIZADO_EM  = "atualizadoEm"
    }

    fun toUpdateMap(): Map<String, Any?> = mapOf(
        FIELD_NOME          to nome,
        FIELD_TELEFONE      to telefone,
        FIELD_EMAIL         to email,
        "dataNascimento"    to dataNascimento,
        FIELD_ADMINISTRADOR to administrador,
        "fotoUrl"           to fotoUrl,
        FIELD_ATUALIZADO_EM to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}
