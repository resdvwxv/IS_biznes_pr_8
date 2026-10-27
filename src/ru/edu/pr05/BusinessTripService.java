package ru.edu.pr05;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Хранит заявки и создаёт их; правила переходов находятся в BusinessTrip. */
public class BusinessTripService {
    private final Map<String, BusinessTrip> trips = new LinkedHashMap<>();

    public BusinessTrip create(String id, Employee employee, LocalDate start, LocalDate end) {
        BusinessTrip trip = employee == null
                ? new BusinessTrip(id, start, end)
                : new BusinessTrip(id, employee, start, end);
        if (trips.containsKey(trip.getId())) {
            throw new IllegalArgumentException("Заявка с id " + trip.getId() + " уже существует");
        }
        trips.put(trip.getId(), trip);
        return trip;
    }

    public Optional<BusinessTrip> find(String id) {
        return Optional.ofNullable(id == null ? null : trips.get(id.trim()));
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        List<BusinessTrip> result = new ArrayList<>();
        for (BusinessTrip t : trips.values()) {
            if (t.getStatus() == status) {
                result.add(t);
            }
        }
        return result;
    }

    public List<BusinessTrip> findAll() {
        return new ArrayList<>(trips.values());
    }
}
