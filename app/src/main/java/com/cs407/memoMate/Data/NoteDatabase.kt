package com.cs407.memoMate.Data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import java.util.Date

@Entity(tableName = "task_list")
data class Task(
    @PrimaryKey(autoGenerate = true) val noteId: Int,
    val significance: Int,
    val ddl: Date,
    val finished: Boolean,
    val noteTitle: String,
    val noteAbstract: String

)

// Converter class to handle conversion between custom type Date and SQL-compatible type Long
class Converters {

    // Converts a timestamp (Long) to a Date object
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    // Converts a Date object to a timestamp (Long)
    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }
}

@Dao
interface TaskDao {

    @Insert
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

    @Query("SELECT * FROM task_list WHERE strftime('%Y-%m', ddl / 1000, 'unixepoch') = :currentMonth")
    suspend fun getTasksForMonth(currentMonth: String): List<Task>

    @Query("SELECT COUNT(*) FROM task_list")
    suspend fun getTaskCount(): Int

    //usage:
    // 1) get current data :
    //          val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    //          val currentDate = dateFormat.format(Date())
    // 2) call the method to get tasks(within 3 days)
    //          val tasks = taskDao.getTasksWithinThreeDays(currentDate)

    @Query("SELECT * FROM task_list WHERE julianday(ddl / 1000, 'unixepoch') - julianday(:currentDate) < 3 AND julianday(ddl / 1000, 'unixepoch') >= julianday(:currentDate)")
    suspend fun getTasksWithinThreeDays(currentDate: String): List<Task>

    @Query("SELECT * FROM task_list WHERE julianday(ddl / 1000, 'unixepoch') - julianday(:currentDate) > 3 AND julianday(ddl / 1000, 'unixepoch') - julianday(:currentDate) <= 5")
    suspend fun getTasksBetweenThreeAndFiveDays(currentDate: String): List<Task>

    @Query("SELECT * FROM task_list WHERE julianday(ddl / 1000, 'unixepoch') - julianday(:currentDate) > 7")
    suspend fun getTasksAfterSevenDays(currentDate: String): List<Task>

}


@Database(entities = [Task::class], version = 1)
@TypeConverters(Converters::class) // Include converters for custom data types like Date
abstract class NoteDatabase : RoomDatabase() {

    // Provide DAO to access the database
    abstract fun taskDao(): TaskDao

    companion object {
        // Singleton prevents multiple instances of the database opening at the same time
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        // Get or create the database instance
        fun getDatabase(context: Context): NoteDatabase {
            // Return existing instance if available; create a new one if not
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "task_database" // Name of the database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

