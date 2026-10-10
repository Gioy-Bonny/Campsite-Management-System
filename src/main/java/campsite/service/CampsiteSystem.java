package campsite.service;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.structures.MyLinkedList;
import campsite.structures.Node;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * The main facade of the system. It holds all campsites and guests and
 * contains the logic for adding, booking, cancelling and removing.
 * It has no GUI code, so it can be unit tested on its own.
 */
public class CampsiteSystem {

    private final MyLinkedList<Campsite> campsites = new MyLinkedList<>();
    private final MyLinkedList<Guest> guests = new MyLinkedList<>();
    private int nextGuestId = 1;
    private int nextBookingId = 1;

    // ------------------------------------------------------------------
    // Adding data
    // ------------------------------------------------------------------

    /**
     * Adds a new campsite.
     *
     * @throws IllegalArgumentException if a campsite with the same name exists
     */
    public Campsite addCampsite(String name, String address, int stars, String contactInfo) {
        if (findCampsite(name) != null) {
            throw new IllegalArgumentException("Campsite '" + name + "' already exists");
        }
        Campsite campsite = new Campsite(name, address, stars, contactInfo);
        campsites.add(campsite);
        return campsite;
    }

    /**
     * Adds a camping area to a campsite.
     *
     * @throws IllegalArgumentException if the campsite is unknown or the area id is already used there
     */
    public CampingArea addCampingArea(String campsiteName, String areaId, String description) {
        Campsite campsite = requireCampsite(campsiteName);
        CampingArea area = new CampingArea(areaId, description);
        campsite.addArea(area); // throws if the identifier is a duplicate
        return area;
    }

    /**
     * Adds a plot to a camping area.
     *
     * @throws IllegalArgumentException if the campsite or area is unknown or the plot id is already used
     */
    public void addPlot(String campsiteName, String areaId, Plot plot) {
        CampingArea area = requireArea(requireCampsite(campsiteName), areaId);
        area.addPlot(plot); // throws if the identifier is a duplicate
    }

    /**
     * Adds a guest and gives them the next free guest id.
     *
     * @return the new guest
     */
    public Guest addGuest(String name, LocalDate dateOfBirth, String address,
                          String phone, String email, String notes) {
        Guest guest = new Guest(nextGuestId, name, dateOfBirth, address, phone, email, notes);
        nextGuestId++; // only used up when the guest was created successfully
        guests.add(guest);
        return guest;
    }

    // ------------------------------------------------------------------
    // Searching
    // ------------------------------------------------------------------

