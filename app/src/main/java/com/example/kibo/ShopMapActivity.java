package com.example.kibo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kibo.api.ApiClient;
import com.example.kibo.api.ApiService;
import com.example.kibo.models.StoreLocationsResponse;
import com.example.kibo.models.StoreLocation;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class ShopMapActivity extends AppCompatActivity {

    // Default location: replace with real shop coordinates
    private static final double DEFAULT_SHOP_LAT = 10.776530; // e.g., Ho Chi Minh City center
    private static final double DEFAULT_SHOP_LNG = 106.700981;

    private MapView mapView;
    private MyLocationNewOverlay myLocationOverlay;
    private ApiService apiService;
    private Double storeLat;
    private Double storeLng;
    private static final int REQ_LOCATION = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_map);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Địa chỉ cửa hàng");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Configure osmdroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        mapView = findViewById(R.id.osm_map);
        if (mapView != null) {
            mapView.setMultiTouchControls(true);
        }

        Button btnDirections = findViewById(R.id.btn_directions);
        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDirections();
            }
        });

        apiService = ApiClient.getApiService();
        setupMyLocation();
        loadStoreLocationAndShowOnMap();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadStoreLocationAndShowOnMap() {
        // First try intent extras
        double lat = getIntent().getDoubleExtra("shop_lat", Double.NaN);
        double lng = getIntent().getDoubleExtra("shop_lng", Double.NaN);
        String title = getIntent().getStringExtra("shop_title");
        if (title == null || title.isEmpty()) {
            title = "Cửa hàng Kibo";
        }
        final String finalTitle = title;

        if (!Double.isNaN(lat) && !Double.isNaN(lng)) {
            storeLat = lat;
            storeLng = lng;
            placeMarker(lat, lng, finalTitle);
            return;
        }

        // Fetch specific store location id = 3 (first page, size 1)
        apiService.getStoreLocationById(3, 1, 1).enqueue(new retrofit2.Callback<StoreLocationsResponse>() {
            @Override
            public void onResponse(retrofit2.Call<StoreLocationsResponse> call, retrofit2.Response<StoreLocationsResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getData() == null || response.body().getData().isEmpty()) {
                    placeMarker(DEFAULT_SHOP_LAT, DEFAULT_SHOP_LNG, finalTitle);
                    return;
                }
                StoreLocation loc = response.body().getData().get(0);
                Double la = loc.getLatitude();
                Double lo = loc.getLongitude();
                String t = loc.getFullAddress() != null && !loc.getFullAddress().isEmpty() ? loc.getFullAddress() : finalTitle;
                if (la == null || lo == null) {
                    placeMarker(DEFAULT_SHOP_LAT, DEFAULT_SHOP_LNG, t);
                } else {
                    storeLat = la;
                    storeLng = lo;
                    placeMarker(la, lo, t);
                }
            }

            @Override
            public void onFailure(retrofit2.Call<StoreLocationsResponse> call, Throwable t) {
                placeMarker(DEFAULT_SHOP_LAT, DEFAULT_SHOP_LNG, finalTitle);
            }
        });
    }

    private void placeMarker(double lat, double lng, String title) {
        if (mapView == null) return;
        mapView.getOverlays().clear();
        GeoPoint point = new GeoPoint(lat, lng);
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setTitle(title);
        mapView.getOverlays().add(marker);
        mapView.getController().setZoom(16.0);
        mapView.getController().setCenter(point);
        // Re-add location overlay if available
        if (myLocationOverlay != null) {
            mapView.getOverlays().add(myLocationOverlay);
        }
        mapView.invalidate();
    }

    private void setupMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
            return;
        }
        enableMyLocationOverlay();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocationOverlay();
            }
        }
    }

    private void enableMyLocationOverlay() {
        if (mapView == null) return;
        if (myLocationOverlay == null) {
            myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
            myLocationOverlay.enableMyLocation();
            myLocationOverlay.enableFollowLocation();
        }
        if (!mapView.getOverlays().contains(myLocationOverlay)) {
            mapView.getOverlays().add(myLocationOverlay);
        }
        mapView.invalidate();
    }

    private void openDirections() {
        if (storeLat == null || storeLng == null) return;
        // Try to read current location from overlay
        GeoPoint current = myLocationOverlay != null ? myLocationOverlay.getMyLocation() : null;
        String url;
        if (current != null) {
            // OSM directions using OSRM
            url = "https://www.openstreetmap.org/directions?engine=fossgis_osrm_car&route="
                    + current.getLatitude() + "," + current.getLongitude() + ";"
                    + storeLat + "," + storeLng;
        } else {
            // Fallback: open destination point
            url = "https://www.openstreetmap.org/?mlat=" + storeLat + "&mlon=" + storeLng + "#map=16/" + storeLat + "/" + storeLng;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}


