package comp5216.sydney.edu.au.reminder

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MainActivity : AppCompatActivity() {
    var listView: ListView? = null
    var tasks: ArrayList<Task>? = null
    var tasksAdapter: TasksAdapter? = null
    var taskDao: TaskDao? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<View?>(R.id.lstViewMain) as ListView

        val applicationScope = CoroutineScope(SupervisorJob())
        val database by lazy { TaskDB.getDatabase(this, applicationScope) }
        taskDao = database.TaskDao()
        // Must call it before creating the adapter, because it references the right item list
        readItemsFromDatabase(taskDao!!)

//        val task = Task()
//        task.title = "Test1"
//        task.dueTimeString = "date"
//        task.dueTimeLong = Long.MAX_VALUE
//
//        tasks = ArrayList()
//        tasks!!.add(task)

        tasksAdapter = TasksAdapter(
            this,
            android.R.layout.simple_list_item_2,
            android.R.id.text1,
            tasks!!
        )

        listView!!.adapter = tasksAdapter

        setupListViewListener()
    }

    val btnlauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            val editedTitle = result.data?.extras?.getString("item")
            val editedDueString = result.data?.extras?.getString("dueString")
            val editedDueLong = result.data?.extras?.getLong("dueLong")

            val task = Task()
            task.title = editedTitle!!
            task.dueTimeString = editedDueString!!
            task.dueTimeLong = editedDueLong!!
            Log.i("MainActivity", task.dueTimeLong.toString())
            tasks?.add(task)

            tasks?.sortBy { task -> task.dueTimeLong }
//            tasks?.forEach { Log.i("MainActivity", it.dueTimeString) }
            saveItemsToDatabase()

            tasksAdapter?.notifyDataSetChanged()
        }
    }

    fun onAddTaskClick(view: View?) {

        val intent = Intent(this@MainActivity, EditTaskActivity::class.java)

        btnlauncher.launch(intent)
    }

    private fun setupListViewListener() {
        listView?.onItemLongClickListener =
            OnItemLongClickListener { parent, view, position, rowID ->
                Log.i("MainActivity", "Long clicked item $position")
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_msg)
                    .setPositiveButton(R.string.delete) { dialogInterface, i ->
                        tasks?.removeAt(position)
                        tasksAdapter?.notifyDataSetChanged()
                        saveItemsToDatabase()
                    }
                    .setNegativeButton(R.string.cancel) { dialogInterface, i ->

                    }
                builder.create().show()
                true
            }

        val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult? ->
            if (result?.resultCode == RESULT_OK) {
                val editedTitle = result.data?.getStringExtra("item")
                val editedDueString = result.data?.getStringExtra("dueString")
                val editedDueInt = result.data?.getLongExtra("dueLong", Long.MAX_VALUE)
                val position = result.data?.getIntExtra("position", -1)

                tasks!![position!!].title = editedTitle!!
                tasks!![position].dueTimeString = editedDueString!!
                Log.i("Update item in list", "$editedTitle, position: $position")
                saveItemsToDatabase()
                tasksAdapter?.notifyDataSetChanged()
            }
        }

        listView?.onItemClickListener =
            OnItemClickListener { parent, view, position, rowID ->
                val updateTitle = tasksAdapter?.getItem(position)!!.title
                val updateDueString = tasksAdapter?.getItem(position)!!.dueTimeString
                val updateDueLong = tasksAdapter?.getItem(position)!!.dueTimeLong
                Log.i("MainActivity", "Clicked item $position $updateTitle $updateDueString")

                val intent = Intent(this@MainActivity, EditTaskActivity::class.java)
                intent.putExtra("item", updateTitle)
                intent.putExtra("dueString", updateDueString)
                intent.putExtra("dueLong", updateDueLong)
                intent.putExtra("position", position)

                launcher.launch(intent)
                tasksAdapter?.notifyDataSetChanged()
            }
    }

    // read items from the database
    private fun readItemsFromDatabase(taskDao: TaskDao) {
        try {
            tasks = ArrayList()
            GlobalScope.launch {
                getItems()
            }
        }catch (e: Exception){
            Log.i("Exception thrown", e.message.toString())
        }
    }
    suspend fun getItems() {
        withContext(Dispatchers.IO) {
        // Get entities from database on IO thread.
        val titles = taskDao?.getAllIds()
            titles?.forEach { id ->
            val taskModel = taskDao?.getById(id)
                val task = Task()
                task.title = taskModel!!.taskTitle
                task.dueTimeString = taskModel.taskDueString
                task.dueTimeLong = taskModel.taskDueLong
            tasks!!.add(task)
            }
        }
    }
    // write items to the database
    private fun saveItemsToDatabase() {
        try {
            GlobalScope.launch {
                deleteAll()
                for (task in tasks!!) {
                    Log.i("SQLite saved item", task.title)
                    val item = TaskModel(task.title, task.dueTimeString, task.dueTimeLong)
                    insert(item)
                }
            }
        }catch (e:Exception){
            Log.i("Exception thrown", e.message.toString())
        }
    }
    suspend fun insert(task: TaskModel) {
        taskDao?.insert(task)
    }
    suspend fun deleteAll() {
        taskDao?.deleteAll()
    }
}