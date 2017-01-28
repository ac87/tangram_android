package com.test.tangramandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.VectorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.Marker;

public class MyLocationMarkerManager implements LocationListener {

    private final String TAG = MyLocationMarkerManager.class.getSimpleName();

    public static final String DEFAULT_STYLE = "style: 'points', color: 'white', size: [52px, 52px], collide: false, flat: true";
    private Context context;
    private MapController mapController;
    private LocationManager locationManager;
    private Marker myLocationMarker;

    private boolean animate = true; // note: if true. setPositionEased seems to cause something to continually rerender the map. Or ViewCompleteListener is lying

    public MyLocationMarkerManager(Context context, final MapController mapController, View myLocationButton) {

        this.context = context;
        this.mapController = mapController;

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        createMarker();

        if (myLocationButton != null)
            myLocationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveToLastLocation();
                }
            });

        requestLocationUpdates();
    }

    public void onResume() {
        requestLocationUpdates();
    }

    public void onPause() {
        stopLocationUpdates();
    }

    private void createMarker() {
        myLocationMarker = mapController.addMarker();
        myLocationMarker.setDrawOrder(250);
        myLocationMarker.setStyling("{ " + DEFAULT_STYLE + " }");
        myLocationMarker.setDrawable(getBitmapDrawable((VectorDrawable) context.getResources().getDrawable(R.drawable.ic_navigation_24dp)));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
            myLocationMarker.setPoint(new LngLat(location.getLongitude(), location.getLatitude()));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static BitmapDrawable getBitmapDrawable(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return new BitmapDrawable(bitmap);
    }

    private void moveToLastLocation() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null)
            mapController.setPositionEased(new LngLat(location.getLongitude(), location.getLatitude()), 200);
        if (mapController.getZoom() < 12)
            mapController.setZoomEased(12, 1000);
    }

    private void requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed");
        if (animate)
            myLocationMarker.setPointEased(new LngLat(location.getLongitude(), location.getLatitude()), 200, MapController.EaseType.LINEAR);
        else
            myLocationMarker.setPoint(new LngLat(location.getLongitude(), location.getLatitude()));

        if (location.hasBearing())
            updateMarkerStyle((int) location.getBearing());
    }

    private void updateMarkerStyle(int bearing) {
        myLocationMarker.setStyling("{ " + DEFAULT_STYLE + ", angle: " + bearing + " }");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public static boolean deviceHasGpsCapability(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.getProvider(LocationManager.GPS_PROVIDER) != null;
    }
}
