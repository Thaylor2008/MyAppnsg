package com.example.myappnsg

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_disciplina")
data class Disciplina(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nome: String,
    val codigo: String,
    val creditos: Int,
    val cargaHoraria: Int,
    val notaConsolidada: Double,
    val semestre: String
)