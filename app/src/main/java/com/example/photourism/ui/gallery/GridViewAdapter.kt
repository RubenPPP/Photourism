package com.example.photourism.ui.gallery

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.photourism.R

class GridViewAdapter(context: Context, GridItemArrayList: ArrayList<GridItem>) :
    ArrayAdapter<GridItem?>(context, 0, GridItemArrayList as List<GridItem?>) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var listitemView = convertView
        if (listitemView == null) {
            // Layout Inflater inflates each item to be displayed in GridView.
            listitemView = LayoutInflater.from(context).inflate(R.layout.griditem, parent, false)
        }

        val item: GridItem? = getItem(position)
        val name = listitemView!!.findViewById<TextView>(R.id.GridItemName)
        val img = listitemView.findViewById<ImageView>(R.id.GridItemImg)

        name.setText(item!!.getName())
        img.setImageBitmap(BitmapFactory.decodeFile(item.getPath()))
        return listitemView
    }
}