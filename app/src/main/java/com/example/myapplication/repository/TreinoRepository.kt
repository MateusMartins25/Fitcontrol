package com.example.myapplication.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.example.myapplication.model.Treino
import com.example.myapplication.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TreinoRepository {

    private val db   = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun colecaoUsuario() = auth.currentUser?.uid?.let { uid ->
        db.collection("usuarios").document(uid).collection("treinos")
    }

    fun listarTreinos(): Flow<Resource<List<Treino>>> = callbackFlow {
        val colecao = colecaoUsuario()
        if (colecao == null) {
            trySend(Resource.Error("Usuário não autenticado."))
            close()
            return@callbackFlow
        }

        trySend(Resource.Loading)

        val listener = colecao
            .orderBy(Treino.FIELD_ATUALIZADO, Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Resource.Error("Erro ao buscar treinos: ${err.localizedMessage}", err))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snap?.toObjects<Treino>() ?: emptyList()))
            }

        awaitClose { listener.remove() }
    }

    suspend fun buscarTreinoPorId(treinoId: String): Resource<Treino> {
        val colecao = colecaoUsuario()
            ?: return Resource.Error("Usuário não autenticado.")

        return try {
            val snap = colecao.document(treinoId).get().await()
            val treino = snap.toObject<Treino>()
                ?: return Resource.Error("Treino não encontrado.")
            Resource.Success(treino)
        } catch (e: Exception) {
            Resource.Error("Erro ao buscar treino: ${e.localizedMessage}", e)
        }
    }

    suspend fun inserirTreino(uid: String, treino: Treino): Resource<String> {
        return try {
            val docRef = db.collection("usuarios").document(uid).collection("treinos").document()
            docRef.set(treino.copy(id = docRef.id, usuarioId = uid)).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error("Erro ao criar treino: ${e.localizedMessage}", e)
        }
    }
}
