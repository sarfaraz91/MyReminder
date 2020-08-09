package com.fyp.reminder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.fyp.reminder.Adapater.OptionScreenItemListAdapter;
import com.fyp.reminder.Utils.PlaceDetailProvider;
import com.google.android.material.navigation.NavigationView;

public class OptionScreenActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private GridLayoutManager mGridLayoutManager;
    private OptionScreenItemListAdapter mOptionScreenItemListAdapter;
    private String[] itemString;
    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_screen);

   /*     Intent i = getIntent();
        String desc = i.getStringExtra("desc");

        Log.d("TAG", "onClick2: "+desc);*/

  /*      Toolbar actionBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(actionBar);

        setTitle(R.string.app_name);
        actionBar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
*/

      Intent i = getIntent();
        long desc = i.getLongExtra("reminder_id",0);



        itemString = PlaceDetailProvider.popularPlaceTagName;
        mOptionScreenItemListAdapter = new OptionScreenItemListAdapter(this, itemString);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mGridLayoutManager = new GridLayoutManager(this, 3);
        mRecyclerView.setLayoutManager(mGridLayoutManager);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemViewCacheSize(36);
        mRecyclerView.setDrawingCacheEnabled(true);
        mRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        mRecyclerView.setAdapter(mOptionScreenItemListAdapter);



    }
}
