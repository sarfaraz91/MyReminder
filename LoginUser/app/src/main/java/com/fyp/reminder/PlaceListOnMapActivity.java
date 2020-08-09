package com.fyp.reminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fyp.reminder.Utils.AppController;
import com.fyp.reminder.Utils.GoogleApiUrl;
import com.fyp.reminder.model.Place;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class PlaceListOnMapActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        ResultCallback<Status>,
        GoogleMap.OnMarkerClickListener {

    public static final String TAG = PlaceListOnMapActivity.class.getSimpleName();
    /**
     * ArrayList to store the Near By Place List
     */
    private ArrayList<Place> mNearByPlaceArrayList = new ArrayList<>();

    private GoogleMap mGoogleMap;
    private boolean mMapReady = false;
    private String mLocationTag;
    private String mLocationName;
    private String mLocationQueryStringUrl;
    private MapFragment mMapFragment;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;
    private LatLng reminderLocationLatLng;
    private final int UPDATE_INTERVAL = 1000;
    private final int FASTEST_INTERVAL = 900;
    private String sub_type = "";
    private RelativeLayout container_nearby_list;
    private String title;
    private String desc;
    private String date;
    private String time;
    private String repeat;
    private String prio;
    private String save;
    private String update;
    private long reminder_id = 0;
    private int request_code_location = 0;
    private int request_code_old = 0;
    public String next_page_token = "";
    private ArrayList<Location> nearbyLocationsList;
    final Handler handler = new Handler();
    private ProgressDialog progressDialog;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_list_on_map);
        container_nearby_list = findViewById(R.id.container_nearby_list);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        Intent i = getIntent();
        title = i.getStringExtra("title");
        desc = i.getStringExtra("desc");
        date = i.getStringExtra("date");
        time = i.getStringExtra("time");
        repeat = i.getStringExtra("repeat");
        prio = i.getStringExtra("prio");
        save = i.getStringExtra("save");
        update = i.getStringExtra("update");
        sub_type = i.getStringExtra("sub_type");
        reminder_id = i.getLongExtra("reminder_id", 0);
        request_code_location = i.getIntExtra("request_code_location", 0);
        request_code_old = i.getIntExtra("request_code_old", 0);

        Log.d("TAG", "onClick1: " + desc);

        if (sub_type.equals("automatic")) {
            container_nearby_list.setVisibility(View.GONE);
        } else {
            container_nearby_list.setVisibility(View.VISIBLE);
        }

        /**
         * Get the reference of the map fragment
         */
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        /**
         * get the intent and get the location Tag
         */
        mLocationTag = getIntent().getStringExtra(GoogleApiUrl.LOCATION_TYPE_EXTRA_TEXT);
        mLocationName = getIntent().getStringExtra(GoogleApiUrl.LOCATION_NAME_EXTRA_TEXT);

        //nearby API
        String currentLocation = getSharedPreferences(
                GoogleApiUrl.CURRENT_LOCATION_SHARED_PREFERENCE_KEY, 0)
                .getString(GoogleApiUrl.CURRENT_LOCATION_DATA_KEY, null);


        if (mLocationTag.equals("book_store") || mLocationTag.equals("fire_station") || mLocationTag.equals("local_government_office")
                || mLocationTag.equals("post_office") || mLocationTag.equals("shopping_mall")) {
            mLocationQueryStringUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.NEARBY_SEARCH_TAG + "/" +
                    GoogleApiUrl.JSON_FORMAT_TAG + "?" + GoogleApiUrl.LOCATION_TAG + "=" +
                    currentLocation + "&" + GoogleApiUrl.RADIUS_TAG + "=" +
                    GoogleApiUrl.RADIUS_VALUE + "&" + GoogleApiUrl.PLACE_TYPE_TAG + "=" + mLocationTag +
                    "&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY;
        } else {
            mLocationQueryStringUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.NEARBY_SEARCH_TAG + "/" +
                    GoogleApiUrl.JSON_FORMAT_TAG + "?" + GoogleApiUrl.LOCATION_TAG + "=" +
                    currentLocation + "&" + GoogleApiUrl.RADIUS_TAG + "=" +
                    GoogleApiUrl.RADIUS_VALUE + "&" + GoogleApiUrl.QUERY + "=" + mLocationTag + "+in+Karachi" +
                    "&hasNextPage=true&nextPage()=true&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY;
        }

      /*  mLocationQueryStringUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.NEARBY_SEARCH_TAG + "/" +
                GoogleApiUrl.JSON_FORMAT_TAG + "?" + GoogleApiUrl.LOCATION_TAG + "=" +
                currentLocation + "&" + GoogleApiUrl.RADIUS_TAG + "=" +
                GoogleApiUrl.RADIUS_VALUE + "&" + GoogleApiUrl.PLACE_TYPE_TAG + "=" + mLocationTag +
                "&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY;
*/

        Log.d(TAG, "checkapi : " + mLocationQueryStringUrl);

