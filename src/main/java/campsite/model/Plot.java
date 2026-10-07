package campsite.model;

import campsite.structures.MyLinkedList;
import campsite.structures.Node;

import java.time.LocalDate;

/**
 * A single plot/pitch inside a camping area. It may hold a static dwelling
 * or be an empty pitch, and it keeps the list of bookings made for it.
 */
public class Plot {

    /**
     * Static dwellings always have power. Bedrooms and beds only apply to
     * static dwellings, so they are set to 0 for an empty pitch.
     *
     * @param identifier   plot name/number, unique within its camping area
     * @param type         type of plot
     * @param hasPower     whether power is available (forced to true for static dwellings)
     * @param bedrooms     number of bedrooms (static dwellings only)
     * @param beds         number of beds (static dwellings only)
     * @param sizeM2       size in square metres (must be above 0)
     * @param costPerNight cost per night in euro (cannot be negative)
     * @param photoUrl     URL of a photo of the plot
     */
    private final String identifier;
    private final PlotType type;
    private final boolean hasPower;
    private final int bedrooms;
    private final int beds;
    private final double sizeM2;
    private final double costPerNight;
    private final String photoUrl;
    private final MyLinkedList<String> facilities = new MyLinkedList<>();
    private final MyLinkedList<Booking> bookings = new MyLinkedList<>();

    public Plot(String identifier, PlotType type, boolean hasPower, int bedrooms, int beds,
                double sizeM2, double costPerNight, String photoUrl) {
        if(identifier == null || identifier.isBlank()){
            throw new IllegalArgumentException("Plot identifier is required");
        }
        if(type == null){
            throw new IllegalArgumentException("Plot type is required");
        }
        if(sizeM2 <= 0){
            throw new IllegalArgumentException("Size must be greater than zero");
        }
        if(costPerNight < 0){
            throw new IllegalArgumentException("Cost per night cannot be negative");
        }
        if(bedrooms < 0 || beds < 0){
            throw new IllegalArgumentException("Bedrooms and beds cannot be negative");
        }
        this.identifier = identifier.trim();
        this.type = type;
        this.sizeM2 = sizeM2;
        this.costPerNight = costPerNight;
        this.photoUrl = photoUrl;

        if(type.isStaticDwelling()){
            this.hasPower = true;
            this.bedrooms = bedrooms;
            this.beds = beds;
        }else{
            this.hasPower = hasPower;
            this.bedrooms = 0;
            this.beds = 0;
        }
    }

    /**
     * Adds a facility such as "air conditioning". Duplicates are ignored.
     *
     * @param facility the facility name
     */
    public void addFacility(String facility){
        if(facility!=null && !facility.isBlank() && !hasFacility(facility)){
            facilities.add(facility.trim());
        }
    }

    /** @return true if the plot has the facility (ignores upper/lower case) */
    public boolean hasFacility(String facility){
        Node<String> current = facilities.getHead();
        while(current!=null){
            if(current.getData().equalsIgnoreCase(facility.trim())){
                return true;
            }
            current = current.getNext();
        }
        return  false;
    }

    /**
     * Checks that no existing booking on this plot clashes with the given dates.
     *
     * @return true if the plot is free for the whole period
     */
    public boolean isAvailable(LocalDate start, LocalDate end){
        Node<Booking> current = bookings.getHead();
        while(current!=null){
            if(current.getData().overlaps(start, end)){
                return false;
            }
            current = current.getNext();
        }
        return true;
    }

    /** Records a booking against this plot. */
    public void addBooking(Booking booking) {
        bookings.add(booking);
    }

    /** @return true if the booking was found and removed */
    public boolean removeBooking(Booking booking) {
        return bookings.remove(booking);
    }


    public String getIdentifier() { return identifier; }
    public PlotType getType() { return type; }
    public boolean hasPower() { return hasPower; }
    public int getBedrooms() { return bedrooms; }
    public int getBeds() { return beds; }
    public double getSizeM2() { return sizeM2; }
    public double getCostPerNight() { return costPerNight; }
    public String getPhotoUrl() { return photoUrl; }
    public MyLinkedList<String> getFacilities() { return facilities; }
    public MyLinkedList<Booking> getBookings() { return bookings; }

    @Override
    public String toString() {
        return identifier + " (" + type + ", " + sizeM2 + "m2, EUR " + costPerNight + "/night"
                + (hasPower ? ", power" : "")
                + (type.isStaticDwelling() ? ", " + bedrooms + " bed rooms, " + beds + " beds" : "")
                + ")";
    }
}
