package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HistoricoTreinoAdapter(
    private val lista: List<HistoricoTreino>
) : RecyclerView.Adapter<HistoricoTreinoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtNomeTreino: TextView = itemView.findViewById(R.id.txtNomeTreino)
        val txtDuracao: TextView = itemView.findViewById(R.id.txtDuracao)
        val txtData: TextView = itemView.findViewById(R.id.txtDia)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historico_treino, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val treino = lista[position]

        holder.txtNomeTreino.text = treino.nomeTreino
        holder.txtDuracao.text = treino.duracao
        holder.txtData.text = treino.data
    }

    override fun getItemCount(): Int {
        return lista.size
    }
}