package com.example.myappnsg

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificação de Auto-Login
        val sharedPreferences = getSharedPreferences("UFMGOrganizaPrefs", Context.MODE_PRIVATE)
        val jaCadastrado = sharedPreferences.getBoolean("IS_REGISTERED", false)

        if (jaCadastrado) {
            val intent = Intent(this, PageHomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        val inputLayoutNome = findViewById<TextInputLayout>(R.id.layoutNome)
        val etNome = findViewById<TextInputEditText>(R.id.etNome)

        val inputLayoutMatricula = findViewById<TextInputLayout>(R.id.layoutMatricula)
        val etMatricula = findViewById<TextInputEditText>(R.id.etMatricula)

        val inputLayoutCurso = findViewById<TextInputLayout>(R.id.layoutCurso)
        val etCurso = findViewById<TextInputEditText>(R.id.etCurso)

        val btnSalvar = findViewById<MaterialButton>(R.id.btnSalvar)

        btnSalvar.setOnClickListener {
            val nome = etNome.text.toString().trim()
            val matricula = etMatricula.text.toString().trim()
            val curso = etCurso.text.toString().trim()

            inputLayoutNome.error = null
            inputLayoutMatricula.error = null
            inputLayoutCurso.error = null

            var ehValido = true

            if (nome.isEmpty()) {
                inputLayoutNome.error = "Por favor, informe seu nome completo."
                ehValido = false
            }

            if (matricula.isEmpty()) {
                inputLayoutMatricula.error = "Por favor, informe sua matrícula."
                ehValido = false
            }

            if (curso.isEmpty()) {
                inputLayoutCurso.error = "Por favor, informe seu curso."
                ehValido = false
            }

            if (ehValido) {
                val editor = sharedPreferences.edit()
                editor.putString("USER_NOME", nome)
                editor.putString("USER_MATRICULA", matricula)
                editor.putString("USER_CURSO", curso)
                editor.putBoolean("IS_REGISTERED", true)
                editor.apply()

                Toast.makeText(this, "Perfil registrado com sucesso!", Toast.LENGTH_SHORT).show()

                // Abre a PageHomeActivity
                val intent = Intent(this, PageHomeActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}
