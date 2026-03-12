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

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)


            val prefs = getSharedPreferences("loginPrefs", MODE_PRIVATE)
            val editor = prefs.edit()

            editor.putString("email", editEmail.getText().toString())
            editor.apply()

            finish()

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