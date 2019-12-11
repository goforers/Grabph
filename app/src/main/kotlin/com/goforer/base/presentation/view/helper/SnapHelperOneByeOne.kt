package com.goforer.base.presentation.view.helper

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper

/**
 * This class is for making RecyclerView to scroll item one by one.
 * */
class SnapHelperOneByeOne: GravitySnapHelper {

    constructor(gravity: Int): super(gravity)
    constructor(gravity: Int, enableSnapLastItem: Boolean): super(gravity, enableSnapLastItem)
    constructor(gravity: Int, enableSnapLastItem: Boolean, snapListener: SnapListener?): super(gravity, enableSnapLastItem, snapListener)

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager?, velocityX: Int, velocityY: Int): Int {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return RecyclerView.NO_POSITION
        }

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val myLayoutaManager = layoutManager as LinearLayoutManager

        var currentPosition = layoutManager.getPosition(currentView)

        val position1 = myLayoutaManager.findFirstVisibleItemPosition()
        val position2 = myLayoutaManager.findLastVisibleItemPosition()

        if (velocityX > 400) {
            currentPosition = position2
        } else if (velocityX <= 400) {
            currentPosition = position1
        }

        if (currentPosition == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }

        return currentPosition
    }
}