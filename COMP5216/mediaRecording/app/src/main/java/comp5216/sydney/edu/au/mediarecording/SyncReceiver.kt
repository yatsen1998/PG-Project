package comp5216.sydney.edu.au.mediarecording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import comp5216.sydney.edu.au.mediarecording.Global.Companion.mediaArray

class SyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val syncHandler = SyncHandler(MainActivity.ins, mediaArray)
        syncHandler.doSync()
    }
}