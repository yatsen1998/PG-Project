package comp5216.sydney.edu.au.reminder

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasklist")
    fun getAll(): Flow<List<TaskModel>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(taskModel: TaskModel)

    @Query("DELETE FROM tasklist")
    suspend fun deleteAll()

    @Query("SELECT taskId FROM tasklist")
    fun getAllIds(): List<Int>

    @Query("SELECT * FROM tasklist WHERE taskId = :id")
    fun getById(id: Int): TaskModel?
}