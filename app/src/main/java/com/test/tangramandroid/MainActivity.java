package com.test.tangramandroid;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapzen.tangram.LabelPickResult;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.MarkerPickResult;
import com.mapzen.tangram.SceneUpdate;
import com.mapzen.tangram.SceneUpdateError;
import com.mapzen.tangram.TouchInput;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements MapView.OnMapReadyCallback, MapController.FeaturePickListener, MapController.LabelPickListener, MapController.MarkerPickListener, MapController.SceneUpdateErrorListener, TouchInput.TapResponder {

    private static final int REQUEST_READWRITE_STORAGE = 123;

    private final String TAG = MainActivity.class.getSimpleName();

    private MapView mapView;
    private MapController mapController;

    private MyLocationMarkerManager myLocation;
    private RouteManager route;

    private boolean isVector = true;

    private final String apiKey = "vector-tiles-tyHL4AY"; // change this.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        final ImageButton buttonToggle = (ImageButton) findViewById(R.id.buttonToggle);
        buttonToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isVector = !isVector;

                mapController.queueEvent(reloadRunnable);
                setSourceToggleDrawable(buttonToggle);
            }
        });

        final ImageButton buttonRoute = (ImageButton) findViewById(R.id.buttonRoute);
        buttonRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (route != null)
                    route.showRoute();
            }
        });


        final ArrayList<SceneUpdate> sceneUpdates = new ArrayList<>(1);
        sceneUpdates.add(new SceneUpdate("global.sdk_mapzen_api_key", apiKey));
        mapView.getMapAsync(this, getBaseScene(isVector), isVector ? sceneUpdates : null);
        setSourceToggleDrawable(buttonToggle);
    }

    private void setSourceToggleDrawable(ImageButton buttonToggle) {
        buttonToggle.setImageResource(isVector ? R.drawable.ic_map_black_24dp : R.drawable.ic_satellite_black_24dp);
    }

    private Runnable reloadRunnable = new Runnable() {
        @Override
        public void run() {
            mapController.loadSceneFile(getBaseScene(isVector));
            if (isVector)
                mapController.queueSceneUpdate(new SceneUpdate("global.sdk_mapzen_api_key", apiKey));
        }
    };

    private String getBaseScene(boolean vector) {
        return (vector ? "vector" : "raster") + ".yaml";
    }

    @Override
    public void onMapReady(MapController mapController) {

        Log.d(TAG, "onMapReady");

        this.mapController = mapController;

        mapController.setPosition(new LngLat(-0.1427848, 51.4943555));
        mapController.setZoom(4);

        mapController.setViewCompleteListener(new MapController.ViewCompleteListener() {
            public void onViewComplete() {
                Log.d(TAG, "View complete");
            }
        });
        mapController.setTapResponder(this);
        mapController.setFeaturePickListener(this);
        mapController.setLabelPickListener(this);
        mapController.setMarkerPickListener(this);
        mapController.setSceneUpdateErrorListener(this);
        mapController.setPickRadius(20);

        if (MyLocationMarkerManager.deviceHasGpsCapability(this))
            myLocation = new MyLocationMarkerManager(this, mapController, findViewById(R.id.buttonMyLocation));

        route = new RouteManager(mapController);
    }

    @Override
    public boolean onSingleTapUp(float x, float y) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(float x, float y) {

        LngLat tappedPoint = mapController.screenPositionToLngLat(new PointF(x, y));

        mapController.pickFeature(x, y);
        mapController.pickLabel(x, y);
        mapController.pickMarker(x, y);

        mapController.setPositionEased(tappedPoint, 500);

        return true;
    }

    @Override
    public void onFeaturePick(Map<String, String> properties, float positionX, float positionY) {
        if (properties.isEmpty()) {
            Log.d(TAG, "Empty selection");
            return;
        }

        String name = properties.get("name");
        if (name == null || name.isEmpty()) {
            name = "unnamed";
        }

        Log.d(TAG, "Picked: " + name);
        final String message = name;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Selected: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onLabelPick(LabelPickResult labelPickResult, float positionX, float positionY) {
        if (labelPickResult == null) {
            Log.d(TAG, "Empty label selection");
            return;
        }

        String name = labelPickResult.getProperties().get("name");
        if (name.isEmpty()) {
            name = "unnamed";
        }

        Log.d(TAG, "Picked label: " + name);
        final String message = name;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Selected label: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMarkerPick(MarkerPickResult markerPickResult, float positionX, float positionY) {
        if (markerPickResult == null) {
            Log.d(TAG, "Empty marker selection");
            return;
        }

        Log.d(TAG, "Picked marker: " + markerPickResult.getMarker().getMarkerId());
        final String message = String.valueOf(markerPickResult.getMarker().getMarkerId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),
                        "Selected Marker: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        int permissionCheck1 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionCheck2 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCheck3 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCheck4 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permissionCheck1 != PackageManager.PERMISSION_GRANTED || permissionCheck2 != PackageManager.PERMISSION_GRANTED
                || permissionCheck3 != PackageManager.PERMISSION_GRANTED || permissionCheck4 != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_READWRITE_STORAGE);
            return;
        }

        if (myLocation != null)
            myLocation.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (myLocation != null)
            myLocation.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSceneUpdateError(SceneUpdateError sceneUpdateError) {
        Log.d(TAG, "Scene update errors "
                + sceneUpdateError.getSceneUpdate().toString()
                + " " + sceneUpdateError.getError().toString());
    }
}
