package com.example.kafedrameetingapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {
    private List<Meeting> meetingList;

    public MeetingAdapter(List<Meeting> meetingList) {
        this.meetingList = meetingList;
    }

    @NonNull
    @Override
    public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meeting, parent, false);
        return new MeetingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
        Meeting meeting = meetingList.get(position);
        holder.topic.setText("Тема: " + meeting.topic);
        holder.agenda.setText("Повестка дня: " + meeting.agenda);
        holder.date.setText("Дата: " + meeting.date);
        holder.time.setText("Время: " + meeting.time);
        holder.protocol.setText("Протокол №: " + meeting.protocolNumber);
    }

    @Override
    public int getItemCount() {
        return meetingList.size();
    }

    static class MeetingViewHolder extends RecyclerView.ViewHolder {
        TextView topic, agenda, date, time, protocol;

        public MeetingViewHolder(@NonNull View itemView) {
            super(itemView);
            topic = itemView.findViewById(R.id.textTopic);
            agenda = itemView.findViewById(R.id.textAgenda);
            date = itemView.findViewById(R.id.textDate);
            time = itemView.findViewById(R.id.textTime);
            protocol = itemView.findViewById(R.id.textProtocol);
        }
    }
}
