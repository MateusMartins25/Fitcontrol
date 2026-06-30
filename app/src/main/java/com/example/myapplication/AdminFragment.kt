package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.produto.CadastroProdutoActivity
import com.example.myapplication.ui.exercicio.CadastroExercicioActivity
import com.example.myapplication.ui.treino.CadastroTreinoActivity
import com.example.myapplication.ui.usuario.CadastroUsuarioActivity

class AdminFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin, container, false)

        view.findViewById<View>(R.id.btnCadastrarProduto).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroProdutoActivity::class.java))
        }

        view.findViewById<View>(R.id.btnCadastrarExercicio).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroExercicioActivity::class.java))
        }

        view.findViewById<View>(R.id.btnCadastrarTreino).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroTreinoActivity::class.java))
        }

        view.findViewById<View>(R.id.btnCadastrarUsuario).setOnClickListener {
            startActivity(Intent(requireContext(), CadastroUsuarioActivity::class.java))
        }

        return view
    }

    companion object {
        fun newInstance() = AdminFragment()
    }
}
