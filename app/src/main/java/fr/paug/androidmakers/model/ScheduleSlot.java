package fr.paug.androidmakers.model;

import android.support.annotation.NonNull;

public class ScheduleSlot implements Comparable<ScheduleSlot>{

    public final int room;
    public final int sessionId;
    public final long startDate;
    public final long endDate;

    public ScheduleSlot(int room, int sessionId, long startDate, long endDate) {
        this.room = room;
        this.sessionId = sessionId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "ScheduleSlot{" +
                "room=" + room +
                ", sessionId=" + sessionId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }

    @Override
    public int compareTo(@NonNull ScheduleSlot o) {
        return Long.valueOf(startDate).compareTo(o.startDate);
    }
}
