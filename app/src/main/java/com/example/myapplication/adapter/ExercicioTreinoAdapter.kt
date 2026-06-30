package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemExercicioTreinoBinding
import com.example.myapplication.model.ExercicioTreino

class ExercicioTreinoAdapter :
    ListAdapter<ExercicioTreino, ExercicioTreinoAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercicioTreinoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    inner class ViewHolder(
        private val binding: ItemExercicioTreinoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercicio: ExercicioTreino, numero: Int) {
            binding.tvNumeroExercicio.text = "%02d".format(numero)
            binding.tvNomeExercicio.text   = exercicio.nome

            binding.tvSeries.text     = "${exercicio.series}x"
            binding.tvRepeticoes.text = "${exercicio.repeticoes} rep"
            binding.tvCarga.text      = if (exercicio.carga > 0)
                "${exercicio.carga.formatarCarga()} kg" else "Livre"
            binding.tvDescanso.text   = if (exercicio.descanso > 0)
                "${exercicio.descanso}s" else "—"

            if (exercicio.observacoes.isNotBlank()) {
                binding.tvObservacoes.text = exercicio.observacoes
                binding.tvObservacoes.visibility = android.view.View.VISIBLE
                binding.labelObservacoes.visibility = android.view.View.VISIBLE
            } else {
                binding.tvObservacoes.visibility = android.view.View.GONE
                binding.labelObservacoes.visibility = android.view.View.GONE
            }
        }
    }

    private fun Double.formatarCarga(): String =
        if (this == kotlin.math.floor(this)) this.toInt().toString()
        else String.format("%.1f", this)

    private companion object DiffCallback : DiffUtil.ItemCallback<ExercicioTreino>() {
        override fun areItemsTheSame(a: ExercicioTreino, b: ExercicioTreino) =
            a.exercicioId == b.exercicioId && a.nome == b.nome

        override fun areContentsTheSame(a: ExercicioTreino, b: ExercicioTreino) = a == b
    }
}
