package campsite.ui;

import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Plot;
import campsite.service.CampsiteSystem;
import campsite.structures.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;

/**
 * A row of linked drop-downs for choosing a campsite and, optionally, one of its camping
 * areas and one of that area's plots. Choosing a campsite fills the area list, and choosing
 * an area fills the plot list. It is used by several tabs.
 */
public class LocationPicker extends HBox {

    private final AppContext ctx;
    private final ComboBox<Campsite> campsiteBox = new ComboBox<>();
    private final ComboBox<CampingArea> areaBox = new ComboBox<>();
    private final ComboBox<Plot> plotBox = new ComboBox<>();

    /**
     * @param showArea whether to show the camping area drop-down
     * @param showPlot whether to show the plot drop-down (needs the area drop-down too)
     */
    public LocationPicker(AppContext ctx, boolean showArea, boolean showPlot) {
        super(8);
        this.ctx = ctx;

        campsiteBox.setPromptText("Campsite");
        areaBox.setPromptText("Camping area");
        plotBox.setPromptText("Plot");

        getChildren().add(campsiteBox);
        if (showArea) {
            getChildren().add(areaBox);
        }
        if (showPlot) {
            getChildren().add(plotBox);
        }

        campsiteBox.valueProperty().addListener((observable, oldValue, newValue) -> fillAreas());
        areaBox.valueProperty().addListener((observable, oldValue, newValue) -> fillPlots());
        refresh();
    }

    /** @return the chosen campsite, or null */
    public Campsite getCampsite() {
        return campsiteBox.getValue();
    }

    /** @return the chosen camping area, or null */
    public CampingArea getArea() {
        return areaBox.getValue();
    }

    /** @return the chosen plot, or null */
    public Plot getPlot() {
        return plotBox.getValue();
    }

    /** Reloads all three lists from the current system, keeping the choices that still exist. */
    public void refresh() {
        CampsiteSystem system = ctx.getSystem();
        String campsiteName = (getCampsite() == null) ? null : getCampsite().getName();
        String areaId = (getArea() == null) ? null : getArea().getIdentifier();
        String plotId = (getPlot() == null) ? null : getPlot().getIdentifier();

        campsiteBox.getItems().clear();
        campsiteBox.setValue(null);
        Node<Campsite> node = system.getCampsites().getHead();
        while (node != null) {
            campsiteBox.getItems().add(node.getData());
            node = node.getNext();
        }

        // Choosing a campsite or area again refills the lists below it
        Campsite campsite = (campsiteName == null) ? null : system.findCampsite(campsiteName);
        if (campsite != null) {
            campsiteBox.setValue(campsite);
            CampingArea area = (areaId == null) ? null : campsite.findArea(areaId);
            if (area != null) {
                areaBox.setValue(area);
                Plot plot = (plotId == null) ? null : area.findPlot(plotId);
                if (plot != null) {
                    plotBox.setValue(plot);
                }
            }
        }
    }

    private void fillAreas() {
        areaBox.getItems().clear();
        areaBox.setValue(null);
        Campsite campsite = campsiteBox.getValue();
        if (campsite != null) {
            Node<CampingArea> node = campsite.getAreas().getHead();
            while (node != null) {
                areaBox.getItems().add(node.getData());
                node = node.getNext();
            }
        }
    }

    private void fillPlots() {
        plotBox.getItems().clear();
        plotBox.setValue(null);
        CampingArea area = areaBox.getValue();
        if (area != null) {
            Node<Plot> node = area.getPlots().getHead();
            while (node != null) {
                plotBox.getItems().add(node.getData());
                node = node.getNext();
            }
        }
    }
}
