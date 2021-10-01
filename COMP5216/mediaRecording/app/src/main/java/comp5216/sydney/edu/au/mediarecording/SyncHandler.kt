package comp5216.sydney.edu.au.mediarecording

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import java.io.File

class SyncHandler(c: Context?, m: ArrayList<File>) {
    var context: Context? = c
    var mediaArray: ArrayList<File> = m
    var storage = Firebase.storage
    var storageReference= storage.reference

    fun run() {
        if (!isWifiOn(context)) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.dialog_sync_anyway_title)
                .setMessage(R.string.dialog_sync_anyway_msg)
                .setPositiveButton(R.string.confirm) { dialogInterface, i ->
                    doSync()
                }
                .setNegativeButton(R.string.cancel) { dialogInterface, i ->

                }
            builder.create().show()
        } else {
            doSync()
        }
    }

    fun isWifiOn(context: Context?): Boolean {
        val connectivityManager: ConnectivityManager = context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo: NetworkInfo? = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    fun doSync() {
        if (mediaArray.size == 0) {
            Toast.makeText(
                context, "No Picture to upload now!", Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Code for showing progressDialog while uploading
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        var uploadTask: UploadTask? = null
        var i = 0
        // Defining the child of storageReference
        while(i < mediaArray.size) {
            if (mediaArray[i].absolutePath.endsWith("jpg")) {
                uploadTask = storageReference.child("images/" + mediaArray[i].name).putFile(getUriFromFile(mediaArray[i])!!)
            } else if (mediaArray[i].absolutePath.endsWith("mp4")) {
                uploadTask = storageReference.child("movies/" + mediaArray[i].name).putFile(getUriFromFile(mediaArray[i])!!)
            }
            uploadTask!!.addOnSuccessListener {
                //// Image uploaded successfully, dismiss the dialog
                progressDialog.dismiss()
                Toast.makeText(
                    context, "FIle Uploaded!!", Toast.LENGTH_SHORT
                ).show()
            } .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    context, "Sync Failed!", Toast.LENGTH_SHORT
                ).show()
            }
            i += 1
        }
    }

    fun getUriFromFile(mediaFile: File): Uri? {
        val mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /*7.0以上要通过FileProvider将File转化为Uri*/
            FileProvider.getUriForFile(
                context!!,
                "comp5216.sydney.edu.au.mediarecording.fileProvider",
                mediaFile
            )
        } else {
            /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
            Uri.fromFile(mediaFile)
        }
        return mediaUri
    }
}