package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemExercicioTreinoBinding
import com.example.myapplication.model.ExercicioTreino

class SelecaoExercicioAdapter(
    private val itens: MutableList<ExercicioTreino>,
    private val onRemover: (Int) -> Unit
) : RecyclerView.Adapter<SelecaoExercicioAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExercicioTreinoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itens[position], position)
    }

    override fun getItemCount(): Int = itens.size

    inner class ViewHolder(
        private val binding: ItemExercicioTreinoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(exercicio: ExercicioTreino, position: Int) {
            binding.tvNumeroExercicio.text = "%02d".format(position + 1)
            binding.tvNomeExercicio.text   = exercicio.nome

            binding.tvSeries.text     = "${exercicio.series}x"
            binding.tvRepeticoes.text = "${exercicio.repeticoes} rep"
            binding.tvCarga.text      = if (exercicio.carga > 0)
                "${exercicio.carga.formatarCarga()} kg" else "Livre"
            binding.tvDescanso.text   = if (exercicio.descanso > 0)
                "${exercicio.descanso}s" else "—"

            if (exercicio.observacoes.isNotBlank()) {
                binding.tvObservacoes.text = exercicio.observacoes
                binding.tvObservacoes.visibility = View.VISIBLE
                binding.labelObservacoes.visibility = View.VISIBLE
            } else {
                binding.tvObservacoes.visibility = View.GONE
                binding.labelObservacoes.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onRemover(adapterPosition)
            }
        }
    }

    private fun Double.formatarCarga(): String =
        if (this == kotlin.math.floor(this)) this.toInt().toString()
        else String.format("%.1f", this)
}
