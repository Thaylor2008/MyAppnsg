package com.example.myappnsg

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface DisciplinaDao {

    @Insert
    fun inserir(disciplina: Disciplina)

    @Query("SELECT * FROM tabela_disciplina")
    fun listarTodas(): List<Disciplina>

    @Query("SELECT * FROM tabela_disciplina WHERE semestre = :semestreAtual")
    fun listarPorSemestre(semestreAtual: String): List<Disciplina>

    @Update
    fun atualizar(disciplina: Disciplina)

    @Delete
    fun deletar(disciplina: Disciplina)
}