package comp5216.sydney.edu.au.mediarecording

import android.app.Application
import java.io.File

class Global : Application() {

    companion object {
        var mediaArray = ArrayList<File>()
    }
}