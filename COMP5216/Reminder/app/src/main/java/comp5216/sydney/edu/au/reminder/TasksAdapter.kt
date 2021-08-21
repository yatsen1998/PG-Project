package comp5216.sydney.edu.au.reminder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class TasksAdapter(context: Context, val resource: Int, val text: Int, list: ArrayList<Task>) :
    ArrayAdapter<Task>(context, resource, text, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val text1 = view.findViewById<View>(android.R.id.text1) as TextView
        val text2 = view.findViewById<View>(android.R.id.text2) as TextView
        text1.text = getItem(position)!!.title
        text2.text = getItem(position)!!.due!!.get(0) + " " + getItem(position)!!.due!!.get(1)
        return view
    }
}