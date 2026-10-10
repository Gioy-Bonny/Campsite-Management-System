package campsite.serivce;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.service.CampsiteSystem;
import campsite.service.DemoData;
import campsite.service.PersistenceService;
import campsite.service.ReportService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.Assert.*;

public class PersistenceServiceTest {

    private static final String CAMP = "Sunny Camp";
    private static final LocalDate START = LocalDate.of(2026, 7, 1);
    private static final LocalDate END = LocalDate.of(2026, 7, 4);

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private CampsiteSystem system;
    private Guest guest;

    @Before
    public void setUp() {
        system = new CampsiteSystem();
        system.addCampsite(CAMP, "1 Beach Road", 4, "info@sunny.ie");
        system.findCampsite(CAMP).addFacility("swimming pool");
        system.findCampsite(CAMP).addFacility("WiFi");
        system.addCampingArea(CAMP, "Lake View", "Next to the lake");
        Plot bungalow = new Plot("B1", PlotType.BUNGALOW, true, 2, 4, 60.0, 80.0, "http://photo/b1.jpg");
        bungalow.addFacility("air conditioning");
        system.addPlot(CAMP, "Lake View", bungalow);
        system.addPlot(CAMP, "Lake View",
                new Plot("P1", PlotType.EMPTY_PITCH, false, 0, 0, 50.0, 20.0, null));
        guest = system.addGuest("Anna Murphy", LocalDate.of(1990, 5, 20), "Cork", "085", "a@mail.ie", null);
    }

    private Booking bookBungalow() {
        BookingPreferences prefs = new BookingPreferences(PlotType.BUNGALOW, 3, true, 30.5, 2, 900);
        return system.makeBooking(CAMP, "Lake View", guest.getId(), START, END, prefs);
    }

    @Test
    public void saveAndLoadKeepsCampsitesAreasPlotsAndFacilities() throws IOException {
        Path file = folder.getRoot().toPath().resolve("data.dat");

        PersistenceService.save(system, file);
        CampsiteSystem loaded = PersistenceService.load(file);

        Campsite campsite = loaded.findCampsite(CAMP);
        assertNotNull(campsite);
        assertEquals(4, campsite.getStars());
        assertTrue(campsite.hasFacility("swimming pool"));
        assertTrue(campsite.hasFacility("wifi"));

        CampingArea area = campsite.findArea("Lake View");
        assertEquals("Next to the lake", area.getDescription());
        assertEquals(2, area.getPlots().size());

        Plot bungalow = area.findPlot("B1");
        assertEquals(PlotType.BUNGALOW, bungalow.getType());
        assertEquals(2, bungalow.getBedrooms());
        assertEquals(80.0, bungalow.getCostPerNight(), 0.001);
        assertTrue(bungalow.hasFacility("air conditioning"));
        assertNull("a missing photo stays missing", area.findPlot("P1").getPhotoUrl());
    }

    @Test
    public void saveAndLoadKeepsGuestsAndBookingsAttachedToTheirPlots() throws IOException {
        Booking original = bookBungalow();
        Path file = folder.getRoot().toPath().resolve("data.dat");

        PersistenceService.save(system, file);
        CampsiteSystem loaded = PersistenceService.load(file);

        Guest loadedGuest = loaded.findGuest(guest.getId());
        assertEquals("Anna Murphy", loadedGuest.getName());
        assertEquals(LocalDate.of(1990, 5, 20), loadedGuest.getDateOfBirth());
        assertNull(loadedGuest.getNotes());

        assertEquals(1, loadedGuest.getBookings().size());
        Booking booking = loadedGuest.getBookings().get(0);
        assertEquals(original.getBookingId(), booking.getBookingId());
        assertEquals(START, booking.getStartDate());
        assertEquals(END, booking.getEndDate());
        assertEquals(PlotType.BUNGALOW, booking.getPreferences().getType());
        assertEquals(30.5, booking.getPreferences().getMinSizeM2(), 0.001);
        assertEquals(240.0, booking.getTotalCost(), 0.001);

        // The loaded plot knows about the booking, so it is not free for those dates any more
        Plot plot = loaded.findCampsite(CAMP).findArea("Lake View").findPlot("B1");
        assertSame(plot, booking.getAssignedPlot());
        assertFalse(plot.isAvailable(START, END));
    }

