package comp5216.sydney.edu.au.mediarecording

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MarshmallowPermission(var activity: Activity) {
    fun checkPermissionForRecord(): Boolean {
        val result = ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionForExternalStorage(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionForCamera(): Boolean {
        val result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionForReadfiles(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionForRecord() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            Toast.makeText(
                activity,
                "Microphone permission needed for recording. Please allow in App Settings for additional functionality.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_PERMISSION_REQUEST_CODE
            )
        }
    }

    fun requestPermissionForExternalStorage() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                activity,
                "External Storage permission needed. Please allow in App Settings for additional functionality.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE
            )
            print("get storage permission")
        }
    }

    fun requestPermissionForCamera() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.CAMERA
            )
        ) {
            Toast.makeText(
                activity,
                "Camera permission needed. Please allow in App Settings for additional functionality.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            print("get camera permission")
        }
    }

    fun requestPermissionForReadfiles() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            Toast.makeText(
                activity,
                "Read files permission needed. Please allow in App Settings for additional functionality.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READFILES_PERMISSION_REQUEST_CODE
            )
        }
    }

    companion object {
        const val RECORD_PERMISSION_REQUEST_CODE = 1
        const val EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 2
        const val CAMERA_PERMISSION_REQUEST_CODE = 3
        const val READFILES_PERMISSION_REQUEST_CODE = 4
    }
}