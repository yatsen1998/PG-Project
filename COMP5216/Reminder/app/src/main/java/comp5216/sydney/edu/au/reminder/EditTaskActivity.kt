package comp5216.sydney.edu.au.reminder

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText

class EditTaskActivity : AppCompatActivity() {
    var position = 0
    var etItem: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_task)

        val editItem = intent.getStringExtra("item")
        position = intent.getIntExtra("position", -1)

        etItem = findViewById<View?>(R.id.etEditItem) as EditText
        etItem!!.setText(editItem)
    }

    fun onSubmit(v: View?) {
        etItem = findViewById<View?>(R.id.etEditItem) as EditText

        val data = Intent()

        data.putExtra("item", etItem!!.text.toString())
        data.putExtra("position", position)

        setResult(RESULT_OK, data)
        finish()
    }
}