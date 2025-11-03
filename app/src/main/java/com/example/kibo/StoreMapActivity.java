package com.example.kibo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class StoreMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "StoreMapActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // OpenRouteService API Key (Free alternative to Google Directions API)
    private static final String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6IjU5MDBjNWRhZjE1NzQ3MDk4NzY0ZDhkZjAzYmI1YmE2IiwiaCI6Im11cm11cjY0In0=";

    // Google Maps API Key - only for map display
    private static final String GOOGLE_API_KEY = "AIzaSyDmWSNu6hMOQAR5zovTt7meDWECV1uE6aU";

    // Store coordinates (FPT University HCM)
    private static final double STORE_LATITUDE = 10.8411276;
    private static final double STORE_LONGITUDE = 106.809883;
    private static final String STORE_NAME = "Kibo Store";
    private static final String STORE_ADDRESS = "FPT University, Thủ Đức, TP.HCM";

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvDistance;
    private TextView tvDuration;
    private ProgressBar progressBar;
    private Handler handler;
    private Polyline currentPolyline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_map);

        setupToolbar();

        tvDistance = findViewById(R.id.tv_distance);
        tvDuration = findViewById(R.id.tv_duration);
        progressBar = findViewById(R.id.progress_bar);

        handler = new Handler(Looper.getMainLooper());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Show loading
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        // Load map directly - no need for extra thread
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
            Toast.makeText(this, "Không thể tải bản đồ", Toast.LENGTH_SHORT).show();
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vị trí cửa hàng");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        try {
            Log.d(TAG, "onMapReady called");
            googleMap = map;

            // Configure map settings
            googleMap.getUiSettings().setZoomControlsEnabled(true);
            googleMap.getUiSettings().setCompassEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true); // Enable My Location button

            // Add store marker
            LatLng storeLocation = new LatLng(STORE_LATITUDE, STORE_LONGITUDE);
            googleMap.addMarker(new MarkerOptions()
                    .position(storeLocation)
                    .title(STORE_NAME)
                    .snippet(STORE_ADDRESS)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15));

            Log.d(TAG, "Map ready, store marker added");

            // Hide progress
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }

            // Get location after a short delay (non-blocking)
            handler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    getCurrentLocationAndDrawRoute();
                }
            }, 300); // Reduced from 500ms to 300ms

        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady", e);
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Lỗi map: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void getCurrentLocationAndDrawRoute() {
        try {
            Log.d(TAG, "getCurrentLocationAndDrawRoute called");

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting location permission");
                ActivityCompat.requestPermissions(this,
                        new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                        LOCATION_PERMISSION_REQUEST_CODE);
                return;
            }

            if (googleMap == null) {
                Log.e(TAG, "GoogleMap is null");
                return;
            }

            googleMap.setMyLocationEnabled(true);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        try {
                            if (location != null) {
                                Log.d(TAG,
                                        "Location found: " + location.getLatitude() + ", " + location.getLongitude());

                                LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                LatLng storeLatLng = new LatLng(STORE_LATITUDE, STORE_LONGITUDE);

                                if (googleMap != null) {
                                    // Add marker for current location
                                    googleMap.addMarker(new MarkerOptions()
                                            .position(currentLatLng)
                                            .title("Vị trí của bạn")
                                            .icon(BitmapDescriptorFactory
                                                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                                    // Adjust camera to show both markers - use moveCamera instead of animateCamera
                                    // to avoid lag
                                    try {
                                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                        builder.include(currentLatLng);
                                        builder.include(storeLatLng);
                                        LatLngBounds bounds = builder.build();
                                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200)); // Changed
                                                                                                                // from
                                                                                                                // animateCamera
                                    } catch (Exception e) {
                                        Log.e(TAG, "Error adjusting camera", e);
                                    }
                                }

                                // Use OpenRouteService API to calculate real route
                                Log.d(TAG, "Calculating route with OpenRouteService API...");
                                Toast.makeText(this, "Đang tính đường đi...", Toast.LENGTH_SHORT).show();
                                getDirectionsFromOpenRouteService(currentLatLng, storeLatLng);
                            } else {
                                Log.d(TAG, "Location is null");
                                Toast.makeText(this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error in location success callback", e);
                            e.printStackTrace();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        Toast.makeText(this, "Lỗi lấy vị trí: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Error in getCurrentLocationAndDrawRoute", e);
            e.printStackTrace();
        }
    }

    /**
     * Get directions from Google Directions API
     * API Documentation:
     * https://developers.google.com/maps/documentation/directions/start
     */
    private void getDirectionsFromGoogleAPI(LatLng origin, LatLng destination) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // Build Google Directions API URL
                String urlString = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + origin.latitude + "," + origin.longitude +
                        "&destination=" + destination.latitude + "," + destination.longitude +
                        "&mode=driving" +
                        "&key=" + GOOGLE_API_KEY;

                Log.d(TAG, "Calling Google Directions API...");

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("API returned error code: " + responseCode);
                }

                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String status = jsonResponse.getString("status");

                if (!status.equals("OK")) {
                    throw new Exception("API status: " + status);
                }

                JSONArray routes = jsonResponse.getJSONArray("routes");
                if (routes.length() == 0) {
                    throw new Exception("No routes found");
                }

                JSONObject route = routes.getJSONObject(0);
                JSONArray legs = route.getJSONArray("legs");
                JSONObject leg = legs.getJSONObject(0);

                // Get distance and duration
                JSONObject distance = leg.getJSONObject("distance");
                JSONObject duration = leg.getJSONObject("duration");

                String distanceText = distance.getString("text");
                String durationText = duration.getString("text");
                double distanceMeters = distance.getDouble("value");
                double durationSeconds = duration.getDouble("value");

                // Format for display
                String distText = "🚗 Khoảng cách: " + distanceText;
                String timeText = "⏱️ Thời gian: " + durationText;

                Log.d(TAG, distText + ", " + timeText);

                // Get route polyline
                JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                String encodedPolyline = overviewPolyline.getString("points");
                List<LatLng> routePoints = decodePolyline(encodedPolyline);

                Log.d(TAG, "Route has " + routePoints.size() + " points");

                // Update UI on main thread
                handler.post(() -> {
                    if (!isFinishing()) {
                        tvDistance.setText(distText);
                        tvDuration.setText(timeText);
                        drawRoute(routePoints);
                        Toast.makeText(this, "✅ Đã tính xong đường đi!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error calling Google Directions API", e);
                handler.post(() -> {
                    if (!isFinishing()) {
                        Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    /**
     * Decode Google's encoded polyline string into list of LatLng points
     * Algorithm:
     * https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(p);
        }

        return poly;
    }

    /**
     * Get directions from OpenRouteService API (Free alternative to Google
     * Directions)
     * API Documentation: https://openrouteservice.org/dev/#/api-docs/v2/directions
     */
    private void getDirectionsFromOpenRouteService(LatLng origin, LatLng destination) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                // Build API URL - format: start=longitude,latitude&end=longitude,latitude
                String urlString = "https://api.openrouteservice.org/v2/directions/driving-car?" +
                        "api_key=" + ORS_API_KEY +
                        "&start=" + origin.longitude + "," + origin.latitude +
                        "&end=" + destination.longitude + "," + destination.latitude;

                Log.d(TAG, "Calling OpenRouteService API...");
                Log.d(TAG, "URL: " + urlString);

                URL url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept",
                        "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Response Code: " + responseCode);

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new Exception("API returned error code: " + responseCode);
                }

                // Read response
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse JSON
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray features = jsonResponse.getJSONArray("features");
                JSONObject feature = features.getJSONObject(0);
                JSONObject properties = feature.getJSONObject("properties");
                JSONObject summary = properties.getJSONObject("summary");

                // Get distance and duration
                double distanceKm = summary.getDouble("distance") / 1000;
                int durationMin = (int) (summary.getDouble("duration") / 60);

                String distText = String.format("Khoảng cách: %.2f km", distanceKm);
                String timeText = "Thời gian dự kiến: " + durationMin + " phút";

                Log.d(TAG, distText + ", " + timeText);

                // Get route coordinates
                JSONObject geometry = feature.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");

                List<LatLng> routePoints = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    routePoints.add(new LatLng(coord.getDouble(1), coord.getDouble(0)));
                }

                Log.d(TAG, "Route has " + routePoints.size() + " points");

                // Update UI on main thread
                handler.post(() -> {
                    if (!isFinishing()) {
                        tvDistance.setText(distText);
                        tvDuration.setText(timeText);
                        drawRoute(routePoints);
                        Toast.makeText(this, "✅ Đã tính xong đường đi!", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error calling OpenRouteService", e);
                handler.post(() -> {
                    if (!isFinishing()) {
                        Toast.makeText(this, "❌ Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } finally {
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception ignored) {
                    }
                }
            }
        }).start();
    }

    /**
     * Draw route polyline on map
     */
    private void drawRoute(List<LatLng> points) {
        try {
            if (points == null || points.isEmpty()) {
                Log.e(TAG, "No points to draw");
                return;
            }

            if (googleMap == null) {
                Log.e(TAG, "GoogleMap is null in drawRoute");
                return;
            }

            // Remove old polyline if exists
            if (currentPolyline != null) {
                currentPolyline.remove();
            }

            // Draw new polyline with green color for motorcycle
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(points)
                    .width(12)
                    .color(Color.rgb(34, 139, 34)) // Forest green for motorcycle route
                    .geodesic(true);

            currentPolyline = googleMap.addPolyline(polylineOptions);
            Log.d(TAG, "Polyline drawn with " + points.size() + " points");
        } catch (Exception e) {
            Log.e(TAG, "Error drawing route", e);
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up to prevent memory leaks
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        googleMap = null;
        currentPolyline = null;
        fusedLocationClient = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove any pending callbacks when activity is paused
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocationAndDrawRoute();
            }
        }
    }
}
