package com.test.tangramandroid;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;

import org.geojson.Feature;
import org.geojson.FeatureCollection;
import org.geojson.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tony on 15/10/2017.
 */

public class MapDataManager {

    private final String TAG = MapDataManager.class.getSimpleName();

    private final Context context;
    private final MapController mapController;

    private MapData data;
    private int featureCount = 0;

    public MapDataManager(Context context, MapController mapController) {
        this.context = context;
        this.mapController = mapController;
    }

    public void onSceneReady() {
        createData();
    }

    private void createData() {
        if (data == null)
            data = mapController.addDataLayer("map-datas");

        new CreateDataOperation().execute();
    }

    // This way is worse than GeoJSON so can't blame the GeoJSON encode or decode
    /*private class CreateDataOperation extends AsyncTask<String, Void, List<LngLat>> {

        @Override
        protected List<LngLat> doInBackground(String... params) {

            List<LngLat> featureCollection = new ArrayList<>();

            for (double lat = 43.452845; lat >= 36.856253; lat = lat - 0.2) {
                for (double lon = -6.313385; lon <= -1.942847; lon = lon + 0.2) {

                    for (double diff = -0.05; diff <= 0.05; diff = diff + 0.0075) {
                        featureCollection.add(new LngLat(lon + diff, lat + diff));
                    }
                }
            }

            return featureCollection;
        }

        @Override
        protected void onPostExecute(List<LngLat> result) {
            if (result != null) {
                for (LngLat point : result) {
                    Map<String, String> props = new HashMap<>();
                    props.put("color", "#0000FF");
                    data.addPoint(point, props);
                }
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }*/

    private class CreateDataOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            FeatureCollection featureCollection = new FeatureCollection();

            for (double lat = 43.452845; lat >= 36.856253; lat = lat - 0.2) {
                for (double lon = -6.313385; lon <= -1.942847; lon = lon + 0.2) {

                    for (double diff = -0.05; diff <= 0.05; diff = diff + 0.0075) {
                        createFeature(featureCollection, lat + diff, lon + diff);
                    }
                }
            }

            featureCount = featureCollection.getFeatures().size();

            String json= null;
            try {
                json = new ObjectMapper().writeValueAsString(featureCollection);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return json;
        }

        private void createFeature(FeatureCollection featureCollection, double lat, double lon) {
            Feature feature = new Feature();
            feature.setGeometry(new Point(lon, lat));
            featureCollection.add(feature);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                data.addGeoJson(result);
                Toast.makeText(context, featureCount + " Data entries Loaded ", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}
