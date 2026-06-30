package com.example.myapplication

import android.view.*
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import android.widget.ImageView

class ProdutoAdapter(produtos: List<ProdutoClass>) :
    RecyclerView.Adapter<ProdutoAdapter.ViewHolder>() {

    private val listaOriginal = produtos.toMutableList()
    private var listaExibida = produtos.toMutableList()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nome = view.findViewById<TextView>(R.id.txtNome)
        val preco = view.findViewById<TextView>(R.id.txtPreco)
        val imagem = view.findViewById<ImageView>(R.id.imgProduto)
        val descricao = view.findViewById<TextView>(R.id.txtDescricao)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_produto, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val produto = listaExibida[position]

        holder.nome.text = produto.nome
        holder.preco.text = produto.preco
        holder.imagem.setImageResource(produto.imagem)
        holder.descricao.text = produto.descricao
    }

    override fun getItemCount(): Int {
        return listaExibida.size
    }

    fun filtrar(categoria: String, textoBusca: String) {

        listaExibida = listaOriginal.filter { produto ->

            val categoriaValida =
                categoria == "Todos" ||
                        produto.categoria.equals(categoria, true)

            val nomeValido =
                produto.nome.contains(textoBusca, true)

            categoriaValida && nomeValido

        }.toMutableList()

        notifyDataSetChanged()
    }
}