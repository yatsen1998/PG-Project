package comp5216.sydney.edu.au.todolist_kotlin_w1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var listView: ListView? = null
    var items: ArrayList<String?>? = null
    var itemsAdapter: ArrayAdapter<String?>? = null
    var addItemEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById<View?>(R.id.lstView) as ListView
        addItemEditText = findViewById<View?>(R.id.txtNewItem) as EditText

        items = ArrayList()
        items!!.add("item one")
        items!!.add("item two")

        itemsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items!!)

        listView!!.adapter = itemsAdapter
    }

    fun onAddItemCLick(view: View?) {
        val date = Date()
        val toAddTime = date.time
        val toAddString = addItemEditText?.text.toString()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        if (toAddString.isNotEmpty()) {
            itemsAdapter?.add(toAddString + " " + sdf.format(toAddTime))
            Toast.makeText(this, sdf.format(toAddTime), Toast.LENGTH_SHORT).show()
            addItemEditText?.setText("")
        }
    }
}