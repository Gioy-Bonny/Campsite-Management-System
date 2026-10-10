package campsite.ui;

import campsite.model.Booking;
import campsite.model.Guest;
import campsite.service.SmartFindCriteria;
import campsite.service.SmartFindResult;
import campsite.service.SmartFindService;
import campsite.service.TextUtil;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * "Smart find": the guest describes the holiday they want and the system suggests a campsite,
 * camping area, plot and dates. The suggestion can then be booked for a chosen guest.
 */
public class SmartFindTab extends VBox implements Refreshable {

    private final AppContext ctx;
    private final DatePicker startPicker = new DatePicker();
    private final DatePicker endPicker = new DatePicker();
    private final TextField guestsField = new TextField("2");
    private final TextField budgetField = new TextField();
    private final ComboBox<Integer> starsBox = new ComboBox<>();
    private final ComboBox<String> typeBox = UiUtil.typeChoice(true);
    private final CheckBox powerBox = new CheckBox("Plot must have power");
    private final TextField minSizeField = new TextField("0");
    private final TextField minBedroomsField = new TextField("0");
    private final TextField flexField = new TextField("0");
    private final TextField campsiteFacilitiesField = new TextField();
    private final TextField plotFacilitiesField = new TextField();
    private final TextArea output = UiUtil.outputArea();
    private final ComboBox<Guest> guestBox = new ComboBox<>();
    private final Button bookButton = new Button("Book this suggestion");

    // The last suggestion, kept so it can be booked with the same criteria
    private SmartFindResult lastResult;
    private SmartFindCriteria lastCriteria;

    public SmartFindTab(AppContext ctx) {
        super(10);
        this.ctx = ctx;
        setPadding(new Insets(10));

        startPicker.setValue(ctx.today().plusDays(14));
        endPicker.setValue(ctx.today().plusDays(21));
        budgetField.setPromptText("total for the whole stay, EUR");
        for (int stars = 1; stars <= 5; stars++) {
            starsBox.getItems().add(stars);
        }
        starsBox.setValue(1);
        campsiteFacilitiesField.setPromptText("swimming pool, WiFi");
        plotFacilitiesField.setPromptText("air conditioning");
        guestBox.setPromptText("Guest");
        bookButton.setDisable(true);
        output.setPrefRowCount(8);

        GridPane grid = UiUtil.formGrid();
        grid.add(new Label("Start date"), 0, 0);
        grid.add(startPicker, 1, 0);
        grid.add(new Label("End date"), 2, 0);
        grid.add(endPicker, 3, 0);
        grid.add(new Label("Number of guests"), 0, 1);
        grid.add(guestsField, 1, 1);
        grid.add(new Label("Budget (EUR)"), 2, 1);
        grid.add(budgetField, 3, 1);
        grid.add(new Label("Minimum stars"), 0, 2);
        grid.add(starsBox, 1, 2);
        grid.add(new Label("Plot type"), 2, 2);
        grid.add(typeBox, 3, 2);
        grid.add(new Label("Minimum size (m2)"), 0, 3);
        grid.add(minSizeField, 1, 3);
        grid.add(new Label("Minimum bedrooms"), 2, 3);
        grid.add(minBedroomsField, 3, 3);
        grid.add(powerBox, 1, 4);
        grid.add(new Label("Dates may be up to this many days later"), 2, 4);
        grid.add(flexField, 3, 4);
        grid.add(new Label("Campsite facilities wanted"), 0, 5);
        grid.add(campsiteFacilitiesField, 1, 5);
        grid.add(new Label("Plot facilities wanted"), 2, 5);
        grid.add(plotFacilitiesField, 3, 5);

        Button findButton = new Button("Find a holiday");
        findButton.setOnAction(event -> UiUtil.guard(this::find));
        bookButton.setOnAction(event -> UiUtil.guard(this::bookSuggestion));

        HBox bookRow = new HBox(8);
        bookRow.getChildren().add(new Label("Book for:"));
        bookRow.getChildren().add(guestBox);
        bookRow.getChildren().add(bookButton);

        getChildren().add(UiUtil.heading("Smart find"));
        getChildren().add(grid);
        getChildren().add(findButton);
        getChildren().add(output);
        getChildren().add(bookRow);
        VBox.setVgrow(output, Priority.ALWAYS);
    }

    @Override
    public void refresh() {
        UiUtil.fillGuestBox(guestBox, ctx.getSystem());
    }

    private void find() {
        lastResult = null;
        lastCriteria = null;
        bookButton.setDisable(true);

        LocalDate start = UiUtil.require(startPicker.getValue(), "a start date");
        LocalDate end = UiUtil.require(endPicker.getValue(), "an end date");
        SmartFindCriteria criteria = new SmartFindCriteria(start, end,
                UiUtil.parseInt(guestsField.getText(), "Number of guests"),
                UiUtil.parseDouble(budgetField.getText(), "Budget"));
        criteria.setMinStars(UiUtil.require(starsBox.getValue(), "a minimum star rating"));
        criteria.setType(UiUtil.typeFromChoice(typeBox.getValue()));
        criteria.setNeedsPower(powerBox.isSelected());
        criteria.setMinSizeM2(UiUtil.parseOptionalDouble(minSizeField.getText(), "Minimum size", 0));
        criteria.setMinBedrooms(UiUtil.parseOptionalInt(minBedroomsField.getText(), "Minimum bedrooms", 0));
        criteria.setMaxShiftDays(UiUtil.parseOptionalInt(flexField.getText(), "Date flexibility", 0));
        criteria.setCampsiteFacilities(TextUtil.splitList(campsiteFacilitiesField.getText()));
        criteria.setPlotFacilities(TextUtil.splitList(plotFacilitiesField.getText()));

        SmartFindResult result = new SmartFindService(ctx.getSystem()).find(criteria);
        if (result == null) {
            output.setText("Nothing suitable was found.\n"
                    + "Try a higher budget, fewer facilities, a lower star rating, or allow the"
                    + " dates to move later.");
            return;
        }
        lastResult = result;
        lastCriteria = criteria;
        bookButton.setDisable(false);
        output.setText("Best suggestion (cheapest match):\n\n" + result);
    }

    private void bookSuggestion() {
        if (lastResult == null || lastCriteria == null) {
            throw new IllegalStateException("Run a search first.");
        }
        Guest guest = UiUtil.require(guestBox.getValue(), "a guest to book for");
        Booking booking = new SmartFindService(ctx.getSystem()).book(lastResult, lastCriteria, guest.getId());
        ctx.changed();

        output.setText("Booked.\n"
                + "Booking id: " + booking.getBookingId() + "\n"
                + "Guest: " + guest.getName() + "\n"
                + "Plot: " + booking.getAssignedPlot() + " in " + booking.getCampsiteName()
                + " / " + booking.getAreaId() + "\n"
                + "Dates: " + booking.getStartDate() + " to " + booking.getEndDate() + "\n"
                + "Total cost: " + TextUtil.euro(booking.getTotalCost()));
        lastResult = null;
        lastCriteria = null;
        bookButton.setDisable(true);
    }
}
