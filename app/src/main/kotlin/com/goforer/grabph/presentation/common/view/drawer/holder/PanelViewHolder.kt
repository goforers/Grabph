/*
 * Copyright 2019 Lukoh Nam, goForer
 *    
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, 
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * You should have received a copy of the GNU General Public License along with this program.  
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.goforer.grabph.presentation.common.view.drawer.holder

import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.goforer.grabph.R
import com.mikepenz.materialdrawer.model.BaseViewHolder

open class PanelViewHolder(view: View) : BaseViewHolder(view) {
    internal var caption: AppCompatTextView = view.findViewById(R.id.material_drawer_count)

    internal var arrowContainer: LinearLayoutCompat = view.findViewById(R.id.material_drawer_arrow_container)

    fun getView(): View {
        return view
    }

    fun getIcon(): AppCompatImageView {
        return icon as AppCompatImageView
    }

    fun getName(): AppCompatTextView {
        return name as AppCompatTextView
    }

    fun getDescription(): AppCompatTextView {
        return description as AppCompatTextView
    }

}
