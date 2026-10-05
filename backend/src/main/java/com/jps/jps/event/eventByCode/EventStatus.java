package com.jps.jps.event.eventByCode;

import java.util.List;
import java.util.Objects;

public enum EventStatus {

    REGISTERED(0, "Registrado"),
    COLLECTED(1, "Coletado"),
    IN_SEPARATION(2, "Em separação"),
    IN_TRANSIT(3, "Em trânsito"),
    OUT_FOR_DELIVERY(4, "Saiu para entrega"),
    DELIVERED(5, "Entregue");

    private final Integer id;
    private final String name;

    EventStatus(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int progressPercentage() {
        int finalStage = values().length - 1;
        return Math.round((id * 100.0f) / finalStage);
    }

    public boolean isTerminal() {
        return this == DELIVERED;
    }

    public List<EventStatus> nextStatuses() {
        return switch (this) {
            case REGISTERED -> List.of(COLLECTED);
            case COLLECTED -> List.of(IN_SEPARATION);
            case IN_SEPARATION -> List.of(IN_TRANSIT);
            case IN_TRANSIT -> List.of(IN_TRANSIT, OUT_FOR_DELIVERY);
            case OUT_FOR_DELIVERY -> List.of(IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED);
            case DELIVERED -> List.of();
        };
    }

    public boolean canTransitionTo(EventStatus target) {
        return target != null && nextStatuses().contains(target);
    }

    public static EventStatus fromId(Integer id) {
        for (EventStatus event : values()) {
            if (Objects.equals(event.id, id)) {
                return event;
            }
        }
        throw new IllegalArgumentException("Unknown status id: " + id);
    }
}
