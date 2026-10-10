package campsite.serivce;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.service.*;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

public class SmartFindServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END = LocalDate.of(2026, 7, 4); // 3 nights

    private CampsiteSystem system;
    private SmartFindService finder;
    private Guest guest;

    @Before
    public void setUp() {
        system = new CampsiteSystem();
        finder = new SmartFindService(system);

        // A cheap two star campsite with one cheap pitch
        system.addCampsite("Cheap Camp", "1 Low Road", 2, "cheap@camp.ie");
        system.findCampsite("Cheap Camp").addFacility("WiFi");
        system.addCampingArea("Cheap Camp", "Area A", "basic");
        system.addPlot("Cheap Camp", "Area A",
                new Plot("C1", PlotType.EMPTY_PITCH, false, 0, 0, 40.0, 15.0, "url"));

        // A five star campsite: a powered pitch, and a bungalow with air conditioning
        system.addCampsite("Fancy Camp", "2 High Road", 5, "fancy@camp.ie");
        system.findCampsite("Fancy Camp").addFacility("swimming pool");
        system.findCampsite("Fancy Camp").addFacility("WiFi");
        system.addCampingArea("Fancy Camp", "Area B", "deluxe");
        system.addPlot("Fancy Camp", "Area B",
                new Plot("F1", PlotType.EMPTY_PITCH, true, 0, 0, 60.0, 40.0, "url"));
        Plot bungalow = new Plot("F2", PlotType.BUNGALOW, true, 2, 4, 70.0, 100.0, "url");
        bungalow.addFacility("air conditioning");
        system.addPlot("Fancy Camp", "Area B", bungalow);

        guest = system.addGuest("Anna Murphy", LocalDate.of(1990, 5, 20), "Cork", "085", "a@mail.ie", "");
    }

    private SmartFindCriteria criteria(double budget) {
        return new SmartFindCriteria(START, END, 2, budget);
    }

    @Test
    public void picksCheapestSuitablePlotAcrossAllCampsites() {
        SmartFindResult result = finder.find(criteria(1000));

        assertEquals("Cheap Camp", result.getCampsite().getName());
        assertEquals("C1", result.getPlot().getIdentifier());
        assertEquals(45.0, result.getTotalCost(), 0.001);
        assertEquals(0, result.getShiftDays());
    }

    @Test
    public void minimumStarsExcludesLowerRatedCampsites() {
        SmartFindCriteria c = criteria(1000);
        c.setMinStars(4);

        assertEquals("Fancy Camp", finder.find(c).getCampsite().getName());
    }

    @Test
    public void requiredCampsiteFacilityIsRespected() {
        SmartFindCriteria c = criteria(1000);
        c.setCampsiteFacilities(TextUtil.splitList("swimming pool"));

        assertEquals("Fancy Camp", finder.find(c).getCampsite().getName());
    }

    @Test
    public void requiredPlotFacilityIsRespected() {
        SmartFindCriteria c = criteria(1000);
        c.setPlotFacilities(TextUtil.splitList("Air Conditioning"));

        assertEquals("F2", finder.find(c).getPlot().getIdentifier());
    }

    @Test
    public void returnsNullWhenNothingFitsTheBudget() {
        assertNull(finder.find(criteria(10)));
    }

    @Test
    public void usesLaterDatesWhenRequestedDatesAreTaken() {
        // Only C1 is affordable (3 nights x 15 = 45), and it is booked 1-4 July
        BookingPreferences any = new BookingPreferences(null, 2, false, 0, 0, 1000);
        system.makeBooking("Cheap Camp", "Area A", guest.getId(), START, END, any);

        SmartFindCriteria sameDates = new SmartFindCriteria(
                LocalDate.of(2026, 7, 2), LocalDate.of(2026, 7, 5), 2, 45);
        assertNull("no flexibility, so nothing is free", finder.find(sameDates));

        sameDates.setMaxShiftDays(7);
        SmartFindResult result = finder.find(sameDates);

        assertEquals("C1", result.getPlot().getIdentifier());
        assertEquals(2, result.getShiftDays());                       // 4 July is the first free day
        assertEquals(LocalDate.of(2026, 7, 4), result.getStart());
        assertEquals(LocalDate.of(2026, 7, 7), result.getEnd());
    }

    @Test
    public void bookingASuggestionReservesExactlyThatPlot() {
        SmartFindCriteria c = criteria(1000);
        c.setPlotFacilities(TextUtil.splitList("air conditioning"));
        SmartFindResult result = finder.find(c);

        Booking booking = finder.book(result, c, guest.getId());

        assertSame(result.getPlot(), booking.getAssignedPlot());
        assertEquals("Fancy Camp", booking.getCampsiteName());
        assertEquals(1, guest.getBookings().size());
        assertFalse(result.getPlot().isAvailable(START, END));
    }

    @Test(expected = IllegalStateException.class)
    public void bookingASuggestionThatWasTakenMeanwhileFails() {
        SmartFindCriteria c = criteria(1000);
        SmartFindResult result = finder.find(c);
        finder.book(result, c, guest.getId());

        finder.book(result, c, guest.getId()); // same plot, same dates
    }

    @Test(expected = IllegalArgumentException.class)
    public void criteriaRejectEndDateBeforeStart() {
        new SmartFindCriteria(END, START, 2, 100);
    }
}
