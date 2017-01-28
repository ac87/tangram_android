package com.test.tangramandroid;

import android.support.annotation.NonNull;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.Marker;
import com.mapzen.tangram.geometry.Polyline;

import java.util.ArrayList;

public class RouteManager {

    public static final String DEFAULT_STYLE = "{ style: 'blended-lines', color: '#550000FF', width: 15px, order: 2000 }";

    private MapController mapController;
    private Marker routeMarker;

    private ArrayList<LngLat> routeLngLats;

    public RouteManager(MapController mapController) {
        this.mapController = mapController;
    }

    public void showRoute() {
        if (routeMarker == null)
            addRoute();

        mapController.setPositionEased(routeLngLats.get(0), 2000);
        if (mapController.getZoom() < 16)
            mapController.setZoomEased(16, 1000);
    }

    @NonNull
    private void addRoute() {

        if (routeMarker != null)
            mapController.removeMarker(routeMarker);

        routeMarker = mapController.addMarker();

        routeLngLats = getRoute();
        Polyline polyline = new Polyline(routeLngLats, null);

        routeMarker.setPolyline(polyline);
        routeMarker.setStyling(DEFAULT_STYLE);
    }

    @NonNull
    private ArrayList<LngLat> getRoute() {
        ArrayList<LngLat> lngLats = new ArrayList<>();
        lngLats.add(new LngLat(-122.421104, 37.826785));
        lngLats.add(new LngLat(-122.421218, 37.826662));
        lngLats.add(new LngLat(-122.420521, 37.826060));
        lngLats.add(new LngLat(-122.420446, 37.825802));
        lngLats.add(new LngLat(-122.420462, 37.825649));
        lngLats.add(new LngLat(-122.420591, 37.825416));
        lngLats.add(new LngLat(-122.420763, 37.825240));
        lngLats.add(new LngLat(-122.421187, 37.825109));
        lngLats.add(new LngLat(-122.421199, 37.825122));
        lngLats.add(new LngLat(-122.420995, 37.825222));
        lngLats.add(new LngLat(-122.420862, 37.825351));
        lngLats.add(new LngLat(-122.420862, 37.825351));
        lngLats.add(new LngLat(-122.420748, 37.825582));
        return lngLats;
    }
}
