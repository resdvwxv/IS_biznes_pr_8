package ru.edu.pr05;

/**
 * Единственное место, где сосредоточены числовые параметры бизнес-правил.
 * Вариант 6: дополнительная проверка даты — длительность командировки
 * не может превышать MAX_TRIP_DAYS = 35 суток (end - start <= 35).
 */
public final class TripRules {
    public static final int MIN_REASON_LENGTH = 10;

    public static final int MAX_TRIP_DAYS = 35;

    private TripRules() { }
}
