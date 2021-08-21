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
        task.due!!.add("date")
        task.due!!.add("time")

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
            val editedDue = result.data?.extras?.getStringArrayList("due")
            val task = Task()

            task.title = editedTitle!!
            task.due = editedDue
            tasks?.add(task)
        }
    }

    fun onAddTaskClick(view: View?) {

        val intent = Intent(this@MainActivity, EditTaskActivity::class.java)

        btnlauncher.launch((intent))
        tasksAdapter?.notifyDataSetChanged()
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
                val editedTitle = result.data?.extras?.getString("item")
                val editedDue = result.data?.extras?.getStringArrayList("due")
                val position = result.data?.getIntExtra("position", -1)

                tasks!![position!!].title = editedTitle!!
                tasks!![position].due = editedDue
                Log.i("Update item in list", "$editedTitle, position: $position")
                tasksAdapter?.notifyDataSetChanged()
            }
        }

        listView?.onItemClickListener =
            OnItemClickListener { parent, view, position, rowID ->
                val updateTitle = tasksAdapter?.getItem(position)!!.title
                val updateDue = tasksAdapter?.getItem(position)!!.due
                Log.i("MainActivity", "Clicked item $position $updateTitle $updateDue")

                val intent = Intent(this@MainActivity, EditTaskActivity::class.java)
                intent.putExtra("item", updateTitle)
                intent.putStringArrayListExtra("due", updateDue)
                intent.putExtra("position", position)

                launcher.launch(intent)
                tasksAdapter?.notifyDataSetChanged()
            }
    }
}