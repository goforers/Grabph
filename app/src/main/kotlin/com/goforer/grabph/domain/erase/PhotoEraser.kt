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

package com.goforer.grabph.domain.erase

import com.goforer.grabph.presentation.event.action.DeletePhotoAction
import org.greenrobot.eventbus.EventBus
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoEraser @Inject constructor() {
    fun deletePhoto(path: String, position: Int): Boolean {
        val file = File(path)
        if (file.exists()) {
            return if (file.delete()) {
                val action = DeletePhotoAction()
                action.position = position
                action.path = path
                EventBus.getDefault().post(action)

                true
            } else {
                false
            }
        }

        return false
    }
}