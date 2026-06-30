package com.example.myapplication.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.myapplication.model.Produto
import com.example.myapplication.util.Resource
import com.example.myapplication.util.StorageHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class ProdutoRepository {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val colecao = db.collection(COLECAO)

    companion object {
        private const val COLECAO = "produtos"
    }

    suspend fun inserirProduto(produto: Produto, imagemUri: Uri? = null): Resource<String> {
        return try {
            val docRef = colecao.document()
            val produtoId = docRef.id

            val produtoFinal = produto.copy(id = produtoId)
            docRef.set(produtoFinal).await()

            Resource.Success(produtoId)
        } catch (e: Exception) {
            Resource.Error("Erro ao inserir produto: ${e.localizedMessage}", e)
        }
    }

    suspend fun atualizarProduto(produto: Produto, imagemUri: Uri? = null): Resource<Unit> {
        return try {
            require(produto.id.isNotBlank()) { "ID do produto é obrigatório para atualizar." }

            val updateMap = produto.toUpdateMap()
            colecao.document(produto.id).update(updateMap).await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao atualizar produto: ${e.localizedMessage}", e)
        }
    }

    suspend fun salvarProduto(produto: Produto, imagemUri: Uri? = null): Resource<String> {
        return if (produto.id.isBlank()) {
            inserirProduto(produto, imagemUri)
        } else {
            when (val result = atualizarProduto(produto, imagemUri)) {
                is Resource.Success -> Resource.Success(produto.id)
                is Resource.Error   -> result
                is Resource.Loading -> Resource.Loading
            }
        }
    }

    suspend fun buscarProdutoPorId(id: String): Resource<Produto> {
        return try {
            val snapshot = colecao.document(id).get().await()
            val produto = snapshot.toObject(Produto::class.java)
                ?: return Resource.Error("Produto não encontrado.")
            Resource.Success(produto)
        } catch (e: Exception) {
            Resource.Error("Erro ao buscar produto: ${e.localizedMessage}", e)
        }
    }

    fun buscarProdutos(): Flow<Resource<List<Produto>>> = callbackFlow {
        trySend(Resource.Loading)

        val listener = colecao
            .orderBy(Produto.FIELD_NOME, Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error("Erro ao listar produtos: ${error.localizedMessage}", error))
                    return@addSnapshotListener
                }
                val produtos = snapshot?.toObjects(Produto::class.java) ?: emptyList()
                trySend(Resource.Success(produtos))
            }

        awaitClose { listener.remove() }
    }

    fun buscarPorNome(query: String): Flow<Resource<List<Produto>>> = flow {
        emit(Resource.Loading)
        try {
            val fim = query.dropLast(1) + (query.last() + 1)
            val snapshot = colecao
                .whereGreaterThanOrEqualTo(Produto.FIELD_NOME, query)
                .whereLessThan(Produto.FIELD_NOME, fim)
                .orderBy(Produto.FIELD_NOME)
                .get()
                .await()
            emit(Resource.Success(snapshot.toObjects(Produto::class.java)))
        } catch (e: Exception) {
            emit(Resource.Error("Erro na busca: ${e.localizedMessage}", e))
        }
    }

    suspend fun deletarProduto(produtoId: String): Resource<Unit> {
        return try {
            colecao.document(produtoId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao deletar produto: ${e.localizedMessage}", e)
        }
    }
}
