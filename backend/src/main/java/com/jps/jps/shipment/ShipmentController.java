package com.jps.jps.shipment;

import com.jps.jps.event.eventByCode.EventByCodeService;
import com.jps.jps.event.eventByCode.EventRequest;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipments")
public class ShipmentController {

    private final ShipmentService shipmentService;
    private final EventByCodeService eventByCodeService;

    public ShipmentController(ShipmentService shipmentService, EventByCodeService eventByCodeService) {
        this.shipmentService = shipmentService;
        this.eventByCodeService = eventByCodeService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> create(@RequestBody @Valid ShipmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shipmentService.create(request));
    }

    @GetMapping("/{trackingCode}")
    public ResponseEntity<ShipmentResponse> findByTrackingCode(@PathVariable String trackingCode) {
        return ResponseEntity.ok(shipmentService.getByTrackingCode(trackingCode));
    }

    @DeleteMapping("/{trackingCode}")
    public ResponseEntity<Void> delete(@PathVariable String trackingCode) {
        shipmentService.delete(trackingCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{trackingCode}/eventos")
    public ResponseEntity<TimelineEventResponse> addEvent(
            @PathVariable String trackingCode,
            @RequestBody @Valid EventRequest request) {
        shipmentService.findByTrackingCode(trackingCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(eventByCodeService.save(trackingCode, request));
    }
}
