package com.dieta.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dieta.app.data.model.DietModel
import kotlinx.coroutines.flow.Flow

@Dao
interface DietModelDao {
    @Query("SELECT * FROM diet_model WHERE :selectedDate BETWEEN startDate AND endDate LIMIT 1")
    fun getModelForDate(selectedDate: String): Flow<DietModel?>

    @Query("SELECT * FROM diet_model WHERE :selectedDate BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getModelForDateDirect(selectedDate: String): DietModel?

    @Query("SELECT * FROM diet_model ORDER BY startDate DESC")
    fun getAllModels(): Flow<List<DietModel>>

    @Query("SELECT * FROM diet_model WHERE id = :id LIMIT 1")
    suspend fun getModelById(id: Long): DietModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: DietModel): Long

    @Update
    suspend fun update(model: DietModel)

    @Delete
    suspend fun delete(model: DietModel)
}
