package ru.edu.pr05;

/** Сотрудник, направляемый в командировку. Неизменяемый объект. */
public record Employee(String name) {
    public Employee {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Имя сотрудника не может быть пустым");
        }
        name = name.trim();
    }
}
