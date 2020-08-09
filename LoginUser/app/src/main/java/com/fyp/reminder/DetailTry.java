package com.fyp.reminder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fyp.reminder.Utils.AppController;
import com.fyp.reminder.Utils.GoogleApiUrl;
import com.fyp.reminder.model.Place;
import com.fyp.reminder.model.PlaceUserRating;
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
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class DetailTry extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        OnMapReadyCallback,
        ResultCallback<Status> {

    public static final String TAG = PlaceDetailActivity.class.getSimpleName();
    private String mCurrentPlaceDetailUrl;
    private ArrayList<PlaceUserRating> mPlaceUserRatingsArrayList = new ArrayList<>();
    private TabLayout mTabLayout;
    //    private ViewPager mViewPager;
    private String mPlaceShareUrl;
//    private ProgressBar mProgressBar;


    private static final int PERMISSION_REQUEST_CODE = 100;
    /**
     * All references of the views
     */

    private GoogleMap mGoogleMap;
    private boolean mMapReady = false;
    private Place mCurrentPlace;

    /* Double lng = mCurrentPlace.getPlaceLongitude();
     Double lat = mCurrentPlace.getPlaceLatitude();*/
    private TextView mLocationAddressTextView;
    private TextView mLocationPhoneTextView;
    private TextView mLocationWebsiteTextView;
    private TextView mLocationOpeningStatusTextView;
    String desc, title, date, time, repeat, prio, save, update;

    //    private ImageView mFavouriteImageIcon;
    private MapFragment mMapFragment;
    LinearLayout location_phone_container, location_website_container, btn_ok;

    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    private LocationRequest locationRequest;
    // Defined in mili seconds.
    // This number in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL = 2000;
    private final int FASTEST_INTERVAL = 900;

    private LatLng reminderLocationLatLng;
    private long reminder_id = 0;
    private int request_code_location = 0;
    private int request_code_old = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_try);

        Intent i = getIntent();

        title = i.getStringExtra("title");
        desc = i.getStringExtra("desc");
        date = i.getStringExtra("date");
        time = i.getStringExtra("time");
        repeat = i.getStringExtra("repeat");
        prio = i.getStringExtra("prio");
        save = i.getStringExtra("save");
        update = i.getStringExtra("update");
        reminder_id = i.getLongExtra("reminder_id",0);
        request_code_location = i.getIntExtra("request_code_location",0);
        request_code_old = i.getIntExtra("request_code_old",0);


        Log.d("TAG", "savee: " + save);

        String currentPlaceDetailId = getIntent().getStringExtra(GoogleApiUrl.LOCATION_ID_EXTRA_TEXT);
        mCurrentPlaceDetailUrl = GoogleApiUrl.BASE_URL + GoogleApiUrl.LOCATION_DETAIL_TAG + "/" +
                GoogleApiUrl.JSON_FORMAT_TAG + "?" + GoogleApiUrl.LOCATION_PLACE_ID_TAG + "=" +
                currentPlaceDetailId + "&" + GoogleApiUrl.API_KEY_TAG + "=" + GoogleApiUrl.API_KEY;
        Log.d(TAG, "jaga " + currentPlaceDetailId);


        //   --------------------------------------------------------------

        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        mLocationAddressTextView = (TextView) findViewById(R.id.location_address_text_view);
        mLocationPhoneTextView = (TextView) findViewById(R.id.location_phone_number_text_view);
        mLocationWebsiteTextView = (TextView) findViewById(R.id.location_website_text_view);
        mLocationOpeningStatusTextView = (TextView) findViewById(R.id.location_status_text_view);
//        mFavouriteImageIcon = (ImageView)findViewById(R.id.location_favourite_icon);
        location_phone_container = (LinearLayout) findViewById(R.id.location_phone_container);
        location_website_container = (LinearLayout) findViewById(R.id.location_website_container);
        btn_ok = (LinearLayout) findViewById(R.id.btn_ok);


