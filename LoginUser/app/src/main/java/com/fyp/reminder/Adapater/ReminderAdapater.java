package com.fyp.reminder.Adapater;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.fyp.reminder.AddReminder;
import com.fyp.reminder.AutoLocationService;
import com.fyp.reminder.GeofenceServiceAutoReminder;
import com.fyp.reminder.GeofenceTrasitionService;
import com.fyp.reminder.MainActivity;
import com.fyp.reminder.NotificationActivity;
import com.fyp.reminder.R;
import com.fyp.reminder.ReminderReciever;
import com.fyp.reminder.SnoozeReciever;
import com.fyp.reminder.Update_reminder;
import com.fyp.reminder.Utils.PreferencesHandler;
import com.fyp.reminder.model.Reminder;
import com.fyp.reminder.sql.DatabaseHelper;

import java.util.List;

public class ReminderAdapater extends RecyclerView.Adapter<ReminderAdapater.ViewHolder> {

    private List<Reminder> mReminderlist;
    private Context mContext;
    private RecyclerView mRecyclerV;
    PreferencesHandler preferencesHandler;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public TextView title;
        public TextView date;
        public TextView place;
        public TextView textViewAddress;
        public TextView time;
        public ImageView active, delete, priority, location;
        public double lat, lng;


        public View layout;

