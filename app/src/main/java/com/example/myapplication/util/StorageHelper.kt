package com.example.myapplication.util

import android.net.Uri
/**import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference*/
import kotlinx.coroutines.tasks.await

/**
 * Utilitário para upload e deleção de imagens no Firebase Storage.
 *
 * Estrutura de pastas no Storage:
 *   produtos/{produtoId}/imagem.jpg
 */
object StorageHelper {

    /**private val storageRef: StorageReference
        get() = FirebaseStorage.getInstance().reference
    */

    /**
     * Faz upload de uma imagem e retorna a URL pública de download.
     *
     * @param produtoId  ID do produto (usado como pasta)
     * @param imageUri   URI local da imagem selecionada
     * @return           URL pública do arquivo no Storage
     */
    /**
    suspend fun uploadImagem(produtoId: String, imageUri: Uri): String {
        val ref = storageRef
            .child("produtos")
            .child(produtoId)
            .child("imagem.jpg")

        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    } */

    /**
     * Remove a imagem do Storage (chamado ao deletar o produto).
     */
    /**
    suspend fun deletarImagem(produtoId: String) {
        try {
            storageRef
                .child("produtos")
                .child(produtoId)
                .child("imagem.jpg")
                .delete()
                .await()
        } catch (_: Exception) {
            // Imagem pode não existir — ignora silenciosamente
        }
    }*/

    /**suspend fun uploadAvatar(usuarioId: String, imageUri: Uri): String {
        val ref = storageRef.child("usuarios/$usuarioId/avatar.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deletarAvatar(usuarioId: String) {
        try {
            storageRef.child("usuarios/$usuarioId/avatar.jpg").delete().await()
        } catch (_: Exception) { }
    }*/
}
