package com.example.photourism.ui.gallery



import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageView
import androidx.fragment.app.Fragment
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

        val gridItemArrayList: ArrayList<GridItem> = ArrayList <GridItem>()
        File(getOutputDirectory().toString()).walk().forEach {
            if (it.path.endsWith(".jpg")) {
                Log.v("Found File", "Found file -> $it")
                gridItemArrayList.add(GridItem(it.name, it.absolutePath))
                var i = 0
                Log.v("GridItem", "Item -> " + gridItemArrayList[i++])
            }
        }
        val adapter = GridViewAdapter(this.context!!, gridItemArrayList)
        grid.adapter = adapter
        return root

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

    fun getOutputDirectory(): File {
        val mediaDir = activity?.externalMediaDirs?.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else activity?.filesDir!!
    }
}