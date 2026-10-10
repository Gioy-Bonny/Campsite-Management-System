package campsite.serivce;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.service.CampsiteSystem;
import campsite.service.RemovalResult;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class CampsiteSystemTest {

    private static final String CAMP = "Sunny Camp";
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END = LocalDate.of(2026, 7, 4); // 3 nights

    private CampsiteSystem system;
    private Guest guest;

    @Before
    public void setUp() {
        system = new CampsiteSystem();
        system.addCampsite(CAMP, "1 Beach Road", 4, "info@sunny.ie");
        system.addCampingArea(CAMP, "Lake View", "Next to the lake");
        guest = system.addGuest("Anna Murphy", LocalDate.of(1990, 5, 20),
                "Cork", "0851234567", "anna@mail.ie", "");
    }

    private Plot pitch(String id, boolean power, double cost) {
        return new Plot(id, PlotType.EMPTY_PITCH, power, 0, 0, 50.0, cost, "url");
    }

    private Plot bungalow(String id, int bedrooms, int beds, double cost) {
        return new Plot(id, PlotType.BUNGALOW, true, bedrooms, beds, 60.0, cost, "url");
    }

    private BookingPreferences anyPlot(double budget) {
        return new BookingPreferences(null, 2, false, 0, 0, budget);
    }

    // ---- adding ----

    @Test
    public void guestIdsAreSequential() {
        Guest second = system.addGuest("Ben Kelly", LocalDate.of(1985, 1, 1), "", "", "", "");

        assertEquals(1, guest.getId());
        assertEquals(2, second.getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateCampsiteNameIsRejected() {
        system.addCampsite("sunny camp", "elsewhere", 3, "x");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateAreaInSameCampsiteIsRejected() {
        system.addCampingArea(CAMP, "lake view", "again");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addingPlotToUnknownAreaIsRejected() {
        system.addPlot(CAMP, "No Such Area", pitch("P1", false, 20));
    }

    // ---- booking ----

    @Test
    public void bookingIsAssignedToMatchingPlotOnly() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.addPlot(CAMP, "Lake View", bungalow("B1", 2, 4, 80));

        BookingPreferences prefs = new BookingPreferences(PlotType.BUNGALOW, 3, true, 0, 2, 1000);
        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, prefs);

        assertNotNull(booking);
        assertEquals("B1", booking.getAssignedPlot().getIdentifier());
        assertEquals(240.0, booking.getTotalCost(), 0.001);
        assertEquals(1, guest.getBookings().size());
    }

    @Test
    public void bookingChoosesCheapestMatchingPlot() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 30));
        system.addPlot(CAMP, "Lake View", pitch("P2", false, 20));

        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        assertEquals("P2", booking.getAssignedPlot().getIdentifier());
    }

    @Test
    public void bookingReturnsNullWhenBudgetIsTooLow() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 100)); // 3 nights = 300

        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(200));

        assertNull(booking);
        assertEquals(0, guest.getBookings().size());
    }

    @Test
    public void overlappingBookingGoesToOtherPlotOrFails() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.addPlot(CAMP, "Lake View", pitch("P2", false, 25));

        Booking first = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));
        Booking second = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));
        Booking third = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        assertEquals("P1", first.getAssignedPlot().getIdentifier());
        assertEquals("P2", second.getAssignedPlot().getIdentifier());
        assertNull(third);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bookingForUnknownGuestIsRejected() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.makeBooking(CAMP, "Lake View", 999, START, END, anyPlot(500));
    }

    // ---- cancelling ----

    @Test
    public void cancelFreesThePlotAndRemovesFromGuest() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        assertTrue(system.cancelBooking(booking.getBookingId(), TODAY));

        assertEquals(0, guest.getBookings().size());
        assertTrue(booking.getAssignedPlot().isAvailable(START, END));
    }

    @Test
    public void cancellingUnknownBookingReturnsFalse() {
        assertFalse(system.cancelBooking(12345, TODAY));
    }

    @Test
    public void pastBookingCannotBeCancelled() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        assertFalse(system.cancelBooking(booking.getBookingId(), LocalDate.of(2026, 8, 1)));
        assertEquals(1, guest.getBookings().size());
    }

    // ---- removing a plot ----

    @Test
    public void removingPlotMovesBookingToPlotInAnotherArea() {
        system.addCampingArea(CAMP, "Forest", "Under the trees");
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.addPlot(CAMP, "Forest", pitch("F1", false, 25));
        Booking booking = system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        RemovalResult result = system.removePlot(CAMP, "Lake View", "P1", TODAY);

        assertEquals(1, result.getMovedCount());
        assertEquals(0, result.getCancelledCount());
        assertEquals("F1", booking.getAssignedPlot().getIdentifier());
        assertEquals("Forest", booking.getAreaId());
        assertEquals(1, guest.getBookings().size());
    }

    @Test
    public void removingPlotCancelsBookingWhenNoAlternativeExists() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, anyPlot(500));

        RemovalResult result = system.removePlot(CAMP, "Lake View", "P1", TODAY);

        assertEquals(0, result.getMovedCount());
        assertEquals(1, result.getCancelledCount());
        assertEquals(0, guest.getBookings().size());
        assertNull(system.findCampsite(CAMP).findArea("Lake View").findPlot("P1"));
    }

    // ---- reset ----

    @Test
    public void resetClearsAllDataAndIds() {
        system.addPlot(CAMP, "Lake View", pitch("P1", false, 20));
        system.reset();

        assertTrue(system.getCampsites().isEmpty());
        assertTrue(system.getGuests().isEmpty());
        Guest newGuest = system.addGuest("New Person", LocalDate.of(2000, 1, 1), "", "", "", "");
        assertEquals(1, newGuest.getId());
    }
}
