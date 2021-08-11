package comp5216.sydney.edu.au.todolist_kotlin_w1

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView

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
        val toAddString = addItemEditText?.text.toString()
        if (toAddString.isNotEmpty()) {
            itemsAdapter?.add(toAddString)
            addItemEditText?.setText("")
        }
    }
}