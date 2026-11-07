package com.example.scamguard.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Contains database operations only.
 * Room runs suspend queries asynchronously and updates observable queries.
 */
@Dao
interface CheckDao {

    @Insert
    suspend fun insert(check: CheckEntity): Long

    @Query(
        "SELECT * FROM checks ORDER BY createdAt DESC, id DESC"
    )
    fun observeAll(): Flow<List<CheckEntity>>

    @Query(
        "SELECT * FROM checks " +
                "ORDER BY createdAt DESC, id DESC LIMIT :limit"
    )
    fun observeRecent(limit: Int): Flow<List<CheckEntity>>

    @Query("SELECT * FROM checks WHERE id = :id")
    fun observeById(id: Long): Flow<CheckEntity?>

    @Query("DELETE FROM checks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM checks")
    suspend fun clearAll()
}