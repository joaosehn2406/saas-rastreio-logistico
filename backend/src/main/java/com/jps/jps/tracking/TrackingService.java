package com.jps.jps.tracking;

import com.jps.jps.event.eventByCode.EventByCodeService;
import com.jps.jps.event.eventByCode.TimelineEventResponse;
import com.jps.jps.shipment.Shipment;
import com.jps.jps.shipment.ShipmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackingService {

    private final ShipmentService shipmentService;
    private final EventByCodeService eventByCodeService;
    private final TrackingTimelineAnalyzer timelineAnalyzer;

    public TrackingService(
            ShipmentService shipmentService,
            EventByCodeService eventByCodeService,
            TrackingTimelineAnalyzer timelineAnalyzer
    ) {
        this.shipmentService = shipmentService;
        this.eventByCodeService = eventByCodeService;
        this.timelineAnalyzer = timelineAnalyzer;
    }

    public TrackingResponse getByTrackingCode(String trackingCode) {
        Shipment shipment = shipmentService.findByTrackingCode(trackingCode);
        List<TimelineEventResponse> events = eventByCodeService.findByTrackingCode(trackingCode);
        TrackingAnalysis analysis = timelineAnalyzer.analyze(shipment.createdAt(), events);

        return new TrackingResponse(
                shipment.trackingCode(),
                shipment.origin(),
                shipment.destination(),
                shipment.createdAt(),
                analysis.currentStatus(),
                analysis.events(),
                analysis.progress()
        );
    }
}
