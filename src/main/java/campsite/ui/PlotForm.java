package campsite.ui;

import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Plot;
import campsite.model.PlotType;
import campsite.service.TextUtil;
import campsite.structures.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Form for adding a plot/pitch to a camping area. */
public class PlotForm extends VBox implements Refreshable {

    private final AppContext ctx;
    private final LocationPicker picker;
    private final TextField idField = new TextField();
    private final ComboBox<String> typeBox = UiUtil.typeChoice(false);
    private final CheckBox powerBox = new CheckBox("Has power (always true for static dwellings)");
    private final TextField bedroomsField = new TextField("0");
    private final TextField bedsField = new TextField("0");
    private final TextField sizeField = new TextField();
    private final TextField costField = new TextField();
    private final TextField photoField = new TextField();
    private final TextField facilitiesField = new TextField();

    public PlotForm(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        this.picker = new LocationPicker(ctx, true, false);
        setPadding(new Insets(10));

        idField.setPromptText("e.g. P12");
        sizeField.setPromptText("square metres");
        costField.setPromptText("euro per night");
        photoField.setPromptText("https://...");
        facilitiesField.setPromptText("air conditioning, decking");

        GridPane grid = UiUtil.formGrid();
        grid.add(new Label("Campsite and area"), 0, 0);
        grid.add(picker, 1, 0);
        grid.add(new Label("Plot identifier"), 0, 1);
        grid.add(idField, 1, 1);
        grid.add(new Label("Type"), 0, 2);
        grid.add(typeBox, 1, 2);
        grid.add(powerBox, 1, 3);
        grid.add(new Label("Bedrooms (static only)"), 0, 4);
        grid.add(bedroomsField, 1, 4);
        grid.add(new Label("Beds (static only)"), 0, 5);
        grid.add(bedsField, 1, 5);
        grid.add(new Label("Size (m2)"), 0, 6);
        grid.add(sizeField, 1, 6);
        grid.add(new Label("Cost per night (EUR)"), 0, 7);
        grid.add(costField, 1, 7);
        grid.add(new Label("Photo URL"), 0, 8);
        grid.add(photoField, 1, 8);
        grid.add(new Label("Facilities (comma separated)"), 0, 9);
        grid.add(facilitiesField, 1, 9);
        Button addButton = new Button("Add plot");
        addButton.setOnAction(event -> UiUtil.guard(this::addPlot));
        grid.add(addButton, 1, 10);

        getChildren().add(UiUtil.heading("New plot / pitch"));
        getChildren().add(grid);
    }

    @Override
    public void refresh() {
        picker.refresh();
    }

    private void addPlot() {
        Campsite campsite = UiUtil.require(picker.getCampsite(), "a campsite");
        CampingArea area = UiUtil.require(picker.getArea(), "a camping area");
        PlotType type = UiUtil.require(UiUtil.typeFromChoice(typeBox.getValue()), "a plot type");

        Plot plot = new Plot(idField.getText(), type, powerBox.isSelected(),
                UiUtil.parseOptionalInt(bedroomsField.getText(), "Bedrooms", 0),
                UiUtil.parseOptionalInt(bedsField.getText(), "Beds", 0),
                UiUtil.parseDouble(sizeField.getText(), "Size"),
                UiUtil.parseDouble(costField.getText(), "Cost per night"),
                UiUtil.blankToNull(photoField.getText()));

        Node<String> facility = TextUtil.splitList(facilitiesField.getText()).getHead();
        while (facility != null) {
            plot.addFacility(facility.getData());
            facility = facility.getNext();
        }

        ctx.getSystem().addPlot(campsite.getName(), area.getIdentifier(), plot);
        ctx.changed();
        picker.refresh();

        idField.clear();
        sizeField.clear();
        costField.clear();
        photoField.clear();
        facilitiesField.clear();
        UiUtil.showInfo("Plot '" + plot.getIdentifier() + "' was added to " + area.getIdentifier() + ".");
    }
}
