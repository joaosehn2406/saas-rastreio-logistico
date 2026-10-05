package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventByCodeService;
import com.jps.jps.event.eventByCode.EventStatus;
import com.jps.jps.event.eventByCode.EventStatusResponse;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import com.jps.jps.shipment.Shipment;
import com.jps.jps.shipment.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackingServiceTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private EventByCodeService eventByCodeService;

    private TrackingService trackingService;

    @BeforeEach
    void setUp() {
        trackingService = new TrackingService(
                shipmentService,
                eventByCodeService,
                new TrackingTimelineAnalyzer()
        );
    }

    @Test
    void combinesShipmentAndTimelineDataInTrackingResponse() {
        String trackingCode = "BR123456789";
        Instant createdAt = Instant.parse("2026-09-01T09:00:00Z");
        Shipment shipment = new Shipment(
                trackingCode,
                "Loja Central",
                "Maria Silva",
                "Blumenau",
                "São Paulo",
                createdAt,
                new BigDecimal("2.35")
        );
        TimelineEventResponse collected = event(
                "2026-09-01T12:00:00Z",
                "Blumenau",
                EventStatus.COLLECTED
        );
        TimelineEventResponse inTransit = event(
                "2026-09-02T08:30:00Z",
                "Curitiba",
                EventStatus.IN_TRANSIT
        );
        when(shipmentService.findByTrackingCode(trackingCode)).thenReturn(shipment);
        when(eventByCodeService.findByTrackingCode(trackingCode)).thenReturn(List.of(collected, inTransit));

        TrackingResponse response = trackingService.getByTrackingCode(trackingCode);

        assertEquals(trackingCode, response.trackingCode());
        assertEquals("Blumenau", response.origin());
        assertEquals("São Paulo", response.destination());
        assertEquals(createdAt, response.createdAt());
        assertEquals(EventStatusResponse.from(EventStatus.IN_TRANSIT), response.currentStatus());
        assertEquals(List.of(inTransit, collected), response.events());
        assertEquals(60, response.progress().percentage());
        assertEquals(List.of("Blumenau", "Curitiba"), response.progress().visitedCities());
        verify(shipmentService).findByTrackingCode(trackingCode);
        verify(eventByCodeService).findByTrackingCode(trackingCode);
    }

    @Test
    void returnsRegisteredProgressForShipmentWithoutEvents() {
        String trackingCode = "BR987654321";
        Instant createdAt = Instant.parse("2026-09-03T14:00:00Z");
        Shipment shipment = new Shipment(
                trackingCode,
                "Empresa Norte",
                "João Souza",
                "Manaus",
                "Recife",
                createdAt,
                new BigDecimal("1.10")
        );
        when(shipmentService.findByTrackingCode(trackingCode)).thenReturn(shipment);
        when(eventByCodeService.findByTrackingCode(trackingCode)).thenReturn(List.of());

        TrackingResponse response = trackingService.getByTrackingCode(trackingCode);

        assertEquals(EventStatusResponse.from(EventStatus.REGISTERED), response.currentStatus());
        assertTrue(response.events().isEmpty());
        assertEquals(0, response.progress().percentage());
        assertEquals(createdAt, response.progress().lastUpdated());
        assertEquals(0, response.progress().eventCount());
    }

    private TimelineEventResponse event(String timestamp, String city, EventStatus status) {
        return new TimelineEventResponse(
                Instant.parse(timestamp),
                "PR",
                city,
                EventStatusResponse.from(status),
                null,
                null,
                null
        );
    }
}
