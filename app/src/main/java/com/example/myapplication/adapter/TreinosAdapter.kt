package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemTreinoBinding
import com.example.myapplication.model.Treino
import java.text.SimpleDateFormat
import java.util.Locale

class TreinosAdapter(
    private val onItemClick: (Treino) -> Unit
) : ListAdapter<Treino, TreinosAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTreinoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemTreinoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))

        fun bind(treino: Treino) {
            binding.tvNomeTreino.text = treino.nome

            val totalEx = treino.totalExercicios
            binding.tvQuantidadeExercicios.text = when (totalEx) {
                0    -> "Nenhum exercício"
                1    -> "1 exercício"
                else -> "$totalEx exercícios"
            }

            val atualizadoEm = treino.atualizadoEm ?: treino.criadoEm
            binding.tvUltimaAtualizacao.text = atualizadoEm
                ?.let { "Atualizado em ${dateFormat.format(it)}" }
                ?: "Sem histórico de atualização"

            binding.tvGrupoMuscular.text = treino.grupoMuscular
                .takeIf { it.isNotBlank() } ?: "Geral"

            binding.root.setOnClickListener { onItemClick(treino) }
        }
    }

    private companion object DiffCallback : DiffUtil.ItemCallback<Treino>() {
        override fun areItemsTheSame(oldItem: Treino, newItem: Treino) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Treino, newItem: Treino) =
            oldItem == newItem
    }
}
