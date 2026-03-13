package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog

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
        holder.serie.text = "${exercicio.serie} séries"
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

        // 🔥 CLIQUE NO ITEM PARA ABRIR O BOTTOM SHEET
        holder.itemView.setOnClickListener {

            val dialog = android.app.Dialog(holder.itemView.context)

            val view = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_exercicio, null)


            dialog.setContentView(view)

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

            // 👇 AQUI começa o código das séries
            val containerSeries = view.findViewById<LinearLayout>(R.id.containerSeries)
            val btnAddSerie = view.findViewById<Button>(R.id.btnAddSerie)

            var numeroSerie = 1

            fun atualizarNumeracaoSeries() {

                for (i in 0 until containerSeries.childCount) {

                    val view = containerSeries.getChildAt(i)

                    val txtNumero = view.findViewById<TextView>(R.id.txtNumeroSerie)

                    txtNumero.text = (i + 1).toString()
                }

                numeroSerie = containerSeries.childCount + 1
            }

            fun adicionarSerie() {

                val serieView = LayoutInflater.from(holder.itemView.context)
                    .inflate(R.layout.item_serie, containerSeries, false)

                val txtNumero = serieView.findViewById<TextView>(R.id.txtNumeroSerie)
                val btnRemover = serieView.findViewById<ImageView>(R.id.btnRemoverSerie)

                txtNumero.text = numeroSerie.toString()

                btnRemover.setOnClickListener {
                    containerSeries.removeView(serieView)
                    atualizarNumeracaoSeries()
                }

                containerSeries.addView(serieView)

                numeroSerie++
            }


            fun gerarSeries(qtdSeries: Int) {

                containerSeries.removeAllViews()

                for (i in 1..qtdSeries) {
                    adicionarSerie()
                }

            }

            val qtdSeries = exercicio.serie
            gerarSeries(qtdSeries)


            btnAddSerie.setOnClickListener {
                adicionarSerie()
            }

            dialog.show()

            val width = (holder.itemView.context.resources.displayMetrics.widthPixels * 0.85).toInt()

            dialog.window?.setLayout(
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val anim = android.view.animation.AnimationUtils.loadAnimation(
                holder.itemView.context,
                R.anim.popup_scale_in
            )

            view.startAnimation(anim)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercicio, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return lista.size
    }

    fun atualizarLista(novaLista: List<Exercicio>) {
        lista.clear()
        lista.addAll(novaLista)
        notifyDataSetChanged()
    }
}