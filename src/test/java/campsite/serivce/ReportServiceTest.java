package campsite.serivce;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.service.CampsiteSystem;
import campsite.service.ReportService;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class ReportServiceTest {

    private static final String CAMP = "Sunny Camp";
    private static final String OTHER_CAMP = "Hill Camp";
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    private CampsiteSystem system;
    private Guest anna;
    private Guest ben;

    @Before
    public void setUp() {
        system = new CampsiteSystem();
        system.addCampsite(CAMP, "1 Beach Road", 4, "info@sunny.ie");
        system.addCampingArea(CAMP, "Lake View", "lake");
        system.addCampingArea(CAMP, "Forest", "trees");
        system.addPlot(CAMP, "Lake View", pitch("P1", 20));
        system.addPlot(CAMP, "Forest", pitch("F1", 30));

        system.addCampsite(OTHER_CAMP, "2 Hill Road", 3, "info@hill.ie");
        system.addCampingArea(OTHER_CAMP, "Top", "hill top");
        system.addPlot(OTHER_CAMP, "Top", pitch("T1", 25));

        anna = system.addGuest("Anna Murphy", LocalDate.of(1990, 5, 20), "Cork", "085", "anna@mail.ie", "");
        ben = system.addGuest("Ben Kelly", LocalDate.of(1985, 1, 1), "Galway", "086", "ben@mail.ie", "");
    }

    private Plot pitch(String id, double cost) {
        return new Plot(id, PlotType.EMPTY_PITCH, false, 0, 0, 50.0, cost, "url");
    }

    private Booking book(String camp, String area, Guest guest, LocalDate start, LocalDate end) {
        BookingPreferences prefs = new BookingPreferences(null, 2, false, 0, 0, 10000);
        Booking booking = system.makeBooking(camp, area, guest.getId(), start, end, prefs);
        assertNotNull("test setup: booking should succeed", booking);
        return booking;
    }

    @Test
    public void campsiteReportGroupsByAreaAndPlotWithTotals() {
        book(CAMP, "Lake View", anna, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4)); // 3 x 20
        book(CAMP, "Forest", ben, LocalDate.of(2026, 7, 10), LocalDate.of(2026, 7, 12));   // 2 x 30

        String report = ReportService.pendingBookingsForCampsite(system, CAMP, TODAY);

        assertTrue(report.contains("Area: Lake View"));
        assertTrue(report.contains("Area: Forest"));
        assertTrue(report.contains("Anna Murphy"));
        assertTrue(report.contains("Ben Kelly"));
        assertTrue(report.contains("Plot total: 1 booking(s), EUR 60.00"));
        assertTrue(report.contains("Campsite total: 2 booking(s), EUR 120.00"));
        assertFalse(report.contains(OTHER_CAMP));
    }

    @Test
    public void overallReportCoversEveryCampsiteAndGrandTotal() {
        book(CAMP, "Lake View", anna, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4)); // 60
        book(OTHER_CAMP, "Top", ben, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 3));  // 50

        String report = ReportService.pendingBookingsOverall(system, TODAY);

        assertTrue(report.contains("CAMPSITE: " + CAMP));
        assertTrue(report.contains("CAMPSITE: " + OTHER_CAMP));
        assertTrue(report.contains("OVERALL TOTAL: 2 booking(s), EUR 110.00"));
    }

    @Test
    public void finishedBookingsAreNotListedAsPending() {
        book(CAMP, "Lake View", anna, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3)); // already over

        String report = ReportService.pendingBookingsForCampsite(system, CAMP, TODAY);

        assertTrue(report.contains("(no pending bookings)"));
        assertTrue(report.contains("Campsite total: 0 booking(s), EUR 0.00"));
    }

    @Test
    public void overallReportOfEmptySystemSaysSo() {
        CampsiteSystem empty = new CampsiteSystem();

        assertTrue(ReportService.pendingBookingsOverall(empty, TODAY).contains("no campsites"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void campsiteReportForUnknownCampsiteIsRejected() {
        ReportService.pendingBookingsForCampsite(system, "Nowhere", TODAY);
    }

    @Test
    public void guestRecordSeparatesPendingAndPastBookings() {
        book(CAMP, "Lake View", anna, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3));  // past, 40
        book(CAMP, "Lake View", anna, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4));  // pending, 60

        String record = ReportService.guestRecord(system, anna.getId(), TODAY);

        assertTrue(record.contains("GUEST #1: Anna Murphy"));
        assertTrue(record.contains("anna@mail.ie"));
        assertTrue(record.contains("PENDING BOOKINGS (1)"));
        assertTrue(record.contains("PAST BOOKINGS (1)"));
        assertTrue(record.contains("Total: 1 booking(s), EUR 60.00"));
        assertTrue(record.contains("Total: 1 booking(s), EUR 40.00"));
    }

    @Test
    public void guestRecordWithoutBookingsSaysNone() {
        String record = ReportService.guestRecord(system, ben.getId(), TODAY);

        assertTrue(record.contains("PENDING BOOKINGS (0)"));
        assertTrue(record.contains("none"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void guestRecordForUnknownGuestIsRejected() {
        ReportService.guestRecord(system, 999, TODAY);
    }
}
