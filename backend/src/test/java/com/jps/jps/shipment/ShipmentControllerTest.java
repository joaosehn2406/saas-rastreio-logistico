package com.jps.jps.shipment;

import com.jps.jps.event.eventByCode.EventByCodeService;
import com.jps.jps.event.eventByCode.EventRequest;
import com.jps.jps.event.eventByCode.EventStatus;
import com.jps.jps.event.eventByCode.EventStatusResponse;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShipmentControllerTest {

    @Mock
    private ShipmentService shipmentService;

    @Mock
    private EventByCodeService eventByCodeService;

    @Test
    void verifiesShipmentBeforeSavingEvent() {
        String trackingCode = "BR123456789";
        EventRequest request = new EventRequest(
                "SC",
                "Blumenau",
                EventStatus.COLLECTED.getId(),
                -26.9155,
                -49.0709,
                "Coleta confirmada"
        );
        TimelineEventResponse savedEvent = new TimelineEventResponse(
                Instant.parse("2026-09-01T12:00:00Z"),
                request.state(),
                request.city(),
                EventStatusResponse.from(EventStatus.COLLECTED),
                request.latitude(),
                request.longitude(),
                request.notes()
        );
        when(eventByCodeService.save(trackingCode, request)).thenReturn(savedEvent);
        ShipmentController controller = new ShipmentController(shipmentService, eventByCodeService);

        ResponseEntity<TimelineEventResponse> response = controller.addEvent(trackingCode, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedEvent, response.getBody());
        InOrder callOrder = inOrder(shipmentService, eventByCodeService);
        callOrder.verify(shipmentService).findByTrackingCode(trackingCode);
        callOrder.verify(eventByCodeService).save(trackingCode, request);
    }

    @Test
    void doesNotSaveEventWhenShipmentDoesNotExist() {
        String trackingCode = "BR000000000";
        EventRequest request = new EventRequest(
                "SC",
                "Blumenau",
                EventStatus.COLLECTED.getId(),
                null,
                null,
                null
        );
        when(shipmentService.findByTrackingCode(trackingCode))
                .thenThrow(new ShipmentNotFoundException(trackingCode));
        ShipmentController controller = new ShipmentController(shipmentService, eventByCodeService);

        assertThrows(
                ShipmentNotFoundException.class,
                () -> controller.addEvent(trackingCode, request)
        );

        verify(eventByCodeService, never()).save(trackingCode, request);
    }
}
