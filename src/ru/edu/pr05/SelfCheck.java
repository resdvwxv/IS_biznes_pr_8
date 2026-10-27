package ru.edu.pr05;

import java.time.LocalDate;

/** Самопроверка модели состояний и граничных случаев. */
public class SelfCheck {
    private static int failed = 0;
    private static int total = 0;

    private static final LocalDate START = LocalDate.of(2026, 10, 10);
    private static final LocalDate END = LocalDate.of(2026, 10, 12);
    private static final Employee EMP = new Employee("Иванов И.И.");

    public static void main(String[] args) {
        try {
            checkCreation();
            checkSubmit();
            checkApproveReject();
            checkCancel();
            checkComplete();
            checkProcessor();
        } catch (Exception e) {
            failed++;
            System.out.println("FAIL exception " + e);
        }
        System.out.println(total + " проверок, провалено: " + failed);
        System.out.println(failed == 0 ? "PASS" : "FAIL  complete TODO methods");
        if (failed != 0) {
            System.exit(1);
        }
    }

    private static BusinessTrip draft() { return new BusinessTrip("T-1", EMP, START, END); }
    private static BusinessTrip submitted() { BusinessTrip t = draft(); t.submit(); return t; }
    private static BusinessTrip approved() { BusinessTrip t = submitted(); t.approve(); return t; }

    private static void checkCreation() {
        check("создание: типовое значение", draft().getStatus() == TripStatus.DRAFT);
        check("создание: нижняя граница (end == start)",
                new BusinessTrip("T", START, START).getStatus() == TripStatus.DRAFT);
        check("вариант 6: ровно 35 суток допустимо",
                new BusinessTrip("T", START, START.plusDays(TripRules.MAX_TRIP_DAYS)).getStatus()
                        == TripStatus.DRAFT);
        expect("вариант 6: 36 суток", IllegalArgumentException.class,
                () -> new BusinessTrip("T", START, START.plusDays(TripRules.MAX_TRIP_DAYS + 1)));
        expect("создание: end раньше start", IllegalArgumentException.class,
                () -> new BusinessTrip("T", END, START));
        expect("создание: пустой id", IllegalArgumentException.class,
                () -> new BusinessTrip(" ", START, END));
        expect("создание: null id", IllegalArgumentException.class,
                () -> new BusinessTrip(null, START, END));
        expect("создание: null дата", IllegalArgumentException.class,
                () -> new BusinessTrip("T", null, END));
    }

    private static void checkSubmit() {
        BusinessTrip t = draft();
        t.submit();
        check("submit: DRAFT -> SUBMITTED", t.getStatus() == TripStatus.SUBMITTED);
        expect("submit: повторный", IllegalStateException.class, t::submit);
        check("submit: статус не изменился после ошибки", t.getStatus() == TripStatus.SUBMITTED);
        expect("submit: без сотрудника", IllegalStateException.class,
                () -> new BusinessTrip("T", START, END).submit());
    }

    private static void checkApproveReject() {
        BusinessTrip a = submitted();
        a.approve();
        check("approve: SUBMITTED -> APPROVED", a.getStatus() == TripStatus.APPROVED);
        expect("approve: из DRAFT", IllegalStateException.class, () -> draft().approve());
        expect("approve: повторный", IllegalStateException.class, a::approve);

        BusinessTrip r = submitted();
        r.reject("1234567890");
        check("reject: ровно 10 символов принимается",
                r.getStatus() == TripStatus.REJECTED && "1234567890".equals(r.getRejectionReason()));
        BusinessTrip r9 = submitted();
        expect("reject: 9 символов", IllegalArgumentException.class, () -> r9.reject("123456789"));
        check("reject: статус не изменился после ошибки", r9.getStatus() == TripStatus.SUBMITTED);
        expect("reject: 10 пробелов", IllegalArgumentException.class,
                () -> submitted().reject("          "));
        expect("reject: null", IllegalArgumentException.class, () -> submitted().reject(null));
        expect("reject: из DRAFT", IllegalStateException.class,
                () -> draft().reject("достаточно длинная причина"));
    }

