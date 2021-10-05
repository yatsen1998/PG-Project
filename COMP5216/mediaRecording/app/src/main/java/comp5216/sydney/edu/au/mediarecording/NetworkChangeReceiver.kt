package comp5216.sydney.edu.au.mediarecording

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import comp5216.sydney.edu.au.mediarecording.Global.Companion.mediaArray

class NetworkChangeReceiver : BroadcastReceiver() {
    var onLine = true
    override fun onReceive(context: Context, intent: Intent) {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = manager.activeNetworkInfo
        if (ni == null || ni.state != NetworkInfo.State.CONNECTED) {
            Log.i("network", "There's no network connectivity")
            onLine = false
        } else {
            if (!onLine) {
                SyncHandler(MainActivity.ins, mediaArray).doSync()
            }
            onLine = true
        }
    }
}