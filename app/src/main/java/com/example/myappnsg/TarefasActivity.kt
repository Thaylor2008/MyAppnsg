package com.example.myappnsg

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class TarefasActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tarefas)
        setupBottomNav(R.id.nav_tarefas)
    }

    private fun setupBottomNav(currentItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav) ?: return
        bottomNav.selectedItemId = currentItemId
        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == currentItemId) return@setOnItemSelectedListener true
            val targetClass: Class<*>? = when (item.itemId) {
                R.id.nav_home -> PageHomeActivity::class.java
                R.id.nav_grade -> GradeActivity::class.java
                R.id.nav_tarefas -> TarefasActivity::class.java
                R.id.nav_desempenho -> DesempenhoMacroActivity::class.java
                R.id.nav_ajustes -> AjustesActivity::class.java
                else -> null
            }
            if (targetClass != null) {
                val intent = Intent(this, targetClass)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
                true
            } else false
        }
    }
}