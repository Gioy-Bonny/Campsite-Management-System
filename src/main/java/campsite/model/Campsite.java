package campsite.model;

import campsite.structures.Node;
import campsite.structures.MyLinkedList;

/**
 * A campsite with its details, facilities (restaurant, pool, WiFi...)
 * and the camping areas it contains.
 */
public class Campsite {

    /**
     * @param name        campsite name
     * @param address     postal address
     * @param stars       star rating from 1 to 5
     * @param contactInfo phone/email/website
     */
    private final String name;
    private final String address;
    private final int stars;
    private final String contactInfo;
    private final MyLinkedList<String> facilities = new MyLinkedList<>();
    private final MyLinkedList<CampingArea> areas = new MyLinkedList<>();

    public Campsite(String name, String address, int stars, String contactInfo) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Campsite name is required");
        }
        if (stars < 1 || stars > 5) {
            throw new IllegalArgumentException("Stars must be between 1 and 5");
        }
        this.name = name.trim();
        this.address = address;
        this.stars = stars;
        this.contactInfo = contactInfo;
    }

    /** Adds a facility such as "swimming pool". Duplicates are ignored. */
    public void addFacility(String facility) {
        if (facility != null && !facility.isBlank() && !hasFacility(facility)) {
            facilities.add(facility.trim());
        }
    }

    /** @return true if the campsite offers the facility (ignores upper/lower case) */
    public boolean hasFacility(String facility) {
        Node<String> current = facilities.getHead();
        while (current != null) {
            if (current.getData().equalsIgnoreCase(facility.trim())) {
                return true;
            }
            current = current.getNext();
        }
        return false;
    }

    /**
     * Adds a camping area to this campsite.
     *
     * @throws IllegalArgumentException if an area with the same identifier already exists
     */
    public void addArea(CampingArea area) {
        if (findArea(area.getIdentifier()) != null) {
            throw new IllegalArgumentException(
                    "Area '" + area.getIdentifier() + "' already exists in " + name);
        }
        areas.add(area);
    }
    /** @return the area with that identifier (ignores case), or null if not found */
    public CampingArea findArea(String areaId) {
        Node<CampingArea> current = areas.getHead();
        while (current != null) {
            if (current.getData().getIdentifier().equalsIgnoreCase(areaId.trim())) {
                return current.getData();
            }
            current = current.getNext();
        }
        return null;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
    public int getStars() { return stars; }
    public String getContactInfo() { return contactInfo; }
    public MyLinkedList<String> getFacilities() { return facilities; }
    public MyLinkedList<CampingArea> getAreas() { return areas; }

    @Override
    public String toString() {
        return name + " (" + stars + " stars, " + areas.size() + " areas)";
    }
}
