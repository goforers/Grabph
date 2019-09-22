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

@file:Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.goforer.grabph.presentation.ui.map

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.goforer.base.presentation.utils.CommonUtils.showToastMessage
import com.goforer.grabph.R
import com.goforer.grabph.presentation.caller.Caller.EXTRA_ADDRESS
import com.goforer.grabph.presentation.caller.Caller.EXTRA_LATITUDE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_LONGITUDE
import com.goforer.grabph.presentation.caller.Caller.EXTRA_PHOTO_TITLE
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*

class MapsActivity: AppCompatActivity(), GoogleMap.OnMarkerDragListener, StreetViewPanorama.OnStreetViewPanoramaChangeListener {
    private lateinit var title: String

    private lateinit var latLng: LatLng
    private lateinit var markerPosition: LatLng

    private lateinit var streetViewPanorama: StreetViewPanorama

    private lateinit var marker: Marker

    private var latitude: Double = 0.toDouble()
    private var longitude: Double = 0.toDouble()

    private var address: String? = null

    companion object {
        private const val MAP_ZOOM = 17f

        private const val MARKER_POSITION_KEY = "MarkerPosition"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_maps)
        savedInstanceState ?: getIntentData()
        savedInstanceState?.let {
            title = savedInstanceState.getString(EXTRA_PHOTO_TITLE, "")
            latitude = savedInstanceState.getDouble(EXTRA_LATITUDE, -1.0)
            longitude = savedInstanceState.getDouble(EXTRA_LONGITUDE, -1.0)
            address = savedInstanceState.getString(EXTRA_ADDRESS, "")
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY)!!
        }

        val streetViewPanoramaFragment = supportFragmentManager.findFragmentById(R.id.streetview_panorama) as SupportStreetViewPanoramaFragment?
        streetViewPanoramaFragment?.getStreetViewPanoramaAsync { panorama ->
            streetViewPanorama = panorama
            streetViewPanorama.setOnStreetViewPanoramaChangeListener(
                    this@MapsActivity)
            // Only need to set the position once as the streetview fragment will maintain its state.
            savedInstanceState ?: streetViewPanorama.setPosition(latLng)
        }

        (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).getMapAsync { map ->
            map.setOnMarkerDragListener(this@MapsActivity)
            map.uiSettings.isZoomControlsEnabled = false
            // Add a marker in my location and move the camera
            val myLocation = LatLng(latitude, longitude)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, MAP_ZOOM))
            val zoom = CameraUpdateFactory.zoomTo(MAP_ZOOM)
            map.animateCamera(zoom)
            markerPosition.let {
                marker = map.addMarker(MarkerOptions()
                        .position(markerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pegman))
                        .title("Address").infoWindowAnchor(0.5f, 0.5f)
                        .snippet(address)
                        .draggable(true))
            }

            marker.showInfoWindow()

            map.setOnMarkerClickListener {
                showMessage()
                false
            }
        }

        val actionBar = supportActionBar
        actionBar?.let {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_back)
            actionBar.displayOptions = ActionBar.DISPLAY_HOME_AS_UP or ActionBar.DISPLAY_USE_LOGO
            actionBar.elevation = 0f
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.title = title
            actionBar.setDisplayShowTitleEnabled(true)
            actionBar.setHomeButtonEnabled(false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(EXTRA_PHOTO_TITLE, title)
        outState.putDouble(EXTRA_LATITUDE, latitude)
        outState.putDouble(EXTRA_LONGITUDE, longitude)
        outState.putString(EXTRA_ADDRESS, address)
        outState.putParcelable(MARKER_POSITION_KEY, marker.position)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        title = savedInstanceState.getString(EXTRA_PHOTO_TITLE, "")
        latitude = savedInstanceState.getDouble(EXTRA_LATITUDE, -1.0)
        longitude = savedInstanceState.getDouble(EXTRA_LONGITUDE, -1.0)
        address = savedInstanceState.getString(EXTRA_ADDRESS, "")
        markerPosition = savedInstanceState.getParcelable(MARKER_POSITION_KEY)!!
    }

    override fun onBackPressed() {
        finishAfterTransition()

        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()

                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onMarkerDragStart(marker: Marker) { }

    override fun onMarkerDrag(marker: Marker) { }

    override fun onMarkerDragEnd(marker: Marker) {
        streetViewPanorama.setPosition(marker.position, 150)
    }

    override fun onStreetViewPanoramaChange(streetViewPanoramaLocation: StreetViewPanoramaLocation?) {
        streetViewPanoramaLocation?.let {
            marker.position = streetViewPanoramaLocation.position
        }
    }

    private fun getIntentData() {
        title = intent.getStringExtra(EXTRA_PHOTO_TITLE)
        latitude = intent.getDoubleExtra(EXTRA_LATITUDE, -1.0)
        longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, -1.0)
        address = intent.getStringExtra(EXTRA_ADDRESS)
        latLng = LatLng(latitude, longitude)
        markerPosition = latLng
    }

    private fun showMessage() {
        showToastMessage(this, "Let's show the tour information", Toast.LENGTH_SHORT)
    }
}
