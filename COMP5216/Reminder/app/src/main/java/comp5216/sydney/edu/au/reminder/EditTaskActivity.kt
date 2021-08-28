/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package comp5216.sydney.edu.au.reminder

import android.annotation.SuppressLint
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Handles the edit_task_activity
 */

class EditTaskActivity : AppCompatActivity() {
    var position = 0
    var editTask: EditText? = null
    var editDate: EditText? = null
    var editTime: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editedItem = intent.getStringExtra("item")
        val editedTime = intent.getStringExtra("dueString")

        position = intent.getIntExtra("position", -1)

        editTask = findViewById<View?>(R.id.etEditItem) as EditText
        editTask!!.setText(editedItem)

        editDate = findViewById<View?>(R.id.etEditDate) as EditText
        editDate!!.inputType = InputType.TYPE_NULL
        setOnDateListener()

        editTime = findViewById<View?>(R.id.etEditTime) as EditText
        editTime!!.inputType = InputType.TYPE_NULL
        setOnTimeListener()
    }

    @SuppressLint("SetTextI18n")
    fun setOnDateListener() {
        editDate?.setOnClickListener(View.OnClickListener {
            Log.i("EditTaskActivity", "Click Date Picker")
            val cldr = Calendar.getInstance()
            val day: Int = cldr.get(Calendar.DAY_OF_MONTH)
            val month: Int = cldr.get(Calendar.MONTH)
            val year: Int = cldr.get(Calendar.YEAR)
            // date picker dialog
            val picker = DatePickerDialog(
                this,
                { view, year, monthOfYear, dayOfMonth ->
                    editDate!!.setText(dayOfMonth.toString() + "/" + (monthOfYear + 1) + "/" + year) },
                year, month, day
            )
            picker.show()
        })
    }

    @SuppressLint("SetTextI18n")
    fun setOnTimeListener() {
        editTime?.setOnClickListener(View.OnClickListener {
            Log.i("EditTaskActivity", "Click Time Picker")
            val cldr = Calendar.getInstance()
            val hour = cldr[Calendar.HOUR_OF_DAY]
            val minutes = cldr[Calendar.MINUTE]
            // time picker dialog
            val picker = TimePickerDialog(this@EditTaskActivity,
                { tp, sHour, sMinute ->
                    editTime!!.setText("$sHour:$sMinute") }, hour, minutes, true
            )
            picker.show()
        })
    }

    fun getDifferenceString(startDate: Date, endDate: Date): String {
        var different = endDate.time - startDate.time
        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24
        val elapsedDays = different / daysInMilli
        different %= daysInMilli
        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        Log.i(
            "EditTaskActivity",
            "$elapsedDays days, $elapsedHours hours," +
                    "$elapsedMinutes minutes, $elapsedSeconds seconds"
        )
        return if (different > 0)
            "$elapsedDays days, $elapsedHours hours, " +
                    "$elapsedMinutes minutes, $elapsedSeconds seconds"
        else
            "OVERDUE"
    }

    fun onSubmitClick(v: View) {

        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val dueDate: Date =
                sdf.parse(editDate!!.text.toString() + " " + editTime!!.text.toString())

        val cal = Calendar.getInstance()
        val curDate = cal.time
        val dueTimeString = getDifferenceString(curDate, dueDate)
        val dueTimeLong = dueDate.time - curDate.time

        val data = Intent()

        editTask = findViewById<View?>(R.id.etEditItem) as EditText

        data.putExtra("item", editTask!!.text.toString())
        data.putExtra("dueString", dueTimeString)
        data.putExtra("dueLong", dueTimeLong)
        data.putExtra("position", position)

        setResult(RESULT_OK, data)
        finish()
    }

    fun onCancelClick(v: View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.dialog_cancel_title)
            .setMessage(R.string.dialog_cancel_edit)
            .setPositiveButton(R.string.confirm) { dialogInterface, i ->
                editTask!!.setText("")
                editDate!!.setText("")
                editTime!!.setText("")

                finish()
            }

        builder.create().show()
    }
}