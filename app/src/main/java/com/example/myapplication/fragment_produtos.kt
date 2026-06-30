package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.produto.CadastroProdutoActivity
import com.example.myapplication.repository.UsuarioRepository
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class fragment_produtos : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(
            R.layout.fragment_produtos,
            container,
            false
        )

        val recycler = view.findViewById<RecyclerView>(R.id.recyclerProdutos)

        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val lista = listOf(
            ProdutoClass("Whey Protein", "R$ 120,00", "disponivel", R.drawable.whey, "Suplementos"),
            ProdutoClass("Creatina", "R$ 80,00", "disponivel", R.drawable.creatina, "Suplementos"),
            ProdutoClass("Pré-treino", "R$ 90,00", "disponivel", R.drawable.pretreino, "Suplementos"),
            ProdutoClass("Gatorade", "R$ 10,00", "disponivel", R.drawable.gatorade, "Bebida"),
            ProdutoClass("Strap", "R$ 70,00", "disponivel", R.drawable.strap, "Acessorio"),
            ProdutoClass("Água", "R$ 6,00", "disponivel", R.drawable.agua, "Bebida")
        )

        val adapter = ProdutoAdapter(lista)
        recycler.adapter = adapter

        val edtBusca = view.findViewById<EditText>(R.id.edtBusca)
        var categoriaSelecionada = "Todos"

        edtBusca.doOnTextChanged { text, _, _, _ ->
            adapter.filtrar(
                categoriaSelecionada,
                text.toString()
            )
        }

        val btnTodos = view.findViewById<Chip>(R.id.chipTodos)
        val btnBebidas = view.findViewById<Chip>(R.id.chipBebidas)
        val btnSuplementos = view.findViewById<Chip>(R.id.chipSuplementos)
        val btnComidas = view.findViewById<Chip>(R.id.chipComidas)
        val btnAcessorios = view.findViewById<Chip>(R.id.chipAcessorios)
        val btnAdicionarProduto = view.findViewById<FloatingActionButton>(R.id.fabAdicionarProduto)

        btnTodos.isChecked = true

        btnTodos.setOnClickListener {
            categoriaSelecionada = "Todos"
            adapter.filtrar(categoriaSelecionada, edtBusca.text.toString())
        }

        btnBebidas.setOnClickListener {
            categoriaSelecionada = "Bebida"
            adapter.filtrar(categoriaSelecionada, edtBusca.text.toString())
        }

        btnSuplementos.setOnClickListener {
            categoriaSelecionada = "Suplementos"
            adapter.filtrar(categoriaSelecionada, edtBusca.text.toString())
        }

        btnAcessorios.setOnClickListener {
            categoriaSelecionada = "Acessorio"
            adapter.filtrar(categoriaSelecionada, edtBusca.text.toString())
        }

        btnComidas.setOnClickListener {
            categoriaSelecionada = "Comida"
            adapter.filtrar(categoriaSelecionada, edtBusca.text.toString())
        }


        btnAdicionarProduto.setOnClickListener {
            startActivity(
                Intent(requireContext(), CadastroProdutoActivity::class.java)
            )
        }

        btnAdicionarProduto.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            val isAdmin = UsuarioRepository().usuarioAtualEhAdmin()
            btnAdicionarProduto.visibility = if (isAdmin) View.VISIBLE else View.GONE
        }

        return view
    }
}