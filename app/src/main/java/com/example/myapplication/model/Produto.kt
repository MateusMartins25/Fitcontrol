package com.example.myapplication.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Produto(
    @DocumentId
    val id: String = "",

    val nome: String = "",
    val descricao: String = "",
    val valor: Double = 0.0,
    val status: String = STATUS_DISPONIVEL,
    val imagemUrl: String = "",

    @ServerTimestamp
    val criadoEm: Date? = null,

    @ServerTimestamp
    val atualizadoEm: Date? = null
) {
    companion object {
        const val STATUS_DISPONIVEL   = "disponivel"
        const val STATUS_INDISPONIVEL = "indisponivel"

        const val FIELD_NOME          = "nome"
        const val FIELD_STATUS        = "status"
        const val FIELD_VALOR         = "valor"
        const val FIELD_IMAGEM_URL    = "imagemUrl"
        const val FIELD_ATUALIZADO_EM = "atualizadoEm"
    }

    fun toUpdateMap(): Map<String, Any?> = mapOf(
        FIELD_NOME          to nome,
        "descricao"         to descricao,
        FIELD_VALOR         to valor,
        FIELD_STATUS        to status,
        FIELD_IMAGEM_URL    to imagemUrl,
        FIELD_ATUALIZADO_EM to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
}
