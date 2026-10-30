package com.example.myappnsg

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PageHomeActivity : AppCompatActivity() {

    private val disciplinaAdapter = DisciplinaAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pagehome)

        // 1. Mapear os elementos do cabeçalho
        val tvSemestre = findViewById<TextView>(R.id.tvSemestre)
        val tvNomeUsuario = findViewById<TextView>(R.id.tvNomeUsuario)
        val tvCursoUsuario = findViewById<TextView>(R.id.tvCursoUsuario)
        val tvAvatar = findViewById<TextView>(R.id.tvAvatar)

        // 2. Ler os dados do perfil salvos no SharedPreferences
        val sharedPreferences = getSharedPreferences("UFMGOrganizaPrefs", MODE_PRIVATE)
        val nomeSalvo = sharedPreferences.getString("USER_NOME", "Ana Silva") ?: "Ana Silva"
        val cursoSalvo = sharedPreferences.getString("USER_CURSO", "Ciência da Computação") ?: "Ciência da Computação"

        // 3. Atualizar as informações na tela
        tvSemestre?.text = "Semestre Atual • 2026/2"
        tvNomeUsuario?.text = nomeSalvo
        tvCursoUsuario?.text = cursoSalvo
        tvAvatar?.text = extrairIniciais(nomeSalvo)

        // 4. Configurar RecyclerView de disciplinas
        val rvDisciplinas = findViewById<RecyclerView>(R.id.rvDisciplinas)
        disciplinaAdapter.onItemClick = { disciplina ->
            val intent = Intent(this, DetalheDisciplinaActivity::class.java)
            intent.putExtra("DISCIPLINA_ID", disciplina.id)
            startActivity(intent)
        }
        rvDisciplinas?.adapter = disciplinaAdapter

        // 5. Carregar as disciplinas do Room Database
        carregarDisciplinas()

        // 6. Mapear o botão flutuante para cadastrar nova disciplina
        val fabAddDisciplina = findViewById<FloatingActionButton>(R.id.fabAddDisciplina)
        fabAddDisciplina?.setOnClickListener {
            exibirDialogNovaDisciplina()
        }

        // 7. Configurar navegação inferior
        setupBottomNav(R.id.nav_home)
    }

    override fun onResume() {
        super.onResume()
        carregarDisciplinas()
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

    private fun carregarDisciplinas() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@PageHomeActivity)
            val lista = db.disciplinaDao().listarTodas()

            // Cálculo do NSG Acumulado (Média ponderada por créditos)
            val totalCreditos = lista.sumOf { it.creditos }
            val nsg = if (lista.isNotEmpty() && totalCreditos > 0) {
                lista.sumOf { it.notaConsolidada * it.creditos } / totalCreditos
            } else if (lista.isNotEmpty()) {
                lista.map { it.notaConsolidada }.average()
            } else {
                0.0
            }

            withContext(Dispatchers.Main) {
                disciplinaAdapter.atualizarLista(lista)

                val tvNsgValor = findViewById<TextView>(R.id.tvNsgValor)
                tvNsgValor?.text = String.format("%.1f", nsg)

                // Lógica da Cor Dinâmica do NSG:
                // - Se nsg < 50.0 -> Vermelho (#E53935)
                // - Se nsg >= 50.0 -> Verde (#4CAF50)
                if (nsg < 50.0) {
                    tvNsgValor?.setTextColor(Color.parseColor("#E53935"))
                } else {
                    tvNsgValor?.setTextColor(Color.parseColor("#4CAF50"))
                }
            }
        }
    }

    private fun exibirDialogNovaDisciplina() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nova_disciplina, null)

        val etNome = dialogView.findViewById<TextInputEditText>(R.id.etNomeDisciplina)
        val etCodigo = dialogView.findViewById<TextInputEditText>(R.id.etCodigoDisciplina)
        val etCreditos = dialogView.findViewById<TextInputEditText>(R.id.etCreditos)
        val etCargaHoraria = dialogView.findViewById<TextInputEditText>(R.id.etCargaHoraria)
        val etNotaConsolidada = dialogView.findViewById<TextInputEditText>(R.id.etNotaConsolidada)
        val etSemestre = dialogView.findViewById<TextInputEditText>(R.id.etSemestre)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Cadastrar Nova Disciplina")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val nome = etNome?.text?.toString()?.trim().orEmpty()
            val codigo = etCodigo?.text?.toString()?.trim().orEmpty()
            val creditosStr = etCreditos?.text?.toString()?.trim().orEmpty()
            val cargaHorariaStr = etCargaHoraria?.text?.toString()?.trim().orEmpty()
            val notaStr = etNotaConsolidada?.text?.toString()?.trim().orEmpty()
            val semestre = etSemestre?.text?.toString()?.trim().orEmpty()

            if (nome.isEmpty() || codigo.isEmpty() || creditosStr.isEmpty() ||
                cargaHorariaStr.isEmpty() || notaStr.isEmpty() || semestre.isEmpty()
            ) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val creditos = creditosStr.toIntOrNull() ?: 0
            val cargaHoraria = cargaHorariaStr.toIntOrNull() ?: 0
            val notaConsolidada = notaStr.toDoubleOrNull() ?: 0.0

            val novaDisciplina = Disciplina(
                nome = nome,
                codigo = codigo,
                creditos = creditos,
                cargaHoraria = cargaHoraria,
                notaConsolidada = notaConsolidada,
                semestre = semestre
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(this@PageHomeActivity)
                db.disciplinaDao().inserir(novaDisciplina)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PageHomeActivity, "Disciplina cadastrada!", Toast.LENGTH_SHORT).show()
                    carregarDisciplinas()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun extrairIniciais(nomeCompleto: String): String {
        val partes = nomeCompleto.trim().split("\\s+".toRegex())
        return when {
            partes.isEmpty() || partes[0].isEmpty() -> "AS"
            partes.size == 1 -> partes[0].take(2).uppercase()
            else -> "${partes.first().first()}${partes.last().first()}".uppercase()
        }
    }
}