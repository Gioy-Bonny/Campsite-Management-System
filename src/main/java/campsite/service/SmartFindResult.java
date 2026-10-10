package campsite.service;

import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Plot;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * One suggestion from the smart find: where to stay and on which dates.
 */
public class SmartFindResult {

    private final Campsite campsite;
    private final CampingArea area;
    private final Plot plot;
    private final LocalDate start;
    private final LocalDate end;
    private final int shiftDays;

    /**
     * @param shiftDays how many days later than requested the suggested stay starts
     */
    public SmartFindResult(Campsite campsite, CampingArea area, Plot plot,
                           LocalDate start, LocalDate end, int shiftDays) {
        this.campsite = campsite;
        this.area = area;
        this.plot = plot;
        this.start = start;
        this.end = end;
        this.shiftDays = shiftDays;
    }

    /** @return nights multiplied by the plot's cost per night */
    public double getTotalCost() {
        return ChronoUnit.DAYS.between(start, end) * plot.getCostPerNight();
    }

    public Campsite getCampsite() { return campsite; }
    public CampingArea getArea() { return area; }
    public Plot getPlot() { return plot; }
    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    public int getShiftDays() { return shiftDays; }

    @Override
    public String toString() {
        return "Campsite: " + campsite.getName() + " (" + campsite.getStars() + " stars)\n"
                + "Camping area: " + area.getIdentifier() + "\n"
                + "Plot: " + plot + "\n"
                + "Dates: " + start + " to " + end
                + (shiftDays > 0 ? " (" + shiftDays + " day(s) later than requested)" : "") + "\n"
                + "Total cost: " + TextUtil.euro(getTotalCost());
    }
}
