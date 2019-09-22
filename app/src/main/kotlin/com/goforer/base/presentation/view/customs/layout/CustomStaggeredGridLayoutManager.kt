package com.goforer.base.presentation.view.customs.layout

import androidx.recyclerview.widget.StaggeredGridLayoutManager

class CustomStaggeredGridLayoutManager constructor(spanCount: Int, orientation: Int): StaggeredGridLayoutManager(spanCount, orientation) {
    var enabledSrcoll: Boolean = false

    override fun canScrollVertically(): Boolean {
        return enabledSrcoll && super.canScrollVertically()
    }
}