        public ViewHolder(View view) {
            super(view);
            layout = view;

            title = (TextView) view.findViewById(R.id.title);
            date = (TextView) view.findViewById(R.id.date);
            time = (TextView) view.findViewById(R.id.time);
            active = (ImageView) view.findViewById(R.id.active);
            delete = (ImageView) view.findViewById(R.id.delete);
            priority = (ImageView) view.findViewById(R.id.priority);
            location = (ImageView) view.findViewById(R.id.location);
            place = (TextView) view.findViewById(R.id.place);


        }
    }

    public void add(int position, Reminder reminder) {
        mReminderlist.add(position, reminder);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        mReminderlist.remove(position);
        notifyItemRemoved(position);
    }


    // Provide a suitable constructor (depends on the kind of dataset)
    public ReminderAdapater(List<Reminder> myDataset, Context context, RecyclerView recyclerView) {
        mReminderlist = myDataset;
        mContext = context;
        mRecyclerV = recyclerView;
        preferencesHandler = new PreferencesHandler(context);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReminderAdapater.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());
        View v =
                inflater.inflate(R.layout.reminder_cell, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element


        final Reminder reminder = mReminderlist.get(position);
        holder.title.setText(reminder.getTitle());
        if (!reminder.getDate().equals("")) {
            holder.date.setText(reminder.getDate());
        } else if (!reminder.getLocation().equals("")) {
            holder.date.setText(reminder.getLocation());
        }

        holder.time.setText(reminder.getTime());
        // holder.place.setText(reminder.getLocation());

        Log.d("TAG", "onBindViewHolder: " + reminder.getLocation());

        if (reminder.getLocation().equals("")) {
            holder.location.setVisibility(View.GONE);
            //holder.priority.setVisibility(View.VISIBLE);
        } else {
            holder.location.setVisibility(View.VISIBLE);
            //holder.priority.setVisibility(View.GONE);
        }

       /* holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Dialog dialog = new Dialog(mContext);

                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                /////make map clear
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

                dialog.setContentView(R.layout.dialouge_map);////your custom content

                MapView mMapView = (MapView) dialog.findViewById(R.id.mapView);
                MapsInitializer.initialize(mContext);

                mMapView.onCreate(dialog.onSaveInstanceState());
                mMapView.onResume();


                mMapView.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(final GoogleMap googleMap) {
                        LatLng posisiabsen = new LatLng(24.861696, 67.073378);
                        googleMap.addMarker(new MarkerOptions().position(posisiabsen).title("Me"));
                        googleMap.moveCamera(CameraUpdateFactory.newLatLng(posisiabsen));
                        googleMap.getUiSettings().setZoomControlsEnabled(true);
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
                    }
                });


                Button dialogButton = (Button) dialog.findViewById(R.id.btn_ok);
                // if button is clicked, close the custom dialog
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();

            }


        });*/


        holder.location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Reminder Location:");
                builder.setMessage(reminder.getLocation());

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();


            }
        });


        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle("Delete Reminder!");
                builder.setMessage("Have you done this task? Really want to delete this reminder?");
           /*   builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {

                      //go to update activity
                      //  goToUpdateActivity(reminder.getId());

                  }
              });*/
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
                        dbHelper.deleteReminderRecord(reminder.getId(),preferencesHandler.getUserIdId(), mContext);

                        mReminderlist.remove(position);
                        mRecyclerV.removeViewAt(position);
                        notifyItemRemoved(position);

                        if(reminder.getSub_type().equals("automatic")){
                            if(preferencesHandler.getServiceAuto()){
                                Intent activityIntent = new Intent(mContext, AddReminder.class);
                                activityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                mContext.startActivity(activityIntent);
                                Intent intent2 = new Intent(mContext, AutoLocationService.class);
                                intent2.setAction("stopservice");
                                mContext.startService(intent2);
                            }
                        }

                        Intent autoLocationIntent = new Intent(mContext, GeofenceServiceAutoReminder.class);
                        PendingIntent pendingIntentAutoLoc = PendingIntent.getBroadcast(
                                mContext, reminder.getRequest_code(), autoLocationIntent, PendingIntent.FLAG_NO_CREATE);

                        boolean autoUp = (pendingIntentAutoLoc != null);
                        if(autoUp){
                            pendingIntentAutoLoc.cancel();
                        }

                        Intent manualLocationIntent = new Intent(mContext, GeofenceTrasitionService.class);
                        PendingIntent pendingIntentmanual = PendingIntent.getBroadcast(
                                mContext, reminder.getRequest_code(), manualLocationIntent, PendingIntent.FLAG_NO_CREATE);

                        boolean manualUp = (pendingIntentmanual != null);
                        if(manualUp){
                            pendingIntentmanual.cancel();
                        }

                        Intent reminderIntent = new Intent(mContext, ReminderReciever.class);
                        NotificationActivity.getDismissIntent(reminder.getRequest_code(), mContext);

                        PendingIntent pendingIntentReminder = PendingIntent.getBroadcast(
                                mContext, reminder.getRequest_code(), reminderIntent, PendingIntent.FLAG_NO_CREATE);

                        boolean alarmUp = (pendingIntentReminder != null);
                        if(alarmUp){
                            AddReminder.manager.cancel(pendingIntentReminder);
                            pendingIntentReminder.cancel();
                            AddReminder.notifManager.cancel(reminder.getRequest_code());
                        }

                        notifyItemRangeChanged(position, mReminderlist.size());
                        notifyDataSetChanged();

                        if (mReminderlist.size() < 1) {
                            AddReminder.container_no_tasks.setVisibility(View.VISIBLE);
                        } else {
                            AddReminder.container_no_tasks.setVisibility(View.GONE);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

            }


        });


        holder.priority.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToUpdateActivity(reminder.getId(), reminder);

            }


        });


        //listen to single view layout click
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


    }

    private void goToUpdateActivity(long reminderId, Reminder reminder) {
        Intent goToUpdate = new Intent(mContext, Update_reminder.class);
        goToUpdate.putExtra("REMINDER_ID", reminderId);
        goToUpdate.putExtra("back", "0");
        goToUpdate.putExtra("prio", reminder.getPriority());
        goToUpdate.putExtra("reminder", reminder);

        mContext.startActivity(goToUpdate);

    }

  /*  private void goToUpdateActivity(long reminderId){
        Intent goToUpdate = new Intent(mContext, UpdateRecordActivity.class);
        goToUpdate.putExtra("USER_ID", reminderId);
        mContext.startActivity(goToUpdate);
    }*/


    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mReminderlist.size();
    }


}





    /*private ArrayList<Reminder> listreminder;
    public ImageView overflow;
    private Context mContext;
    private ArrayList<Reminder> mFilteredList;


    public ReminderAdapater(ArrayList<Reminder> listBeneficiary, Context mContext) {
        this.listreminder = listBeneficiary;
        this.mContext = mContext;
        this.mFilteredList = listBeneficiary;


    }

    public class BeneficiaryViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewName;
        public TextView textViewEmail;
        public TextView textViewAddress;
        public TextView textViewCountry;
        public  ImageView overflow;

        public BeneficiaryViewHolder(View view) {
            super(view);
            textViewName = (TextView) view.findViewById(R.id.textViewName);
            textViewEmail = (TextView) view.findViewById(R.id.textViewEmail);
            textViewAddress = (TextView) view.findViewById(R.id.textViewAddress);
            textViewCountry = (TextView) view.findViewById(R.id.textViewCountry);

        }


    }




    @Override
    public BeneficiaryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // inflating recycler item view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reminder_cell, parent, false);

        return new BeneficiaryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final BeneficiaryViewHolder holder, int position) {
        holder.textViewName.setText(listreminder.get(position).getTitle());
        holder.textViewEmail.setText(listreminder.get(position).getDescr());
        holder.textViewAddress.setText(listreminder.get(position).getDate());
        holder.textViewCountry.setText(listreminder.get(position).getTime());


    }


    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }




}
*/