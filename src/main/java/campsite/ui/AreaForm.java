package campsite.ui;

import campsite.model.CampingArea;
import campsite.model.Campsite;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Form for adding a camping area to a campsite. */
public class AreaForm extends VBox implements Refreshable {

    private final AppContext ctx;
    private final LocationPicker picker;
    private final TextField idField = new TextField();
    private final TextArea descriptionArea = new TextArea();

    public AreaForm(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        this.picker = new LocationPicker(ctx, false, false);
        setPadding(new Insets(10));

        idField.setPromptText("e.g. Lake View Area 1");
        descriptionArea.setPrefRowCount(3);

        GridPane grid = UiUtil.formGrid();
        grid.add(new Label("Campsite"), 0, 0);
        grid.add(picker, 1, 0);
        grid.add(new Label("Area identifier"), 0, 1);
        grid.add(idField, 1, 1);
        grid.add(new Label("Description"), 0, 2);
        grid.add(descriptionArea, 1, 2);
        Button addButton = new Button("Add camping area");
        addButton.setOnAction(event -> UiUtil.guard(this::addArea));
        grid.add(addButton, 1, 3);

        getChildren().add(UiUtil.heading("New camping area"));
        getChildren().add(new Label("The identifier must be different from every other area in the same campsite."));
        getChildren().add(grid);
    }

    @Override
    public void refresh() {
        picker.refresh();
    }

    private void addArea() {
        Campsite campsite = UiUtil.require(picker.getCampsite(), "a campsite");
        CampingArea area = ctx.getSystem().addCampingArea(
                campsite.getName(), idField.getText(), descriptionArea.getText());
        ctx.changed();
        picker.refresh();

        idField.clear();
        descriptionArea.clear();
        UiUtil.showInfo("Camping area '" + area.getIdentifier() + "' was added to "
                + campsite.getName() + ".");
    }
}
