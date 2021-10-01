package comp5216.sydney.edu.au.mediarecording

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    // Components
    var gridView: GridView? = null
    var cameraMenuButton: FloatingActionButton? = null
    var imageButton: FloatingActionButton? = null
    var videoButton: FloatingActionButton? = null
    private var isFABOpen = false

    // Arrays
    var imageByteArray = ArrayList<ByteArray>()
    var mediaArray = ArrayList<File>()

    // Default media file name and Uri
    var imageFileName: String = "photo.jpg"
    var videoFileName: String = "video.mp4"
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    // Permission control, including Android and Firebase
    var marshmallowPermission = MarshmallowPermission(this)
    var auth = FirebaseAuth.getInstance()

    // Alarm and Sync handle
    lateinit var syncHandler: SyncHandler
    var alarmManager: AlarmManager? = null
    var pendingIntent: PendingIntent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ins = this;

        // Find mainView component
        gridView = findViewById<View?>(R.id.mainGridView) as GridView
        cameraMenuButton = findViewById(R.id.mainFabCameraMenu)
        imageButton = findViewById(R.id.mainFabTakeImage)
        videoButton = findViewById(R.id.mainFabTakeVideo)

        //val user = auth.currentUser
        //if (user != null) {
        //} else {
        signInAnonymously()
        //}

        cameraMenuButton?.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        updateAlarmManager(mediaArray)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancels the pendingIntent if it is no longer needed after this activity is destroyed.
        alarmManager!!.cancel(pendingIntent)
    }

    fun updateAlarmManager(mediaArray: ArrayList<File>) {
        alarmManager!!.cancel(pendingIntent)
        if (mediaArray.size != 0) {
            val intent = Intent(this, SyncReceiver::class.java)
            intent.putExtra("mediaArray", mediaArray)
            pendingIntent = PendingIntent.getBroadcast(
                this,
                SYNC_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.set(Calendar.HOUR_OF_DAY, 2)
            calendar.set(Calendar.MINUTE, 0)

            // Starts the alarm manager
            alarmManager!!.setRepeating(
                AlarmManager.RTC,
                calendar.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }

    private fun signInAnonymously() {
        //SignIn anonymously to avoid security problems
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("SignIn", "signInAnonymously:success")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignIn", "signInAnonymously:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showFABMenu() {
        isFABOpen = true
        imageButton!!.animate().translationX(-resources.getDimension(R.dimen.standard_70))
        videoButton!!.animate().translationX(-resources.getDimension(R.dimen.standard_125))
    }

    private fun closeFABMenu() {
        isFABOpen = false
        imageButton!!.animate().translationX(0F)
        videoButton!!.animate().translationX(0F)
    }

    fun onRequestTakeImage(view: View?) {
        if (!marshmallowPermission.checkPermissionForCamera()
            || !marshmallowPermission.checkPermissionForExternalStorage()
        ) {
            marshmallowPermission.requestPermissionForCamera()
        } else {
            // create Intent to take a picture and return control to the calling application
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create a photo file and its reference
            val imageFile = getMediaFile("image")
            mediaArray.add(imageFile)
            updateAlarmManager(mediaArray)

            imageUri = getUriFromFile(imageFile)
            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            // If you call startActivityForResult() using an intent that no app can handle,
            // your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA)
            }
        }
    }

    fun onRequestTakeVideo(view: View?) {
        if (!marshmallowPermission.checkPermissionForCamera()
            || !marshmallowPermission.checkPermissionForExternalStorage()
        ) {
            marshmallowPermission.requestPermissionForCamera()
        } else {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val videoFile = getMediaFile("video")
            mediaArray.add(videoFile)
            updateAlarmManager(mediaArray)

            videoUri = getUriFromFile(videoFile)
            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)

            // If you call startActivityForResult() using an intent that no app can handle,
            // your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_RECORD_VIDEO)
            }
        }
    }

    fun onCloudSyncClick(view: View?) {
        syncHandler = SyncHandler(this, mediaArray)
        syncHandler.run()
    }

    fun getUriFromFile(mediaFile: File): Uri? {
        var mediaUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            /*7.0以上要通过FileProvider将File转化为Uri*/
            FileProvider.getUriForFile(
                this,
                "comp5216.sydney.edu.au.mediarecording.fileProvider",
                mediaFile
            )
        } else {
            /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
            Uri.fromFile(mediaFile)
        }
        return mediaUri
    }


    // Returns the Uri for a photo/media stored on disk given the fileName and type
    private fun getMediaFile(type: String): File {
        lateinit var mediaFile: File

        // set file name
        val timeStamp = SimpleDateFormat(
            "yyyyMMDD_HHmmss", Locale.getDefault()
        ).format(Date())

        // Get safe media storage directory depending on type

        if (type == "image") {
            imageFileName = "IMG_$timeStamp.jpg"
            val imageStorageDir =
                File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString()
                            + File.separator
                )
            if (!imageStorageDir.exists()) {
                imageStorageDir.mkdirs()
            }
            try {
                // Create the file target for the media based on filename
                mediaFile = File(imageStorageDir, imageFileName)
            } catch (ex: Exception) {
                Log.e("getImageFile", ex.stackTrace.toString())
            }
            return mediaFile
        } else if (type == "video") {
            videoFileName = "VIDEO_$timeStamp.mp4"
            val videoStorageDir =
                File(
                    getExternalFilesDir(Environment.DIRECTORY_MOVIES).toString()
                            + File.separator
                )
            if (!videoStorageDir.exists()) {
                videoStorageDir.mkdirs()
            }
            try {
                // Create the file target for the media based on filename
                mediaFile = File(videoStorageDir, videoFileName)
            } catch (ex: Exception) {
                Log.e("getVideoFile", ex.stackTrace.toString())
            }
            return mediaFile
        }
        return mediaFile
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                val imageThumbnail = ThumbnailUtils.extractThumbnail(
                    BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!)),
                    1024, 1024
                )
                val bytes = ByteArrayOutputStream()
                imageThumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                imageByteArray.add(bytes.toByteArray())
                bytes.close()
            } else {
                Toast.makeText(
                    this, "Picture wasn't taken!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_VIDEO) {
            if (resultCode == RESULT_OK) {
                val videoThumbnail = ThumbnailUtils.createVideoThumbnail(
                    videoUri!!.path.toString(), MediaStore.Video.Thumbnails.MICRO_KIND
                )

                val bytes = ByteArrayOutputStream()
                videoThumbnail!!.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                imageByteArray.add(bytes.toByteArray())
                bytes.close()
            } else {
                Toast.makeText(
                    this, "Video wasn't taken!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        gridView!!.adapter = ImageAdapter(this, imageByteArray)
    }

    companion object {
        //request codess
        private const val SYNC_REQUEST_CODE = 100
        private const val MY_PERMISSIONS_REQUEST_OPEN_CAMERA: Int = 101
        private const val MY_PERMISSIONS_REQUEST_READ_PHOTOS: Int = 102
        private const val MY_PERMISSIONS_REQUEST_RECORD_VIDEO: Int = 103
        private const val MY_PERMISSIONS_REQUEST_READ_VIDEOS: Int = 104

        var ins: MainActivity? = null
        fun getInstance(): MainActivity? {
            return ins
        }
    }
}
