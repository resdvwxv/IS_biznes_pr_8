package ru.edu.pr05;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

/** Точка входа: только чтение файла и вывод протокола. */
public class Main {
    public static void main(String[] args) throws IOException {
        Path file = Path.of(args.length > 0 ? args[0] : "data/sample.csv");
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);

        BusinessTripService service = new BusinessTripService();
        TripCommandProcessor processor = new TripCommandProcessor(service, LocalDate.now());
        processor.processAll(lines).forEach(System.out::println);

        System.out.println("--- Итоговые статусы ---");
        for (BusinessTrip t : service.findAll()) {
            System.out.println(t.getId() + ": " + t.getStatus());
        }
    }
}
