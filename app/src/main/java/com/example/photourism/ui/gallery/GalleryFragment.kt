package com.example.photourism.ui.gallery



import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.photourism.R
import com.example.photourism.databinding.FragmentGalleryBinding
import java.io.File


class GalleryFragment : Fragment(R.layout.fragment_gallery) {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_gallery, container, false)

         val grid = root.findViewById<GridView>(R.id.galleryGrid)

        readDirectory(grid)
        return root

   }

   fun  readDirectory(gridView: GridView){
       File(getString(R.string.GalleryDir)).walk().forEach {

           if (it.endsWith(".jpg")) {
               val myBitmap = BitmapFactory.decodeFile(it.getAbsolutePath())
               displayImg(myBitmap, gridView)
           }
       }
   }

   fun displayImg( img : Bitmap, gridView: GridView) {
       val newView: ImageView = ImageView(this.context)

        newView.setImageBitmap(img)
        gridView.addView(newView)

        newView.layoutParams.height = 200
        newView.layoutParams.width = 200
   }
   override fun onDestroyView() {
       super.onDestroyView()
       _binding = null
   }
}