package campsite.ui;

import campsite.service.CampsiteSystem;

import java.time.LocalDate;

/**
 * The state shared by all tabs of the GUI: the campsite system itself and a version
 * counter. The data lives here, in our own classes, and never inside JavaFX components;
 * the components only display it.
 *
 * <p>Loading a file or loading demo data replaces the whole system, which is why the tabs
 * ask this context for the current system each time instead of keeping their own reference.
 */
public class AppContext {

    private CampsiteSystem system = new CampsiteSystem();
    private int version = 0;

    /** @return the system currently in use */
    public CampsiteSystem getSystem() {
        return system;
    }

    /** Replaces the whole system (after loading a file or demo data). */
    public void setSystem(CampsiteSystem system) {
        this.system = system;
        changed();
    }

    /** Must be called after any change to the data, so views know they are out of date. */
    public void changed() {
        version++;
    }

    /** @return a number that goes up every time the data changes */
    public int getVersion() {
        return version;
    }

    /** @return today's date; a booking is pending until its end date has passed */
    public LocalDate today() {
        return LocalDate.now();
    }
}
