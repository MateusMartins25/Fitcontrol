package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExercicioAdapter(private val lista: MutableList<Exercicio>) :
    RecyclerView.Adapter<ExercicioAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nome: TextView = itemView.findViewById(R.id.txtNome)
        val serie: TextView = itemView.findViewById(R.id.txtSeries)
        val repeticoes: TextView = itemView.findViewById(R.id.txtRepeticoes)
        val descanso : TextView = itemView.findViewById(R.id.txtDescanso)
        val imagem: ImageView = itemView.findViewById(R.id.imgExercicio)
        val btnConcluir: ImageView = itemView.findViewById(R.id.btnConcluir)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val exercicio = lista[position]

        holder.nome.text = exercicio.nome
        holder.serie.text = exercicio.serie
        holder.repeticoes.text = exercicio.repeticoes
        holder.descanso.text = exercicio.descanso
        holder.imagem.setImageResource(exercicio.imagem)

        if (exercicio.concluido) {
            holder.btnConcluir.setImageResource(android.R.drawable.checkbox_on_background)
            holder.itemView.alpha = 0.5f
        } else {
            holder.btnConcluir.setImageResource(android.R.drawable.checkbox_off_background)
            holder.itemView.alpha = 1f
        }

        holder.btnConcluir.setOnClickListener {

            exercicio.concluido = !exercicio.concluido
            notifyItemChanged(position)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicio, parent, false)

        return ViewHolder(view)
    }


    fun atualizarLista(novaLista: List<Exercicio>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return lista.size
    }


}