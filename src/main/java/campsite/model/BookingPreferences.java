package campsite.model;

/**
 * The criteria a guest gives when booking. The system uses these to find a
 * suitable plot. Instances are immutable.
 */
public class BookingPreferences {

    /**
     * @param type        wanted plot type, or null for any type
     * @param numGuests   number of guests (at least 1)
     * @param needsPower  true if the plot must have power
     * @param minSizeM2   minimum plot size in square metres (0 = no minimum)
     * @param minBedrooms minimum bedrooms (0 = no minimum, ignored for empty pitches)
     * @param budget      total budget for the stay in euro
     */
    private final PlotType type;
    private final int numGuests;
    private final boolean needsPower;
    private final double minSizeM2;
    private final int minBedrooms;
    private final double budget;


    public BookingPreferences(PlotType type, int numGuests, boolean needsPower, double minSizeM2, int minBedrooms, double budget) {
        this.type = type;
        this.numGuests = numGuests;
        this.needsPower = needsPower;
        this.minSizeM2 = minSizeM2;
        this.minBedrooms = minBedrooms;
        this.budget = budget;

        if (numGuests < 1) {
            throw new IllegalArgumentException("At least one guest is required");
        }
        if(minSizeM2 < 0 || minBedrooms < 0 || budget < 0 ) {
            throw new IllegalArgumentException("Size, bedrooms and budget cannot be negative");
        }


    }

    public PlotType getType() {return type;}
    public int getNumGuests() {return numGuests;}
    public boolean isNeedsPower() {return needsPower;}
    public double getMinSizeM2() {return minSizeM2;}
    public int getMinBedrooms() {return minBedrooms;}
    public double getBudget() {return budget;}

    @Override
    public String toString(){
        return "type=" + (type == null ? "any" : type)
                + ", guests=" + numGuests
                + ", power=" + (needsPower ? "yes" : "no")
                + ", minSize=" + minSizeM2 + "m2"
                + ", minBedrooms=" + minBedrooms
                + ", budget=EUR " + budget;

    }
}
