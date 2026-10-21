package com.dieta.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dieta.app.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_log WHERE date = :date ORDER BY id ASC")
    fun getLogsForDate(date: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE date = :date ORDER BY id ASC")
    suspend fun getLogsForDateDirect(date: String): List<DailyLog>

    @Query("SELECT * FROM daily_log WHERE date = :date AND mealTemplateId = :templateId LIMIT 1")
    fun getLogForMealAndDate(templateId: Long, date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_log WHERE date = :date AND mealTemplateId = :templateId LIMIT 1")
    suspend fun getLogForMealAndDateDirect(templateId: Long, date: String): DailyLog?

    @Query("SELECT * FROM daily_log WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): DailyLog?

    @Query("SELECT * FROM daily_log WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    fun getLogsBetweenDates(startDate: String, endDate: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_log WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, id ASC")
    suspend fun getLogsBetweenDatesDirect(startDate: String, endDate: String): List<DailyLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(log: DailyLog): Long

    @Delete
    suspend fun delete(log: DailyLog)

    @Query("DELETE FROM daily_log WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM daily_log WHERE date = :date AND mealTemplateId = :templateId")
    suspend fun deleteForMealAndDate(templateId: Long, date: String)
}
