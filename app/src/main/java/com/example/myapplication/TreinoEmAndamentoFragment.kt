package com.example.myapplication.ui.treino

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.ExercicioTreinoAdapter
import com.example.myapplication.databinding.FragmentTreinoEmAndamentoBinding
import com.example.myapplication.model.Treino
import com.example.myapplication.util.Resource
import com.example.myapplication.viewmodel.TreinoEmAndamentoViewModel
import kotlinx.coroutines.launch

class TreinoEmAndamentoFragment : Fragment() {

    private var _binding: FragmentTreinoEmAndamentoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TreinoEmAndamentoViewModel by viewModels {
        TreinoEmAndamentoViewModel.Factory()
    }

    private val exercicioAdapter by lazy { ExercicioTreinoAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreinoEmAndamentoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupBotaoFinalizar()

        val treinoId = requireArguments().getString(ARG_TREINO_ID)!!
        viewModel.carregarTreino(treinoId)
        observarTreino()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val nomeProvisorio = arguments?.getString(ARG_TREINO_NOME) ?: "Treino"
        binding.tvTitulo.text = nomeProvisorio
    }

    private fun setupRecyclerView() {
        binding.recyclerExercicios.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerExercicios.adapter = exercicioAdapter
        binding.recyclerExercicios.isNestedScrollingEnabled = false
    }

    private fun setupBotaoFinalizar() {
        binding.btnFinalizarTreino.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun observarTreino() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.treino.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> mostrarLoading()
                        is Resource.Success -> mostrarTreino(resource.data)
                        is Resource.Error   -> mostrarErro(resource.message)
                    }
                }
            }
        }
    }

    private fun mostrarLoading() {
        binding.progressBar.visibility        = View.VISIBLE
        binding.layoutConteudo.visibility     = View.GONE
        binding.tvErro.visibility             = View.GONE
        binding.containerBtnFinalizar.visibility = View.GONE
    }

    private fun mostrarTreino(treino: Treino) {
        binding.progressBar.visibility        = View.GONE
        binding.tvErro.visibility             = View.GONE
        binding.layoutConteudo.visibility     = View.VISIBLE
        binding.containerBtnFinalizar.visibility = View.VISIBLE

        binding.tvTitulo.text       = treino.nome
        binding.tvNomeTreino.text   = treino.nome
        binding.tvGrupoMuscular.text = treino.grupoMuscular
            .takeIf { it.isNotBlank() } ?: "Geral"

        val total = treino.totalExercicios
        binding.tvTotalExercicios.text = when (total) {
            0    -> "Nenhum exercício"
            1    -> "1 exercício"
            else -> "$total exercícios"
        }

        exercicioAdapter.submitList(treino.exercicios)

        if (treino.exercicios.isEmpty()) {
            binding.tvSemExercicios.visibility  = View.VISIBLE
            binding.recyclerExercicios.visibility = View.GONE
        } else {
            binding.tvSemExercicios.visibility  = View.GONE
            binding.recyclerExercicios.visibility = View.VISIBLE
        }
    }

    private fun mostrarErro(mensagem: String) {
        binding.progressBar.visibility        = View.GONE
        binding.layoutConteudo.visibility     = View.GONE
        binding.containerBtnFinalizar.visibility = View.GONE
        binding.tvErro.visibility             = View.VISIBLE
        binding.tvErro.text                   = mensagem
    }

    companion object {
        private const val ARG_TREINO_ID   = "treino_id"
        private const val ARG_TREINO_NOME = "treino_nome"

        fun newInstance(treinoId: String, treinoNome: String): TreinoEmAndamentoFragment {
            return TreinoEmAndamentoFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TREINO_ID,   treinoId)
                    putString(ARG_TREINO_NOME, treinoNome)
                }
            }
        }
    }
}
