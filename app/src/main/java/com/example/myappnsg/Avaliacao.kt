package com.example.myappnsg

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tabela_avaliacao",
    foreignKeys = [
        ForeignKey(
            entity = Disciplina::class,
            parentColumns = ["id"],
            childColumns = ["disciplinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Avaliacao(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val disciplinaId: Int,
    val nome: String,
    val valorMaximo: Double,
    val notaObtida: Double? = null,
    val data: String,
    val concluida: Boolean = false
)