//        mCurrentPlace = getCurrentPlaceAllDetails.;
//        Log.d(TAG, "latii2 :" + mCurrentPlace.getPlaceLatitude() + "lngi2:"+ mCurrentPlace.getPlaceLongitude());
        createGoogleApi();

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


    private void getCurrentPlaceAllDetails(final String currentPlaceDetailUrl, final GoogleMap mGoogleMap) {
        String jsonArrayTag = "jsonArrayTag";
        JsonObjectRequest placeJsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                currentPlaceDetailUrl, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        mProgressBar.setVisibility(View.GONE);
                        try {
                            JSONObject rootJsonObject = response.getJSONObject("result");

                            String currentPlaceId = rootJsonObject.getString("place_id");
                            Double currentPlaceLatitude = rootJsonObject
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lat");
                            Double currentPlaceLongitude = rootJsonObject
                                    .getJSONObject("geometry").getJSONObject("location")
                                    .getDouble("lng");
                            String currentPlaceName = rootJsonObject.getString("name");
                            String currentPlaceOpeningHourStatus = rootJsonObject
                                    .has("opening_hours") ? rootJsonObject.getJSONObject("opening_hours")
                                    .getString("open_now") : "Status Not Available";
                            if (currentPlaceOpeningHourStatus.equals("true"))
                                currentPlaceOpeningHourStatus = getString(R.string.open_now);
                            else if (currentPlaceOpeningHourStatus.equals("false"))
                                currentPlaceOpeningHourStatus = getString(R.string.closed);

                            Double currentPlaceRating = rootJsonObject.has("rating") ?
                                    rootJsonObject.getDouble("rating") : 0;
                            String currentPlaceAddress = rootJsonObject.has("formatted_address") ?
                                    rootJsonObject.getString("formatted_address") :
                                    "Address Not Available";
                            String currentPlacePhoneNumber = rootJsonObject
                                    .has("international_phone_number") ? rootJsonObject
                                    .getString("international_phone_number") :
                                    "Phone Number Not Registered";
                            String currentPlaceWebsite = rootJsonObject.has("website") ?
                                    rootJsonObject.getString("website") :
                                    "Website Not Registered";
                            String currentPlaceShareLink = rootJsonObject.getString("url");
                            mPlaceShareUrl = currentPlaceShareLink;

                            mCurrentPlace = new Place(
                                    currentPlaceId,
                                    currentPlaceLatitude,
                                    currentPlaceLongitude,
                                    currentPlaceName,
                                    currentPlaceOpeningHourStatus,
                                    currentPlaceRating,
                                    currentPlaceAddress,
                                    currentPlacePhoneNumber,
                                    currentPlaceWebsite, currentPlaceShareLink);


                            location_phone_container.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            if (mCurrentPlace.getPlacePhoneNumber().charAt(0) != '+')
                                                Toast.makeText(DetailTry.this, R.string.phone_number_not_registered_string,
                                                        Toast.LENGTH_SHORT).show();
                                            else {
                                                Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
                                                phoneIntent.setData(Uri.parse("tel:" + mCurrentPlace.getPlacePhoneNumber()));
                                                startActivity(phoneIntent);
                                            }
                                        }
                                    });

                            location_website_container.setOnClickListener(
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            if (mCurrentPlace.getPlaceWebsite().charAt(0) != 'h')
                                                Toast.makeText(DetailTry.this, R.string.website_not_registered_string,
                                                        Toast.LENGTH_SHORT).show();
                                            else {
                                                Intent websiteUrlIntent = new Intent(Intent.ACTION_VIEW);
                                                websiteUrlIntent.setData(Uri.parse(mCurrentPlace.getPlaceWebsite()));
                                                startActivity(websiteUrlIntent);
                                            }
                                        }
                                    }
                            );


                            if (mCurrentPlace != null) {
                                mLocationAddressTextView.setText(mCurrentPlace.getPlaceAddress());
                                mLocationPhoneTextView.setText(mCurrentPlace.getPlacePhoneNumber());
                                mLocationWebsiteTextView.setText(mCurrentPlace.getPlaceWebsite());
                                mLocationOpeningStatusTextView.setText(mCurrentPlace.getPlaceOpeningHourStatus());
                            }


                     /*       lat = mCurrentPlace.getPlaceLatitude();
                            lng = mCurrentPlace.getPlaceLongitude();*/

                            Log.d(TAG, "latii : " + mCurrentPlace.getPlaceLatitude() + "lngi:" + mCurrentPlace.getPlaceLongitude());

                            GoogleApiUrl.lat = mCurrentPlace.getPlaceLatitude();
                            GoogleApiUrl.lng = mCurrentPlace.getPlaceLongitude();
                            final String name = mCurrentPlace.getPlaceName();
                            /*mCurrentPlace.setPlaceLatitude(mCurrentPlace.getPlaceLatitude());
                            mCurrentPlace.setPlaceLongitude(mCurrentPlace.getPlaceLongitude());*/

                            Log.d(TAG, "latii2 : " + GoogleApiUrl.lat + "lngi:" + GoogleApiUrl.lng + "place:" + mCurrentPlace.getPlaceName());

                            Log.d("TAG", "saveee: " + save);


                            if (save.equals("1"))
                                btn_ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {

                                        Intent a = new Intent(DetailTry.this, CreateReminder.class);
                                        a.putExtra("location", name);
                                        a.putExtra("title", title);
                                        a.putExtra("desc", desc);
                                        a.putExtra("date", date);
                                        a.putExtra("time", time);
                                        a.putExtra("repeat", repeat);
                                        a.putExtra("prio", prio);
                                        a.putExtra("manual",true);
                                        a.putExtra("request_code_location", request_code_location);
//                                        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(a);
                                        if(request_code_old != 0){
                                            Intent intent = new Intent(DetailTry.this, GeofenceTrasitionService.class);
                                            PendingIntent pendingIntentManual =PendingIntent.getService(
                                                    DetailTry.this, request_code_old, intent, 0);

                                            boolean ManulUp = (pendingIntentManual != null);
                                            if(ManulUp){
                                                clearGeofence();
                                            }
                                        }
                                        startGeofence(reminderLocationLatLng, name);
                                        finish();

                                    }
                                });

                            else if (save.equals("2"))
                                btn_ok.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent a = new Intent(DetailTry.this, Update_reminder.class);

                                        a.putExtra("location", name);
                                        a.putExtra("title", title);
                                        a.putExtra("desc", desc);
                                        a.putExtra("date", date);
                                        a.putExtra("time", time);
                                        a.putExtra("repeat", repeat);
                                        a.putExtra("prio", prio);
                                        a.putExtra("back", "1");
                                        a.putExtra("manual",true);
                                        a.putExtra("reminder_id",reminder_id);
                                        a.putExtra("request_code_location", request_code_location);
