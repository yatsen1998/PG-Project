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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class MainActivity : AppCompatActivity() {
    var listView: ListView? = null
    var tasks: ArrayList<Task>? = null
    var tasksAdapter: TasksAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<View?>(R.id.lstViewMain) as ListView

        val task = Task()
        task.title = "Test1"
        task.dueTimeString = "date"
        task.dueTimeLong = Long.MAX_VALUE

        tasks =ArrayList()
        tasks!!.add(task)

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
            tasks?.forEach { Log.i("MainActivity", it.dueTimeString) }

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
                    .setPositiveButton(R.string.delete) { dialogInterface, i->
                            tasks?.removeAt(position)
                            tasksAdapter?.notifyDataSetChanged()
                    }
                    .setNegativeButton(R.string.cancel) {dialogInterface, i->

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
}