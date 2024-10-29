package dte.masteriot.mdp.suertees.viewlists;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dte.masteriot.mdp.suertees.R;
import dte.masteriot.mdp.suertees.objects.Incident;

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
//        holder.typeTextView.setText(incident.getType());
        holder.dateTextView.setText(incident.getDate());
//        holder.locationTextView.setText(incident.getLocation());
        holder.urgencyTextView.setText(incident.getUrgency());

        // Long click listener
        holder.itemView.setOnLongClickListener(v -> {
            Intent intent = new Intent(context, IncidentDetailActivity.class); // Change to your detail activity
            intent.putExtra("incidentId", incident.getId()); // Pass the incident ID to the detail activity
            context.startActivity(intent);
            return true; // Indicate that the long click was handled
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
