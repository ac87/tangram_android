package com.test.tangramandroid;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;

public class MainActivity extends AppCompatActivity implements MapView.OnMapReadyCallback {

    private static final int REQUEST_READWRITE_STORAGE = 123;

    private final String TAG = MainActivity.class.getSimpleName();

    private MapView mapView;
    private MapController mapController;

    private MyLocationMarkerManager myLocation;

    private boolean isVector = true;

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

        mapView.getMapAsync(this, getBaseScene(isVector));
        setSourceToggleDrawable(buttonToggle);
    }

    private void setSourceToggleDrawable(ImageButton buttonToggle) {
        buttonToggle.setImageResource(isVector ? R.drawable.ic_map_black_24dp : R.drawable.ic_satellite_black_24dp);
    }

    private Runnable reloadRunnable = new Runnable() {
        @Override
        public void run() {
            mapController.loadSceneFile(getBaseScene(isVector));
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

        if (MyLocationMarkerManager.deviceHasGpsCapability(this))
            myLocation = new MyLocationMarkerManager(this, mapController, findViewById(R.id.buttonMyLocation));
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
}
