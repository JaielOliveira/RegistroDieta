package com.dieta.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dieta.app.data.model.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
    @Query("SELECT * FROM patient WHERE id = 1 LIMIT 1")
    fun getPatient(): Flow<Patient?>

    @Query("SELECT * FROM patient WHERE id = 1 LIMIT 1")
    suspend fun getPatientDirect(): Patient?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(patient: Patient)
}
