package com.example.myappnsg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator

class DisciplinaAdapter(
    private var listaDisciplinas: List<Disciplina> = emptyList()
) : RecyclerView.Adapter<DisciplinaAdapter.DisciplinaViewHolder>() {

    fun atualizarLista(novaLista: List<Disciplina>) {
        listaDisciplinas = novaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DisciplinaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_disciplina, parent, false)
        return DisciplinaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DisciplinaViewHolder, position: Int) {
        holder.bind(listaDisciplinas[position])
    }

    override fun getItemCount(): Int = listaDisciplinas.size

    class DisciplinaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCodigo: TextView = itemView.findViewById(R.id.tvCodigoDisciplina)
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomeDisciplina)
        private val tvNota: TextView = itemView.findViewById(R.id.tvNotaDisciplina)
        private val tvInfo: TextView = itemView.findViewById(R.id.tvInfoDisciplina)
        private val progressNota: LinearProgressIndicator = itemView.findViewById(R.id.progressNota)

        fun bind(disciplina: Disciplina) {
            tvCodigo.text = disciplina.codigo
            tvNome.text = disciplina.nome
            tvNota.text = "${disciplina.notaConsolidada}/100 pts"
            tvInfo.text = "${disciplina.creditos} cr. • ${disciplina.cargaHoraria}h • ${disciplina.semestre}"

            val progress = disciplina.notaConsolidada.toInt().coerceIn(0, 100)
            progressNota.progress = progress

            val context = itemView.context
            if (disciplina.notaConsolidada >= 60.0) {
                val colorGreen = ContextCompat.getColor(context, R.color.green_success)
                tvNota.setTextColor(colorGreen)
                progressNota.setIndicatorColor(colorGreen)
            } else {
                val colorRed = ContextCompat.getColor(context, R.color.red_danger)
                tvNota.setTextColor(colorRed)
                progressNota.setIndicatorColor(colorRed)
            }
        }
    }
}