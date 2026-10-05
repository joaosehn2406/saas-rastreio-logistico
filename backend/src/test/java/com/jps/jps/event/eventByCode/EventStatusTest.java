package com.jps.jps.event.eventByCode;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EventStatusTest {

    @Test
    void resolvesEveryStatusByItsIdentifier() {
        for (EventStatus status : EventStatus.values()) {
            assertEquals(status, EventStatus.fromId(status.getId()));
        }
    }

    @Test
    void rejectsUnknownAndNullIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> EventStatus.fromId(99));
        assertThrows(IllegalArgumentException.class, () -> EventStatus.fromId(null));
    }

    @Test
    void calculatesProgressForEveryStage() {
        assertEquals(0, EventStatus.REGISTERED.progressPercentage());
        assertEquals(20, EventStatus.COLLECTED.progressPercentage());
        assertEquals(40, EventStatus.IN_SEPARATION.progressPercentage());
        assertEquals(60, EventStatus.IN_TRANSIT.progressPercentage());
        assertEquals(80, EventStatus.OUT_FOR_DELIVERY.progressPercentage());
        assertEquals(100, EventStatus.DELIVERED.progressPercentage());
    }

    @Test
    void exposesOnlyDeliveredAsTerminal() {
        for (EventStatus status : EventStatus.values()) {
            assertEquals(status == EventStatus.DELIVERED, status.isTerminal());
        }
    }

    @Test
    void exposesExpectedNextStatuses() {
        assertEquals(List.of(EventStatus.COLLECTED), EventStatus.REGISTERED.nextStatuses());
        assertEquals(List.of(EventStatus.IN_SEPARATION), EventStatus.COLLECTED.nextStatuses());
        assertEquals(List.of(EventStatus.IN_TRANSIT), EventStatus.IN_SEPARATION.nextStatuses());
        assertEquals(
                List.of(EventStatus.IN_TRANSIT, EventStatus.OUT_FOR_DELIVERY),
                EventStatus.IN_TRANSIT.nextStatuses()
        );
        assertEquals(
                List.of(EventStatus.IN_TRANSIT, EventStatus.OUT_FOR_DELIVERY, EventStatus.DELIVERED),
                EventStatus.OUT_FOR_DELIVERY.nextStatuses()
        );
        assertTrue(EventStatus.DELIVERED.nextStatuses().isEmpty());
    }

    @Test
    void validatesStatusTransitions() {
        assertTrue(EventStatus.REGISTERED.canTransitionTo(EventStatus.COLLECTED));
        assertTrue(EventStatus.IN_TRANSIT.canTransitionTo(EventStatus.IN_TRANSIT));
        assertTrue(EventStatus.OUT_FOR_DELIVERY.canTransitionTo(EventStatus.DELIVERED));
        assertFalse(EventStatus.REGISTERED.canTransitionTo(EventStatus.DELIVERED));
        assertFalse(EventStatus.DELIVERED.canTransitionTo(EventStatus.IN_TRANSIT));
        assertFalse(EventStatus.IN_TRANSIT.canTransitionTo(null));
    }
}
