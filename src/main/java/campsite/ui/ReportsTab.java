package campsite.ui;

import campsite.model.Campsite;
import campsite.service.ReportService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Views: pending bookings (for one campsite or for the whole system) and a guest's record.
 * The text itself is built by {@link ReportService}; this tab only shows it.
 */
public class ReportsTab extends VBox implements Refreshable {

    private final AppContext ctx;
    private final LocationPicker picker;
    private final TextField guestIdField = new TextField();
    private final TextArea output = UiUtil.outputArea();

    public ReportsTab(AppContext ctx) {
        super(10);
        this.ctx = ctx;
        this.picker = new LocationPicker(ctx, false, false);
        setPadding(new Insets(10));

        guestIdField.setPromptText("guest id");
        guestIdField.setPrefColumnCount(6);

        Button overallButton = new Button("Pending bookings: whole system");
        overallButton.setOnAction(event -> UiUtil.guard(this::showOverall));
        Button campsiteButton = new Button("Pending bookings: this campsite");
        campsiteButton.setOnAction(event -> UiUtil.guard(this::showCampsite));
        Button guestButton = new Button("Show guest record");
        guestButton.setOnAction(event -> UiUtil.guard(this::showGuest));

        HBox overallRow = new HBox(8);
        overallRow.getChildren().add(overallButton);

        HBox campsiteRow = new HBox(8);
        campsiteRow.getChildren().add(picker);
        campsiteRow.getChildren().add(campsiteButton);

        HBox guestRow = new HBox(8);
        guestRow.getChildren().add(new Label("Guest id:"));
        guestRow.getChildren().add(guestIdField);
        guestRow.getChildren().add(guestButton);

        getChildren().add(UiUtil.heading("Views"));
        getChildren().add(overallRow);
        getChildren().add(campsiteRow);
        getChildren().add(guestRow);
        getChildren().add(output);
        VBox.setVgrow(output, Priority.ALWAYS);
    }

    @Override
    public void refresh() {
        picker.refresh();
    }

    private void showOverall() {
        output.setText(ReportService.pendingBookingsOverall(ctx.getSystem(), ctx.today()));
    }

    private void showCampsite() {
        Campsite campsite = UiUtil.require(picker.getCampsite(), "a campsite");
        output.setText(ReportService.pendingBookingsForCampsite(
                ctx.getSystem(), campsite.getName(), ctx.today()));
    }

    private void showGuest() {
        int guestId = UiUtil.parseInt(guestIdField.getText(), "Guest id");
        output.setText(ReportService.guestRecord(ctx.getSystem(), guestId, ctx.today()));
    }
}
