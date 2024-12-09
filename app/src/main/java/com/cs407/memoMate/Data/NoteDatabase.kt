package com.cs407.memoMate.Data

import android.content.Context
import android.os.Parcelable
import androidx.room.*
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@Entity(tableName = "task_list")
data class Task(
    @PrimaryKey(autoGenerate = true) val noteId: Int = 0,
    val significance: Int,
    val ddl: String, // Date as a string in "MM/dd/yyyy" format
    val finished: Boolean,
    val noteTitle: String,
    val noteAbstract: String,
    val importance: Int,
) : Parcelable

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task): Int

    @Delete
    suspend fun deleteTask(task: Task): Int

    @Query("SELECT * FROM task_list WHERE noteId = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("DELETE FROM task_list WHERE noteId = :taskId")
    suspend fun deleteTaskById(taskId: Int): Int

    @Query("SELECT * FROM task_list")
    fun getAllTasks(): List<Task>

    @Query("SELECT * FROM task_list WHERE strftime('%Y-%m', ddl) = :currentMonth")
    suspend fun getTasksForMonth(currentMonth: String): List<Task>

    @Query("SELECT COUNT(*) FROM task_list")
    suspend fun getTaskCount(): Int



    @Query("""
    SELECT * 
    FROM task_list 
    WHERE julianday(ddl) - julianday(:currentDate) < 3 
      AND julianday(ddl) >= julianday(:currentDate)
    """)
    suspend fun getTasksWithinThreeDays(currentDate: String): List<Task>

    @Query("""
    SELECT * 
    FROM task_list 
    WHERE julianday(ddl) - julianday(:currentDate) > 3 
      AND julianday(ddl) - julianday(:currentDate) <= 5
    """)
    suspend fun getTasksBetweenThreeAndFiveDays(currentDate: String): List<Task>

    @Query("""
    SELECT * 
    FROM task_list 
    WHERE julianday(ddl) - julianday(:currentDate) > 7
    """)
    suspend fun getTasksAfterSevenDays(currentDate: String): List<Task>

}

@Database(entities = [Task::class], version = 2, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getDatabase(context: Context): NoteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "task_database"
                )
                    .fallbackToDestructiveMigration() // For development. Use migrations in production.
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

