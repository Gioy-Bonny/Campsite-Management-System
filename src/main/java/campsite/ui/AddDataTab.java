package campsite.ui;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/** Groups the four "add" forms (campsite, camping area, plot, guest) into one tab. */
public class AddDataTab extends TabPane implements Refreshable {

    private final CampsiteForm campsiteForm;
    private final AreaForm areaForm;
    private final PlotForm plotForm;
    private final GuestForm guestForm;

    public AddDataTab(AppContext ctx) {
        campsiteForm = new CampsiteForm(ctx);
        areaForm = new AreaForm(ctx);
        plotForm = new PlotForm(ctx);
        guestForm = new GuestForm(ctx);

        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getTabs().add(makeTab("Campsite", campsiteForm));
        getTabs().add(makeTab("Camping area", areaForm));
        getTabs().add(makeTab("Plot / pitch", plotForm));
        getTabs().add(makeTab("Guest", guestForm));

        // Reload the drop-downs of a form when it is shown, in case data changed meanwhile
        getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> refresh());
    }

    @Override
    public void refresh() {
        campsiteForm.refresh();
        areaForm.refresh();
        plotForm.refresh();
        guestForm.refresh();
    }

    private Tab makeTab(String title, Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }
}
