package campsite.ui;

/**
 * Implemented by every tab. The main window calls {@link #refresh()} when a tab is
 * selected, so the tab can reload its drop-down lists and views from the current data.
 */
public interface Refreshable {

    /** Reloads whatever the tab displays from the current system data. */
    void refresh();
}
