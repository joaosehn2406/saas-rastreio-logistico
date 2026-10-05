package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventStatus;
import com.jps.jps.event.eventByCode.EventStatusResponse;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class TrackingTimelineAnalyzer {

    private static final Comparator<TimelineEventResponse> NEWEST_FIRST = Comparator.comparing(
            TimelineEventResponse::timestamp,
            Comparator.nullsLast(Comparator.reverseOrder())
    );

    public TrackingAnalysis analyze(Instant shipmentCreatedAt, List<TimelineEventResponse> sourceEvents) {
        List<TimelineEventResponse> events = orderedEvents(sourceEvents);
        EventStatus currentStatus = currentStatus(events);
        TrackingProgress progress = progress(shipmentCreatedAt, currentStatus, events);
        return new TrackingAnalysis(events, EventStatusResponse.from(currentStatus), progress);
    }

    public List<TimelineEventResponse> orderedEvents(List<TimelineEventResponse> sourceEvents) {
        if (sourceEvents == null || sourceEvents.isEmpty()) {
            return List.of();
        }

        List<TimelineEventResponse> ordered = new ArrayList<>(sourceEvents);
        ordered.sort(NEWEST_FIRST);
        return List.copyOf(ordered);
    }

    public EventStatus currentStatus(List<TimelineEventResponse> orderedEvents) {
        if (orderedEvents == null) {
            return EventStatus.REGISTERED;
        }

        return orderedEvents.stream()
                .map(TimelineEventResponse::status)
                .filter(status -> status != null && status.id() != null)
                .map(status -> EventStatus.fromId(status.id()))
                .findFirst()
                .orElse(EventStatus.REGISTERED);
    }

    private TrackingProgress progress(
            Instant shipmentCreatedAt,
            EventStatus currentStatus,
            List<TimelineEventResponse> events
    ) {
        Instant lastUpdated = events.stream()
                .map(TimelineEventResponse::timestamp)
                .filter(timestamp -> timestamp != null)
                .findFirst()
                .orElse(shipmentCreatedAt);

        List<String> visitedCities = visitedCities(events);
        List<EventStatusResponse> nextStatuses = currentStatus.nextStatuses()
                .stream()
                .map(EventStatusResponse::from)
                .toList();

        return new TrackingProgress(
                currentStatus.progressPercentage(),
                currentStatus.getId() + 1,
                EventStatus.values().length,
                currentStatus.isTerminal(),
                events.size(),
                lastUpdated,
                visitedCities,
                nextStatuses
        );
    }

    private List<String> visitedCities(List<TimelineEventResponse> events) {
        Map<String, String> cities = new LinkedHashMap<>();

        for (int index = events.size() - 1; index >= 0; index--) {
            String city = events.get(index).city();
            if (city == null || city.isBlank()) {
                continue;
            }

            String normalizedCity = city.trim();
            cities.putIfAbsent(normalizedCity.toLowerCase(Locale.ROOT), normalizedCity);
        }

        return List.copyOf(cities.values());
    }
}
