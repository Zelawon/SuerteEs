package dte.masteriot.mdp.suertees.Subscriber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dte.masteriot.mdp.suertees.R;

public class IncidentAdapter extends RecyclerView.Adapter<IncidentAdapter.IncidentViewHolder> {

    private List<String> incidentList;

    public IncidentAdapter(List<String> incidentList) {
        this.incidentList = incidentList;
    }

    @NonNull
    @Override
    public IncidentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscriber, parent, false);
        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IncidentViewHolder holder, int position) {
        String incident = incidentList.get(position);
        holder.incidentTextView.setText(incident);
    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public static class IncidentViewHolder extends RecyclerView.ViewHolder {

        TextView incidentTextView;

        public IncidentViewHolder(@NonNull View itemView) {
            super(itemView);
            incidentTextView = itemView.findViewById(R.id.textViewIncidentz);
        }
    }
}
