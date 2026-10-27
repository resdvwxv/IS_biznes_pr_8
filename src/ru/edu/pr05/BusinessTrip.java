package ru.edu.pr05;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

public class BusinessTrip {
    private final String id;
    private final LocalDate start;
    private final LocalDate end;
    private Employee employee;
    private TripStatus status = TripStatus.DRAFT;
    private String rejectionReason;

    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Идентификатор заявки не задан");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Даты начала и окончания обязательны");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                    "Дата окончания " + end + " раньше даты начала " + start);
        }
        long days = ChronoUnit.DAYS.between(start, end);
        if (days > TripRules.MAX_TRIP_DAYS) {
            throw new IllegalArgumentException("Командировка длится " + days
                    + " сут., максимум " + TripRules.MAX_TRIP_DAYS);
        }
        this.id = id.trim();
        this.start = start;
        this.end = end;
    }

    public BusinessTrip(String id, Employee employee, LocalDate start, LocalDate end) {
        this(id, start, end);
        assignEmployee(employee);
    }

    /** Назначить сотрудника; допустимо только пока заявка не отправлена. */
    public void assignEmployee(Employee employee) {
        requireStatus("назначить сотрудника", TripStatus.DRAFT);
        if (employee == null) {
            throw new IllegalArgumentException("Сотрудник не задан");
        }
        this.employee = employee;
    }

    public void submit() {
        requireStatus("отправить на согласование", TripStatus.DRAFT);
        if (employee == null) {
            throw new IllegalStateException("Нельзя отправить заявку " + id + ": не указан сотрудник");
        }
        status = TripStatus.SUBMITTED;
    }

    public void approve() {
        requireStatus("согласовать", TripStatus.SUBMITTED);
        status = TripStatus.APPROVED;
    }

    public void reject(String reason) {
        requireStatus("отклонить", TripStatus.SUBMITTED);
        if (reason == null || reason.trim().length() < TripRules.MIN_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    "Причина отклонения должна содержать минимум "
                            + TripRules.MIN_REASON_LENGTH + " символов");
        }
        rejectionReason = reason.trim();
        status = TripStatus.REJECTED;
    }

    public void cancel() {
        requireStatus("отменить", TripStatus.DRAFT, TripStatus.SUBMITTED, TripStatus.APPROVED);
        status = TripStatus.CANCELLED;
    }

    public void complete(LocalDate today) {
        requireStatus("завершить", TripStatus.APPROVED);
        if (today == null) {
            throw new IllegalArgumentException("Текущая дата не задана");
        }
        if (!today.isAfter(end)) {
            throw new IllegalStateException(
                    "Нельзя завершить заявку " + id + " до окончания командировки (" + end + ")");
        }
        status = TripStatus.COMPLETED;
    }

    /** Общая проверка допустимости перехода: статус не меняется при ошибке. */
    private void requireStatus(String action, TripStatus... allowed) {
        if (!Arrays.asList(allowed).contains(status)) {
            throw new IllegalStateException("Нельзя " + action + " заявку " + id
                    + " в статусе " + status + " (допустимо: " + Arrays.toString(allowed) + ")");
        }
    }

    public String getId() { return id; }
    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    public Employee getEmployee() { return employee; }
    public TripStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
}
