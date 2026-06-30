package com.example.myapplication.repository

import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import com.example.myapplication.model.Usuario
import com.example.myapplication.util.Resource
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UsuarioRepository {

    private val colecao = FirebaseFirestore.getInstance().collection(COLECAO)

    companion object {
        private const val COLECAO = "usuarios"
        private const val SENHA_PADRAO = "123456"
        private const val APP_SECUNDARIO = "UsuarioCreation"
    }

    private fun authSecundario(): FirebaseAuth {
        val app = try {
            FirebaseApp.getInstance(APP_SECUNDARIO)
        } catch (e: IllegalStateException) {
            FirebaseApp.initializeApp(
                FirebaseApp.getInstance().applicationContext,
                FirebaseApp.getInstance().options,
                APP_SECUNDARIO
            )
        }
        return FirebaseAuth.getInstance(app!!)
    }

    suspend fun inserirUsuario(usuario: Usuario, fotoUri: Uri? = null): Resource<String> {
        return try {
            val auth = authSecundario()
            val authResult = auth.createUserWithEmailAndPassword(usuario.email, SENHA_PADRAO).await()
            val novoUid = authResult.user?.uid
                ?: return Resource.Error("Não foi possível criar o acesso do usuário.")
            auth.signOut()

            colecao.document(novoUid).set(usuario.copy(id = novoUid)).await()
            Resource.Success(novoUid)
        } catch (e: Exception) {
            Resource.Error("Erro ao inserir usuário: ${e.localizedMessage}", e)
        }
    }

    suspend fun atualizarUsuario(usuario: Usuario, fotoUri: Uri? = null): Resource<Unit> {
        return try {
            require(usuario.id.isNotBlank()) { "ID obrigatório para atualizar." }

            colecao.document(usuario.id)
                .update(usuario.toUpdateMap())
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao atualizar usuário: ${e.localizedMessage}", e)
        }
    }

    suspend fun salvarUsuario(usuario: Usuario, fotoUri: Uri? = null): Resource<String> {
        return if (usuario.id.isBlank()) {
            inserirUsuario(usuario, fotoUri)
        } else {
            when (val r = atualizarUsuario(usuario, fotoUri)) {
                is Resource.Success -> Resource.Success(usuario.id)
                is Resource.Error   -> r
                else                -> Resource.Loading
            }
        }
    }

    suspend fun buscarUsuarioPorId(id: String): Resource<Usuario> {
        return try {
            val snap = colecao.document(id).get().await()
            val usuario = snap.toObject<Usuario>()
                ?: return Resource.Error("Usuário não encontrado.")
            Resource.Success(usuario)
        } catch (e: Exception) {
            Resource.Error("Erro ao buscar usuário: ${e.localizedMessage}", e)
        }
    }

    suspend fun buscarUsuarioAtual(): Resource<Usuario> {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
            ?: return Resource.Error("Usuário não autenticado.")
        return buscarUsuarioPorId(uid)
    }

    suspend fun usuarioAtualEhAdmin(): Boolean {
        return when (val r = buscarUsuarioAtual()) {
            is Resource.Success -> r.data.administrador
            else -> false
        }
    }

    fun listarUsuarios(): Flow<Resource<List<Usuario>>> = callbackFlow {
        trySend(Resource.Loading)
        val listener = colecao
            .orderBy(Usuario.FIELD_NOME, Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Resource.Error("Erro ao listar: ${err.localizedMessage}", err))
                    return@addSnapshotListener
                }
                trySend(Resource.Success(snap?.toObjects<Usuario>() ?: emptyList()))
            }
        awaitClose { listener.remove() }
    }

    suspend fun deletarUsuario(usuarioId: String): Resource<Unit> {
        return try {
            colecao.document(usuarioId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("Erro ao deletar usuário: ${e.localizedMessage}", e)
        }
    }
}
