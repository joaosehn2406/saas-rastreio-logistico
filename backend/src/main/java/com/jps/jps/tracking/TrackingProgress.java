package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventStatusResponse;

import java.time.Instant;
import java.util.List;

public record TrackingProgress(
        int percentage,
        int completedStages,
        int totalStages,
        boolean delivered,
        int eventCount,
        Instant lastUpdated,
        List<String> visitedCities,
        List<EventStatusResponse> nextStatuses
) {
    public TrackingProgress {
        visitedCities = List.copyOf(visitedCities);
        nextStatuses = List.copyOf(nextStatuses);
    }
}
