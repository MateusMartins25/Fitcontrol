package com.example.myapplication.ui.treino

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.SelecaoExercicioAdapter
import com.example.myapplication.databinding.ActivityCadastroTreinoBinding
import com.example.myapplication.databinding.DialogAdicionarExercicioTreinoBinding
import com.example.myapplication.model.Exercicio
import com.example.myapplication.model.ExercicioTreino
import com.example.myapplication.model.Treino
import com.example.myapplication.model.Usuario
import com.example.myapplication.repository.ExercicioRepository
import com.example.myapplication.repository.TreinoRepository
import com.example.myapplication.repository.UsuarioRepository
import com.example.myapplication.util.Resource
import kotlinx.coroutines.launch

class CadastroTreinoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCadastroTreinoBinding

    private val usuarioRepository = UsuarioRepository()
    private val exercicioRepository = ExercicioRepository()
    private val treinoRepository = TreinoRepository()

    private var listaUsuarios: List<Usuario> = emptyList()
    private var listaExerciciosCatalogo: List<Exercicio> = emptyList()
    private var usuarioSelecionadoId: String? = null

    private val exerciciosAdicionados = mutableListOf<ExercicioTreino>()

    private lateinit var selecaoAdapter: SelecaoExercicioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCadastroTreinoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupListeners()
        carregarUsuarios()
        carregarExerciciosCatalogo()
    }

    private fun setupRecyclerView() {
        selecaoAdapter = SelecaoExercicioAdapter(exerciciosAdicionados) { position ->
            confirmarRemocao(position)
        }
        binding.recyclerExercicios.layoutManager = LinearLayoutManager(this)
        binding.recyclerExercicios.adapter = selecaoAdapter
        atualizarVisibilidadeListaVazia()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.spinnerUsuario.setOnItemSelectedListener { position ->
            usuarioSelecionadoId = listaUsuarios.getOrNull(position)?.id
        }

        binding.btnAdicionarExercicio.setOnClickListener {
            abrirDialogAdicionarExercicio()
        }

        binding.btnSalvarTreino.setOnClickListener {
            salvarTreino()
        }
    }

    private fun android.widget.AdapterView<*>.setOnItemSelectedListener(onSelected: (Int) -> Unit) {
        this.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onSelected(position)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun carregarUsuarios() {
        lifecycleScope.launch {
            usuarioRepository.listarUsuarios().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        listaUsuarios = resource.data
                        val nomes = listaUsuarios.map {
                            if (it.email.isNotBlank()) "${it.nome} (${it.email})" else it.nome
                        }
                        val adapter = ArrayAdapter(this@CadastroTreinoActivity, android.R.layout.simple_spinner_item, nomes)
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerUsuario.adapter = adapter
                        usuarioSelecionadoId = listaUsuarios.firstOrNull()?.id
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@CadastroTreinoActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun carregarExerciciosCatalogo() {
        lifecycleScope.launch {
            exercicioRepository.listarExercicios().collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        listaExerciciosCatalogo = resource.data
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@CadastroTreinoActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Loading -> Unit
                }
            }
        }
    }

    private fun abrirDialogAdicionarExercicio() {
        if (listaExerciciosCatalogo.isEmpty()) {
            Toast.makeText(this, "Nenhum exercício cadastrado no catálogo.", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogBinding = DialogAdicionarExercicioTreinoBinding.inflate(LayoutInflater.from(this))

        val nomes = listaExerciciosCatalogo.map { it.nome }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, nomes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spinnerExercicio.adapter = adapter

        fun preencherCampos(exercicio: Exercicio) {
            dialogBinding.etSeriesDialog.setText(exercicio.series.toString())
            dialogBinding.etRepeticoesDialog.setText(exercicio.repeticoes.toString())
            dialogBinding.etCargaDialog.setText(exercicio.carga.toString())
            dialogBinding.etDescansoDialog.setText(exercicio.descanso.toString())
        }

        preencherCampos(listaExerciciosCatalogo.first())

        dialogBinding.spinnerExercicio.setOnItemSelectedListener { position ->
            listaExerciciosCatalogo.getOrNull(position)?.let { preencherCampos(it) }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnAdicionarExercicioDialog.setOnClickListener {
            val position = dialogBinding.spinnerExercicio.selectedItemPosition
            val exercicioCatalogo = listaExerciciosCatalogo.getOrNull(position)
            if (exercicioCatalogo == null) {
                Toast.makeText(this, "Selecione um exercício.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val series = dialogBinding.etSeriesDialog.text.toString().toIntOrNull() ?: 0
            val repeticoes = dialogBinding.etRepeticoesDialog.text.toString().toIntOrNull() ?: 0
            val carga = dialogBinding.etCargaDialog.text.toString().toDoubleOrNull() ?: 0.0
            val descanso = dialogBinding.etDescansoDialog.text.toString().toIntOrNull() ?: 0
            val observacoes = dialogBinding.etObservacoesDialog.text.toString().trim()

            exerciciosAdicionados.add(
                ExercicioTreino(
                    exercicioId = exercicioCatalogo.id,
                    nome = exercicioCatalogo.nome,
                    series = series,
                    repeticoes = repeticoes,
                    carga = carga,
                    descanso = descanso,
                    observacoes = observacoes
                )
            )
            selecaoAdapter.notifyItemInserted(exerciciosAdicionados.size - 1)
            atualizarVisibilidadeListaVazia()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun confirmarRemocao(position: Int) {
        if (position < 0 || position >= exerciciosAdicionados.size) return

        AlertDialog.Builder(this)
            .setMessage("Remover este exercício?")
            .setPositiveButton("Remover") { _, _ ->
                exerciciosAdicionados.removeAt(position)
                selecaoAdapter.notifyItemRemoved(position)
                selecaoAdapter.notifyItemRangeChanged(position, exerciciosAdicionados.size)
                atualizarVisibilidadeListaVazia()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun atualizarVisibilidadeListaVazia() {
        binding.tvListaVazia.visibility = if (exerciciosAdicionados.isEmpty())
            android.view.View.VISIBLE else android.view.View.GONE
        binding.recyclerExercicios.visibility = if (exerciciosAdicionados.isEmpty())
            android.view.View.GONE else android.view.View.VISIBLE
    }

    private fun salvarTreino() {
        val uid = usuarioSelecionadoId
        val nome = binding.etNomeTreino.text.toString().trim()
        val grupoMuscular = binding.etGrupoMuscular.text.toString().trim()

        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Selecione um aluno.", Toast.LENGTH_SHORT).show()
            return
        }
        if (nome.isBlank()) {
            Toast.makeText(this, "Informe o nome do treino.", Toast.LENGTH_SHORT).show()
            return
        }
        if (exerciciosAdicionados.isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um exercício.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSalvarTreino.isEnabled = false
        binding.btnSalvarTreino.text = "Salvando..."

        lifecycleScope.launch {
            val treino = Treino(
                nome = nome,
                grupoMuscular = grupoMuscular,
                exercicios = exerciciosAdicionados.toList()
            )

            when (val resultado = treinoRepository.inserirTreino(uid, treino)) {
                is Resource.Success -> {
                    Toast.makeText(this@CadastroTreinoActivity, "Treino criado!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    Toast.makeText(this@CadastroTreinoActivity, resultado.message, Toast.LENGTH_SHORT).show()
                    binding.btnSalvarTreino.isEnabled = true
                    binding.btnSalvarTreino.text = "Salvar Treino"
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
