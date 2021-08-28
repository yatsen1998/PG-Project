package comp5216.sydney.edu.au.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasklist")
data class TaskModel(
    @field:ColumnInfo(name = "taskTitle") val taskTitle: String,
    @field:ColumnInfo(name = "taskDueString") val taskDueString: String,
    @field:ColumnInfo(name = "taskDueLong") val taskDueLong: Long
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "taskId")
    var taskId: Int = 0
}