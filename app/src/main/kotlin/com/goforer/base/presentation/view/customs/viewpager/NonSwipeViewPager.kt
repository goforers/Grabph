package com.goforer.base.presentation.view.customs.viewpager

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * This is just for blocking swiping from original viewPager
 * */

class NonSwipeViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs){

    private var disable: Boolean = false;

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if(disable) false else super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if(disable) false else super.onTouchEvent(ev)
    }

    fun disableSwipe(disable: Boolean){
        this.disable = disable
    }
}