//                                        a.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(a);
                                        if(request_code_old != 0){
                                            Intent intent = new Intent(DetailTry.this, GeofenceTrasitionService.class);
                                            PendingIntent pendingIntentManual =PendingIntent.getService(
                                                    DetailTry.this, request_code_old, intent, 0);

                                            boolean ManulUp = (pendingIntentManual != null);
                                            if(ManulUp){
                                                clearGeofence();
                                            }
                                        }
                                        startGeofence(reminderLocationLatLng, name);
                                        finish();
                                    }
                                });


                            if (rootJsonObject.has("reviews")) {

                                JSONArray reviewJsonArray = rootJsonObject.getJSONArray("reviews");
                                for (int i = 0; i < reviewJsonArray.length(); i++) {
                                    JSONObject currentUserRating = (JSONObject) reviewJsonArray.
                                            get(i);

                                    PlaceUserRating currentPlaceUserRating = new PlaceUserRating(
                                            currentUserRating.getString("author_name"),
                                            currentUserRating.getString("profile_photo_url"),
                                            currentUserRating.getDouble("rating"),
                                            currentUserRating.getString("relative_time_description"),
                                            currentUserRating.getString("text"));

                                    mPlaceUserRatingsArrayList.add(currentPlaceUserRating);
                                }
                            }


                            Bundle currentPlaceDetailData = new Bundle();
                            currentPlaceDetailData.putParcelable(
                                    GoogleApiUrl.CURRENT_LOCATION_DATA_KEY, mCurrentPlace);


                            Bundle currentPlaceUserRatingDetail = new Bundle();
                            currentPlaceUserRatingDetail.putParcelableArrayList(
                                    GoogleApiUrl.CURRENT_LOCATION_USER_RATING_KEY,
                                    mPlaceUserRatingsArrayList);

                            String currentLocationString = DetailTry.this.getSharedPreferences(
                                    GoogleApiUrl.CURRENT_LOCATION_SHARED_PREFERENCE_KEY, 0)
                                    .getString(GoogleApiUrl.CURRENT_LOCATION_DATA_KEY, null);
                            String currentPlace[] = currentLocationString.split(",");

                           /* CameraPosition cameraPosition = CameraPosition.builder()
                                    .target(new LatLng(Double.valueOf(currentPlace[0])
                                            , Double.valueOf(currentPlace[1])))
                                    .zoom(13)
                                    .bearing(0)
                                    .tilt(0)
                                    .build();
                            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));*/

                            Geocoder geocoder = new Geocoder(DetailTry.this, Locale.getDefault());
                            Address currentPlaceLocation = null;
                            Address reminderPlaceLocation = null;
                            try {
                                List<Address> currentAddresses = geocoder.getFromLocation(Double
                                        .valueOf(currentPlace[0]), Double.valueOf(currentPlace[1]), 1);
                                currentPlaceLocation = currentAddresses.get(0);

                                if(GoogleApiUrl.lat > 0) {
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position((new LatLng(currentPlaceLocation.getLatitude(), currentPlaceLocation.getLongitude())))
                                            .title(currentPlaceLocation.getAddressLine(0))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_current_location)));
                                    mGoogleMap.addMarker(new MarkerOptions()
                                            .position((new LatLng(
                                                    GoogleApiUrl.lat, GoogleApiUrl.lng)))
                                            .title(mCurrentPlace != null ? mCurrentPlace.getPlaceName() : "")
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location_on_map)));


                                    CameraPosition cameraPosition = CameraPosition.builder()
                                            .target((new LatLng(
                                                    GoogleApiUrl.lat, GoogleApiUrl.lng)))
                                            .zoom(13)
                                            .bearing(0)
                                            .tilt(0)
                                            .build();

                                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                                    reminderLocationLatLng = new LatLng(GoogleApiUrl.lat, GoogleApiUrl.lng);
                                }else{
                                    Toast.makeText(DetailTry.this, "try again", Toast.LENGTH_SHORT).show();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
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
                    }
                });
        //Adding request to request queue
        AppController.getInstance().addToRequestQueue(placeJsonObjectRequest, jsonArrayTag);
    }


    @Override
    public void onMapReady(GoogleMap mGoogleMap) {

//        Log.d(TAG, "latii2 :" + mCurrentPlace.getPlaceLatitude() + "lngi2:"+ mCurrentPlace.getPlaceLongitude());

        mMapReady = true;
        getCurrentPlaceAllDetails(mCurrentPlaceDetailUrl,mGoogleMap);

        

//        Log.d(TAG, "latii2 :" + mCurrentPlace.getPlaceLatitude() + "lngi2:"+ mCurrentPlace.getPlaceLongitude());

       /* PolylineOptions joinTwoPlace = new PolylineOptions();
        joinTwoPlace.geodesic(true).add(new LatLng(Double.valueOf(currentPlace[0])
                , Double.valueOf(currentPlace[1])))
                .add(new LatLng( GoogleApiUrl.lat,  GoogleApiUrl.lng))
                .width(5)
                .color(ContextCompat.getColor(DetailTry.this, R.color.colorPrimary));

        mGoogleMap.addPolyline(joinTwoPlace);*/
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

        Intent intent = new Intent(this, GeofenceTrasitionService.class);
        intent.putExtra("name", locationName);
        intent.putExtra("requestcode",request_code_location);

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


    // Clear Geofence
    private void clearGeofence() {
        Log.d(TAG, "clearGeofence()");
        LocationServices.GeofencingApi.removeGeofences(
                googleApiClient,
                deleteGeofencePendingIntent()
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if ( status.isSuccess() ) {
                    // remove drawing
                    //removeGeofenceDraw();
                }
            }
        });
    }

    public PendingIntent deleteGeofencePendingIntent(){
        Log.d(TAG, "createGeofencePendingIntent");
        Intent intent = new Intent(this, GeofenceTrasitionService.class);

        PendingIntent pendingIntentManual =PendingIntent.getService(
                this, request_code_old, intent, 0);

        return pendingIntentManual;
    }


}
