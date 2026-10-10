package campsite.ui;

import campsite.model.Guest;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/** Form for adding a guest. The guest id is given by the system. */
public class GuestForm extends VBox implements Refreshable {

    private final AppContext ctx;
    private final TextField nameField = new TextField();
    private final DatePicker birthPicker = new DatePicker();
    private final TextField addressField = new TextField();
    private final TextField phoneField = new TextField();
    private final TextField emailField = new TextField();
    private final TextArea notesArea = new TextArea();

    public GuestForm(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        setPadding(new Insets(10));

        notesArea.setPrefRowCount(3);
        notesArea.setPromptText("allergies, accessibility needs, anything useful");

        GridPane grid = UiUtil.formGrid();
        grid.add(new Label("Name"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Date of birth"), 0, 1);
        grid.add(birthPicker, 1, 1);
        grid.add(new Label("Address"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Telephone"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Email"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Other information"), 0, 5);
        grid.add(notesArea, 1, 5);
        Button addButton = new Button("Add guest");
        addButton.setOnAction(event -> UiUtil.guard(this::addGuest));
        grid.add(addButton, 1, 6);

        getChildren().add(UiUtil.heading("New guest"));
        getChildren().add(grid);
    }

    @Override
    public void refresh() {
        // Nothing to reload: this form has no lists that depend on the data
    }

    private void addGuest() {
        LocalDate birth = UiUtil.require(birthPicker.getValue(), "a date of birth");
        Guest guest = ctx.getSystem().addGuest(nameField.getText(), birth, addressField.getText(),
                phoneField.getText(), emailField.getText(), notesArea.getText());
        ctx.changed();

        nameField.clear();
        birthPicker.setValue(null);
        addressField.clear();
        phoneField.clear();
        emailField.clear();
        notesArea.clear();
        UiUtil.showInfo(guest.getName() + " was added. Guest id: " + guest.getId());
    }
}