/*
        Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);
        String actionBarTitleText = getResources().getString(R.string.near_by_tag) +
                " " + mLocationName + " " + getString(R.string.list_tag);
        setTitle(actionBarTitleText);
        actionBar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        ((TextView) findViewById(R.id.place_list_placeholder_text_view))
                .setText(/*getResources().getString(R.string.near_by_tag) + " " + */mLocationName +
                        " " + getString(R.string.list_tag));

        findViewById(R.id.place_list_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent placeDetailTransferIntent = new Intent(PlaceListOnMapActivity.this, PlaceListActivity.class);


                placeDetailTransferIntent.putExtra("title", title);
                placeDetailTransferIntent.putExtra("desc", desc);
                placeDetailTransferIntent.putExtra("date", date);
                placeDetailTransferIntent.putExtra("time", time);
                placeDetailTransferIntent.putExtra("repeat", repeat);
                placeDetailTransferIntent.putExtra("prio", prio);
                placeDetailTransferIntent.putExtra("save", save);
                placeDetailTransferIntent.putExtra("update", update);
                placeDetailTransferIntent.putExtra("reminder_id", reminder_id);
                placeDetailTransferIntent.putExtra("request_code_location", request_code_location);
                placeDetailTransferIntent.putExtra("request_code_old", request_code_old);

                placeDetailTransferIntent.putParcelableArrayListExtra(
                        GoogleApiUrl.ALL_NEARBY_LOCATION_KEY, mNearByPlaceArrayList);
                placeDetailTransferIntent.putExtra(GoogleApiUrl.LOCATION_TYPE_EXTRA_TEXT, mLocationTag);
                placeDetailTransferIntent.putExtra(GoogleApiUrl.LOCATION_NAME_EXTRA_TEXT, mLocationName);
                startActivity(placeDetailTransferIntent);
                overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_in);
            }
        });
        createGoogleApi();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mMapReady = true;

        /**
         * Helper Method to put marker on Google map
         */
        getLocationOnGoogleMap(mLocationQueryStringUrl);


    }


    public void prepare2ndUrl() {
        mLocationQueryStringUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.NEARBY_SEARCH_TAG + "/" +
                GoogleApiUrl.JSON_FORMAT_TAG + "?" + /*GoogleApiUrl.LOCATION_TAG + "=" +
                currentLocation + "&" + GoogleApiUrl.RADIUS_TAG + "=" +
                GoogleApiUrl.RADIUS_VALUE + "&" + */ GoogleApiUrl.QUERY + "=" + mLocationTag + "+in+Karachi" +
                "&hasNextPage=true&nextPage()=true&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY +
                "&pagetoken=" + next_page_token;
    }

    public void prepare1stUrl() {
        mLocationQueryStringUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.NEARBY_SEARCH_TAG + "/" +
                GoogleApiUrl.JSON_FORMAT_TAG + "?" + /*GoogleApiUrl.LOCATION_TAG + "=" +
                currentLocation + "&" + GoogleApiUrl.RADIUS_TAG + "=" +
                GoogleApiUrl.RADIUS_VALUE + "&" + */ GoogleApiUrl.QUERY + "=" + mLocationTag + "+in+Karachi" +
                "&hasNextPage=true&nextPage()=true&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY;
    }

    private void getLocationOnGoogleMap(String locationQueryStringUrl) {
        //Tag to cancel request
        String jsonArrayTag = "jsonArrayTag";
        JsonObjectRequest placeJsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                locationQueryStringUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            next_page_token = "";
                            if (response.has("next_page_token")) {
                                next_page_token = response.get("next_page_token").toString();
                            }

                            JSONArray rootJsonArray = response.getJSONArray("results");
                            if (rootJsonArray.length() == 0) {
                                progressDialog.dismiss();
                                if (count == 0) {
                                    ((TextView) findViewById(R.id.place_list_placeholder_text_view))
                                            .setText(/*getResources().getString(R.string.no_near_by_tag)*/
                                                    "No " + mLocationName + " " + getString(R.string.found));

                                    if (sub_type.equals("automatic")) {
                                        NoRecordDialog();
                                    }
                                }
                            } else {
                                count = 1;
                                for (int i = 0; i < rootJsonArray.length(); i++) {

                                    try {
                                        JSONObject singlePlaceJsonObject = (JSONObject) rootJsonArray.get(i);

                                        String currentPlaceId = singlePlaceJsonObject.getString("place_id");
                                        Double currentPlaceLatitude = singlePlaceJsonObject
                                                .getJSONObject("geometry").getJSONObject("location")
                                                .getDouble("lat");
                                        Double currentPlaceLongitude = singlePlaceJsonObject
                                                .getJSONObject("geometry").getJSONObject("location")
                                                .getDouble("lng");
                                        String currentPlaceName = singlePlaceJsonObject.getString("name");

                                        String currentPlaceOpeningHourStatus = singlePlaceJsonObject
                                                .has("opening_hours") ? singlePlaceJsonObject
                                                .getJSONObject("opening_hours")
                                                .getString("open_now") : "Status Not Available";

                                        Double currentPlaceRating = singlePlaceJsonObject.has("rating") ?
                                                singlePlaceJsonObject.getDouble("rating") : 0;
                                        String currentPlaceAddress = singlePlaceJsonObject.has("vicinity") ?
                                                singlePlaceJsonObject.getString("vicinity") :
                                                "Address Not Available";
                                        Place singlePlaceDetail = new Place(
                                                currentPlaceId,
                                                currentPlaceLatitude,
                                                currentPlaceLongitude,
                                                currentPlaceName,
                                                currentPlaceOpeningHourStatus,
                                                currentPlaceRating,
                                                currentPlaceAddress);
                                        mNearByPlaceArrayList.add(singlePlaceDetail);
                                    } catch (Exception e) {
                                        Log.d("error", e.getMessage());
                                    }


                                }
                                if (mMapReady) {
                                    //Set the camera position
                                    String currentLocationString = getSharedPreferences(
                                            GoogleApiUrl.CURRENT_LOCATION_SHARED_PREFERENCE_KEY, 0)
                                            .getString(GoogleApiUrl.CURRENT_LOCATION_DATA_KEY, null);
                                    String currentPlace[] = currentLocationString.split(",");
                                    LatLng currentLocation = new LatLng(Double.valueOf(currentPlace[0])
                                            , Double.valueOf(currentPlace[1]));
                                    CameraPosition cameraPosition = CameraPosition.builder()
                                            .target(currentLocation)
                                            .zoom(15)
                                            .bearing(0)
                                            .tilt(0)
                                            .build();
                                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),
                                            1500, null);

                                    nearbyLocationsList = new ArrayList<>();
                                    for (int i = 0; i < mNearByPlaceArrayList.size(); i++) {
                                        Double currentLatitude = mNearByPlaceArrayList.get(i).getPlaceLatitude();
                                        Double currentLongitude = mNearByPlaceArrayList.get(i).getPlaceLongitude();
                                        LatLng currentLatLng = new LatLng(currentLatitude, currentLongitude);
                                        mGoogleMap.addMarker(new MarkerOptions()
                                                .position(currentLatLng)
                                                .title(mNearByPlaceArrayList.get(i).getPlaceName())
                                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_map)));

                                        Location location = new Location("");
                                        location.setLatitude(currentLatitude);//your coords of course
                                        location.setLongitude(currentLongitude);

                                        Bundle bundle = new Bundle();
                                        bundle.putString("LocationName", mNearByPlaceArrayList.get(i).getPlaceName());
                                        location.setExtras(bundle);
                                        nearbyLocationsList.add(location);
                                    }

                                    Geocoder geocoder = new Geocoder(PlaceListOnMapActivity.this, Locale.getDefault());
                                    Address address = null;
                                    try {
                                        List<Address> addresses = geocoder.getFromLocation(currentLocation.latitude, currentLocation.longitude, 1);
                                        address = addresses.get(0);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position(currentLocation)
                                            .title(address.getAddressLine(0))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location)));

                                    ArrayList<Location> sortedLocationsList = sortLocations(nearbyLocationsList, currentLocation.latitude, currentLocation.longitude);

                                    if (!next_page_token.equals("")) {
                                        prepare2ndUrl();

                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                getLocationOnGoogleMap(mLocationQueryStringUrl);
                                            }
                                        }, 2000);

                                    } else {
                                        progressDialog.dismiss();
                                        if (count == 0) {
                                            ((TextView) findViewById(R.id.place_list_placeholder_text_view))
                                                    .setText(/*getResources().getString(R.string.no_near_by_tag)*/
                                                            "No " + mLocationName + " " + getString(R.string.found));
                                        }
                                        if (sub_type.equals("automatic")) {
                                            confirmationDialog(mLocationName, nearbyLocationsList);
                                        }
                                    }


                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, error.getMessage());
                        progressDialog.dismiss();
                    }
                });
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(placeJsonObjectRequest, jsonArrayTag);

    }

    public void getNearestLocation(ArrayList<Location> nearbyLocationsList) {
        double lat = nearbyLocationsList.get(0).getLatitude();
        double lon = nearbyLocationsList.get(0).getLongitude();
        Bundle bundle = nearbyLocationsList.get(0).getExtras();
        String locationName = bundle.getString("LocationName");
        reminderLocationLatLng = new LatLng(lat, lon);

        Intent intent = new Intent(this, AutoLocationService.class);
        intent.putParcelableArrayListExtra("nearbyLocationsList", nearbyLocationsList);
        ContextCompat.startForegroundService(this, intent);


       /* if(request_code_old != 0){
            Intent intent = new Intent(this, GeofenceServiceAutoReminder.class);
            PendingIntent pendingIntentAuto =PendingIntent.getService(
                    this, request_code_old, intent, 0);

            boolean AutoUp = (pendingIntentAuto != null);
            if(AutoUp){
                clearGeofence();
            }
        }
        startGeofence(reminderLocationLatLng, locationName);*/

        Toast.makeText(PlaceListOnMapActivity.this, "started.........", Toast.LENGTH_SHORT).show();
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getTitle(), Toast.LENGTH_SHORT).show();
        return false;
    }


    public static ArrayList<Location> sortLocations(ArrayList<Location> locations, final double myLatitude, final double myLongitude) {
        Comparator comp = new Comparator<Location>() {
            @Override
            public int compare(Location o, Location o2) {
                float[] result1 = new float[3];
                android.location.Location.distanceBetween(myLatitude, myLongitude, o.getLatitude(), o.getLongitude(), result1);
                Float distance1 = result1[0];

                float[] result2 = new float[3];
                android.location.Location.distanceBetween(myLatitude, myLongitude, o2.getLatitude(), o2.getLongitude(), result2);
                Float distance2 = result2[0];

                return distance1.compareTo(distance2);
            }
        };


        Collections.sort(locations, comp);
        return locations;
    }


    private final int REQ_PERMISSION = 999;

    // Check for permission to access Location
    private boolean checkPermission() {
        Log.d(TAG, "checkPermission()");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED);
    }

    // Asks for permission
    private void askPermission() {
        Log.d(TAG, "askPermission()");
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }


    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult()");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(TAG, "permissionsDenied()");
        // TODO close app and warn user
    }

    // Get last known location
    private void getLastKnownLocation() {
        Log.d(TAG, "getLastKnownLocation()");
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.i(TAG, "LasKnown location. " +
                        "Long: " + lastLocation.getLongitude() +
                        " | Lat: " + lastLocation.getLatitude());
                startLocationUpdates();
            } else {
                Log.w(TAG, "No location retrieved yet");
                startLocationUpdates();
            }
        } else askPermission();
    }

    // Start Geofence creation process

    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 100.0f; // in meters


    private void startGeofence(LatLng latLng, String locationName) {
        Log.i(TAG, "startGeofence()");
        if (latLng != null) {
            Geofence geofence = createGeofence(latLng, GEOFENCE_RADIUS);
            GeofencingRequest geofenceRequest = createGeofenceRequest(geofence);
            addGeofence(geofenceRequest, locationName);
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }

    // Create a Geofence
    private Geofence createGeofence(LatLng latLng, float radius) {
        Log.d(TAG, "createGeofence");
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // Create a Geofence Request
    private GeofencingRequest createGeofenceRequest(Geofence geofence) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    private PendingIntent createGeofencePendingIntent(String locationName) {
        Log.d(TAG, "createGeofencePendingIntent");
        if (geoFencePendingIntent != null)
            return geoFencePendingIntent;

       /* Random rand = new Random();
        int request_code = rand.nextInt(1000);*/

        Intent intent = new Intent(this, GeofenceServiceAutoReminder.class);
        intent.putExtra("name", locationName);
        intent.putExtra("requestcode", request_code_location);
        return PendingIntent.getService(
                this, request_code_location, intent, 0);
    }

    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request, String locationName) {
        Log.d(TAG, "addGeofence");
        if (checkPermission())
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    createGeofencePendingIntent(locationName)
            ).setResultCallback(this);
    }

    private final String KEY_GEOFENCE_LAT = "GEOFENCE LATITUDE";
    private final String KEY_GEOFENCE_LON = "GEOFENCE LONGITUDE";

    // Saving GeoFence marker with prefs mng
    private void saveGeofence() {
        Log.d(TAG, "saveGeofence()");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putLong(KEY_GEOFENCE_LAT, Double.doubleToRawLongBits(reminderLocationLatLng.latitude));
        editor.putLong(KEY_GEOFENCE_LON, Double.doubleToRawLongBits(reminderLocationLatLng.longitude));
        editor.apply();
    }

    // Recovering last Geofence marker
    private void recoverGeofenceMarker() {
        Log.d(TAG, "recoverGeofenceMarker");
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

        if (sharedPref.contains(KEY_GEOFENCE_LAT) && sharedPref.contains(KEY_GEOFENCE_LON)) {
            double lat = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LAT, -1));
            double lon = Double.longBitsToDouble(sharedPref.getLong(KEY_GEOFENCE_LON, -1));
            LatLng latLng = new LatLng(lat, lon);
            //markerForGeofence(latLng);
            //drawGeofence();
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(TAG, "onResult: " + status);
        if (status.isSuccess()) {
            saveGeofence();
        } else {
            // inform about fail
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        getLastKnownLocation();
        recoverGeofenceMarker();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
    }

    // Create GoogleApiClient instance
    private void createGoogleApi() {
        Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private void startLocationUpdates() {
        Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Call GoogleApiClient connection when starting the Activity
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Disconnect GoogleApiClient when stopping Activity
        googleApiClient.disconnect();
    }

    public void confirmationDialog(final String location, final ArrayList<Location> nearbyLocationsList) {
        try {
            new AlertDialog.Builder(PlaceListOnMapActivity.this)
                    .setTitle("Automatic Reminder")
                    .setMessage(location)
                    .setCancelable(false)
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            //getNearestLocation(nearbyLocationsList);

                            if (save.equals("1")) {
                                Intent a = new Intent(PlaceListOnMapActivity.this, CreateReminder.class);
                                a.putExtra("location", location);
                                a.putExtra("title", title);
                                a.putExtra("desc", desc);
                                a.putExtra("date", date);
                                a.putExtra("time", time);
                                a.putExtra("repeat", repeat);
                                a.putExtra("prio", prio);
                                a.putExtra("automatic", true);
                                a.putExtra("request_code_location", request_code_location);
                                a.putParcelableArrayListExtra("nearbyLocationsList", nearbyLocationsList);
                                startActivity(a);
                                finish();
                            } else if (save.equals("2")) {
                                Intent a = new Intent(PlaceListOnMapActivity.this, Update_reminder.class);
                                a.putExtra("location", location);
                                a.putExtra("title", title);
                                a.putExtra("desc", desc);
                                a.putExtra("date", date);
                                a.putExtra("time", time);
                                a.putExtra("repeat", repeat);
                                a.putExtra("prio", prio);
                                a.putExtra("back", "1");
                                a.putExtra("automatic", true);
                                a.putExtra("reminder_id", reminder_id);
                                a.putExtra("request_code_location", request_code_location);
                                a.putParcelableArrayListExtra("nearbyLocationsList", nearbyLocationsList);

                                startActivity(a);
                                finish();
                            }


                            dialog.dismiss();
                        }
                    })
                    .show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
            Log.d("badtoken", e.getMessage());
        }

    }


    public void NoRecordDialog() {
        try {
            new AlertDialog.Builder(PlaceListOnMapActivity.this)
                    .setTitle("Automatic Reminder")
                    .setMessage("No Record Found.")
                    .setCancelable(false)
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    .setPositiveButton("Back", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue with delete operation
                            //getNearestLocation(nearbyLocationsList);
                            dialog.dismiss();
                            onBackPressed();
                        }
                    })
                    .show();
        } catch (WindowManager.BadTokenException e) {
            //use a log message
            Log.d("badtoken", e.getMessage());
        }

    }


    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                deleteGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if (status.isSuccess()) {
                    // remove drawing
                    //removeGeofenceDraw();
                }
            }
        });
    }

    public PendingIntent deleteGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        Intent intent = new Intent(this, GeofenceServiceAutoReminder.class);

        PendingIntent pendingIntentAuto = PendingIntent.getService(
                this, request_code_old, intent, 0);

        return pendingIntentAuto;
    }

}