    /** @return the campsite with that name (ignores case), or null if not found */
    public Campsite findCampsite(String name) {
        if (name == null) {
            return null;
        }
        Node<Campsite> current = campsites.getHead();
        while (current != null) {
            if (current.getData().getName().equalsIgnoreCase(name.trim())) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    /** @return the guest with that id, or null if not found */
    public Guest findGuest(int guestId) {
        Node<Guest> current = guests.getHead();
        while (current != null) {
            if (current.getData().getId() == guestId) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Booking
    // ------------------------------------------------------------------

    /**
     * Makes a booking in the given camping area. The system looks through the
     * plots of that area and assigns the cheapest one that is free for the dates
     * and matches every preference.
     *
     * @return the new booking, or null if no suitable plot is available
     * @throws IllegalArgumentException if the campsite, area or guest is unknown, or the dates are invalid
     */
    public Booking makeBooking(String campsiteName, String areaId, int guestId,
                               LocalDate start, LocalDate end, BookingPreferences preferences) {
        Campsite campsite = requireCampsite(campsiteName);
        CampingArea area = requireArea(campsite, areaId);
        Guest guest = findGuest(guestId);
        if (guest == null) {
            throw new IllegalArgumentException("No guest with id " + guestId);
        }

        // The Booking constructor validates the dates; the id is only used once we succeed
        Booking booking = new Booking(nextBookingId, campsite.getName(), area.getIdentifier(),
                guestId, start, end, preferences);

        Plot plot = findBestPlot(area, preferences, start, end);
        if (plot == null) {
            return null;
        }

        completeBooking(booking, plot, guest);
        return booking;
    }

    /**
     * Books one specific plot. Used by the smart find, which has already chosen the plot,
     * so the area is not searched again. The plot must be free for the dates.
     *
     * @return the new booking
     * @throws IllegalArgumentException if the campsite, area, plot or guest is unknown, or the dates are invalid
     * @throws IllegalStateException    if the plot is already booked for part of the period
     */
    public Booking bookPlot(String campsiteName, String areaId, String plotId, int guestId,
                            LocalDate start, LocalDate end, BookingPreferences preferences) {
        Campsite campsite = requireCampsite(campsiteName);
        CampingArea area = requireArea(campsite, areaId);
        Plot plot = area.findPlot(plotId);
        if (plot == null) {
            throw new IllegalArgumentException("No plot '" + plotId + "' in " + areaId);
        }
        Guest guest = findGuest(guestId);
        if (guest == null) {
            throw new IllegalArgumentException("No guest with id " + guestId);
        }

        Booking booking = new Booking(nextBookingId, campsite.getName(), area.getIdentifier(),
                guestId, start, end, preferences);
        if (!plot.isAvailable(start, end)) {
            throw new IllegalStateException("Plot " + plotId + " is no longer free for those dates");
        }

        completeBooking(booking, plot, guest);
        return booking;
    }

    /** Records a new booking on its plot and its guest, and uses up the booking id. */
    private void completeBooking(Booking booking, Plot plot, Guest guest) {
        nextBookingId++;
        booking.setAssignedPlot(plot);
        plot.addBooking(booking);
        guest.addBooking(booking);
    }

    /**
     * Cancels a pending booking, using today's date to decide if it is still pending.
     *
     * @see #cancelBooking(int, LocalDate)
     */
    public boolean cancelBooking(int bookingId) {
        return cancelBooking(bookingId, LocalDate.now());
    }

    /**
     * Cancels a pending booking: it is removed from its plot and from the guest.
     * Bookings that have already finished are kept as history and cannot be cancelled.
     *
     * @param today the date used to decide whether the booking is still pending
     * @return true if a pending booking with that id was found and cancelled
     */
    public boolean cancelBooking(int bookingId, LocalDate today) {
        Node<Guest> guestNode = guests.getHead();
        while (guestNode != null) {
            Guest guest = guestNode.getData();
            Node<Booking> bookingNode = guest.getBookings().getHead();
            while (bookingNode != null) {
                Booking booking = bookingNode.getData();
                if (booking.getBookingId() == bookingId) {
                    if (booking.isPast(today)) {
                        return false;
                    }
                    if (booking.getAssignedPlot() != null) {
                        booking.getAssignedPlot().removeBooking(booking);
                    }
                    guest.removeBooking(booking);
                    return true;
                }
                bookingNode = bookingNode.getNext();
            }
            guestNode = guestNode.getNext();
        }
        return false;
    }

    // ------------------------------------------------------------------
    // Removing a plot
    // ------------------------------------------------------------------

    /**
     * Removes a plot, using today's date to decide which bookings are pending.
     *
     * @see #removePlot(String, String, String, LocalDate)
     */
    public RemovalResult removePlot(String campsiteName, String areaId, String plotId) {
        return removePlot(campsiteName, areaId, plotId, LocalDate.now());
    }

    /**
     * Removes a plot from its camping area. Each pending booking of that plot is moved to
     * another suitable plot, first in the same area and then in any other area of the same
     * campsite. A booking that cannot be moved is cancelled. Past bookings stay in the
     * guest's history.
     *
     * @param today the date used to decide which bookings are still pending
     * @return how many bookings were moved and how many cancelled
     * @throws IllegalArgumentException if the campsite, area or plot is unknown
     */
    public RemovalResult removePlot(String campsiteName, String areaId, String plotId, LocalDate today) {
        Campsite campsite = requireCampsite(campsiteName);
        CampingArea area = requireArea(campsite, areaId);
        Plot plot = area.findPlot(plotId);
        if (plot == null) {
            throw new IllegalArgumentException("No plot '" + plotId + "' in " + areaId);
        }

        // Copy the pending bookings first, because we change the plot's own list afterwards
        MyLinkedList<Booking> pending = new MyLinkedList<>();
        Node<Booking> node = plot.getBookings().getHead();
        while (node != null) {
            if (!node.getData().isPast(today)) {
                pending.add(node.getData());
            }
            node = node.getNext();
        }

        // Remove the plot first so it cannot be chosen as its own replacement
        area.removePlot(plot);

        int moved = 0;
        int cancelled = 0;
        Node<Booking> pendingNode = pending.getHead();
        while (pendingNode != null) {
            Booking booking = pendingNode.getData();
            plot.removeBooking(booking);
            if (reassign(campsite, area, booking)) {
                moved++;
            } else {
                Guest guest = findGuest(booking.getGuestId());
                if (guest != null) {
                    guest.removeBooking(booking);
                }
                booking.setAssignedPlot(null);
                cancelled++;
            }
            pendingNode = pendingNode.getNext();
        }
        return new RemovalResult(moved, cancelled);
    }

    // ------------------------------------------------------------------
    // Reset
    // ------------------------------------------------------------------

    /** Clears all system data and restarts the id counters. */
    public void reset() {
        campsites.clear();
        guests.clear();
        nextGuestId = 1;
        nextBookingId = 1;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    public MyLinkedList<Campsite> getCampsites() { return campsites; }
    public MyLinkedList<Guest> getGuests() { return guests; }

    // The id counters are only needed by the persistence service (same package)
    int getNextGuestId() { return nextGuestId; }
    int getNextBookingId() { return nextBookingId; }

    void setIdCounters(int nextGuestId, int nextBookingId) {
        this.nextGuestId = nextGuestId;
        this.nextBookingId = nextBookingId;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Checks whether a plot satisfies every preference for the given dates.
     * Static dwellings must also have at least one bed per guest.
     * The budget is compared with the total cost of the stay.
     * Package-private so the smart find uses exactly the same rules as a normal booking.
     */
    boolean matches(Plot plot, BookingPreferences prefs, LocalDate start, LocalDate end) {
        if (prefs.getType() != null && plot.getType() != prefs.getType()) {
            return false;
        }
        if (prefs.isNeedsPower() && !plot.hasPower()) {
            return false;
        }
        if (plot.getSizeM2() < prefs.getMinSizeM2()) {
            return false;
        }
        if (plot.getBedrooms() < prefs.getMinBedrooms()) {
            return false;
        }
        if (plot.getType().isStaticDwelling() && plot.getBeds() < prefs.getNumGuests()) {
            return false;
        }
        long nights = ChronoUnit.DAYS.between(start, end);
        if (nights * plot.getCostPerNight() > prefs.getBudget()) {
            return false;
        }
        return plot.isAvailable(start, end);
    }

    /** @return the cheapest matching plot in the area, or null if none matches */
    Plot findBestPlot(CampingArea area, BookingPreferences prefs, LocalDate start, LocalDate end) {
        Plot best = null;
        Node<Plot> current = area.getPlots().getHead();
        while (current != null) {
            Plot plot = current.getData();
            if (matches(plot, prefs, start, end)
                    && (best == null || plot.getCostPerNight() < best.getCostPerNight())) {
                best = plot;
            }
            current = current.getNext();
        }
        return best;
    }

    /** Tries the preferred area first, then every other area of the campsite. */
    private boolean reassign(Campsite campsite, CampingArea firstChoice, Booking booking) {
        if (tryAssignInArea(firstChoice, booking)) {
            return true;
        }
        Node<CampingArea> current = campsite.getAreas().getHead();
        while (current != null) {
            if (current.getData() != firstChoice && tryAssignInArea(current.getData(), booking)) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    private boolean tryAssignInArea(CampingArea area, Booking booking) {
        Plot plot = findBestPlot(area, booking.getPreferences(),
                booking.getStartDate(), booking.getEndDate());
        if (plot == null) {
            return false;
        }
        plot.addBooking(booking);
        booking.setAssignedPlot(plot);
        booking.setAreaId(area.getIdentifier());
        return true;
    }

    private Campsite requireCampsite(String name) {
        Campsite campsite = findCampsite(name);
        if (campsite == null) {
            throw new IllegalArgumentException("No campsite named '" + name + "'");
        }
        return campsite;
    }

    private CampingArea requireArea(Campsite campsite, String areaId) {
        CampingArea area = areaId == null ? null : campsite.findArea(areaId);
        if (area == null) {
            throw new IllegalArgumentException(
                    "No area '" + areaId + "' in " + campsite.getName());
        }
        return area;
    }
}
