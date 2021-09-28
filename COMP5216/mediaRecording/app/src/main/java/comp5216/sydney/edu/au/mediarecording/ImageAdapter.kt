package comp5216.sydney.edu.au.mediarecording

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import comp5216.sydney.edu.au.mediarecording.SquareImageView
import java.util.ArrayList

class ImageAdapter(c: Context, images: ArrayList<ByteArray>) : BaseAdapter() {
    var context: Context = c
    private var images: ArrayList<ByteArray> = images

    override fun getCount(): Int {
        return images.size
    }

    override fun getItem(position: Int): Any {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val img = BitmapFactory.decodeByteArray(images[position], 0, images[position].size)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val view = inflater.inflate(R.layout.grid_image, null)
        val imageView = view.findViewById<SquareImageView>(R.id.gridImageView)
        imageView.setImageBitmap(img)
        return view
    }
}