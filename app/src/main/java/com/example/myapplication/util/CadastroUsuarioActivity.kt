package com.example.myapplication.ui.usuario

import android.app.Activity
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.example.myapplication.R
import com.example.myapplication.model.Usuario
import com.example.myapplication.util.Resource
import com.example.myapplication.util.TelefoneMaskWatcher
import com.example.myapplication.viewmodel.CadastroUsuarioViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

/**
 * Tela de Cadastro / Edição de Usuário.
 *
 * Modos:
 *  - Inserção → intent sem extras
 *  - Edição   → intent com EXTRA_USUARIO_ID
 */
class CadastroUsuarioActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_USUARIO_ID = "extra_usuario_id"
    }

    // ─── ViewModel ────────────────────────────────────────────────────────────
    private val viewModel: CadastroUsuarioViewModel by viewModels {
        CadastroUsuarioViewModel.Factory()
    }

    // ─── Views ────────────────────────────────────────────────────────────────
    private lateinit var toolbar: Toolbar
    private lateinit var btnBack: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var containerAvatar: FrameLayout
    private lateinit var ivAvatarPlaceholder: ImageView
    private lateinit var ivAvatar: ImageView
    private lateinit var etNome: android.widget.EditText
    private lateinit var etTelefone: android.widget.EditText
    private lateinit var etEmail: android.widget.EditText
    private lateinit var containerDataNascimento: LinearLayout
    private lateinit var tvDataNascimento: TextView
    private lateinit var cardAdministrador: LinearLayout
    private lateinit var switchAdministrador: SwitchMaterial
    private lateinit var tvAdminDescricao: TextView
    private lateinit var btnSalvarUsuario: Button

    // ─── Estado ───────────────────────────────────────────────────────────────
    private var fotoUri: Uri? = null
    private var cameraUri: Uri? = null
    private var usuarioEmEdicaoId: String = ""
    private var dataNascimentoSelecionada: String = ""

    // ─── Launchers ────────────────────────────────────────────────────────────
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { setFotoSelecionada(it) }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) cameraUri?.let { setFotoSelecionada(it) }
        }

    // ─────────────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        bindViews()
        setupToolbar()
        setupAvatar()
        setupTelefoneMask()
        setupDataNascimento()
        setupAdminSwitch()
        setupSaveButton()
        observarViewModel()

        usuarioEmEdicaoId = intent.getStringExtra(EXTRA_USUARIO_ID).orEmpty()
        if (usuarioEmEdicaoId.isNotBlank()) {
            viewModel.carregarUsuario(usuarioEmEdicaoId)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bind views
    // ─────────────────────────────────────────────────────────────────────────

    private fun bindViews() {
        toolbar                   = findViewById(R.id.toolbar)
        btnBack                   = findViewById(R.id.btnBack)
        btnMore                   = findViewById(R.id.btnMore)
        containerAvatar           = findViewById(R.id.containerAvatar)
        ivAvatarPlaceholder       = findViewById(R.id.ivAvatarPlaceholder)
        ivAvatar                  = findViewById(R.id.ivAvatar)
        etNome                    = findViewById(R.id.etNome)
        etTelefone                = findViewById(R.id.etTelefone)
        etEmail                   = findViewById(R.id.etEmail)
        containerDataNascimento   = findViewById(R.id.containerDataNascimento)
        tvDataNascimento          = findViewById(R.id.tvDataNascimento)
        cardAdministrador         = findViewById(R.id.cardAdministrador)
        switchAdministrador       = findViewById(R.id.switchAdministrador)
        tvAdminDescricao          = findViewById(R.id.tvAdminDescricao)
        btnSalvarUsuario          = findViewById(R.id.btnSalvarUsuario)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Toolbar
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnMore.setOnClickListener { mostrarMenuOpcoes() }
    }

    private fun mostrarMenuOpcoes() {
        val opcoes = if (usuarioEmEdicaoId.isNotBlank())
            arrayOf("Limpar formulário", "Excluir usuário")
        else
            arrayOf("Limpar formulário")

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
            .setTitle("Excluir usuário")
            .setMessage("Tem certeza? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ -> viewModel.deletarUsuario(usuarioEmEdicaoId) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Avatar
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupAvatar() {
        containerAvatar.setOnClickListener { mostrarDialogFotoAvatar() }
    }

    private fun mostrarDialogFotoAvatar() {
        AlertDialog.Builder(this)
            .setTitle("Foto do usuário")
            .setItems(arrayOf("Galeria", "Câmera")) { _, which ->
                if (which == 0) pickImageLauncher.launch("image/*") else launchCamera()
            }.show()
    }

    private fun launchCamera() {
        val file = File(cacheDir, "usuario_${System.currentTimeMillis()}.jpg")
        val uri  = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    private fun setFotoSelecionada(uri: Uri) {
        fotoUri = uri
        Glide.with(this).load(uri).transform(CircleCrop()).into(ivAvatar)
        ivAvatar.visibility            = View.VISIBLE
        ivAvatarPlaceholder.visibility = View.GONE
    }

    private fun carregarFotoUrl(url: String) {
        if (url.isBlank()) return
        Glide.with(this).load(url).transform(CircleCrop()).into(ivAvatar)
        ivAvatar.visibility            = View.VISIBLE
        ivAvatarPlaceholder.visibility = View.GONE
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Máscara de telefone
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupTelefoneMask() {
        etTelefone.addTextChangedListener(TelefoneMaskWatcher(etTelefone))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DatePicker para data de nascimento
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupDataNascimento() {
        containerDataNascimento.setOnClickListener { abrirDatePicker() }
        tvDataNascimento.setOnClickListener { abrirDatePicker() }
    }

    private fun abrirDatePicker() {
        val cal = Calendar.getInstance()

        // Se já tem data, pré-seleciona no picker
        if (dataNascimentoSelecionada.isNotBlank()) {
            try {
                val parts = dataNascimentoSelecionada.split("/")
                cal.set(parts[2].toInt(), parts[1].toInt() - 1, parts[0].toInt())
            } catch (_: Exception) { }
        }

        DatePickerDialog(
            this,
            R.style.DatePickerTheme,
            { _, year, month, day ->
                dataNascimentoSelecionada = "%02d/%02d/%04d".format(day, month + 1, year)
                tvDataNascimento.text = dataNascimentoSelecionada
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Impede seleção de datas futuras
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Switch Administrador
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupAdminSwitch() {
        atualizarCardAdmin(isAdmin = false)

        switchAdministrador.setOnCheckedChangeListener { _, isChecked ->
            atualizarCardAdmin(isAdmin = isChecked)
        }

        // Clique no card inteiro também alterna o switch
        cardAdministrador.setOnClickListener {
            switchAdministrador.isChecked = !switchAdministrador.isChecked
        }
    }

    private fun atualizarCardAdmin(isAdmin: Boolean) {
        cardAdministrador.background = getDrawable(
            if (isAdmin) R.drawable.bg_card_admin_active else R.drawable.bg_card
        )
        tvAdminDescricao.text = if (isAdmin)
            "Acesso total ao sistema habilitado"
        else
            "Sem acesso administrativo"

        tvAdminDescricao.setTextColor(
            getColor(if (isAdmin) R.color.accent_yellow else R.color.text_secondary)
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Salvar
    // ─────────────────────────────────────────────────────────────────────────

    private fun setupSaveButton() {
        btnSalvarUsuario.setOnClickListener {
            if (validarCampos()) {
                viewModel.salvarUsuario(construirUsuario(), fotoUri)
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nome     = etNome.text.toString().trim()
        val telefone = etTelefone.text.toString().trim()
        val email    = etEmail.text.toString().trim()

        if (nome.isEmpty()) {
            etNome.error = "Informe o nome completo"
            etNome.requestFocus(); return false
        }
        if (telefone.length < 14) {
            etTelefone.error = "Telefone inválido"
            etTelefone.requestFocus(); return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "E-mail inválido"
            etEmail.requestFocus(); return false
        }
        if (dataNascimentoSelecionada.isEmpty()) {
            Toast.makeText(this, "Selecione a data de nascimento", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun construirUsuario(): Usuario {
        val fotoUrlAtual = viewModel.usuarioEditando.value?.fotoUrl.orEmpty()
        return Usuario(
            id              = usuarioEmEdicaoId,
            nome            = etNome.text.toString().trim(),
            telefone        = etTelefone.text.toString().trim(),
            email           = etEmail.text.toString().trim(),
            dataNascimento  = dataNascimentoSelecionada,
            administrador   = switchAdministrador.isChecked,
            fotoUrl         = fotoUrlAtual
        )
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Observar ViewModel
    // ─────────────────────────────────────────────────────────────────────────

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Resultado do salvamento
                launch {
                    viewModel.salvarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                btnSalvarUsuario.isEnabled = false
                                btnSalvarUsuario.text = "Salvando…"
                            }
                            is Resource.Success -> {
                                btnSalvarUsuario.isEnabled = true
                                btnSalvarUsuario.text = "Salvar Usuário"
                                Toast.makeText(
                                    this@CadastroUsuarioActivity,
                                    if (usuarioEmEdicaoId.isNotBlank()) "Usuário atualizado!" else "Usuário cadastrado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> {
                                btnSalvarUsuario.isEnabled = true
                                btnSalvarUsuario.text = "Salvar Usuário"
                                Toast.makeText(this@CadastroUsuarioActivity, resource.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }

                // Usuário carregado para edição
                launch {
                    viewModel.usuarioEditando.collect { usuario ->
                        usuario?.let { preencherFormulario(it) }
                    }
                }

                // Deleção
                launch {
                    viewModel.deletarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Toast.makeText(this@CadastroUsuarioActivity, "Usuário excluído.", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> Toast.makeText(this@CadastroUsuarioActivity, resource.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Preencher formulário (modo edição)
    // ─────────────────────────────────────────────────────────────────────────

    private fun preencherFormulario(usuario: Usuario) {
        usuarioEmEdicaoId = usuario.id
        etNome.setText(usuario.nome)
        etTelefone.setText(usuario.telefone)
        etEmail.setText(usuario.email)

        dataNascimentoSelecionada = usuario.dataNascimento
        tvDataNascimento.text = usuario.dataNascimento

        switchAdministrador.isChecked = usuario.administrador
        atualizarCardAdmin(usuario.administrador)

        carregarFotoUrl(usuario.fotoUrl)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Limpar formulário
    // ─────────────────────────────────────────────────────────────────────────

    private fun limparFormulario() {
        usuarioEmEdicaoId = ""
        fotoUri = null
        dataNascimentoSelecionada = ""
        etNome.text?.clear()
        etTelefone.text?.clear()
        etEmail.text?.clear()
        tvDataNascimento.text = ""
        switchAdministrador.isChecked = false
        atualizarCardAdmin(false)
        ivAvatar.visibility            = View.GONE
        ivAvatarPlaceholder.visibility = View.VISIBLE
    }
}
