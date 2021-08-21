package comp5216.sydney.edu.au.reminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

class EditTaskActivity : AppCompatActivity() {
    var position = 0
    var editTask: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editItem = intent.getStringExtra("item")
        position = intent.getIntExtra("position", -1)

        editTask = findViewById<View?>(R.id.etEditItem) as EditText
        editTask!!.setText(editItem)
    }

    fun onSubmitClick(v: View?) {
        editTask = findViewById<View?>(R.id.etEditItem) as EditText

        val data = Intent()

        data.putExtra("item", editTask!!.text.toString())
        data.putExtra("position", position)

        setResult(RESULT_OK, data)
        finish()
    }

    fun onCancelClick(v: View?) {
        val builder = AlertDialog.Builder(EditTaskActivity@this)
        builder.setTitle(R.string.dialog_cancel_title)
            .setMessage(R.string.dialog_cancel_edit)
            .setPositiveButton(R.string.confirm) { dialogInterface, i->
                editTask!!.setText("")
            }
            .setNegativeButton(R.string.cancel) { dialogInterface, i->

            }
        builder.create().show()
    }
}