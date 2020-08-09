package com.fyp.reminder.Adapater;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
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
import com.fyp.reminder.PlaceListOnMapActivity;

import com.fyp.reminder.Utils.GoogleApiUrl;
import com.fyp.reminder.Utils.PlaceDetailProvider;
import com.google.android.material.snackbar.Snackbar;

import java.util.Random;

public class OptionScreenItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    //Context of the activity
    private Context mContext;
    private String[] mPlacesListTag;


    public OptionScreenItemListAdapter(Context context, String[] placesListTag) {
        mContext = context;
        mPlacesListTag = placesListTag;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new HomeScreenItemListHolder(LayoutInflater.from(mContext)
                .inflate(R.layout.home_screen_item_layout, parent, false));

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((HomeScreenItemListHolder) holder).bindView(position);

        Intent i = ((Activity) mContext).getIntent();
        long desc = i.getLongExtra("reminder_id",0);
    }

    @Override
    public int getItemCount() {
        return mPlacesListTag.length;
    }


    private class HomeScreenItemListHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mPlaceTextView;
        private ImageView mPlaceImageView;
        private int mItemPosition;

        private HomeScreenItemListHolder(View itemView) {
            super(itemView);
            mPlaceTextView = (TextView) itemView.findViewById(R.id.place_text_view);
            mPlaceImageView = (ImageView) itemView.findViewById(R.id.place_icon);

            mPlaceImageView.setOnClickListener(this);
        }

        private void bindView(int position) {
            mPlaceTextView.setText(mPlacesListTag[position]);

            mPlaceImageView.setImageDrawable(ContextCompat.getDrawable(mContext,
                    PlaceDetailProvider.popularPlaceTagIcon[position]));

            /*mPlaceTextView.setTypeface(Typeface.createFromAsset(mContext.getAssets(),
                    "Roboto-Regular.ttf"));*/
            int[] colorArray = PlaceDetailProvider.accentColor;

            int randomColor = colorArray[new Random().nextInt(colorArray.length)];

            ((GradientDrawable) mPlaceImageView.getBackground())
                    .setColor(ContextCompat.getColor(mContext, randomColor));

            mItemPosition = position;

        }

        @Override
        public void onClick(View v) {

            if (isNetworkAvailable()) {
                /*
                 * get the tag for query parameter like Atm, Bank etc.
                 */
                String locationTag = mPlacesListTag[mItemPosition];

                if (locationTag.equals("Bus Stand"))
                    locationTag = "bus_station";
                else if (locationTag.equals("Government Office"))
                    locationTag = "local_government_office";
                else if (locationTag.equals("Railway Station"))
                    locationTag = "train_station";
                else if (locationTag.equals("Hotel"))
                    locationTag = "restaurant";
                else if (locationTag.equals("Police Station"))
                    locationTag = "police";
                else
                    locationTag = locationTag.replace(' ', '_').toLowerCase();
              /*  *
                 * Intent to start Place list activity with locationTag as extra data.
                 */
//                Intent i = new Intent();
//                String desc = i.getStringExtra("desc");
//                Log.d("TAG", "onClick1: "+desc);


                Intent i = ((Activity) mContext).getIntent();
                String title = i.getStringExtra("title");
                String desc = i.getStringExtra("desc");
                String date = i.getStringExtra("date");
                String time = i.getStringExtra("time");
                String repeat = i.getStringExtra("repeat");
                String prio = i.getStringExtra("prio");

                String save = i.getStringExtra("save");
                String update = i.getStringExtra("update");
                String sub_type = i.getStringExtra("sub_type");
                long reminder_id = i.getLongExtra("reminder_id",0);
                int request_code_location = i.getIntExtra("request_code_location",0);
                int request_code_old = i.getIntExtra("request_code_old",0);
                Log.d("TAG", "onClick1: "+  desc );


                              Intent placeTagIntent = new Intent(mContext, PlaceListOnMapActivity.class);

                placeTagIntent.putExtra("title",title);
                placeTagIntent.putExtra("desc",desc);
                placeTagIntent.putExtra("date",date);
                placeTagIntent.putExtra("time",time);
                placeTagIntent.putExtra("repeat",repeat);
                placeTagIntent.putExtra("prio",prio);
                placeTagIntent.putExtra("save",save);
                placeTagIntent.putExtra("update",update);
                placeTagIntent.putExtra("sub_type",sub_type);
                placeTagIntent.putExtra("reminder_id",reminder_id);
                placeTagIntent.putExtra("request_code_location",request_code_location);
                placeTagIntent.putExtra("request_code_old",request_code_old);


                placeTagIntent.putExtra(GoogleApiUrl.LOCATION_NAME_EXTRA_TEXT,
                        PlaceDetailProvider.popularPlaceTagName[mItemPosition]);
                placeTagIntent.putExtra(GoogleApiUrl.LOCATION_TYPE_EXTRA_TEXT, locationTag);
                mContext.startActivity(placeTagIntent);
            } else
                Snackbar.make(mPlaceImageView, R.string.no_connection_string,
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