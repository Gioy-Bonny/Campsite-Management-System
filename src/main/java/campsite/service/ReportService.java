package campsite.service;

import campsite.model.Booking;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
import campsite.model.Plot;
import campsite.structures.Node;

import java.time.LocalDate;

/**
 * Builds the text reports: pending bookings (per campsite and for the whole
 * system) and a guest's record. The reports are plain strings, so the GUI only
 * has to display them and the logic can be unit tested without JavaFX.
 * A booking is "pending" until its end date has passed.
 */
public final class ReportService {

    private ReportService() {
    }

    /** Running totals for a group of bookings. */
    private static class Totals {
        private int count;
        private double cost;

        void addBooking(Booking booking) {
            count++;
            cost += booking.getTotalCost();
        }

        void add(Totals other) {
            count += other.count;
            cost += other.cost;
        }

        String describe() {
            return count + " booking(s), " + TextUtil.euro(cost);
        }
    }

    /**
     * Pending bookings of one campsite, organised by camping area and then by plot.
     *
     * @param today the date used to decide which bookings are still pending
     * @throws IllegalArgumentException if the campsite does not exist
     */
    public static String pendingBookingsForCampsite(CampsiteSystem system, String campsiteName,
                                                    LocalDate today) {
        Campsite campsite = system.findCampsite(campsiteName);
        if (campsite == null) {
            throw new IllegalArgumentException("No campsite named '" + campsiteName + "'");
        }
        StringBuilder text = new StringBuilder();
        appendCampsite(text, system, campsite, today);
        return text.toString();
    }

    /**
     * Pending bookings of the whole system, organised by campsite, camping area and plot,
     * with a grand total at the end.
     */
    public static String pendingBookingsOverall(CampsiteSystem system, LocalDate today) {
        if (system.getCampsites().isEmpty()) {
            return "There are no campsites in the system.\n";
        }
        StringBuilder text = new StringBuilder();
        Totals overall = new Totals();
        Node<Campsite> node = system.getCampsites().getHead();
        while (node != null) {
            overall.add(appendCampsite(text, system, node.getData(), today));
            text.append('\n');
            node = node.getNext();
        }
        text.append("OVERALL TOTAL: ").append(overall.describe()).append('\n');
        return text.toString();
    }

    /**
     * A guest's details followed by all their pending and past bookings.
     *
     * @throws IllegalArgumentException if there is no guest with that id
     */
    public static String guestRecord(CampsiteSystem system, int guestId, LocalDate today) {
        Guest guest = system.findGuest(guestId);
        if (guest == null) {
            throw new IllegalArgumentException("No guest with id " + guestId);
        }
        StringBuilder text = new StringBuilder();
        text.append("GUEST #").append(guest.getId()).append(": ").append(guest.getName()).append('\n');
        text.append("  Date of birth: ").append(guest.getDateOfBirth()).append('\n');
        text.append("  Address: ").append(TextUtil.orDash(guest.getAddress())).append('\n');
        text.append("  Phone: ").append(TextUtil.orDash(guest.getPhone())).append('\n');
        text.append("  Email: ").append(TextUtil.orDash(guest.getEmail())).append('\n');
        text.append("  Notes: ").append(TextUtil.orDash(guest.getNotes())).append('\n');
        appendGuestBookings(text, "PENDING BOOKINGS", guest, today, false);
        appendGuestBookings(text, "PAST BOOKINGS", guest, today, true);
        return text.toString();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static Totals appendCampsite(StringBuilder text, CampsiteSystem system,
                                         Campsite campsite, LocalDate today) {
        Totals campsiteTotals = new Totals();
        text.append("CAMPSITE: ").append(campsite.getName())
                .append(" (").append(campsite.getStars()).append(" stars)\n");
        if (campsite.getAreas().isEmpty()) {
            text.append("  (no camping areas)\n");
        }

        Node<CampingArea> areaNode = campsite.getAreas().getHead();
        while (areaNode != null) {
            CampingArea area = areaNode.getData();
            Totals areaTotals = new Totals();
            text.append("  Area: ").append(area.getIdentifier()).append('\n');

            Node<Plot> plotNode = area.getPlots().getHead();
            while (plotNode != null) {
                Plot plot = plotNode.getData();
                Totals plotTotals = new Totals();
                text.append("    Plot: ").append(plot).append('\n');

                Node<Booking> bookingNode = plot.getBookings().getHead();
                while (bookingNode != null) {
                    Booking booking = bookingNode.getData();
                    if (!booking.isPast(today)) {
                        text.append("      ").append(describeForPlot(system, booking)).append('\n');
                        plotTotals.addBooking(booking);
                    }
                    bookingNode = bookingNode.getNext();
                }

                if (plotTotals.count == 0) {
                    text.append("      (no pending bookings)\n");
                } else {
                    text.append("      Plot total: ").append(plotTotals.describe()).append('\n');
                }
                areaTotals.add(plotTotals);
                plotNode = plotNode.getNext();
            }

            text.append("  Area total: ").append(areaTotals.describe()).append('\n');
            campsiteTotals.add(areaTotals);
            areaNode = areaNode.getNext();
        }

        text.append("Campsite total: ").append(campsiteTotals.describe()).append('\n');
        return campsiteTotals;
    }

    private static String describeForPlot(CampsiteSystem system, Booking booking) {
        Guest guest = system.findGuest(booking.getGuestId());
        String guestName = (guest == null) ? "unknown guest" : guest.getName();
        return "Booking #" + booking.getBookingId()
                + " | guest " + booking.getGuestId() + " " + guestName
                + " | " + booking.getStartDate() + " to " + booking.getEndDate()
                + " (" + booking.getNights() + " nights)"
                + " | " + TextUtil.euro(booking.getTotalCost());
    }

    private static void appendGuestBookings(StringBuilder text, String title, Guest guest,
                                            LocalDate today, boolean past) {
        Totals totals = new Totals();
        StringBuilder lines = new StringBuilder();
        Node<Booking> node = guest.getBookings().getHead();
        while (node != null) {
            Booking booking = node.getData();
            if (booking.isPast(today) == past) {
                lines.append("  ").append(describeWithPlace(booking)).append('\n');
                totals.addBooking(booking);
            }
            node = node.getNext();
        }

        text.append('\n').append(title).append(" (").append(totals.count).append(")\n");
        if (totals.count == 0) {
            text.append("  none\n");
        } else {
            text.append(lines);
            text.append("  Total: ").append(totals.describe()).append('\n');
        }
    }

    private static String describeWithPlace(Booking booking) {
        Plot plot = booking.getAssignedPlot();
        return "Booking #" + booking.getBookingId()
                + " | " + booking.getCampsiteName() + " / " + booking.getAreaId()
                + " / plot " + (plot == null ? "unassigned" : plot.getIdentifier())
                + " | " + booking.getStartDate() + " to " + booking.getEndDate()
                + " (" + booking.getNights() + " nights)"
                + " | " + TextUtil.euro(booking.getTotalCost());
    }
}
