package comp5216.sydney.edu.au.mediarecording

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var gridView: GridView? = null
    var imageArray = ArrayList<ByteArray>()

    val APP_TAG = "MobileComputingTutorial"
    var imageFileName: String = "photo.jpg"
    var videoFileName = "video.mp4"

    var marshmallowPermission = MarshmallowPermission(this)
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById<View?>(R.id.mainGridView) as GridView

        gridView!!.adapter = ImageAdapter(this, imageArray)
    }

    fun onRequestCameraClick(view: View?) {
        if (!marshmallowPermission.checkPermissionForCamera()
            || !marshmallowPermission.checkPermissionForExternalStorage()
        ) {
            marshmallowPermission.requestPermissionForCamera()
        } else {
            // create Intent to take a picture and return control to the calling application
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create a photo file and its reference
            val imageFile = getImageFile()

            imageUri = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                /*7.0以上要通过FileProvider将File转化为Uri*/
                FileProvider.getUriForFile(
                    this,
                    "comp5216.sydney.edu.au.mediaaccess.fileProvider",
                    imageFile
                )
            }else {
                /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                Uri.fromFile(imageFile)
            }
            // Add extended data to the intent
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

            // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
            // So as long as the result is not null, it's safe to use the intent.
            if (intent.resolveActivity(packageManager) != null) {
                // Start the image capture intent to take photo
                startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OPEN_CAMERA)
            }
        }
    }

    // Returns the Uri for a photo/media stored on disk given the fileName and type
    fun getImageFile(): File {
        lateinit var imageFile: File
        // set file name
        val timeStamp = SimpleDateFormat(
                "yyyyMMDD_HHmmss", Locale.getDefault()).format(Date())
        imageFileName = "IMG_$timeStamp.jpg"

        // Get safe media storage directory depending on type
        val imageStorageDir = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString() + File.separator)

        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs()
        }

        try {
            // Create the file target for the media based on filename
            imageFile = File.createTempFile(imageFileName, ".jpg", imageStorageDir)
        } catch (ex: Exception) {
            Log.e("getImageFile", ex.stackTrace.toString())
        }
        return imageFile
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_PERMISSIONS_REQUEST_OPEN_CAMERA) {
            if (resultCode == RESULT_OK) {
                val takenImage = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!))
                val imageThumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri!!)), 1024, 1024)
                val bytes = ByteArrayOutputStream()
                imageThumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes)
                imageArray.add(bytes.toByteArray())
                bytes.close()
            } else {
                Toast.makeText(
                    this, "Picture wasn't taken!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        gridView!!.adapter = ImageAdapter(this, imageArray)
    }

    companion object {
        //request codes
        private const val MY_PERMISSIONS_REQUEST_OPEN_CAMERA: Int = 101
        private const val MY_PERMISSIONS_REQUEST_READ_PHOTOS: Int = 102
        private const val MY_PERMISSIONS_REQUEST_RECORD_VIDEO: Int = 103
        private const val MY_PERMISSIONS_REQUEST_READ_VIDEOS: Int = 104
    }
}