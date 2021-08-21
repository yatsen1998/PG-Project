package comp5216.sydney.edu.au.reminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.util.*
import android.widget.DatePicker

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.widget.TimePicker

import android.app.TimePickerDialog
import android.app.TimePickerDialog.OnTimeSetListener


class EditTaskActivity : AppCompatActivity() {
    var position = 0
    var editTask: EditText? = null
    var editDate: EditText? = null
    var editTime: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editedItem = intent.getStringExtra("item")
        val editedDate = intent.getStringExtra("date")
        position = intent.getIntExtra("position", -1)

        editTask = findViewById<View?>(R.id.etEditItem) as EditText
        editTask!!.setText(editedItem)

        editDate = findViewById<View?>(R.id.etEditDate) as EditText
        editDate!!.inputType = InputType.TYPE_NULL;
        setupOnDateListener()

        editTime = findViewById<View?>(R.id.etEditTime) as EditText
        editTime!!.inputType = InputType.TYPE_NULL;
        setupOnTimeListener()
    }

    fun setupOnDateListener() {
        editDate?.setOnClickListener(View.OnClickListener {
            Log.i("EditTaskActivity", "Click Date Picker")
            val cldr = Calendar.getInstance()
            val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
            val month: Int = cldr.get(Calendar.MONTH)
            val year: Int = cldr.get(Calendar.YEAR)
            // date picker dialog
            val picker = DatePickerDialog(this,
                { view, year, monthOfYear, dayOfMonth -> editDate!!.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) },
                year, month, day)
            picker.show()
        })
    }

    fun setupOnTimeListener() {
        editTime?.setOnClickListener(View.OnClickListener {
            Log.i("EditTaskActivity", "Click Time Picker")
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]
            val minutes = cldr[Calendar.MINUTE]
            // time picker dialog
            val picker = TimePickerDialog(this@EditTaskActivity,
                { tp, sHour, sMinute -> editTime!!.setText("$sHour:$sMinute") }, hour, minutes, true)
            picker.show()
        })
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
        val builder = AlertDialog.Builder(this)
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