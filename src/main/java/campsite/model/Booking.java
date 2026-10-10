package campsite.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


/**
 * A booking made by one guest for a stay in one camping area.
 * Once the system finds a suitable plot, it is stored in {@code assignedPlot}.
 * The end date is the check-out day, so a stay from the 1st to the 3rd is 2 nights.
 */
public class Booking {

    /**
     * @param bookingId    unique booking number
     * @param campsiteName name of the campsite (area ids are only unique inside a campsite)
     * @param areaId       identifier of the camping area
     * @param guestId      identifier of the guest making the booking
     * @param startDate    check-in date
     * @param endDate      check-out date (must be after the start date)
     * @param preferences  the guest's criteria
     */
    private final int bookingId;
    private final String campsiteName;
    private  String areaId;
    private final int guestId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BookingPreferences preferences;
    private Plot assignedPlot;

    public Booking(int bookingId, String campsiteName, String areaId, int GuestId,
                   LocalDate startDate, LocalDate endDate, BookingPreferences preferences) {
        if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("end date must be after start date");
        }
        if (preferences == null) {
            throw new IllegalArgumentException("preferences are required");
        }

        this.bookingId = bookingId;
        this.campsiteName = campsiteName;
        this.areaId = areaId;
        this.guestId = GuestId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.preferences = preferences;
    }

    /** @return number of nights of the stay */
    public int getNights(){
        return (int) ChronoUnit.DAYS.between(startDate, endDate);
    }

    /** @return nights multiplied by the assigned plot's nightly cost (0 if no plot assigned yet) */
    public double getTotalCost(){
        if (assignedPlot == null) {
            return 0.0;
        }
        return getNights() * assignedPlot.getCostPerNight();
    }

    /**
     * Checks if this booking's dates clash with another date range.
     * Back-to-back stays are allowed: one guest leaving on the day another arrives is fine.
     *
     * @return true if the two periods overlap
     */
    public boolean overlaps(LocalDate otherStart, LocalDate otherEnd) {
        return otherStart.isBefore(endDate) && startDate.isBefore(otherEnd);
    }

    /** @return true if the stay has already finished on the given date */
    public boolean isPast(LocalDate today) {
        return endDate.isBefore(today);
    }

    public int getBookingId() {return bookingId;}
    public String getCampsiteName() {return campsiteName;}
    public String getAreaId() {return areaId;}
    public int getGuestId() {return guestId;}
    public LocalDate getStartDate() {return startDate;}
    public LocalDate getEndDate() {return endDate;}
    public BookingPreferences getPreferences() {return preferences;}
    public Plot getAssignedPlot(){return assignedPlot;}

    public void setAssignedPlot(Plot assignedPlot) {
        this.assignedPlot = assignedPlot;
    }

    /** Changes the camping area, used when a booking is moved to a plot in another area. */
    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    @Override
    public String toString() {
        return "Booking #" + bookingId
                + " | " + campsiteName + " / " + areaId
                + " / plot " + (assignedPlot == null ? "unassigned" : assignedPlot.getIdentifier())
                + " | guest " + guestId
                + " | " + startDate + " to " + endDate + " (" + getNights() + " nights)"
                + " | EUR " + getTotalCost();
    }
}
