package campsite.model;

/**
 * The kinds of plot/pitch a camping area can contain.
 * Static dwellings come with power and bedrooms; an empty pitch does not.
 */
public enum PlotType {
    STATIC_MOBILE_HOME("Static mobile home", true),
    BUNGALOW("Bungalow", true),
    APARTMENT("Apartment", true),
    EMPTY_PITCH("Empty pitch", false);

    private final String name;
    private final boolean staticDwelling;
    PlotType(String name, boolean staticDwelling) {
        this.name = name;
        this.staticDwelling = staticDwelling;
    }

    /** @return true if this type is a permanent dwelling (not a tent/caravan pitch) */
    public boolean isStaticDwelling() {
        return staticDwelling;
    }

    /** @return a readable name, also used when the type is printed or shown in the GUI */
    @Override
    public String toString() {
        return name;
    }
}
