/*
 * Copyright (C)  2015 - 2019 Lukoh Nam, goForer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goforer.base.presentation.view.holder

import androidx.recyclerview.widget.ItemTouchHelper

/**
 * A ItemHolderBinder describes an item view and metadata about its place within the RecyclerView.
 *
 *
 * All ViewHolder extended [BaseViewHolder] implementations should subclass ItemHolderBinder
 * and implements bindItemHolder method to to display the item at the specified position.
 *
 */
interface ItemHolderBinder<T> {
    /**
     * Called by RecyclerView to display the item at the specified position. This method
     * should update the contents of the item's view to reflect the item at the given position.
     *
     * @param holder A BaseViewHolder describes an item view and metadata about its place within
     * the RecyclerView
     * @param item The ItemHolder which should be updated to represent the contents of the
     * item at the given position in the data set
     * @param position The ItemHolder given position in the data set
     */
    fun bindItemHolder(holder: BaseViewHolder<*>, item: T, position: Int)

    /**
     * Called when the [ItemTouchHelper] first registers an item as being moved or swiped.
     * Implementations should update the item view to indicate it's active state.
     */
    fun onItemSelected()


    /**
     * Called when the [ItemTouchHelper] has completed the move or swipe, and the active item
     * state should be cleared.
     */
    fun onItemClear()
}