package com.example.myapplication.ui.exercicio

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.model.Exercicio
import com.example.myapplication.util.Resource
import com.example.myapplication.viewmodel.CadastroExercicioViewModel
import kotlinx.coroutines.launch

class CadastroExercicioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_EXERCICIO_ID = "extra_exercicio_id"
    }

    private val viewModel: CadastroExercicioViewModel by viewModels {
        CadastroExercicioViewModel.Factory()
    }

    private lateinit var toolbar: Toolbar
    private lateinit var btnBack: ImageButton
    private lateinit var btnMore: ImageButton

    private lateinit var etSearchExercicio: EditText
    private lateinit var cardExercicioEncontrado: LinearLayout
    private lateinit var tvExercicioNome: TextView
    private lateinit var tvExercicioMeta: TextView

    private lateinit var etNomeExercicio: EditText

    private lateinit var chipPeito: TextView
    private lateinit var chipCostas: TextView
    private lateinit var chipBiceps: TextView
    private lateinit var chipTriceps: TextView
    private lateinit var chipOmbro: TextView
    private lateinit var chipPernas: TextView
    private lateinit var chipGluteos: TextView
    private lateinit var chipAbdomen: TextView

    private lateinit var equipBarra: LinearLayout
    private lateinit var equipHalter: LinearLayout
    private lateinit var equipMaquina: LinearLayout
    private lateinit var equipLivre: LinearLayout

    private lateinit var etSeries: EditText
    private lateinit var etRepeticoes: EditText
    private lateinit var etCarga: EditText
    private lateinit var etDescanso: EditText

    private lateinit var btnIniciante: LinearLayout
    private lateinit var btnMedio: LinearLayout
    private lateinit var btnAvancado: LinearLayout
    private lateinit var tvIniciante: TextView
    private lateinit var tvMedio: TextView
    private lateinit var tvAvancado: TextView

    private lateinit var etInstrucoes: EditText

    private lateinit var btnSalvarExercicio: Button

    private var exercicioEmEdicaoId: String = ""
    private val gruposSelecionados = mutableSetOf<String>()
    private var equipamentoSelecionado: String = ""
    private var dificuldadeSelecionada: String = Exercicio.DIFICULDADE_INICIANTE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_exercicio)

        bindViews()
        setupToolbar()
        setupBusca()
        setupGruposMusculares()
        setupEquipamentos()
        setupDificuldade()
        setupSaveButton()
        observarViewModel()

        exercicioEmEdicaoId = intent.getStringExtra(EXTRA_EXERCICIO_ID).orEmpty()
        if (exercicioEmEdicaoId.isNotBlank()) {
            viewModel.carregarExercicio(exercicioEmEdicaoId)
        }
    }

    private fun bindViews() {
        toolbar                  = findViewById(R.id.toolbar)
        btnBack                  = findViewById(R.id.btnBack)
        btnMore                  = findViewById(R.id.btnMore)
        etSearchExercicio        = findViewById(R.id.etSearchExercicio)
        cardExercicioEncontrado  = findViewById(R.id.cardExercicioEncontrado)
        tvExercicioNome          = findViewById(R.id.tvExercicioNome)
        tvExercicioMeta          = findViewById(R.id.tvExercicioMeta)
        etNomeExercicio          = findViewById(R.id.etNomeExercicio)
        chipPeito                = findViewById(R.id.chipPeito)
        chipCostas               = findViewById(R.id.chipCostas)
        chipBiceps               = findViewById(R.id.chipBiceps)
        chipTriceps              = findViewById(R.id.chipTriceps)
        chipOmbro                = findViewById(R.id.chipOmbro)
        chipPernas               = findViewById(R.id.chipPernas)
        chipGluteos              = findViewById(R.id.chipGluteos)
        chipAbdomen              = findViewById(R.id.chipAbdomen)
        equipBarra               = findViewById(R.id.equipBarra)
        equipHalter              = findViewById(R.id.equipHalter)
        equipMaquina             = findViewById(R.id.equipMaquina)
        equipLivre               = findViewById(R.id.equipLivre)
        etSeries                 = findViewById(R.id.etSeries)
        etRepeticoes             = findViewById(R.id.etRepeticoes)
        etCarga                  = findViewById(R.id.etCarga)
        etDescanso               = findViewById(R.id.etDescanso)
        btnIniciante             = findViewById(R.id.btnIniciante)
        btnMedio                 = findViewById(R.id.btnMedio)
        btnAvancado              = findViewById(R.id.btnAvancado)
        tvIniciante              = findViewById(R.id.tvIniciante)
        tvMedio                  = findViewById(R.id.tvMedio)
        tvAvancado               = findViewById(R.id.tvAvancado)
        etInstrucoes             = findViewById(R.id.etInstrucoes)
        btnSalvarExercicio       = findViewById(R.id.btnSalvarExercicio)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnMore.setOnClickListener { mostrarMenuOpcoes() }
    }

    private fun mostrarMenuOpcoes() {
        val opcoes = if (exercicioEmEdicaoId.isNotBlank())
            arrayOf("Limpar formulário", "Excluir exercício")
        else arrayOf("Limpar formulário")

        AlertDialog.Builder(this)
            .setItems(opcoes) { _, which ->
                when {
                    which == 0 -> limparFormulario()
                    which == 1 -> confirmarExclusao()
                }
            }.show()
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir exercício")
            .setMessage("Tem certeza? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ -> viewModel.deletarExercicio(exercicioEmEdicaoId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupBusca() {
        cardExercicioEncontrado.visibility = View.GONE

        etSearchExercicio.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s.toString().trim())
            }
        })

        cardExercicioEncontrado.setOnClickListener {
            val resultado = (viewModel.resultadoBusca.value as? Resource.Success)?.data
            resultado?.firstOrNull()?.let { preencherFormulario(it) }
        }
    }

    private fun setupGruposMusculares() {
        val chips = mapOf(
            chipPeito   to "Peito",
            chipCostas  to "Costas",
            chipBiceps  to "Bíceps",
            chipTriceps to "Tríceps",
            chipOmbro   to "Ombro",
            chipPernas  to "Pernas",
            chipGluteos to "Glúteos",
            chipAbdomen to "Abdômen"
        )

        chips.forEach { (chip, grupo) ->
            chip.setOnClickListener {
                if (gruposSelecionados.contains(grupo)) {
                    gruposSelecionados.remove(grupo)
                    chip.setBackgroundResource(R.drawable.bg_chip_normal)
                    chip.setTextColor(getColor(R.color.ex_text_secondary))
                } else {
                    gruposSelecionados.add(grupo)
                    chip.setBackgroundResource(R.drawable.bg_chip_selected)
                    chip.setTextColor(getColor(R.color.ex_accent))
                }
            }
        }
    }

    private fun atualizarChipsGrupo(grupos: List<String>) {
        gruposSelecionados.clear()
        gruposSelecionados.addAll(grupos)

        val chips = mapOf(
            chipPeito   to "Peito",
            chipCostas  to "Costas",
            chipBiceps  to "Bíceps",
            chipTriceps to "Tríceps",
            chipOmbro   to "Ombro",
            chipPernas  to "Pernas",
            chipGluteos to "Glúteos",
            chipAbdomen to "Abdômen"
        )

        chips.forEach { (chip, grupo) ->
            val sel = grupos.contains(grupo)
            chip.setBackgroundResource(if (sel) R.drawable.bg_chip_selected else R.drawable.bg_chip_normal)
            chip.setTextColor(getColor(if (sel) R.color.ex_accent else R.color.ex_text_secondary))
        }
    }

    private fun setupEquipamentos() {
        val equipamentos = mapOf(
            equipBarra   to Exercicio.EQUIP_BARRA,
            equipHalter  to Exercicio.EQUIP_HALTER,
            equipMaquina to Exercicio.EQUIP_MAQUINA,
            equipLivre   to Exercicio.EQUIP_LIVRE
        )

        equipamentos.forEach { (view, equip) ->
            view.setOnClickListener {
                equipamentoSelecionado = equip
                atualizarEquipamentoUI(equip)
            }
        }
    }

    private fun atualizarEquipamentoUI(equipSelecionado: String) {
        val iconIds = mapOf(
            equipBarra   to R.id.ivIconBarra,
            equipHalter  to R.id.ivIconHalter,
            equipMaquina to R.id.ivIconMaquina,
            equipLivre   to R.id.ivIconLivre
        )
        val labelIds = mapOf(
            equipBarra   to R.id.tvLabelBarra,
            equipHalter  to R.id.tvLabelHalter,
            equipMaquina to R.id.tvLabelMaquina,
            equipLivre   to R.id.tvLabelLivre
        )
        val nomes = mapOf(
            equipBarra   to Exercicio.EQUIP_BARRA,
            equipHalter  to Exercicio.EQUIP_HALTER,
            equipMaquina to Exercicio.EQUIP_MAQUINA,
            equipLivre   to Exercicio.EQUIP_LIVRE
        )

        listOf(equipBarra, equipHalter, equipMaquina, equipLivre).forEach { container ->
            val isSel = nomes[container] == equipSelecionado
            val iconView  = container.findViewById<android.widget.ImageView>(iconIds[container]!!)
            val labelView = container.findViewById<TextView>(labelIds[container]!!)

            (iconView.parent as? android.widget.FrameLayout)?.setBackgroundResource(
                if (isSel) R.drawable.bg_equip_icon_selected else R.drawable.bg_equip_icon
            )
            iconView.setColorFilter(
                getColor(if (isSel) R.color.ex_accent else R.color.ex_text_secondary)
            )
            labelView.setTextColor(
                getColor(if (isSel) R.color.ex_accent else R.color.ex_text_secondary)
            )
        }
    }

    private fun setupDificuldade() {
        atualizarDificuldadeUI(Exercicio.DIFICULDADE_INICIANTE)

        btnIniciante.setOnClickListener {
            dificuldadeSelecionada = Exercicio.DIFICULDADE_INICIANTE
            atualizarDificuldadeUI(Exercicio.DIFICULDADE_INICIANTE)
        }
        btnMedio.setOnClickListener {
            dificuldadeSelecionada = Exercicio.DIFICULDADE_MEDIO
            atualizarDificuldadeUI(Exercicio.DIFICULDADE_MEDIO)
        }
        btnAvancado.setOnClickListener {
            dificuldadeSelecionada = Exercicio.DIFICULDADE_AVANCADO
            atualizarDificuldadeUI(Exercicio.DIFICULDADE_AVANCADO)
        }
    }

    private fun atualizarDificuldadeUI(dif: String) {
        btnIniciante.setBackgroundResource(R.drawable.bg_dif_unselected)
        btnMedio.setBackgroundResource(R.drawable.bg_dif_unselected)
        btnAvancado.setBackgroundResource(R.drawable.bg_dif_unselected)
        tvIniciante.setTextColor(getColor(R.color.ex_text_secondary))
        tvMedio.setTextColor(getColor(R.color.ex_text_secondary))
        tvAvancado.setTextColor(getColor(R.color.ex_text_secondary))

        when (dif) {
            Exercicio.DIFICULDADE_INICIANTE -> {
                btnIniciante.setBackgroundResource(R.drawable.bg_dif_iniciante_selected)
                tvIniciante.setTextColor(getColor(R.color.ex_green))
            }
            Exercicio.DIFICULDADE_MEDIO -> {
                btnMedio.setBackgroundResource(R.drawable.bg_dif_medio_selected)
                tvMedio.setTextColor(getColor(R.color.ex_yellow))
            }
            Exercicio.DIFICULDADE_AVANCADO -> {
                btnAvancado.setBackgroundResource(R.drawable.bg_dif_avancado_selected)
                tvAvancado.setTextColor(getColor(R.color.ex_red))
            }
        }
    }

    private fun setupSaveButton() {
        btnSalvarExercicio.setOnClickListener {
            if (validarCampos()) {
                viewModel.salvarExercicio(construirExercicio())
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nome = etNomeExercicio.text.toString().trim()

        if (nome.isEmpty()) {
            etNomeExercicio.error = "Informe o nome do exercício"
            etNomeExercicio.requestFocus(); return false
        }
        if (gruposSelecionados.isEmpty()) {
            Toast.makeText(this, "Selecione ao menos um grupo muscular", Toast.LENGTH_SHORT).show()
            return false
        }
        if (equipamentoSelecionado.isEmpty()) {
            Toast.makeText(this, "Selecione o equipamento", Toast.LENGTH_SHORT).show()
            return false
        }
        if (etSeries.text.toString().trim().isEmpty()) {
            etSeries.error = "Informe as séries"
            etSeries.requestFocus(); return false
        }
        if (etRepeticoes.text.toString().trim().isEmpty()) {
            etRepeticoes.error = "Informe as repetições"
            etRepeticoes.requestFocus(); return false
        }
        return true
    }

    private fun construirExercicio(): Exercicio {
        return Exercicio(
            id               = exercicioEmEdicaoId,
            nome             = etNomeExercicio.text.toString().trim(),
            gruposMusculares = gruposSelecionados.toList(),
            equipamento      = equipamentoSelecionado,
            series           = etSeries.text.toString().trim().toIntOrNull() ?: 0,
            repeticoes       = etRepeticoes.text.toString().trim().toIntOrNull() ?: 0,
            carga            = etCarga.text.toString().trim().replace(",", ".").toDoubleOrNull() ?: 0.0,
            descanso         = etDescanso.text.toString().trim().toIntOrNull() ?: 0,
            dificuldade      = dificuldadeSelecionada,
            instrucoes       = etInstrucoes.text.toString().trim()
        )
    }

    private fun preencherFormulario(exercicio: Exercicio) {
        exercicioEmEdicaoId = exercicio.id
        etNomeExercicio.setText(exercicio.nome)
        atualizarChipsGrupo(exercicio.gruposMusculares)
        equipamentoSelecionado = exercicio.equipamento
        atualizarEquipamentoUI(exercicio.equipamento)
        etSeries.setText(if (exercicio.series > 0) exercicio.series.toString() else "")
        etRepeticoes.setText(if (exercicio.repeticoes > 0) exercicio.repeticoes.toString() else "")
        etCarga.setText(if (exercicio.carga > 0) exercicio.carga.toString() else "")
        etDescanso.setText(if (exercicio.descanso > 0) exercicio.descanso.toString() else "")
        dificuldadeSelecionada = exercicio.dificuldade
        atualizarDificuldadeUI(exercicio.dificuldade)
        etInstrucoes.setText(exercicio.instrucoes)
        etSearchExercicio.setText("")
        cardExercicioEncontrado.visibility = View.GONE
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.salvarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                btnSalvarExercicio.isEnabled = false
                                btnSalvarExercicio.text = "Salvando…"
                            }
                            is Resource.Success -> {
                                btnSalvarExercicio.isEnabled = true
                                btnSalvarExercicio.text = getString(R.string.btn_salvar_exercicio)
                                Toast.makeText(
                                    this@CadastroExercicioActivity,
                                    if (exercicioEmEdicaoId.isNotBlank()) "Exercício atualizado!" else "Exercício cadastrado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> {
                                btnSalvarExercicio.isEnabled = true
                                btnSalvarExercicio.text = getString(R.string.btn_salvar_exercicio)
                                Toast.makeText(this@CadastroExercicioActivity, resource.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.exercicioEditando.collect { exercicio ->
                        exercicio?.let { preencherFormulario(it) }
                    }
                }

                launch {
                    viewModel.resultadoBusca.collect { resource ->
                        if (resource is Resource.Success && resource.data.isNotEmpty()
                            && etSearchExercicio.text.length >= 2) {

                            val primeiro = resource.data.first()
                            cardExercicioEncontrado.visibility = View.VISIBLE
                            tvExercicioNome.text = primeiro.nome
                            tvExercicioMeta.text = buildString {
                                append(primeiro.gruposMusculares.firstOrNull() ?: "")
                                if (primeiro.dificuldade.isNotBlank()) {
                                    append(" · ${primeiro.dificuldade}")
                                }
                            }
                        } else {
                            cardExercicioEncontrado.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.deletarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Toast.makeText(this@CadastroExercicioActivity, "Exercício excluído.", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> Toast.makeText(this@CadastroExercicioActivity, resource.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun limparFormulario() {
        exercicioEmEdicaoId = ""
        etNomeExercicio.text?.clear()
        etSeries.text?.clear()
        etRepeticoes.text?.clear()
        etCarga.text?.clear()
        etDescanso.text?.clear()
        etInstrucoes.text?.clear()
        etSearchExercicio.text?.clear()
        cardExercicioEncontrado.visibility = View.GONE

        atualizarChipsGrupo(emptyList())
        equipamentoSelecionado = ""
        atualizarEquipamentoUI("")
        dificuldadeSelecionada = Exercicio.DIFICULDADE_INICIANTE
        atualizarDificuldadeUI(Exercicio.DIFICULDADE_INICIANTE)
    }
}
