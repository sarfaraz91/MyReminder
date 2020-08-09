package com.fyp.reminder.Adapater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.reminder.R;
import com.fyp.reminder.DetailTry;

import com.fyp.reminder.Utils.GoogleApiUrl;
import com.fyp.reminder.Utils.HelperClass;
import com.fyp.reminder.model.Place;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class PlaceListAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //Context of the activity
    private Context mContext;
    private ArrayList<Place> mNearByPlaceArrayList = new ArrayList<>();

    public PlaceListAdapter(Context context, ArrayList<Place> nearByPlaceArrayList) {
        mContext = context;
        mNearByPlaceArrayList = nearByPlaceArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PlaceListAdapterViewHolder(LayoutInflater
                .from(mContext).inflate(R.layout.place_list_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((PlaceListAdapterViewHolder) holder).bindView(position);
    }

    @Override
    public int getItemCount() {
        return mNearByPlaceArrayList.size();
    }

    private class PlaceListAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        int mItemPosition;
        //reference of the views
        private TextView mPlaceDistanceTextView;
        private TextView mPlaceNameTextView;
        private TextView mPlaceAddressTextView;
        private TextView mPlaceOpenStatusTextView;
        private MaterialRatingBar mPlaceRating;
        private ImageView mLocationIcon;


        private PlaceListAdapterViewHolder(View itemView) {
            super(itemView);




            mPlaceDistanceTextView = (TextView) itemView.findViewById(R.id.place_distance_text_view);
            mPlaceNameTextView = (TextView) itemView.findViewById(R.id.place_name);
            mPlaceAddressTextView = (TextView) itemView.findViewById(R.id.place_address);
            mPlaceOpenStatusTextView = (TextView) itemView.findViewById(R.id.place_open_status);
            mPlaceRating = (MaterialRatingBar) itemView.findViewById(R.id.place_rating);
            mLocationIcon = (ImageView) itemView.findViewById(R.id.location_icon);

            itemView.setOnClickListener(this);
        }

        private void bindView(int position) {



            mItemPosition = position;

            mPlaceNameTextView.setText(mNearByPlaceArrayList.get(mItemPosition).getPlaceName());
          /*  mPlaceNameTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                    "Roboto-Regular.ttf"));*/
            mPlaceAddressTextView.setText(mNearByPlaceArrayList.get(mItemPosition).getPlaceAddress());
            /*mPlaceAddressTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                    "Roboto-Regular.ttf"));*/
            if (mNearByPlaceArrayList.get(mItemPosition).getPlaceOpeningHourStatus().equals("true")) {
                mPlaceOpenStatusTextView.setText(R.string.open_now);
          /*      mPlaceOpenStatusTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                        "Roboto-Regular.ttf"));*/
            } else if (mNearByPlaceArrayList.get(mItemPosition).getPlaceOpeningHourStatus().equals("false")) {
                mPlaceOpenStatusTextView.setText(R.string.closed);
                /*mPlaceOpenStatusTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                        "Roboto-Regular.ttf"));*/
            } else {
                mPlaceOpenStatusTextView.setText(mNearByPlaceArrayList.get(mItemPosition)
                        .getPlaceOpeningHourStatus());
                /*mPlaceOpenStatusTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                        "Roboto-Regular.ttf"));*/
            }
            mPlaceRating.setRating(Float.parseFloat(String.valueOf(mNearByPlaceArrayList.get(mItemPosition)
                    .getPlaceRating())));

            mLocationIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.color_divider));

            String currentLocation = mContext.getSharedPreferences(
                    GoogleApiUrl.CURRENT_LOCATION_SHARED_PREFERENCE_KEY, 0)
                    .getString(GoogleApiUrl.CURRENT_LOCATION_DATA_KEY, null);

            String[] currentLocationLatitudeAndLongitude = currentLocation.split(",");

            Double distance = HelperClass.getDistanceFromLatLonInKm(
                    Double.parseDouble(currentLocationLatitudeAndLongitude[0]),
                    Double.parseDouble(currentLocationLatitudeAndLongitude[1]),
                    mNearByPlaceArrayList.get(mItemPosition).getPlaceLatitude(),
                    mNearByPlaceArrayList.get(mItemPosition).getPlaceLongitude()) / 1000;

            String distanceBetweenTwoPlace = String.valueOf(distance);
            mPlaceDistanceTextView.setText("~ " + distanceBetweenTwoPlace.substring(0, 3) + " Km");
        }
        @Override
        public void onClick(View v) {

            Intent i = ((Activity) mContext).getIntent();
            String title = i.getStringExtra("title");
            String desc = i.getStringExtra("desc");
            String date = i.getStringExtra("date");
            String time = i.getStringExtra("time");
            String repeat = i.getStringExtra("repeat");
            String prio = i.getStringExtra("prio");
            String save = i.getStringExtra("save");
            String update = i.getStringExtra("update");
            long reminder_id = i.getLongExtra("reminder_id",0);
            int request_code_location = i.getIntExtra("request_code_location",0);
            int request_code_old = i.getIntExtra("request_code_old",0);

            Log.d("TAG", "onClick1: "+  desc );

            if (isNetworkAvailable()) {
                Intent currentLocationDetailIntent = new Intent(mContext, DetailTry.class);


                currentLocationDetailIntent.putExtra("desc", desc);
                currentLocationDetailIntent.putExtra("title",title);
                currentLocationDetailIntent.putExtra("desc",desc);
                currentLocationDetailIntent.putExtra("date",date);
                currentLocationDetailIntent.putExtra("time",time);
                currentLocationDetailIntent.putExtra("repeat",repeat);
                currentLocationDetailIntent.putExtra("prio",prio);
                currentLocationDetailIntent.putExtra("save",save);
                currentLocationDetailIntent.putExtra("update",update);
                currentLocationDetailIntent.putExtra("reminder_id",reminder_id);
                currentLocationDetailIntent.putExtra("request_code_location",request_code_location);
                currentLocationDetailIntent.putExtra("request_code_old",request_code_old);


                currentLocationDetailIntent.putExtra(GoogleApiUrl.LOCATION_ID_EXTRA_TEXT,
                        mNearByPlaceArrayList.get(mItemPosition).getPlaceId());
                mContext.startActivity(currentLocationDetailIntent);
            } else
                Snackbar.make(mLocationIcon, R.string.no_connection_string,
                        Snackbar.LENGTH_SHORT).show();
        }

        private boolean isNetworkAvailable() {
            ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
        }
    }
}
