package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventStatus;
import com.jps.jps.event.eventByCode.EventStatusResponse;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingTimelineAnalyzerTest {

    private TrackingTimelineAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new TrackingTimelineAnalyzer();
    }

    @Test
    void createsInitialProgressWhenShipmentHasNoEvents() {
        Instant createdAt = Instant.parse("2026-09-10T10:15:30Z");

        TrackingAnalysis analysis = analyzer.analyze(createdAt, List.of());

        assertTrue(analysis.events().isEmpty());
        assertEquals(EventStatusResponse.from(EventStatus.REGISTERED), analysis.currentStatus());
        assertEquals(0, analysis.progress().percentage());
        assertEquals(1, analysis.progress().completedStages());
        assertEquals(6, analysis.progress().totalStages());
        assertEquals(0, analysis.progress().eventCount());
        assertEquals(createdAt, analysis.progress().lastUpdated());
        assertFalse(analysis.progress().delivered());
        assertTrue(analysis.progress().visitedCities().isEmpty());
        assertEquals(
                List.of(EventStatusResponse.from(EventStatus.COLLECTED)),
                analysis.progress().nextStatuses()
        );
    }

    @Test
    void treatsNullEventCollectionAsAnEmptyTimeline() {
        Instant createdAt = Instant.parse("2026-09-11T08:00:00Z");

        TrackingAnalysis analysis = analyzer.analyze(createdAt, null);

        assertTrue(analysis.events().isEmpty());
        assertEquals(createdAt, analysis.progress().lastUpdated());
        assertEquals(EventStatusResponse.from(EventStatus.REGISTERED), analysis.currentStatus());
    }

    @Test
    void ordersEventsFromNewestToOldestWithoutChangingInput() {
        TimelineEventResponse oldest = event(
                "2026-09-01T10:00:00Z",
                "Blumenau",
                EventStatus.COLLECTED
        );
        TimelineEventResponse newest = event(
                "2026-09-03T10:00:00Z",
                "Curitiba",
                EventStatus.IN_TRANSIT
        );
        TimelineEventResponse middle = event(
                "2026-09-02T10:00:00Z",
                "Joinville",
                EventStatus.IN_SEPARATION
        );
        List<TimelineEventResponse> source = List.of(oldest, newest, middle);

        List<TimelineEventResponse> ordered = analyzer.orderedEvents(source);

        assertEquals(List.of(newest, middle, oldest), ordered);
        assertEquals(List.of(oldest, newest, middle), source);
        assertNotSame(source, ordered);
        assertThrows(UnsupportedOperationException.class, () -> ordered.add(oldest));
    }

    @Test
    void usesNewestValidEventAsCurrentStatus() {
        TimelineEventResponse invalidNewest = new TimelineEventResponse(
                Instant.parse("2026-09-04T10:00:00Z"),
                "SC",
                "Florianópolis",
                null,
                null,
                null,
                null
        );
        TimelineEventResponse validOlder = event(
                "2026-09-03T10:00:00Z",
                "Curitiba",
                EventStatus.OUT_FOR_DELIVERY
        );

        TrackingAnalysis analysis = analyzer.analyze(
                Instant.parse("2026-09-01T10:00:00Z"),
                List.of(validOlder, invalidNewest)
        );

        assertEquals(EventStatusResponse.from(EventStatus.OUT_FOR_DELIVERY), analysis.currentStatus());
        assertEquals(80, analysis.progress().percentage());
    }

    @Test
    void listsVisitedCitiesInTravelOrderAndRemovesDuplicates() {
        List<TimelineEventResponse> events = List.of(
                event("2026-09-04T10:00:00Z", " São Paulo ", EventStatus.OUT_FOR_DELIVERY),
                event("2026-09-03T10:00:00Z", "são paulo", EventStatus.IN_TRANSIT),
                event("2026-09-02T10:00:00Z", "Curitiba", EventStatus.IN_TRANSIT),
                event("2026-09-01T10:00:00Z", "Blumenau", EventStatus.COLLECTED)
        );

        TrackingAnalysis analysis = analyzer.analyze(
                Instant.parse("2026-08-31T10:00:00Z"),
                events
        );

        assertEquals(List.of("Blumenau", "Curitiba", "são paulo"), analysis.progress().visitedCities());
        assertEquals(4, analysis.progress().eventCount());
        assertEquals(Instant.parse("2026-09-04T10:00:00Z"), analysis.progress().lastUpdated());
    }

    @Test
    void ignoresBlankAndNullCities() {
        TimelineEventResponse nullCity = new TimelineEventResponse(
                Instant.parse("2026-09-02T10:00:00Z"),
                "SC",
                null,
                EventStatusResponse.from(EventStatus.IN_TRANSIT),
                null,
                null,
                null
        );
        TimelineEventResponse blankCity = event(
                "2026-09-01T10:00:00Z",
                "   ",
                EventStatus.COLLECTED
        );

        TrackingAnalysis analysis = analyzer.analyze(
                Instant.parse("2026-08-31T10:00:00Z"),
                List.of(nullCity, blankCity)
        );

        assertTrue(analysis.progress().visitedCities().isEmpty());
    }

    @Test
    void marksDeliveredShipmentAsCompleteWithoutNextStatuses() {
        TimelineEventResponse delivered = event(
                "2026-09-05T18:30:00Z",
                "São Paulo",
                EventStatus.DELIVERED
        );

        TrackingAnalysis analysis = analyzer.analyze(
                Instant.parse("2026-09-01T10:00:00Z"),
                List.of(delivered)
        );

        assertEquals(100, analysis.progress().percentage());
        assertEquals(6, analysis.progress().completedStages());
        assertTrue(analysis.progress().delivered());
        assertTrue(analysis.progress().nextStatuses().isEmpty());
    }

    private TimelineEventResponse event(String timestamp, String city, EventStatus status) {
        return new TimelineEventResponse(
                Instant.parse(timestamp),
                "SC",
                city,
                EventStatusResponse.from(status),
                null,
                null,
                null
        );
    }
}
