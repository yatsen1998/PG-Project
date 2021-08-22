package comp5216.sydney.edu.au.reminder

import android.icu.text.CaseMap
import java.util.*
import kotlin.collections.ArrayList

class Task {
    var title: String = ""
    var dueTimeString: String = ""
    var dueTimeLong: Long = Long.MAX_VALUE
}