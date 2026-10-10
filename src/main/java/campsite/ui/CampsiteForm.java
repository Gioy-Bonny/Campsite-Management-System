package campsite.ui;

import campsite.model.Campsite;
import campsite.service.TextUtil;
import campsite.structures.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Form for adding a campsite, and for adding more facilities (restaurant, pool, WiFi...)
 * to a campsite that already exists.
 */
public class CampsiteForm extends VBox implements Refreshable {

    private final AppContext ctx;
    private final TextField nameField = new TextField();
    private final TextField addressField = new TextField();
    private final ComboBox<Integer> starsBox = new ComboBox<>();
    private final TextField contactField = new TextField();
    private final TextField facilitiesField = new TextField();
    private final LocationPicker picker;
    private final TextField moreFacilitiesField = new TextField();

    public CampsiteForm(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        this.picker = new LocationPicker(ctx, false, false);
        setPadding(new Insets(10));

        for (int stars = 1; stars <= 5; stars++) {
            starsBox.getItems().add(stars);
        }
        starsBox.setValue(3);
        facilitiesField.setPromptText("pool, WiFi, laundry");
        moreFacilitiesField.setPromptText("spa, gym");

        GridPane newCampsite = UiUtil.formGrid();
        newCampsite.add(new Label("Name"), 0, 0);
        newCampsite.add(nameField, 1, 0);
        newCampsite.add(new Label("Address"), 0, 1);
        newCampsite.add(addressField, 1, 1);
        newCampsite.add(new Label("Stars"), 0, 2);
        newCampsite.add(starsBox, 1, 2);
        newCampsite.add(new Label("Contact info"), 0, 3);
        newCampsite.add(contactField, 1, 3);
        newCampsite.add(new Label("Facilities (comma separated)"), 0, 4);
        newCampsite.add(facilitiesField, 1, 4);
        Button addButton = new Button("Add campsite");
        addButton.setOnAction(event -> UiUtil.guard(this::addCampsite));
        newCampsite.add(addButton, 1, 5);

        GridPane moreFacilities = UiUtil.formGrid();
        moreFacilities.add(new Label("Campsite"), 0, 0);
        moreFacilities.add(picker, 1, 0);
        moreFacilities.add(new Label("Facilities (comma separated)"), 0, 1);
        moreFacilities.add(moreFacilitiesField, 1, 1);
        Button facilitiesButton = new Button("Add facilities");
        facilitiesButton.setOnAction(event -> UiUtil.guard(this::addFacilities));
        moreFacilities.add(facilitiesButton, 1, 2);

        getChildren().add(UiUtil.heading("New campsite"));
        getChildren().add(newCampsite);
        getChildren().add(UiUtil.heading("Add facilities to an existing campsite"));
        getChildren().add(moreFacilities);
    }

    @Override
    public void refresh() {
        picker.refresh();
    }

    private void addCampsite() {
        int stars = UiUtil.require(starsBox.getValue(), "a star rating");
        Campsite campsite = ctx.getSystem().addCampsite(
                nameField.getText(), addressField.getText(), stars, contactField.getText());
        addFacilitiesTo(campsite, facilitiesField.getText());
        ctx.changed();
        picker.refresh();

        nameField.clear();
        addressField.clear();
        contactField.clear();
        facilitiesField.clear();
        UiUtil.showInfo("Campsite '" + campsite.getName() + "' was added.");
    }

    private void addFacilities() {
        Campsite campsite = UiUtil.require(picker.getCampsite(), "a campsite");
        if (TextUtil.splitList(moreFacilitiesField.getText()).isEmpty()) {
            throw new IllegalArgumentException("Type at least one facility.");
        }
        addFacilitiesTo(campsite, moreFacilitiesField.getText());
        ctx.changed();
        moreFacilitiesField.clear();
        UiUtil.showInfo("Facilities of " + campsite.getName() + ": "
                + TextUtil.join(campsite.getFacilities()));
    }

    private void addFacilitiesTo(Campsite campsite, String text) {
        Node<String> node = TextUtil.splitList(text).getHead();
        while (node != null) {
            campsite.addFacility(node.getData());
            node = node.getNext();
        }
    }
}
