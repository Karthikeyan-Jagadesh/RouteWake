package com.routewake.app.ui.components

import android.content.Context
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.routewake.app.theme.RouteWakeAccentLime
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode

@Composable
fun NativeMapView(
    currentLat: Double?,
    currentLng: Double?,
    destLat: Double?,
    destLng: Double?,
    destName: String? = null,
    routePoints: List<Pair<Double, Double>> = emptyList(),
    modifier: Modifier = Modifier,
    enablePinning: Boolean = false,
    onMapPinSelected: ((Double, Double) -> Unit)? = null
) {
    if (LocalInspectionMode.current) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = RouteWakeAccentLime,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = destName ?: "Interactive Route Map Preview",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
            }
        }
        return
    }

    val context = LocalContext.current
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(14.0)

                    // Default view if no coords yet
                    val startPoint = if (currentLat != null && currentLng != null) {
                        GeoPoint(currentLat, currentLng)
                    } else if (destLat != null && destLng != null) {
                        GeoPoint(destLat, destLng)
                    } else {
                        GeoPoint(13.0827, 80.2707) // Default Chennai / India coordinates from original project
                    }
                    controller.setCenter(startPoint)

                    if (enablePinning && onMapPinSelected != null) {
                        val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                p?.let { onMapPinSelected(it.latitude, it.longitude) }
                                return true
                            }
                            override fun longPressHelper(p: GeoPoint?): Boolean = false
                        })
                        overlays.add(eventsOverlay)
                    }

                    mapViewInstance = this
                }
            },
            update = { mapView ->
                mapView.overlays.removeAll { it is Marker || it is Polyline }

                // Add Polyline if available
                if (routePoints.isNotEmpty()) {
                    val polyline = Polyline(mapView).apply {
                        outlinePaint.color = AndroidColor.parseColor("#00F6FF")
                        outlinePaint.strokeWidth = 10f
                        outlinePaint.strokeCap = Paint.Cap.ROUND
                        val geoPoints = routePoints.map { GeoPoint(it.first, it.second) }
                        setPoints(geoPoints)
                    }
                    mapView.overlays.add(polyline)
                } else if (currentLat != null && currentLng != null && destLat != null && destLng != null) {
                    // Straight line route fallback
                    val polyline = Polyline(mapView).apply {
                        outlinePaint.color = AndroidColor.parseColor("#10B981")
                        outlinePaint.strokeWidth = 6f
                        setPoints(listOf(GeoPoint(currentLat, currentLng), GeoPoint(destLat, destLng)))
                    }
                    mapView.overlays.add(polyline)
                }

                // Add Destination Marker
                if (destLat != null && destLng != null) {
                    val destMarker = Marker(mapView).apply {
                        position = GeoPoint(destLat, destLng)
                        title = destName ?: "Destination"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(destMarker)
                }

                // Add Current Location Marker
                if (currentLat != null && currentLng != null) {
                    val userMarker = Marker(mapView).apply {
                        position = GeoPoint(currentLat, currentLng)
                        title = "Current Location"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    }
                    mapView.overlays.add(userMarker)
                }

                mapView.invalidate()
            }
        )

        // Floating Recenter Button
        if (currentLat != null && currentLng != null) {
            FloatingActionButton(
                onClick = {
                    mapViewInstance?.controller?.animateTo(GeoPoint(currentLat, currentLng))
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(44.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = RouteWakeAccentLime
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Recenter GPS",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
