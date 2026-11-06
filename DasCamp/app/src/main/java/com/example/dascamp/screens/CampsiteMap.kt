package com.example.dascamp.screens

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@SuppressLint("ClickableViewAccessibility")  //hide touch warnings -> harmless, for the scrollable map implementation
@Composable
fun CampsiteMap(latitude: Double,
                longitude: Double,
                modifier: Modifier = Modifier,
                onLocationPicked: ((latitude: Double, longitude: Double) -> Unit)? = null // location picked by owner, can be null when map needs to be view only
){
    val context = LocalContext.current  //apps link to andriod, needed to create the map
    val currentCallback by rememberUpdatedState(onLocationPicked)  //keep latest location picked

    //create map
    val map = remember{
        Configuration.getInstance().apply{
            load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            userAgentValue = "DasCamp-nobugs (301 student project) https://github.com/CMPUT301F26nobugs/nobugs-project"  //required by OpenStreetMap -> app identifier
        }
        MapView(context).apply{
            setTileSource(  //map tiles
                XYTileSource(
                    "OSM", 0, 19, 256, ".png",
                    arrayOf("https://tile.openstreetmap.org/"),
                    "© OpenStreetMap contributors"  //text shown by CopyrightOverlay
                )
            )
            setMultiTouchControls(true)  //multitouch so that you can pinch zoom
            controller.setZoom(10.0)  //how zoomed in it is
            controller.setCenter(GeoPoint(latitude, longitude))  //set map start location to location picked
            overlays.add(CopyrightOverlay(context))  //show the OpenStreetMap credit

            //allow map to be scrollable without scrolling the parent page
            setOnTouchListener{touchedMap, _ ->
                val parent = touchedMap.parent  //the page holding the map
                if(parent != null){  //if parent exists
                    parent.requestDisallowInterceptTouchEvent(true)  //tell parent to let the map handle the drag touch
                }
                false  //return false, map uses its own drag handler
            }
        }  //bc this is the last line, remeber returns the map view
    }

    //location pin
    val pin = remember{
        Marker(map).apply{  //mark the location
            position = GeoPoint(latitude, longitude)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "Campsite Location"
        }
    }

    //runs when map appears and disapears
    DisposableEffect(map){
        //listen for taps on the map
        val tapReciver = object: MapEventsReceiver{
            override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                val callback = currentCallback
                if(callback == null){  //if there is no tap/ignore taps -> for when map is view only
                    return false
                }
                pin.position = p  //moving the pin
                map.invalidate()  //redraw the map
                callback(p.latitude, p.longitude)  //tell the owner screen

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