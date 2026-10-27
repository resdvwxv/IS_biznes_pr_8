# Практическая работа 8

Инкапсуляция, enum и управление состоянием объекта: система оформления командировок.

## Структура

```
src/ru/edu/pr05/
├── TripStatus.java             enum: DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, COMPLETED
├── Employee.java               record сотрудника (имя не пустое)
├── TripRules.java              параметры правил (MIN_REASON_LENGTH, MAX_TRIP_DAYS = 35, вариант 6)
├── BusinessTrip.java           модель: статус меняется только доменными методами
├── BusinessTripService.java    хранение и поиск заявок (create, find, findByStatus)
├── TripCommandProcessor.java   разбор CSV и применение команд
├── Main.java                   точка входа: чтение файла и вывод протокола
└── SelfCheck.java              самопроверка (45 проверок)
data/
├── sample.csv                  открытый набор из задания
└── boundary.csv                собственный набор граничных проверок
DECISIONS.md                    два принципиальных решения
```

Имя пакета `pr05` сохранено из исходного проекта, чтобы не ломать структуру.

## Сборка и запуск (без Maven)

Нужна JDK 17+ (используются record и switch-выражения).

```bash
./run_selfcheck.sh                                # сборка + SelfCheck, ожидается PASS
java -cp out ru.edu.pr05.Main                     # data/sample.csv
java -cp out ru.edu.pr05.Main data/boundary.csv   # свои проверки
```

Вручную:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr05.SelfCheck
```

Если в консоли вместо кириллицы «????», добавьте `-Dstdout.encoding=UTF-8` (Java 19+)
или `-Dfile.encoding=UTF-8`.

## Формат CSV

```
id;start;end;action[;employee[;param]]
```

| Поле | Описание |
| :--- | :--- |
| `action` | `SUBMIT`, `APPROVE`, `REJECT`, `CANCEL`, `COMPLETE` |
| `employee` | нужен при первом упоминании заявки, иначе `SUBMIT` вернёт ошибку |
| `param` | `REJECT` — причина; `COMPLETE` — дата `yyyy-MM-dd` (по умолчанию текущая) |

Строки с ошибками не прерывают обработку: в протоколе печатается `ERROR строка N: …`.
