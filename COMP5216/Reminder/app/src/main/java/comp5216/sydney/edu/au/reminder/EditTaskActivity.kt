package comp5216.sydney.edu.au.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*


class EditTaskActivity : AppCompatActivity() {
    var position = 0
    var editTask: EditText? = null
    var editDate: EditText? = null
    var editTime: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editedItem = intent.getStringExtra("item")
        val editedDate = intent.getStringExtra("due")
        position = intent.getIntExtra("position", -1)

        editTask = findViewById<View?>(R.id.etEditItem) as EditText
        editTask!!.setText(editedItem)

        editDate = findViewById<View?>(R.id.etEditDate) as EditText
        editDate!!.inputType = InputType.TYPE_NULL;
        setOnDateListener()

        editTime = findViewById<View?>(R.id.etEditTime) as EditText
        editTime!!.inputType = InputType.TYPE_NULL;
        setOnTimeListener()
    }

    fun setOnDateListener() {
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

    fun setOnTimeListener() {
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

    fun getDifference(startDate: Date, endDate: Date):String {
        //milliseconds
        var different = endDate.time - startDate.time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different = different % daysInMilli
        val elapsedHours = different / hoursInMilli
        different = different % hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different = different % minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        Log.i( "EditTaskActivity",
            "$elapsedDays days, $elapsedHours hours, $elapsedMinutes minutes, $elapsedSeconds seconds")
        if (different > 0)
            return "$elapsedDays days, $elapsedHours hours, $elapsedMinutes minutes, $elapsedSeconds seconds"
        else
            return "OVERDUE"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun onSubmitClick(v: View?) {

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val dueDate: Date = sdf.parse(editDate?.text.toString() + " " + editTime?.text.toString())

        val cal = Calendar.getInstance()
        val curDate = cal.time

        val dueTime = getDifference(curDate, dueDate)

        val data = Intent()

        editTask = findViewById<View?>(R.id.etEditItem) as EditText

        data.putExtra("item", editTask!!.text.toString())
        data.putExtra("due", dueTime)
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