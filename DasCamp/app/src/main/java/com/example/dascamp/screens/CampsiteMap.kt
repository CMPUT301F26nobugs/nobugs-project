package com.example.dascamp.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.osmdroid.api.IGeoPoint
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint

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
        }
    }
}