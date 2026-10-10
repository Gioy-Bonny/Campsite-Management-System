package campsite.service;

import campsite.model.Booking;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Plot;
import campsite.structures.MyLinkedList;
import campsite.structures.Node;

import java.time.LocalDate;

/**
 * The "smart find" for camping holidays. It looks through every campsite, area and plot
 * and suggests the best stay for what the guest asked for.
 *
 * <p>How it works:
 * <ol>
 *   <li>It first tries the dates the guest asked for. If nothing is free, it tries the same
 *       stay one day later, then two days later, and so on, up to the flexibility given.</li>
 *   <li>For one set of dates, a campsite must have enough stars and every required facility,
 *       and a plot must satisfy the normal booking rules
 *       ({@link CampsiteSystem#matches}) plus every required plot facility.</li>
 *   <li>Among the suitable plots, the cheapest total cost wins. If two cost the same,
 *       the campsite with more stars wins.</li>
 * </ol>
 */
public class SmartFindService {

    private final CampsiteSystem system;

    public SmartFindService(CampsiteSystem system) {
        this.system = system;
    }

    /**
     * Finds the best suggestion for the criteria.
     *
     * @return the suggestion, or null if nothing suitable exists
     */
    public SmartFindResult find(SmartFindCriteria criteria) {
        for (int shift = 0; shift <= criteria.getMaxShiftDays(); shift++) {
            LocalDate start = criteria.getStart().plusDays(shift);
            LocalDate end = criteria.getEnd().plusDays(shift);
            SmartFindResult best = findBestForDates(criteria, start, end, shift);
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    /**
     * Books the plot of a suggestion for a guest, using the same criteria as preferences.
     *
     * @return the new booking
     * @throws IllegalArgumentException if the guest does not exist
     * @throws IllegalStateException    if the plot was booked by someone else in the meantime
     */
    public Booking book(SmartFindResult result, SmartFindCriteria criteria, int guestId) {
        return system.bookPlot(result.getCampsite().getName(), result.getArea().getIdentifier(),
                result.getPlot().getIdentifier(), guestId,
                result.getStart(), result.getEnd(), criteria.toPreferences());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private SmartFindResult findBestForDates(SmartFindCriteria criteria, LocalDate start,
                                             LocalDate end, int shift) {
        SmartFindResult best = null;
        Node<Campsite> campsiteNode = system.getCampsites().getHead();
        while (campsiteNode != null) {
            Campsite campsite = campsiteNode.getData();
            if (campsiteQualifies(campsite, criteria)) {
                Node<CampingArea> areaNode = campsite.getAreas().getHead();
                while (areaNode != null) {
                    CampingArea area = areaNode.getData();
                    Node<Plot> plotNode = area.getPlots().getHead();
                    while (plotNode != null) {
                        Plot plot = plotNode.getData();
                        if (plotQualifies(plot, criteria, start, end)) {
                            SmartFindResult candidate =
                                    new SmartFindResult(campsite, area, plot, start, end, shift);
                            if (best == null || isBetter(candidate, best)) {
                                best = candidate;
                            }
                        }
                        plotNode = plotNode.getNext();
                    }
                    areaNode = areaNode.getNext();
                }
            }
            campsiteNode = campsiteNode.getNext();
        }
        return best;
    }

    private boolean campsiteQualifies(Campsite campsite, SmartFindCriteria criteria) {
        return campsite.getStars() >= criteria.getMinStars()
                && hasAll(campsite.getFacilities(), criteria.getCampsiteFacilities());
    }

    private boolean plotQualifies(Plot plot, SmartFindCriteria criteria, LocalDate start, LocalDate end) {
        return system.matches(plot, criteria.toPreferences(), start, end)
                && hasAll(plot.getFacilities(), criteria.getPlotFacilities());
    }

    /** @return true if every wanted facility appears in the offered list (ignores case) */
    private boolean hasAll(MyLinkedList<String> offered, MyLinkedList<String> wanted) {
        Node<String> wantedNode = wanted.getHead();
        while (wantedNode != null) {
            if (!containsIgnoreCase(offered, wantedNode.getData())) {
                return false;
            }
            wantedNode = wantedNode.getNext();
        }
        return true;
    }

    private boolean containsIgnoreCase(MyLinkedList<String> list, String text) {
        Node<String> node = list.getHead();
        while (node != null) {
            if (node.getData().equalsIgnoreCase(text.trim())) {
                return true;
            }
            node = node.getNext();
        }
        return false;
    }

    /** Cheaper total cost wins; with equal cost, the campsite with more stars wins. */
    private boolean isBetter(SmartFindResult candidate, SmartFindResult best) {
        if (candidate.getTotalCost() != best.getTotalCost()) {
            return candidate.getTotalCost() < best.getTotalCost();
        }
        return candidate.getCampsite().getStars() > best.getCampsite().getStars();
    }
}
