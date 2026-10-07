package campsite.model;

import campsite.structures.Node;
import campsite.structures.MyLinkedList;

/**
 * A camping area inside a campsite, such as "Lake View Area 1".
 * It contains the plots/pitches of that area.
 */
public class CampingArea {

    private final String identifier;
    private final String description;
    private final MyLinkedList<Plot> plots = new MyLinkedList<>();

    /**
     * @param identifier  area name, unique within its campsite
     * @param description short description of the area
     */
    public CampingArea(String identifier, String description) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Area identifier is required");
        }
        this.identifier = identifier;
        this.description = description;
    }

    /**
     * Adds a plot to this area.
     *
     * @throws IllegalArgumentException if a plot with the same identifier already exists here
     */
    public void addPlot(Plot plot) {
        if (findPlot(plot.getIdentifier()) != null) {
            throw new IllegalArgumentException("Plot " + plot.getIdentifier() + " already exists in" + identifier);
        }
        plots.add(plot);
    }

    /**
     * @return the plot with that identifier (ignores case), or null if not found
     */
    public Plot findPlot(String plotId) {
        Node<Plot> current = plots.getHead();
        while (current != null) {
            if (current.getData().getIdentifier().equalsIgnoreCase(plotId.trim())) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    /**
     * @return true if the plot was found and removed from this area
     */
    public boolean removePlot(Plot plot) {
        return plots.remove(plot);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDescription() {
        return description;
    }

    public MyLinkedList<Plot> getPlots() {
        return plots;
    }

    @Override
    public String toString() {
        return identifier + " (" + plots.size() + " plots)";
    }
}