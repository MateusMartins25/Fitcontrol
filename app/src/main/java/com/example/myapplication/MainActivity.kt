package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, InicioFragment())
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNav.setOnItemSelectedListener { item ->

            when (item.itemId) {
                R.id.nav_inicio -> {
                    loadFragment(InicioFragment())
                    true
                }

                R.id.nav_treinos -> {
                    loadFragment(TreinoFragment())
                    true
                }

                R.id.nav_produtos -> {
                    //loadFragment(PerfilFragment())
                    true
                }

                R.id.nav_agenda -> {
                    //loadFragment(TreinoFragment())
                    true
                }

                R.id.nav_perfil -> {
                    loadFragment(PerfilFragment())
                    true
                }

                else -> false
            }
        }


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadFragment(fragment: Fragment){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

}


