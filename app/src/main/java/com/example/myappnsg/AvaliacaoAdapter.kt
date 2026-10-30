package com.example.myappnsg

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class AvaliacaoAdapter(
    private var listaAvaliacoes: List<Avaliacao> = emptyList(),
    var onEditClick: ((Avaliacao) -> Unit)? = null,
    var onDeleteClick: ((Avaliacao) -> Unit)? = null,
    var onCheckChanged: ((Avaliacao, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<AvaliacaoAdapter.AvaliacaoViewHolder>() {

    fun atualizarLista(novaLista: List<Avaliacao>) {
        listaAvaliacoes = novaLista
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvaliacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_avaliacao, parent, false)
        return AvaliacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: AvaliacaoViewHolder, position: Int) {
        val item = listaAvaliacoes[position]
        holder.bind(item)
        holder.btnEditar.setOnClickListener { onEditClick?.invoke(item) }
        holder.btnDeletar.setOnClickListener { onDeleteClick?.invoke(item) }

        holder.cbConcluida.setOnCheckedChangeListener(null)
        holder.cbConcluida.isChecked = item.concluida
        holder.cbConcluida.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged?.invoke(item, isChecked)
        }
    }

    override fun getItemCount(): Int = listaAvaliacoes.size

    class AvaliacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cbConcluida: CheckBox = itemView.findViewById(R.id.cbConcluida)
        private val tvNome: TextView = itemView.findViewById(R.id.tvNomeAvaliacao)
        private val tvNota: TextView = itemView.findViewById(R.id.tvNotaAvaliacao)
        private val tvData: TextView = itemView.findViewById(R.id.tvDataAvaliacao)
        private val tvStatusTexto: TextView = itemView.findViewById(R.id.tvStatusTexto)
        val btnEditar: ImageView = itemView.findViewById(R.id.btnEditarAvaliacao)
        val btnDeletar: ImageView = itemView.findViewById(R.id.btnDeletarAvaliacao)

        fun bind(avaliacao: Avaliacao) {
            tvNome.text = avaliacao.nome
            val notaFormatada = avaliacao.notaObtida?.let { "$it / ${avaliacao.valorMaximo} pts" }
                ?: "Pendente (${avaliacao.valorMaximo} pts)"
            tvNota.text = notaFormatada
            tvData.text = "Data: ${avaliacao.data} • Valor: ${avaliacao.valorMaximo} pts"

            val context = itemView.context
            if (avaliacao.concluida) {
                tvStatusTexto.text = "Concluída"
                tvStatusTexto.setTextColor(ContextCompat.getColor(context, R.color.green_success))
            } else {
                tvStatusTexto.text = "Pendente"
                tvStatusTexto.setTextColor(ContextCompat.getColor(context, R.color.orange_warning))
            }
        }
    }
}