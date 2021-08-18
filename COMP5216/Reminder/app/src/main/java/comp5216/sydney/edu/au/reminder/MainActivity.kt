package comp5216.sydney.edu.au.reminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {
    var listView: ListView? = null
    var items: ArrayList<String?>? = null
    var itemsAdapter: ArrayAdapter<String?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<View?>(R.id.lstViewMain) as ListView

        items = ArrayList()
        items!!.add("test1")
        items!!.add("test2")

        itemsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items!!)

        listView!!.adapter = itemsAdapter

        setupListViewListener()
    }

    val btnlauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult? ->
        if (result?.resultCode == RESULT_OK) {
            val editedItem = result.data?.extras?.getString("item")
            itemsAdapter?.add(editedItem!!)
        }
    }

    fun onAddItemClick(view: View?) {

        val intent = Intent(this@MainActivity, EditTaskActivity::class.java)

        btnlauncher.launch((intent))
        itemsAdapter?.notifyDataSetChanged()
//        if (toAddString.isNotEmpty()) {
//            itemsAdapter?.add(toAddString)
//            addItemEditText?.setText("")
//        }
    }

    private fun setupListViewListener() {
        listView?.onItemLongClickListener =
            OnItemLongClickListener { parent, view, position, rowID ->
                Log.i("MainActivity", "Long clicked item $position")
                val builder = AlertDialog.Builder(this@MainActivity)
                builder.setTitle(R.string.dialog_delete_title)
                    .setMessage(R.string.dialog_delete_msg)
                    .setPositiveButton(R.string.delete) { dialogInterface, i->
                        items?.removeAt(position)
                        itemsAdapter?.notifyDataSetChanged()
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
                val editedItem = result.data?.extras?.getString("item")
                val position = result.data?.getIntExtra("position", -1)

                items?.set(position!!, editedItem)
                Log.i("Update item in list", "$editedItem, position: $position")
                itemsAdapter?.notifyDataSetChanged()
            }
        }

        listView?.onItemClickListener =
            OnItemClickListener { parent, view, position, rowID ->
                val updateItem = itemsAdapter?.getItem(position) as String?
                Log.i("MainActivity", "Clicked item $position $updateItem")
                val intent = Intent(this@MainActivity, EditTaskActivity::class.java)
                intent.putExtra("item", updateItem)
                intent.putExtra("position", position)

                launcher.launch(intent)
                itemsAdapter?.notifyDataSetChanged()
            }
    }
}