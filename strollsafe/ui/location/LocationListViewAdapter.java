package com.example.strollsafe.ui.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.strollsafe.R;
import com.example.strollsafe.pwd.PWDLocation;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class LocationListViewAdapter extends RecyclerView.Adapter<LocationListViewAdapter.ViewHolder> {

    // creating a variable for array list and context.
    private ArrayList<PWDLocation> PWDLocationList;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Context context;

    // creating a constructor for our variables.
    public LocationListViewAdapter(ArrayList<PWDLocation> PWDLocationList, Context context) {
        this.PWDLocationList = PWDLocationList;
        this.context = context;
    }

    @NonNull
    @Override
    public LocationListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // below line is to inflate our layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.location_view_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull LocationListViewAdapter.ViewHolder holder, int position) {
        // setting data to our views of recycler view.
        PWDLocation location = PWDLocationList.get(position);
        holder.tv_streetAddress.setText(location.getAddress());
        holder.tv_lastHereDateTime.setText("Last here on " + location.getLastHereDateTime().format(DATE_FORMAT));
    }

    @Override
    public int getItemCount() {
        // returning the size of array list.
        return PWDLocationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our views.
        private TextView tv_streetAddress;
        private TextView tv_lastHereDateTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_streetAddress = itemView.findViewById(R.id.tv_streetAddress);
            tv_lastHereDateTime = itemView.findViewById(R.id.tv_lastHereDateTime);
        }
    } // end of ViewHolder

}
