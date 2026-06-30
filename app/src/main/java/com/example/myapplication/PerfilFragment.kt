package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.repository.UsuarioRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PerfilFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_perfil, container, false)

        val btnLogout = view.findViewById<View>(R.id.btnLogout)
        val btnEditarPerfil = view.findViewById<View>(R.id.itemEditarPerfil)
        val btnHistorico = view.findViewById<View>(R.id.itemHistorico)
        val sectionAdmin = view.findViewById<View>(R.id.sectionAdmin)
        val btnPainelAdmin = view.findViewById<View>(R.id.btnPainelAdmin)
        val txtNome = view.findViewById<android.widget.TextView>(R.id.txtNome)

        lifecycleScope.launch {
            val result = UsuarioRepository().buscarUsuarioAtual()
            if (result is com.example.myapplication.util.Resource.Success) {
                val usuario = result.data
                txtNome.text = usuario.nome
                if (usuario.administrador) sectionAdmin.visibility = View.VISIBLE
            }
        }

        btnPainelAdmin.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        btnEditarPerfil.setOnClickListener {
            startActivity(
                Intent(requireContext(), EditProfileActivity::class.java)
            )
        }

        btnHistorico.setOnClickListener {
            val intent = Intent(requireContext(), HistoricoTreinosActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {

            AlertDialog.Builder(requireContext())
                .setTitle("Sair")
                .setMessage("Deseja realmente sair da conta?")
                .setPositiveButton("Sim") { _, _ ->

                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PerfilFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}