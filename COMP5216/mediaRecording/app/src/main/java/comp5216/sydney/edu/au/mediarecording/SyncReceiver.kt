package comp5216.sydney.edu.au.mediarecording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File

class SyncReceiver: BroadcastReceiver() {
    lateinit var mediaArray: ArrayList<File>
    override fun onReceive(context: Context?, intent: Intent?) {
        mediaArray = intent!!.getSerializableExtra("mediaArray") as ArrayList<File>
        val syncHandler = SyncHandler(MainActivity.ins, mediaArray)
        syncHandler.doSync()
    }
}