package com.example.myapplication.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.example.myapplication.model.Exercicio
import com.example.myapplication.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ExercicioRepository {

    private val colecao = FirebaseFirestore.getInstance().collection(COLECAO)

    companion object {
        private const val COLECAO = "exercicios"
    }

    suspend fun inserirExercicio(exercicio: Exercicio): Resource<String> {
        return try {
            val docRef = colecao.document()
            docRef.set(exercicio.copy(id = docRef.id)).await()
            Resource.Success(docRef.id)
        } catch (e: Exception) {
            Resource.Error("Erro ao inserir exercício: ${e.localizedMessage}", e)
        }
    }

    suspend fun atualizarExercicio(exercicio: Exercicio): Resource<Unit> {
        return try {
            require(exercicio.id.isNotBlank()) { "ID obrigatório para atualizar." }
            colecao.document(exercicio.id).update(exercicio.toUpdateMap()).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao atualizar exercício: ${e.localizedMessage}", e)
        }
    }

    suspend fun salvarExercicio(exercicio: Exercicio): Resource<String> {
        return if (exercicio.id.isBlank()) {
            inserirExercicio(exercicio)
        } else {
            when (val r = atualizarExercicio(exercicio)) {
                is Resource.Success -> Resource.Success(exercicio.id)
                is Resource.Error   -> r
                else                -> Resource.Loading
            }
        }
    }

    suspend fun buscarPorId(id: String): Resource<Exercicio> {
        return try {
            val snap = colecao.document(id).get().await()
            val exercicio = snap.toObject<Exercicio>()
                ?: return Resource.Error("Exercício não encontrado.")
            Resource.Success(exercicio)
        } catch (e: Exception) {
            Resource.Error("Erro ao buscar exercício: ${e.localizedMessage}", e)
        }
    }

    fun listarExercicios(): Flow<Resource<List<Exercicio>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = colecao
            .orderBy(Exercicio.FIELD_NOME, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Resource.Error("Erro ao listar: ${err.localizedMessage}", err))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snap?.toObjects<Exercicio>() ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    fun buscarPorNome(query: String): Flow<Resource<List<Exercicio>>> = flow {
        emit(Resource.Loading)
        try {
            val fim = query.dropLast(1) + (query.last() + 1)
            val snap = colecao
                .whereGreaterThanOrEqualTo(Exercicio.FIELD_NOME, query)
                .whereLessThan(Exercicio.FIELD_NOME, fim)
                .orderBy(Exercicio.FIELD_NOME)
                .get().await()
            emit(Resource.Success(snap.toObjects<Exercicio>()))
        } catch (e: Exception) {
            emit(Resource.Error("Erro na busca: ${e.localizedMessage}", e))
        }
    }

    suspend fun deletarExercicio(id: String): Resource<Unit> {
        return try {
            colecao.document(id).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao deletar exercício: ${e.localizedMessage}", e)
        }
    }
}
