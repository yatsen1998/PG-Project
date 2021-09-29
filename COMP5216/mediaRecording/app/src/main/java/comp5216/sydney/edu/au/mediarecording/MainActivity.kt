package comp5216.sydney.edu.au.mediarecording

import android.app.ProgressDialog
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    var gridView: GridView? = null
    var cameraMenuButton: FloatingActionButton? = null
    var imageButton: FloatingActionButton? = null
    var videoButton: FloatingActionButton? = null

    var imageArray = ArrayList<ByteArray>()
    var videoArray = ArrayList<ByteArray>()

    var imageFileName: String = "photo.jpg"
    var videoFileName: String = "video.mp4"

    var marshmallowPermission = MarshmallowPermission(this)
    private var imageUri: Uri? = null
    private var videoUri: Uri? = null

    private var isFABOpen = false

    var auth = FirebaseAuth.getInstance()
    var storage: FirebaseStorage? = null
    var storageReference: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById<View?>(R.id.mainGridView) as GridView

        cameraMenuButton = findViewById(R.id.mainFabCameraMenu)
        imageButton = findViewById(R.id.mainFabTakeImage)
        videoButton = findViewById(R.id.mainFabTakeVideo)

        val user = auth.currentUser
        if (user == null) {
            signInAnonymously()
        }
        storage = Firebase.storage
        storageReference = storage!!.reference

        cameraMenuButton?.setOnClickListener {
            if (!isFABOpen) {
                showFABMenu()
            } else {
                closeFABMenu()
            }
        }

        //setupGridviewListener()
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
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        // [END signin_anonymously]
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

            imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                /*7.0以上要通过FileProvider将File转化为Uri*/
                FileProvider.getUriForFile(
                    this,
                    "comp5216.sydney.edu.au.mediarecording.fileProvider",
                    imageFile
                )
            } else {
                /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                Uri.fromFile(imageFile)
            }
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
            val intent= Intent(MediaStore.ACTION_VIDEO_CAPTURE)

            val videoFile = getMediaFile("video")

            videoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                /*7.0以上要通过FileProvider将File转化为Uri*/
                FileProvider.getUriForFile(
                    this,
                    "comp5216.sydney.edu.au.mediarecording.fileProvider",
                    videoFile
                )
            } else {
                /*7.0以下则直接使用Uri的fromFile方法将File转化为Uri*/
                Uri.fromFile(videoFile)
            }
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
                mediaFile = File.createTempFile(imageFileName, ".jpg", imageStorageDir)
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
                mediaFile = File.createTempFile(imageFileName, ".mp4", videoStorageDir)
            } catch (ex: Exception) {
                Log.e("getImageFile", ex.stackTrace.toString())
            }
            return mediaFile
        }
        return mediaFile
    }

    private fun setupGridviewListener() {
        //TODO
    }

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
                imageArray.add(bytes.toByteArray())
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
                imageArray.add(bytes.toByteArray())
                bytes.close()
            } else {
                Toast.makeText(
                    this, "Video wasn't taken!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        gridView!!.adapter = ImageAdapter(this, imageArray)
    }

    fun onCloudSyncClick(view: View?) {
        if (imageUri == null) {
            Toast.makeText(
                this, "No Picture to upload now!", Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Code for showing progressDialog while uploading
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        // Defining the child of storageReference
        val uploadTask = storageReference?.child("images/$imageFileName")?.putFile(imageUri!!)
        uploadTask!!.addOnSuccessListener {
            //// Image uploaded successfully, dismiss the dialog
            progressDialog.dismiss()
            Toast.makeText(
                this, "Image Uploaded!!", Toast.LENGTH_SHORT
            ).show()
        } .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(
                    this, "Image Sync Failed!", Toast.LENGTH_SHORT
                ).show()
        }
    }

    companion object {
        //request codes
        private const val MY_PERMISSIONS_REQUEST_OPEN_CAMERA: Int = 101
        private const val MY_PERMISSIONS_REQUEST_READ_PHOTOS: Int = 102
        private const val MY_PERMISSIONS_REQUEST_RECORD_VIDEO: Int = 103
        private const val MY_PERMISSIONS_REQUEST_READ_VIDEOS: Int = 104
    }
}