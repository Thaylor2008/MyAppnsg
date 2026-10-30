package com.example.myappnsg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AvaliacaoDao {

    @Insert
    suspend fun inserir(avaliacao: Avaliacao)

    @Query("SELECT * FROM tabela_avaliacao WHERE disciplinaId = :disciplinaId")
    suspend fun listarPorDisciplina(disciplinaId: Int): List<Avaliacao>

    @Update
    suspend fun atualizar(avaliacao: Avaliacao)

    @Delete
    suspend fun deletar(avaliacao: Avaliacao)
}