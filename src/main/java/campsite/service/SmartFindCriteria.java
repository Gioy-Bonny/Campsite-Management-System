package campsite.service;

import campsite.model.BookingPreferences;
import campsite.model.PlotType;
import campsite.structures.MyLinkedList;

import java.time.LocalDate;

/**
 * What a guest asks the smart find for. The dates, number of guests and budget are
 * required; everything else is optional and has a neutral default.
 */
public class SmartFindCriteria {

    private final LocalDate start;
    private final LocalDate end;
    private final int numGuests;
    private final double budget;

    private int minStars = 1;
    private PlotType type = null;          // null means any type
    private boolean needsPower = false;
    private double minSizeM2 = 0;
    private int minBedrooms = 0;
    private int maxShiftDays = 0;          // how many days later the stay may start
    private MyLinkedList<String> campsiteFacilities = new MyLinkedList<>();
    private MyLinkedList<String> plotFacilities = new MyLinkedList<>();

    /**
     * @param start     desired check-in date
     * @param end       desired check-out date (after the start)
     * @param numGuests number of guests (at least 1)
     * @param budget    total budget for the whole stay in euro
     */
    public SmartFindCriteria(LocalDate start, LocalDate end, int numGuests, double budget) {
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        if (numGuests < 1) {
            throw new IllegalArgumentException("At least one guest is required");
        }
        if (budget < 0) {
            throw new IllegalArgumentException("Budget cannot be negative");
        }
        this.start = start;
        this.end = end;
        this.numGuests = numGuests;
        this.budget = budget;
    }

    /** @return the booking preferences these criteria correspond to */
    public BookingPreferences toPreferences() {
        return new BookingPreferences(type, numGuests, needsPower, minSizeM2, minBedrooms, budget);
    }

    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    public int getNumGuests() { return numGuests; }
    public double getBudget() { return budget; }
    public int getMinStars() { return minStars; }
    public PlotType getType() { return type; }
    public boolean needsPower() { return needsPower; }
    public double getMinSizeM2() { return minSizeM2; }
    public int getMinBedrooms() { return minBedrooms; }
    public int getMaxShiftDays() { return maxShiftDays; }
    public MyLinkedList<String> getCampsiteFacilities() { return campsiteFacilities; }
    public MyLinkedList<String> getPlotFacilities() { return plotFacilities; }

    public void setMinStars(int minStars) {
        if (minStars < 1 || minStars > 5) {
            throw new IllegalArgumentException("Minimum stars must be between 1 and 5");
        }
        this.minStars = minStars;
    }

    public void setType(PlotType type) { this.type = type; }

    public void setNeedsPower(boolean needsPower) { this.needsPower = needsPower; }

    public void setMinSizeM2(double minSizeM2) {
        if (minSizeM2 < 0) {
            throw new IllegalArgumentException("Minimum size cannot be negative");
        }
        this.minSizeM2 = minSizeM2;
    }

    public void setMinBedrooms(int minBedrooms) {
        if (minBedrooms < 0) {
            throw new IllegalArgumentException("Minimum bedrooms cannot be negative");
        }
        this.minBedrooms = minBedrooms;
    }

    public void setMaxShiftDays(int maxShiftDays) {
        if (maxShiftDays < 0) {
            throw new IllegalArgumentException("Date flexibility cannot be negative");
        }
        this.maxShiftDays = maxShiftDays;
    }

    /** @param facilities facilities the campsite must offer (pool, WiFi...) */
    public void setCampsiteFacilities(MyLinkedList<String> facilities) {
        this.campsiteFacilities = facilities;
    }

    /** @param facilities facilities the plot must have (air conditioning...) */
    public void setPlotFacilities(MyLinkedList<String> facilities) {
        this.plotFacilities = facilities;
    }
}
