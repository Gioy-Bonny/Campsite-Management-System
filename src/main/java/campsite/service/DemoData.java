package campsite.service;

import campsite.model.BookingPreferences;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;

import java.time.LocalDate;

/**
 * Fills an empty system with sample data, so the GUI can be tried out and demonstrated
 * without typing everything in. Booking dates are relative to today, so there are always
 * some pending bookings and one past booking.
 */
public final class DemoData {

    private DemoData() {
    }

    /**
     * @throws IllegalStateException if the system already contains data
     */
    public static void load(CampsiteSystem system) {
        if (!system.getCampsites().isEmpty() || !system.getGuests().isEmpty()) {
            throw new IllegalStateException("Demo data can only be loaded into an empty system");
        }
        LocalDate today = LocalDate.now();

        // Campsite 1: four stars, two areas
        String sunny = "Sunny Shores";
        system.addCampsite(sunny, "1 Beach Road, Tralee, Co. Kerry", 4, "info@sunnyshores.ie")
                .addFacility("swimming pool");
        system.findCampsite(sunny).addFacility("restaurant");
        system.findCampsite(sunny).addFacility("WiFi");
        system.findCampsite(sunny).addFacility("laundry");
        system.addCampingArea(sunny, "Lake View Area 1", "Pitches and homes next to the lake");
        system.addCampingArea(sunny, "Forest Area 2", "Quiet area under the trees");

        Plot bungalow = new Plot("LV-B1", PlotType.BUNGALOW, true, 2, 4, 55, 95,
                "https://picsum.photos/seed/bungalow1/320/200");
        bungalow.addFacility("air conditioning");
        system.addPlot(sunny, "Lake View Area 1", bungalow);
        system.addPlot(sunny, "Lake View Area 1", new Plot("LV-M1", PlotType.STATIC_MOBILE_HOME,
                true, 2, 6, 45, 70, "https://picsum.photos/seed/mobile1/320/200"));
        system.addPlot(sunny, "Lake View Area 1", new Plot("LV-P1", PlotType.EMPTY_PITCH,
                true, 0, 0, 60, 28, "https://picsum.photos/seed/pitch1/320/200"));
        system.addPlot(sunny, "Lake View Area 1", new Plot("LV-P2", PlotType.EMPTY_PITCH,
                false, 0, 0, 80, 18, "https://picsum.photos/seed/pitch2/320/200"));
        system.addPlot(sunny, "Forest Area 2", new Plot("FO-P1", PlotType.EMPTY_PITCH,
                true, 0, 0, 70, 30, "https://picsum.photos/seed/pitch3/320/200"));
        Plot apartment = new Plot("FO-A1", PlotType.APARTMENT, true, 1, 2, 40, 85,
                "https://picsum.photos/seed/apartment1/320/200");
        apartment.addFacility("air conditioning");
        system.addPlot(sunny, "Forest Area 2", apartment);

        // Campsite 2: three stars, one area
        String mountain = "Mountain Rest";
        system.addCampsite(mountain, "Hilltop Lane, Killarney, Co. Kerry", 3, "hello@mountainrest.ie")
                .addFacility("shop");
        system.findCampsite(mountain).addFacility("WiFi");
        system.findCampsite(mountain).addFacility("gym");
        system.addCampingArea(mountain, "Hilltop Area 1", "Views over the valley");
        system.addPlot(mountain, "Hilltop Area 1", new Plot("HT-P1", PlotType.EMPTY_PITCH,
                true, 0, 0, 50, 22, "https://picsum.photos/seed/pitch4/320/200"));
        system.addPlot(mountain, "Hilltop Area 1", new Plot("HT-M1", PlotType.STATIC_MOBILE_HOME,
                true, 3, 8, 60, 110, "https://picsum.photos/seed/mobile2/320/200"));

        // Guests
        Guest anna = system.addGuest("Anna Murphy", LocalDate.of(1990, 5, 20), "12 Main St, Cork",
                "0851234567", "anna@mail.ie", "Vegetarian");
        Guest ben = system.addGuest("Ben Kelly", LocalDate.of(1985, 1, 9), "4 Park Rd, Galway",
                "0867654321", "ben@mail.ie", "");
        Guest clara = system.addGuest("Clara Byrne", LocalDate.of(1978, 11, 30), "7 Hill View, Dublin",
                "0831112223", "clara@mail.ie", "Needs ground-floor accommodation");

        // Pending bookings
        system.makeBooking(sunny, "Lake View Area 1", anna.getId(), today.plusDays(20), today.plusDays(27),
                new BookingPreferences(PlotType.BUNGALOW, 3, true, 0, 2, 1000));
        system.makeBooking(sunny, "Lake View Area 1", ben.getId(), today.plusDays(20), today.plusDays(25),
                new BookingPreferences(PlotType.EMPTY_PITCH, 2, true, 0, 0, 400));
        system.makeBooking(mountain, "Hilltop Area 1", clara.getId(), today.plusDays(40), today.plusDays(43),
                new BookingPreferences(null, 2, false, 0, 0, 500));

        // A past booking, so the guest record shows both kinds
        system.makeBooking(mountain, "Hilltop Area 1", anna.getId(), today.minusDays(90), today.minusDays(85),
                new BookingPreferences(PlotType.EMPTY_PITCH, 2, true, 0, 0, 300));
    }
}
