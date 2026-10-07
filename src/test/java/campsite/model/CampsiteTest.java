package campsite.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CampsiteTest {

    private Campsite campsite;

    @Before
    public void setUp() {
        campsite = new Campsite("Sunny Camp", "1 Beach Road, Kerry", 4, "info@sunny.ie");
    }

    @Test
    public void addedAreaCanBeFoundCaseInsensitively() {
        CampingArea area = new CampingArea("Lake View Area 1", "Next to the lake");
        campsite.addArea(area);

        assertSame(area, campsite.findArea("lake view area 1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateAreaIdentifierIsRejected() {
        campsite.addArea(new CampingArea("Lake View Area 1", "first"));
        campsite.addArea(new CampingArea("Lake View Area 1", "second"));
    }

    @Test
    public void sameAreaNameIsAllowedInDifferentCampsites() {
        Campsite other = new Campsite("Other Camp", "2 Hill Road", 3, "other@camp.ie");
        campsite.addArea(new CampingArea("Area 1", "x"));

        other.addArea(new CampingArea("Area 1", "y"));

        assertEquals(1, other.getAreas().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void starsOutsideRangeAreRejected() {
        new Campsite("Bad", "addr", 6, "contact");
    }

    @Test
    public void duplicateFacilitiesAreIgnored() {
        campsite.addFacility("WiFi");
        campsite.addFacility("wifi");

        assertEquals(1, campsite.getFacilities().size());
        assertTrue(campsite.hasFacility("WIFI"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicatePlotIdentifierInAreaIsRejected() {
        CampingArea area = new CampingArea("Area 1", "x");
        area.addPlot(new Plot("P1", PlotType.EMPTY_PITCH, false, 0, 0, 30.0, 20.0, "url"));
        area.addPlot(new Plot("P1", PlotType.BUNGALOW, true, 2, 4, 50.0, 80.0, "url"));
    }
}