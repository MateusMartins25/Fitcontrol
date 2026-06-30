package com.example.myapplication.produto

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
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
import com.example.myapplication.R
import com.example.myapplication.model.Produto
import com.example.myapplication.util.Resource
import com.example.myapplication.viewmodel.CadastroProdutoViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class CadastroProdutoActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_PRODUTO_ID = "extra_produto_id"
    }

    private val viewModel: CadastroProdutoViewModel by viewModels {
        CadastroProdutoViewModel.Factory()
    }

    private lateinit var toolbar: Toolbar
    private lateinit var btnBack: ImageButton
    private lateinit var btnMore: ImageButton
    private lateinit var etSearchProduto: EditText
    private lateinit var itemProdutoEncontrado: View
    private lateinit var containerImagem: FrameLayout
    private lateinit var layoutPlaceholder: LinearLayout
    private lateinit var ivProdutoImagem: ImageView
    private lateinit var etNomeProduto: EditText
    private lateinit var etDescricao: EditText
    private lateinit var btnStatusDisponivel: LinearLayout
    private lateinit var btnStatusIndisponivel: LinearLayout
    private lateinit var tvDisponivel: TextView
    private lateinit var tvIndisponivel: TextView
    private lateinit var dotDisponivel: View
    private lateinit var dotIndisponivel: View
    private lateinit var etValor: EditText
    private lateinit var btnSalvarProduto: Button

    private var imagemSelecionadaUri: Uri? = null
    private var statusDisponivel: Boolean = true
    private var cameraImageUri: Uri? = null
    private var produtoEmEdicaoId: String = ""

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { setImagemSelecionada(it) }
        }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) cameraImageUri?.let { setImagemSelecionada(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_produto)

        bindViews()
        setupToolbar()
        setupImagePicker()
        setupStatusButtons()
        setupValorMask()
        setupSearch()
        setupSaveButton()
        observarViewModel()

        produtoEmEdicaoId = intent.getStringExtra(EXTRA_PRODUTO_ID).orEmpty()
        if (produtoEmEdicaoId.isNotBlank()) {
            viewModel.carregarProduto(produtoEmEdicaoId)
        }
    }

    private fun bindViews() {
        toolbar               = findViewById(R.id.toolbar)
        btnBack               = findViewById(R.id.btnBack)
        btnMore               = findViewById(R.id.btnMore)
        etSearchProduto       = findViewById(R.id.etSearchProduto)
        itemProdutoEncontrado = findViewById(R.id.itemProdutoEncontrado)
        containerImagem       = findViewById(R.id.containerImagem)
        layoutPlaceholder     = findViewById(R.id.layoutPlaceholder)
        ivProdutoImagem       = findViewById(R.id.ivProdutoImagem)
        etNomeProduto         = findViewById(R.id.etNomeProduto)
        etDescricao           = findViewById(R.id.etDescricao)
        btnStatusDisponivel   = findViewById(R.id.btnStatusDisponivel)
        btnStatusIndisponivel = findViewById(R.id.btnStatusIndisponivel)
        tvDisponivel          = findViewById(R.id.tvDisponivel)
        tvIndisponivel        = findViewById(R.id.tvIndisponivel)
        dotDisponivel         = findViewById(R.id.dotDisponivel)
        dotIndisponivel       = findViewById(R.id.dotIndisponivel)
        etValor               = findViewById(R.id.etValor)
        btnSalvarProduto      = findViewById(R.id.btnSalvarProduto)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        btnMore.setOnClickListener { mostrarMenuOpcoes() }
    }

    private fun mostrarMenuOpcoes() {
        val opcoes = if (produtoEmEdicaoId.isNotBlank()) {
            arrayOf("Limpar formulário", "Excluir produto")
        } else {
            arrayOf("Limpar formulário")
        }

        AlertDialog.Builder(this)
            .setItems(opcoes) { _, which ->
                when {
                    which == 0 -> limparFormulario()
                    which == 1 && produtoEmEdicaoId.isNotBlank() -> confirmarExclusao()
                }
            }
            .show()
    }

    private fun confirmarExclusao() {
        AlertDialog.Builder(this)
            .setTitle("Excluir produto")
            .setMessage("Tem certeza que deseja excluir este produto? Esta ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                viewModel.deletarProduto(produtoEmEdicaoId)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun setupImagePicker() {
        containerImagem.setOnClickListener { mostrarDialogFonte() }
    }

    private fun mostrarDialogFonte() {
        AlertDialog.Builder(this)
            .setTitle("Escolher imagem")
            .setItems(arrayOf("Galeria", "Câmera")) { _, which ->
                if (which == 0) pickImageLauncher.launch("image/*") else launchCamera()
            }
            .show()
    }

    private fun launchCamera() {
        val file = File(cacheDir, "produto_${System.currentTimeMillis()}.jpg")
        val uri  = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    private fun setImagemSelecionada(uri: Uri) {
        imagemSelecionadaUri = uri
        ivProdutoImagem.setImageURI(uri)
        ivProdutoImagem.visibility  = View.VISIBLE
        layoutPlaceholder.visibility = View.GONE
    }

    private fun carregarImagemUrl(url: String) {
        if (url.isBlank()) return
        Glide.with(this)
            .load(url)
            .centerCrop()
            .into(ivProdutoImagem)
        ivProdutoImagem.visibility   = View.VISIBLE
        layoutPlaceholder.visibility = View.GONE
    }

    private fun setupStatusButtons() {
        atualizarStatusUI(disponivel = true)
        btnStatusDisponivel.setOnClickListener   { statusDisponivel = true;  atualizarStatusUI(true) }
        btnStatusIndisponivel.setOnClickListener { statusDisponivel = false; atualizarStatusUI(false) }
    }

    private fun atualizarStatusUI(disponivel: Boolean) {
        btnStatusDisponivel.isSelected   = disponivel
        btnStatusIndisponivel.isSelected = !disponivel

        tvDisponivel.setTextColor(
            getColor(if (disponivel) R.color.status_available_text else R.color.text_secondary)
        )
        tvIndisponivel.setTextColor(
            getColor(if (!disponivel) R.color.text_primary else R.color.text_secondary)
        )
        dotDisponivel.setBackgroundResource(
            if (disponivel) R.drawable.shape_dot_green else R.drawable.shape_dot_gray
        )
    }

    private fun setupValorMask() {
        val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
        etValor.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                isUpdating = true
                val digits = s.toString().replace(Regex("[^\\d]"), "")
                if (digits.isEmpty()) { etValor.setText(""); isUpdating = false; return }
                val formatted = DecimalFormat("#,##0.00", symbols).format(digits.toLong() / 100.0)
                etValor.setText(formatted)
                etValor.setSelection(formatted.length)
                isUpdating = false
            }
        })
    }

    private fun setupSearch() {
        itemProdutoEncontrado.visibility = View.GONE

        etSearchProduto.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.onQueryChanged(s.toString().trim())
            }
        })

        itemProdutoEncontrado.setOnClickListener {
            val resultado = (viewModel.resultadoBusca.value as? Resource.Success)?.data
            resultado?.firstOrNull()?.let { preencherFormulario(it) }
        }
    }

    private fun setupSaveButton() {
        btnSalvarProduto.setOnClickListener {
            if (validarCampos()) {
                val produto = construirProduto()
                viewModel.salvarProduto(produto, imagemSelecionadaUri)
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nome  = etNomeProduto.text.toString().trim()
        val valor = etValor.text.toString().trim()
        if (nome.isEmpty()) {
            etNomeProduto.error = "Informe o nome do produto"
            etNomeProduto.requestFocus(); return false
        }
        if (valor.isEmpty()) {
            etValor.error = "Informe o valor"
            etValor.requestFocus(); return false
        }
        return true
    }

    private fun construirProduto(): Produto {
        val valorDouble = etValor.text.toString()
            .replace(".", "").replace(",", ".")
            .toDoubleOrNull() ?: 0.0

        val imagemUrlAtual = (viewModel.produtoEditando.value?.imagemUrl).orEmpty()

        return Produto(
            id        = produtoEmEdicaoId,
            nome      = etNomeProduto.text.toString().trim(),
            descricao = etDescricao.text.toString().trim(),
            valor     = valorDouble,
            status    = if (statusDisponivel) Produto.STATUS_DISPONIVEL else Produto.STATUS_INDISPONIVEL,
            imagemUrl = imagemUrlAtual
        )
    }

    private fun preencherFormulario(produto: Produto) {
        produtoEmEdicaoId = produto.id
        etNomeProduto.setText(produto.nome)
        etDescricao.setText(produto.descricao)

        val symbols   = DecimalFormatSymbols(Locale("pt", "BR"))
        val formatted = DecimalFormat("#,##0.00", symbols).format(produto.valor)
        etValor.setText(formatted)
        etValor.setSelection(formatted.length)

        statusDisponivel = produto.status == Produto.STATUS_DISPONIVEL
        atualizarStatusUI(statusDisponivel)

        carregarImagemUrl(produto.imagemUrl)
        etSearchProduto.setText("")
        itemProdutoEncontrado.visibility = View.GONE
    }

    private fun observarViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.salvarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                btnSalvarProduto.isEnabled = false
                                btnSalvarProduto.text = "Salvando…"
                            }
                            is Resource.Success -> {
                                btnSalvarProduto.isEnabled = true
                                btnSalvarProduto.text = getString(R.string.btn_salvar_produto)
                                Toast.makeText(
                                    this@CadastroProdutoActivity,
                                    if (produtoEmEdicaoId.isNotBlank()) "Produto atualizado!" else "Produto cadastrado!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> {
                                btnSalvarProduto.isEnabled = true
                                btnSalvarProduto.text = getString(R.string.btn_salvar_produto)
                                Toast.makeText(
                                    this@CadastroProdutoActivity,
                                    resource.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }

                launch {
                    viewModel.produtoEditando.collect { produto ->
                        produto?.let { preencherFormulario(it) }
                    }
                }

                launch {
                    viewModel.resultadoBusca.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                val lista = resource.data
                                if (lista.isNotEmpty() && etSearchProduto.text.length >= 2) {
                                    val primeiro = lista.first()
                                    itemProdutoEncontrado.visibility = View.VISIBLE
                                    itemProdutoEncontrado.findViewById<TextView>(R.id.tvProdutoNome)
                                        ?.text = primeiro.nome
                                    val symbols = DecimalFormatSymbols(Locale("pt", "BR"))
                                    itemProdutoEncontrado.findViewById<TextView>(R.id.tvProdutoPreco)
                                        ?.text = "R$ ${DecimalFormat("#,##0.00", symbols).format(primeiro.valor)}"
                                    itemProdutoEncontrado.findViewById<TextView>(R.id.tvProdutoStatus)
                                        ?.text = if (primeiro.status == Produto.STATUS_DISPONIVEL) "Disponível" else "Indisponível"
                                    val thumb = itemProdutoEncontrado.findViewById<ImageView>(R.id.ivProdutoThumb)
                                    if (thumb != null && primeiro.imagemUrl.isNotBlank()) {
                                        Glide.with(this@CadastroProdutoActivity)
                                            .load(primeiro.imagemUrl).centerCrop().into(thumb)
                                    }
                                } else {
                                    itemProdutoEncontrado.visibility = View.GONE
                                }
                            }
                            else -> itemProdutoEncontrado.visibility = View.GONE
                        }
                    }
                }

                launch {
                    viewModel.deletarEvent.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                Toast.makeText(this@CadastroProdutoActivity, "Produto excluído.", Toast.LENGTH_SHORT).show()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            is Resource.Error -> Toast.makeText(this@CadastroProdutoActivity, resource.message, Toast.LENGTH_LONG).show()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    private fun limparFormulario() {
        produtoEmEdicaoId = ""
        etNomeProduto.text?.clear()
        etDescricao.text?.clear()
        etValor.text?.clear()
        imagemSelecionadaUri = null
        ivProdutoImagem.visibility   = View.GONE
        layoutPlaceholder.visibility = View.VISIBLE
        statusDisponivel = true
        atualizarStatusUI(true)
        etSearchProduto.text?.clear()
        itemProdutoEncontrado.visibility = View.GONE
    }
}
