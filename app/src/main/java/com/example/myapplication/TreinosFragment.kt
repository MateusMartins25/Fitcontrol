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
import com.example.myapplication.adapter.TreinosAdapter
import com.example.myapplication.databinding.FragmentTreinosBinding
import com.example.myapplication.model.Treino
import com.example.myapplication.util.Resource
import com.example.myapplication.viewmodel.TreinosViewModel
import kotlinx.coroutines.launch

class TreinosFragment : Fragment() {

    private var _binding: FragmentTreinosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TreinosViewModel by viewModels { TreinosViewModel.Factory() }

    private val adapter by lazy {
        TreinosAdapter { treino -> abrirTreino(treino) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTreinosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observarTreinos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        binding.recyclerTreinos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerTreinos.adapter = adapter
    }

    private fun observarTreinos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.treinos.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> mostrarLoading()
                        is Resource.Success -> mostrarTreinos(resource.data)
                        is Resource.Error   -> mostrarErro(resource.message)
                    }
                }
            }
        }
    }

    private fun mostrarLoading() {
        binding.progressBar.visibility    = View.VISIBLE
        binding.recyclerTreinos.visibility = View.GONE
        binding.layoutVazio.visibility    = View.GONE
        binding.tvErro.visibility         = View.GONE
    }

    private fun mostrarTreinos(treinos: List<Treino>) {
        binding.progressBar.visibility = View.GONE
        binding.tvErro.visibility      = View.GONE

        if (treinos.isEmpty()) {
            binding.recyclerTreinos.visibility = View.GONE
            binding.layoutVazio.visibility     = View.VISIBLE
        } else {
            binding.layoutVazio.visibility     = View.GONE
            binding.recyclerTreinos.visibility = View.VISIBLE
            adapter.submitList(treinos)
        }
    }

    private fun mostrarErro(mensagem: String) {
        binding.progressBar.visibility    = View.GONE
        binding.recyclerTreinos.visibility = View.GONE
        binding.layoutVazio.visibility    = View.GONE
        binding.tvErro.visibility         = View.VISIBLE
        binding.tvErro.text               = mensagem
    }

    private fun abrirTreino(treino: Treino) {
        val fragment = TreinoEmAndamentoFragment.newInstance(treino.id, treino.nome)
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_left,
                R.anim.slide_in_left,
                R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    companion object {
        fun newInstance() = TreinosFragment()
    }
}