    @Test
    public void idCountersContinueAfterLoading() throws IOException {
        bookBungalow();
        Path file = folder.getRoot().toPath().resolve("data.dat");

        PersistenceService.save(system, file);
        CampsiteSystem loaded = PersistenceService.load(file);

        Guest second = loaded.addGuest("Ben Kelly", LocalDate.of(1985, 1, 1), "", "", "", "");
        assertEquals(2, second.getId());
        Booking next = loaded.makeBooking(CAMP, "Lake View", second.getId(), START, END,
                new BookingPreferences(PlotType.EMPTY_PITCH, 2, false, 0, 0, 500));
        assertEquals(2, next.getBookingId());
    }

    @Test
    public void pastBookingOfARemovedPlotSurvivesSaveAndLoad() throws IOException {
        bookBungalow();
        // The stay is over by 1 August, so removing the plot keeps it as history
        system.removePlot(CAMP, "Lake View", "B1", LocalDate.of(2026, 8, 1));
        Path file = folder.getRoot().toPath().resolve("data.dat");

        PersistenceService.save(system, file);
        CampsiteSystem loaded = PersistenceService.load(file);

        assertNull(loaded.findCampsite(CAMP).findArea("Lake View").findPlot("B1"));
        Booking booking = loaded.findGuest(guest.getId()).getBookings().get(0);
        assertEquals("B1", booking.getAssignedPlot().getIdentifier());
        assertEquals(240.0, booking.getTotalCost(), 0.001);
    }

    @Test
    public void savingOverAnExistingFileReplacesIt() throws IOException {
        Path file = folder.getRoot().toPath().resolve("data.dat");
        PersistenceService.save(system, file);

        system.addCampsite("Second Camp", "elsewhere", 3, "x");
        PersistenceService.save(system, file);

        assertNotNull(PersistenceService.load(file).findCampsite("Second Camp"));
        assertFalse("no temporary file is left behind",
                Files.exists(folder.getRoot().toPath().resolve("data.dat.tmp")));
    }

    @Test(expected = IOException.class)
    public void loadingAFileThatIsNotACampsiteFileFails() throws IOException {
        Path file = folder.newFile("garbage.dat").toPath();
        Files.writeString(file, "this is definitely not campsite data");

        PersistenceService.load(file);
    }

    @Test(expected = IOException.class)
    public void loadingATruncatedFileFails() throws IOException {
        bookBungalow();
        Path file = folder.getRoot().toPath().resolve("data.dat");
        PersistenceService.save(system, file);
        long half = Files.size(file) / 2;
        try (java.nio.channels.FileChannel channel =
                     java.nio.channels.FileChannel.open(file, java.nio.file.StandardOpenOption.WRITE)) {
            channel.truncate(half);
        }

        PersistenceService.load(file);
    }

    @Test(expected = IOException.class)
    public void loadingAMissingFileFails() throws IOException {
        PersistenceService.load(folder.getRoot().toPath().resolve("missing.dat"));
    }

    @Test
    public void demoDataCanBeSavedAndLoaded() throws IOException {
        CampsiteSystem demo = new CampsiteSystem();
        DemoData.load(demo);
        Path file = folder.getRoot().toPath().resolve("demo.dat");

        PersistenceService.save(demo, file);
        CampsiteSystem loaded = PersistenceService.load(file);

        assertEquals(2, loaded.getCampsites().size());
        assertEquals(3, loaded.getGuests().size());
        assertEquals(
                ReportService.pendingBookingsOverall(demo, LocalDate.now()),
                ReportService.pendingBookingsOverall(loaded, LocalDate.now()));
    }

    @Test(expected = IllegalStateException.class)
    public void demoDataCannotBeLoadedIntoANonEmptySystem() {
        DemoData.load(system);
    }
}
