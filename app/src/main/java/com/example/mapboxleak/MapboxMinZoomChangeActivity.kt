package com.example.mapboxleak

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.postDelayed
import androidx.fragment.app.Fragment
import androidx.fragment.app.commitNow
import com.example.mapboxleak.databinding.ActivityMapboxLeakBinding
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.layers.getLayerAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.MapAnimationOptions

class MapboxMinZoomChangeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapboxLeakBinding

    private val mapFragment
        get() = supportFragmentManager.findFragmentByTag("map")!! as MinZoomChangeFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapboxLeakBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btToggle.setOnClickListener {
            it.visibility = View.GONE
            mapFragment.toggleBackgroundLayer(0.0)
            mapFragment.zoom()
        }

        supportFragmentManager.commitNow {
            val fragment = MinZoomChangeFragment()
            this.add(binding.mapContainer.id, fragment, "map")
        }

    }

}

class MinZoomChangeFragment : Fragment() {

    private val mapView
        get() = requireView() as MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = MapView(
        requireContext(),
        MapInitOptions(
            requireContext(),
            textureView = false,
            plugins = listOf(
                Plugin.Mapbox(Plugin.MAPBOX_CAMERA_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_GESTURES_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_COMPASS_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_LOGO_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_ATTRIBUTION_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_LOCATION_COMPONENT_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_LIFECYCLE_PLUGIN_ID),
                Plugin.Mapbox(Plugin.MAPBOX_MAP_OVERLAY_PLUGIN_ID)
            ),
            resourceOptions = MapInitOptions.getDefaultResourceOptions(requireContext())
                .toBuilder()
                .accessToken(resources.getString(R.string.mapbox_access_token))
                .tileStoreUsageMode(TileStoreUsageMode.DISABLED)
                .build(),
            mapOptions = MapInitOptions.getDefaultMapOptions(requireContext()).toBuilder().apply {
                this.optimizeForTerrain(false)
                this.contextMode(ContextMode.UNIQUE)
            }.build()
        )
    ).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // if clustering is enabled one view annotation will be missing
        loadStyle()
    }

    @OptIn(MapboxExperimental::class)
    fun loadStyle() {

        mapView.getMapboxMap().loadStyle(
            styleExtension = style(Style.OUTDOORS) {
            }
        ) {
            toggleBackgroundLayer(22.0)

            mapView.getMapboxMap()
                .setCamera(
                    CameraOptions.Builder().center(fromLngLat(-122.4241, 37.78)).zoom(10.0).build()
                )
        }
    }

    fun toggleBackgroundLayer(minZoom: Double) {
        mapView.getMapboxMap().getStyle {
            listOfNotNull(
                it.getLayerAs("landcover"),
                it.getLayerAs("water"),
                it.getLayerAs("water-shadow"),
                it.getLayerAs("national-park")
            ).forEach { zoomChangeLayer ->
                zoomChangeLayer.minZoom(minZoom)
            }

        }
    }

    fun zoom() {
        mapView.postDelayed(2000) {
            mapView.getMapboxMap().getStyle {
                mapView.getMapboxMap()
                    .cameraAnimationsPlugin {
                        this.easeTo(CameraOptions.Builder().zoom(9.0)
                            .build(),
                            MapAnimationOptions.mapAnimationOptions {
                                duration(3000)
                                animatorListener(animatorListener = object :
                                    AnimatorListenerAdapter() {
                                    override fun onAnimationEnd(
                                        animation: Animator?,
                                        isReverse: Boolean
                                    ) {
                                        this@cameraAnimationsPlugin.easeTo(CameraOptions.Builder()
                                            .zoom(11.0)
                                            .build(),
                                            MapAnimationOptions.mapAnimationOptions {
                                                duration(3000)
                                            }
                                        )
                                    }
                                })
                            }
                        )
                    }
            }
        }
    }

}