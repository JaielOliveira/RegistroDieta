package com.dieta.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dieta.app.data.model.MealTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTemplateDao {
    @Query("SELECT * FROM meal_template WHERE modelId = :modelId ORDER BY id ASC")
    fun getTemplatesForModel(modelId: Long): Flow<List<MealTemplate>>

    @Query("SELECT * FROM meal_template WHERE modelId = :modelId ORDER BY id ASC")
    suspend fun getTemplatesForModelDirect(modelId: Long): List<MealTemplate>

    @Query("SELECT * FROM meal_template WHERE id = :id LIMIT 1")
    suspend fun getTemplateById(id: Long): MealTemplate?

    @Query("SELECT * FROM meal_template ORDER BY id ASC")
    suspend fun getAllTemplatesDirect(): List<MealTemplate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(template: MealTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(templates: List<MealTemplate>)

    @Update
    suspend fun update(template: MealTemplate)

    @Delete
    suspend fun delete(template: MealTemplate)

    @Query("DELETE FROM meal_template WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_template WHERE modelId = :modelId")
    suspend fun deleteTemplatesForModel(modelId: Long)
}
