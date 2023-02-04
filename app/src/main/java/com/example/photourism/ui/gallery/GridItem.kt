package com.example.photourism.ui.gallery

class GridItem (private var name: String, private var path: String) {

    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    fun getPath(): String {
        return path
    }

    fun setPath(path: String) {
        this.path = path
    }
}