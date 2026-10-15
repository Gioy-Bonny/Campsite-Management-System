# Campsite Management System

Data Structures and Algorithms 1, CA Exercise 1.

A JavaFX application for managing campsites, their camping areas, plots/pitches, guests and
bookings. Every data structure is built from scratch: the project uses **no** `java.util`
collections and **no** arrays. All lists are `MyLinkedList`.

## Running it

- **IntelliJ:** open the project as a Maven project and run `campsite.Main`.
- **Maven:** `mvn javafx:run`
- **Tests:** `mvn test`

Requirements: Java 21 and Maven (JavaFX 21 and JUnit 4 are downloaded by Maven).
Use *System > Load demo data* to try the program without typing everything in.

## Features and where they are implemented

| Requirement | Where |
|---|---|
| Custom ADTs | `model/` (`Campsite`, `CampingArea`, `Plot`, `Guest`, `Booking`, `BookingPreferences`, `PlotType`) |
| Custom linked list | `structures/MyLinkedList`, `structures/Node` |
| Add campsite / area / plot / guest | `CampsiteSystem`, GUI tab *Add data* |
| Booking with automatic plot assignment | `CampsiteSystem.makeBooking` |
| Cancel booking | `CampsiteSystem.cancelBooking` |
| Pending bookings (per campsite and overall), guest record | `ReportService`, GUI tab *Views* |
| Interactive drill-down | GUI tab *Drill-down* (`DrillDownTab`) |
| Smart find | `SmartFindService`, GUI tab *Smart find* |
| Remove plot, reassigning bookings | `CampsiteSystem.removePlot` |
| Reset | `CampsiteSystem.reset`, GUI tab *System* |
| Save / load | `PersistenceService` (binary file), GUI tab *System* |

## Project layout

```
src/main/java/campsite
  Main.java            starts the JavaFX application
  structures/          MyLinkedList, Node
  model/               the ADTs (no GUI code)
  service/             all the logic (no GUI code, fully unit tested)
  ui/                  JavaFX tabs; they only display data held in the service layer
src/test/java/campsite  JUnit 4 tests, same packages as the code they test
```

The GUI never stores data: it asks `AppContext` for the current `CampsiteSystem` and shows it.

## Design notes

- **Booking rules.** A plot is suitable if it is free for the dates, has the wanted type,
  power, size and bedrooms, has a bed for every guest (static dwellings), and
  `nights x cost per night` is within the budget. The cheapest suitable plot is chosen.
- **Dates.** The end date is the check-out day, so a stay from the 1st to the 3rd is 2 nights.
  Two stays overlap only if `start1 < end2 && start2 < end1`, so back-to-back stays are fine.
- **Pending bookings** are bookings whose end date has not passed yet.
- **Remove plot.** Pending bookings of the plot are moved to the cheapest suitable plot, first
  in the same area, then in other areas of the same campsite. Bookings that cannot be moved
  are cancelled and counted in the result shown to the user. Past bookings stay in the
  guest's history.
- **Smart find.** Tries the requested dates, then the same stay 1, 2, ... days later (up to
  the flexibility the guest allows). A campsite needs enough stars and all wanted facilities;
  a plot must pass the normal booking rules and have the wanted plot facilities. The
  cheapest total cost wins, and the higher star rating breaks ties.
- **Save / load.** A binary file written with `DataOutputStream` by walking the linked lists.
  Loading builds a new system first, so a corrupt file never damages the current data.
- **JavaFX controls** (`TreeView`, `ComboBox`) keep their own internal item lists for display
  only. The real data is always in the linked lists.
