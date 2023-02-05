package com.example.photourism.ui.gallery

class GridItem (private var name: String, private var path: String) {

    public fun getName(): String {
        return name
    }

    public fun setName(name: String) {
        this.name = name
    }

    public fun getPath(): String {
        return path
    }

    public fun setPath(path: String) {
        this.path = path
    }
}