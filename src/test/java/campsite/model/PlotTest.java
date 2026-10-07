package campsite.model;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class PlotTest {

    private Plot plot;

    @Before
    public void setUp() {
        plot = new Plot("P1", PlotType.EMPTY_PITCH, true, 0, 0, 50.0, 25.0, "http://photo/p1.jpg");
    }

    private Booking booking(int id, LocalDate start, LocalDate end) {
        BookingPreferences prefs = new BookingPreferences(null, 2, false, 0, 0, 500);
        return new Booking(id, "Sunny Camp", "Lake View", 1, start, end, prefs);
    }

    @Test
    public void staticDwellingAlwaysHasPower() {
        Plot bungalow = new Plot("B1", PlotType.BUNGALOW, false, 2, 4, 60.0, 90.0, "url");

        assertTrue(bungalow.hasPower());
        assertEquals(2, bungalow.getBedrooms());
    }

    @Test
    public void emptyPitchHasNoBedrooms() {
        Plot pitch = new Plot("E1", PlotType.EMPTY_PITCH, false, 3, 5, 40.0, 20.0, "url");

        assertEquals(0, pitch.getBedrooms());
        assertEquals(0, pitch.getBeds());
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSizeIsRejected() {
        new Plot("X", PlotType.EMPTY_PITCH, false, 0, 0, -5.0, 10.0, "url");
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeCostIsRejected() {
        new Plot("X", PlotType.EMPTY_PITCH, false, 0, 0, 30.0, -1.0, "url");
    }

    @Test
    public void overlappingBookingMakesPlotUnavailable() {
        plot.addBooking(booking(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 8)));

        assertFalse(plot.isAvailable(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 10)));
        assertFalse(plot.isAvailable(LocalDate.of(2026, 6, 28), LocalDate.of(2026, 7, 2)));
    }

    @Test
    public void backToBackBookingsAreAllowed() {
        plot.addBooking(booking(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 8)));

        assertTrue(plot.isAvailable(LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 12)));
        assertTrue(plot.isAvailable(LocalDate.of(2026, 6, 24), LocalDate.of(2026, 7, 1)));
    }

    @Test
    public void removingBookingFreesThePlot() {
        Booking b = booking(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 8));
        plot.addBooking(b);
        plot.removeBooking(b);

        assertTrue(plot.isAvailable(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 8)));
    }

    @Test
    public void totalCostIsNightsTimesPlotCost() {
        Booking b = booking(1, LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 4));
        b.setAssignedPlot(plot);

        assertEquals(3, b.getNights());
        assertEquals(75.0, b.getTotalCost(), 0.001);
    }

    @Test(expected = IllegalArgumentException.class)
    public void bookingEndBeforeStartIsRejected() {
        booking(1, LocalDate.of(2026, 7, 8), LocalDate.of(2026, 7, 1));
    }
}