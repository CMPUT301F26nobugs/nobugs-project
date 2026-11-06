package com.example.dascamp.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@Composable
fun CampsiteMap(latitude: Double,
                longitude: Double,
                modifier: Modifier = Modifier,
                onLocationPicked: ((latitude: Double, longitude: Double) -> Unit) // location picked by owner
){
    val context = LocalContext.current  //apps link to andriod, needed to create the map
    val currentCallback by rememberUpdatedState(onLocationPicked)  //keep latest location picked

    //create map
    val map = remember{
        Configuration.getInstance().apply{
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = context.packageName  //required by OpenStreetMap
        }
        MapView(context).apply{
            setTileSource(TileSourceFactory.MAPNIK)  //standard map
            setMultiTouchControls(true)  //multitouch so that you can pinch zoom
            controller.setZoom(10.0)  //how zoomed in it is
            controller.setCenter(GeoPoint(latitude, longitude))  //set map start location to location picked
        }  //bc this is the last line, remeber returns the map view
    }

    //location pin
    val pin = remember{
        Marker(map).apply{  //mark the location
            position = GeoPoint(latitude, longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            //---------------------------------------
            title = "Campsite Name"  //PLACEHODLER
            //---------------------------------------
        }
    }

    //runs when map appears and disapears
    DisposableEffect(map){
        //listen for taps on the map
        val tapReciver = object: MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                pin.position = p  //moving the pin
                map.invalidate()  //redraw the map
                currentCallback(p.latitude, p.longitude)  //tell the owner screen
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false  //ignore long presses
        }
        map.overlays.add(MapEventsOverlay(tapReciver))  //start the tap listener
        map.overlays.add(pin)  //show the pin on the screen
        map.onResume()  //load the map -> tell the map listener thats its on the screen

        onDispose{
            map.onPause()  //tell the map listenr that the map is leaving the screen
            map.onDetach()  //free up the maps memory
        }
    }
    AndroidView(factory = {map}, modifier = modifier)
}