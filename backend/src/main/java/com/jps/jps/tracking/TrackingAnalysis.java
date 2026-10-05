package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventStatusResponse;
import com.jps.jps.event.eventByCode.TimelineEventResponse;

import java.util.List;

public record TrackingAnalysis(
        List<TimelineEventResponse> events,
        EventStatusResponse currentStatus,
        TrackingProgress progress
) {
    public TrackingAnalysis {
        events = List.copyOf(events);
    }
}
