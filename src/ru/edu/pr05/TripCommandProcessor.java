package ru.edu.pr05;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TripCommandProcessor {
    private static final Set<String> ACTIONS =
            Set.of("SUBMIT", "APPROVE", "REJECT", "CANCEL", "COMPLETE");

    private final BusinessTripService service;
    private final LocalDate today;

    public TripCommandProcessor(BusinessTripService service, LocalDate today) {
        this.service = service;
        this.today = today;
    }

    /** Обрабатывает строки (первая — заголовок) и возвращает протокол. */
    public List<String> processAll(List<String> lines) {
        List<String> log = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.isBlank()) {
                continue;
            }
            log.add(processLine(i + 1, line));
        }
        return log;
    }

    String processLine(int lineNo, String line) {
        try {
            String[] f = line.split(";", -1);
            if (f.length < 4) {
                throw new IllegalArgumentException("ожидается минимум 4 поля, получено " + f.length);
            }
            String id = f[0].trim();
            LocalDate start = parseDate("start", f[1]);
            LocalDate end = parseDate("end", f[2]);
            String action = f[3].trim().toUpperCase();
            String employee = f.length > 4 ? f[4].trim() : "";
            String param = f.length > 5 ? f[5].trim() : "";
            if (!ACTIONS.contains(action)) {
                throw new IllegalArgumentException("Неизвестная команда: '" + action + "'");
            }

            BusinessTrip trip = obtain(id, start, end, employee);
            apply(trip, action, param);
            return "OK    строка " + lineNo + ": " + id + " " + action + " -> " + trip.getStatus();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "ERROR строка " + lineNo + ": " + e.getMessage();
        }
    }

    private BusinessTrip obtain(String id, LocalDate start, LocalDate end, String employee) {
        BusinessTrip existing = service.find(id).orElse(null);
        if (existing == null) {
            return service.create(id, employee.isEmpty() ? null : new Employee(employee), start, end);
        }
        if (!existing.getStart().equals(start) || !existing.getEnd().equals(end)) {
            throw new IllegalArgumentException("Конфликт данных: даты заявки " + id
                    + " отличаются от ранее загруженных");
        }
        return existing;
    }

    private void apply(BusinessTrip trip, String action, String param) {
        switch (action) {
            case "SUBMIT" -> trip.submit();
            case "APPROVE" -> trip.approve();
            case "REJECT" -> trip.reject(param);
            case "CANCEL" -> trip.cancel();
            case "COMPLETE" -> trip.complete(param.isEmpty() ? today : parseDate("param", param));
            default -> throw new IllegalStateException("Команда не поддерживается: " + action);
        }
    }

    private static LocalDate parseDate(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Поле " + field + " пустое");
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "Поле " + field + ": неверный формат даты '" + value.trim() + "' (нужен yyyy-MM-dd)");
        }
    }
}