    private static void checkCancel() {
        BusinessTrip d = draft(); d.cancel();
        check("cancel: из DRAFT", d.getStatus() == TripStatus.CANCELLED);
        BusinessTrip s = submitted(); s.cancel();
        check("cancel: из SUBMITTED", s.getStatus() == TripStatus.CANCELLED);
        BusinessTrip a = approved(); a.cancel();
        check("cancel: из APPROVED", a.getStatus() == TripStatus.CANCELLED);
        expect("cancel: повторный", IllegalStateException.class, d::cancel);
        BusinessTrip done = approved(); done.complete(END.plusDays(1));
        expect("cancel: после COMPLETED", IllegalStateException.class, done::cancel);
        BusinessTrip rej = submitted(); rej.reject("причина отклонения");
        expect("cancel: из REJECTED", IllegalStateException.class, rej::cancel);
    }

    private static void checkComplete() {
        BusinessTrip t = approved();
        t.complete(END.plusDays(1));
        check("complete: на следующий день после окончания", t.getStatus() == TripStatus.COMPLETED);
        BusinessTrip same = approved();
        expect("complete: в день окончания", IllegalStateException.class, () -> same.complete(END));
        check("complete: статус не изменился после ошибки", same.getStatus() == TripStatus.APPROVED);
        expect("complete: до окончания", IllegalStateException.class, () -> approved().complete(START));
        expect("complete: из SUBMITTED", IllegalStateException.class,
                () -> submitted().complete(END.plusDays(5)));
        expect("complete: null дата", IllegalArgumentException.class, () -> approved().complete(null));
    }

    private static void checkProcessor() {
        BusinessTripService svc = new BusinessTripService();
        TripCommandProcessor p = new TripCommandProcessor(svc, END.plusDays(1));
        check("csv: SUBMIT без сотрудника -> ошибка",
                p.processLine(2, "T-01;2026-10-10;2026-10-12;SUBMIT").startsWith("ERROR"));
        check("csv: end раньше start -> ошибка",
                p.processLine(3, "T-02;2026-10-12;2026-10-10;SUBMIT").startsWith("ERROR"));
        check("csv: неверный формат даты -> ошибка",
                p.processLine(4, "T-03;10.10.2026;2026-10-12;SUBMIT;Петров").startsWith("ERROR"));
        check("csv: неизвестная команда -> ошибка",
                p.processLine(5, "T-04;2026-10-10;2026-10-12;FLY;Петров").startsWith("ERROR"));
        check("csv: пустая команда -> ошибка",
                p.processLine(6, "T-05;2026-10-10;2026-10-12;;Петров").startsWith("ERROR"));
        check("csv: корректная строка",
                p.processLine(7, "T-06;2026-10-10;2026-10-12;SUBMIT;Сидоров").startsWith("OK"));
        check("csv: повтор той же заявки продолжает цепочку",
                p.processLine(8, "T-06;2026-10-10;2026-10-12;APPROVE").startsWith("OK"));
        check("csv: конфликт дат одного id -> ошибка",
                p.processLine(9, "T-06;2026-11-01;2026-11-02;CANCEL").startsWith("ERROR"));
        check("csv: COMPLETE по текущей дате процессора",
                p.processLine(10, "T-06;2026-10-10;2026-10-12;COMPLETE").startsWith("OK"));
        check("csv: 36 суток -> ошибка",
                p.processLine(11, "T-07;2026-10-01;2026-11-06;SUBMIT;Орлов").startsWith("ERROR"));
        check("csv: 35 суток -> OK",
                p.processLine(12, "T-08;2026-10-01;2026-11-05;SUBMIT;Орлов").startsWith("OK"));
        check("service: findByStatus", svc.findByStatus(TripStatus.COMPLETED).size() == 1);
    }

    private static void check(String name, boolean cond) {
        total++;
        if (!cond) {
            failed++;
            System.out.println("FAIL  " + name);
        }
    }

    private static void expect(String name, Class<? extends Exception> type, Runnable action) {
        total++;
        try {
            action.run();
            failed++;
            System.out.println("FAIL  " + name + " (исключение не выброшено)");
        } catch (Exception e) {
            if (!type.isInstance(e)) {
                failed++;
                System.out.println("FAIL  " + name + " (получено " + e.getClass().getSimpleName() + ")");
            }
        }
    }
}
