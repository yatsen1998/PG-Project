package comp5216.sydney.edu.au.mediarecording

import android.content.Context
import android.util.AttributeSet
import android.widget.VideoView

class SquareVideoView : VideoView {
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!,
        attrs,
        defStyle
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width: Int = measuredWidth
        setMeasuredDimension(width, width)
    }
}