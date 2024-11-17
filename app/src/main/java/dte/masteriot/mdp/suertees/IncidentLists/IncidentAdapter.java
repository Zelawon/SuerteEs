package dte.masteriot.mdp.suertees.IncidentLists;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dte.masteriot.mdp.suertees.AdminActions.IncidentAdminActivity;
import dte.masteriot.mdp.suertees.Objects.Incident;
import dte.masteriot.mdp.suertees.R;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {
    private List<Incident> incidentList;
    private Context context;

    public IncidentAdapter(List<Incident> incidentList, Context context) {
        this.incidentList = incidentList;
        this.context = context;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_incident, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        Incident incident = incidentList.get(position);
        holder.titleTextView.setText(incident.getTitle());
        holder.descTextView.setText(incident.getDescription());
        holder.dateTextView.setText(incident.getDate());
        holder.urgencyTextView.setText(incident.getUrgency());

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Intent intent;
            // Check if the user is the specified admin
            if ("4KobjJEKCQWxVVRx7lgjSVXkp7I2".equals(currentUserId)) {
                intent = new Intent(context, IncidentAdminActivity.class); // Navigate to admin activity
            } else {
                intent = new Intent(context, IncidentDetailActivity.class); // Navigate to detail activity
            }
            intent.putExtra("incidentId", incident.getId()); // Pass the incident ID
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public static class IncidentViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descTextView;
        //        public TextView typeTextView;
        public TextView dateTextView;
        //        public TextView locationTextView;
        public TextView urgencyTextView;

        public IncidentViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            descTextView = itemView.findViewById(R.id.descTextView);
//            typeTextView = itemView.findViewById(R.id.typeTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
//            locationTextView = itemView.findViewById(R.id.locationTextView);
            urgencyTextView = itemView.findViewById(R.id.urgencyTextView);
        }
    }
}
