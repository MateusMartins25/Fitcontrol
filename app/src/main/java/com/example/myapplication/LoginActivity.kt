package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        val editEmail = findViewById<EditText>(R.id.editEmail)
        val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
        val emailSalvo = prefs.getString("email", "")
        editEmail.setText(emailSalvo)
        val logo = findViewById<ImageView>(R.id.logo)
        val solicitar = findViewById<TextView>(R.id.txtSolicitar)
        val botao = findViewById<Button>(R.id.btnLogin)
        val animation = AnimationUtils.loadAnimation(this, R.anim.logo_animation)

        logo.startAnimation(animation)

        botao.setOnClickListener {

            val auth = FirebaseAuth.getInstance()

            val email = editEmail.text.toString().trim()
            val senha = "123456"

            if (email.isEmpty()) {
                Toast.makeText(this, "Digite um email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {

                        val erro = task.exception

                        val mensagem = when (erro) {

                            is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                                "Usuário não encontrado 😕"

                            is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                                "Email ou senha inválidos 🔐"

                            else ->
                                "Erro ao fazer login. Tente novamente."
                        }

                        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show()
                    }
                }

        }

        solicitar.setOnClickListener {
            val numero = "5547992112319"
            val mensagem = "Olá! Gostaria de solicitar o cadastro do meu e-mail no aplicativo da academia para acessar meus treinos. Obrigado!"

            val url = "https://wa.me/$numero?text=" +
                    java.net.URLEncoder.encode(mensagem, "UTF-8")

            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            startActivity(intent)

        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}