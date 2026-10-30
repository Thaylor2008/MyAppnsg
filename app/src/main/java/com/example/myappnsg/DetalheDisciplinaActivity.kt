package com.example.myappnsg

import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalheDisciplinaActivity : AppCompatActivity() {

    private var disciplinaId: Int = -1
    private var disciplinaAtual: Disciplina? = null
    private val avaliacaoAdapter = AvaliacaoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_disciplina)

        disciplinaId = intent.getIntExtra("DISCIPLINA_ID", -1)

        findViewById<ImageView>(R.id.btnVoltar)?.setOnClickListener {
            finish()
        }

        avaliacaoAdapter.onEditClick = { avaliacao ->
            exibirDialogEditarAvaliacao(avaliacao)
        }

        avaliacaoAdapter.onDeleteClick = { avaliacao ->
            confirmarExclusaoAvaliacao(avaliacao)
        }

        avaliacaoAdapter.onCheckChanged = { avaliacao, isChecked ->
            atualizarStatusConcluida(avaliacao, isChecked)
        }

        val rvAvaliacoes = findViewById<RecyclerView>(R.id.rvAvaliacoes)
        rvAvaliacoes?.adapter = avaliacaoAdapter

        val fabAddAvaliacao = findViewById<FloatingActionButton>(R.id.fabAddAvaliacao)
        fabAddAvaliacao?.setOnClickListener {
            exibirDialogNovaAvaliacao()
        }

        carregarDetalhes()
    }

    private fun carregarDetalhes() {
        if (disciplinaId == -1) return

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@DetalheDisciplinaActivity)
            val disciplinas = db.disciplinaDao().listarTodas()
            disciplinaAtual = disciplinas.find { it.id == disciplinaId }

            val avaliacoes = db.avaliacaoDao().listarPorDisciplina(disciplinaId)

            // Regras de cálculo exatas:
            // - Nota Atual: Soma de notaObtida SOMENTE onde concluida == true
            val notaAtual = avaliacoes.filter { it.concluida }.mapNotNull { it.notaObtida }.sum()

            // - Restam em Disputa: Soma de valorMaximo SOMENTE onde concluida == false
            val restamEmDisputa = avaliacoes.filter { !it.concluida }.sumOf { it.valorMaximo }

            // - Falta p/ 60: maxOf(0.0, 60.0 - notaAtual)
            val faltaPara60 = maxOf(0.0, 60.0 - notaAtual)

            // Atualizar nota consolidada da disciplina no banco
            disciplinaAtual?.let { disc ->
                val disciplinaAtualizada = disc.copy(notaConsolidada = notaAtual)
                db.disciplinaDao().atualizar(disciplinaAtualizada)
                disciplinaAtual = disciplinaAtualizada
            }

            withContext(Dispatchers.Main) {
                disciplinaAtual?.let { disc ->
                    findViewById<TextView>(R.id.tvCodigoHeader)?.text = "${disc.codigo} • Semestre ${disc.semestre}"
                    findViewById<TextView>(R.id.tvNomeHeader)?.text = disc.nome

                    val tvNotaAtualVal = findViewById<TextView>(R.id.tvNotaAtualVal)
                    tvNotaAtualVal?.text = String.format("%.1f", notaAtual)

                    // Cor Dinâmica da Nota Atual:
                    // - Se notaAtual < 60.0 -> Vermelho (#E53935)
                    // - Se notaAtual >= 60.0 -> Verde (#4CAF50)
                    if (notaAtual < 60.0) {
                        tvNotaAtualVal?.setTextColor(Color.parseColor("#E53935"))
                    } else {
                        tvNotaAtualVal?.setTextColor(Color.parseColor("#4CAF50"))
                    }

                    findViewById<TextView>(R.id.tvFalta60Val)?.text = String.format("%.1f", faltaPara60)
                    findViewById<TextView>(R.id.tvRestamVal)?.text = String.format("%.1f", restamEmDisputa)
                }
                avaliacaoAdapter.atualizarLista(avaliacoes)
            }
        }
    }

    private fun atualizarStatusConcluida(avaliacao: Avaliacao, concluida: Boolean) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@DetalheDisciplinaActivity)
            val avaliacaoAtualizada = avaliacao.copy(concluida = concluida)
            db.avaliacaoDao().atualizar(avaliacaoAtualizada)

            carregarDetalhes()
        }
    }

    private fun exibirDialogNovaAvaliacao() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nova_avaliacao, null)

        val etNome = dialogView.findViewById<TextInputEditText>(R.id.etNomeAvaliacao)
        val etValorMaximo = dialogView.findViewById<TextInputEditText>(R.id.etValorMaximo)
        val etNotaObtida = dialogView.findViewById<TextInputEditText>(R.id.etNotaObtida)
        val etData = dialogView.findViewById<TextInputEditText>(R.id.etDataAvaliacao)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Nova Avaliação")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val nome = etNome?.text?.toString()?.trim().orEmpty()
            val valorMaxStr = etValorMaximo?.text?.toString()?.trim().orEmpty()
            val notaObtidaStr = etNotaObtida?.text?.toString()?.trim().orEmpty()
            val data = etData?.text?.toString()?.trim().orEmpty()

            if (nome.isEmpty() || valorMaxStr.isEmpty() || data.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valorMaximo = valorMaxStr.toDoubleOrNull() ?: 0.0
            val notaObtida = notaObtidaStr.toDoubleOrNull()

            val novaAvaliacao = Avaliacao(
                disciplinaId = disciplinaId,
                nome = nome,
                valorMaximo = valorMaximo,
                notaObtida = notaObtida,
                data = data,
                concluida = notaObtida != null
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(this@DetalheDisciplinaActivity)
                db.avaliacaoDao().inserir(novaAvaliacao)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DetalheDisciplinaActivity, "Avaliação adicionada!", Toast.LENGTH_SHORT).show()
                    carregarDetalhes()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun exibirDialogEditarAvaliacao(avaliacao: Avaliacao) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nova_avaliacao, null)

        val etNome = dialogView.findViewById<TextInputEditText>(R.id.etNomeAvaliacao)
        val etValorMaximo = dialogView.findViewById<TextInputEditText>(R.id.etValorMaximo)
        val etNotaObtida = dialogView.findViewById<TextInputEditText>(R.id.etNotaObtida)
        val etData = dialogView.findViewById<TextInputEditText>(R.id.etDataAvaliacao)

        etNome?.setText(avaliacao.nome)
        etValorMaximo?.setText(avaliacao.valorMaximo.toString())
        etNotaObtida?.setText(avaliacao.notaObtida?.toString() ?: "")
        etData?.setText(avaliacao.data)

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Editar Avaliação")
            .setView(dialogView)
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Salvar", null)
            .create()

        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
            val nome = etNome?.text?.toString()?.trim().orEmpty()
            val valorMaxStr = etValorMaximo?.text?.toString()?.trim().orEmpty()
            val notaObtidaStr = etNotaObtida?.text?.toString()?.trim().orEmpty()
            val data = etData?.text?.toString()?.trim().orEmpty()

            if (nome.isEmpty() || valorMaxStr.isEmpty() || data.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val valorMaximo = valorMaxStr.toDoubleOrNull() ?: 0.0
            val notaObtida = notaObtidaStr.toDoubleOrNull()

            val avaliacaoAtualizada = avaliacao.copy(
                nome = nome,
                valorMaximo = valorMaximo,
                notaObtida = notaObtida,
                data = data,
                concluida = notaObtida != null || avaliacao.concluida
            )

            lifecycleScope.launch(Dispatchers.IO) {
                val db = AppDatabase.getDatabase(this@DetalheDisciplinaActivity)
                db.avaliacaoDao().atualizar(avaliacaoAtualizada)

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DetalheDisciplinaActivity, "Avaliação atualizada!", Toast.LENGTH_SHORT).show()
                    carregarDetalhes()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun confirmarExclusaoAvaliacao(avaliacao: Avaliacao) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Excluir Avaliação")
            .setMessage("Deseja excluir esta avaliação?")
            .setNegativeButton("Cancelar", null)
            .setPositiveButton("Excluir") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(this@DetalheDisciplinaActivity)
                    db.avaliacaoDao().deletar(avaliacao)

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@DetalheDisciplinaActivity, "Avaliação excluída!", Toast.LENGTH_SHORT).show()
                        carregarDetalhes()
                    }
                }
            }
            .show()
    }
}