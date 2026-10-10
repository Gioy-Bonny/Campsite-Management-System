package campsite.ui;

import campsite.model.Booking;
import campsite.model.BookingPreferences;
import campsite.model.CampingArea;
import campsite.model.Campsite;
import campsite.model.Guest;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

/**
 * Making a booking in a camping area (the system picks a suitable free plot) and
 * cancelling a pending booking by its id.
 */
public class BookingTab extends VBox implements Refreshable {

    private final AppContext ctx;
    private final ComboBox<Guest> guestBox = new ComboBox<>();
    private final LocationPicker picker;
    private final DatePicker startPicker = new DatePicker();
    private final DatePicker endPicker = new DatePicker();
    private final ComboBox<String> typeBox = UiUtil.typeChoice(true);
    private final TextField guestsField = new TextField("2");
    private final CheckBox powerBox = new CheckBox("Plot must have power");
    private final TextField minSizeField = new TextField("0");
    private final TextField minBedroomsField = new TextField("0");
    private final TextField budgetField = new TextField();
    private final TextField cancelIdField = new TextField();
    private final TextArea resultArea = UiUtil.outputArea();

    public BookingTab(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        this.picker = new LocationPicker(ctx, true, false);
        setPadding(new Insets(10));

        guestBox.setPromptText("Guest");
        startPicker.setValue(ctx.today().plusDays(14));
        endPicker.setValue(ctx.today().plusDays(21));
        budgetField.setPromptText("total for the whole stay, EUR");
        cancelIdField.setPromptText("booking id");
        resultArea.setPrefRowCount(6);

        GridPane grid = UiUtil.formGrid();
        grid.add(new Label("Guest"), 0, 0);
        grid.add(guestBox, 1, 0);
        grid.add(new Label("Campsite and area"), 0, 1);
        grid.add(picker, 1, 1);
        grid.add(new Label("Start date (check-in)"), 0, 2);
        grid.add(startPicker, 1, 2);
        grid.add(new Label("End date (check-out)"), 0, 3);
        grid.add(endPicker, 1, 3);
        grid.add(new Label("Plot type"), 0, 4);
        grid.add(typeBox, 1, 4);
        grid.add(new Label("Number of guests"), 0, 5);
        grid.add(guestsField, 1, 5);
        grid.add(powerBox, 1, 6);
        grid.add(new Label("Minimum size (m2)"), 0, 7);
        grid.add(minSizeField, 1, 7);
        grid.add(new Label("Minimum bedrooms"), 0, 8);
        grid.add(minBedroomsField, 1, 8);
        grid.add(new Label("Budget (EUR)"), 0, 9);
        grid.add(budgetField, 1, 9);
        Button bookButton = new Button("Find a plot and book");
        bookButton.setOnAction(event -> UiUtil.guard(this::makeBooking));
        grid.add(bookButton, 1, 10);

        GridPane cancelGrid = UiUtil.formGrid();
        cancelGrid.add(new Label("Booking id"), 0, 0);
        cancelGrid.add(cancelIdField, 1, 0);
        Button cancelButton = new Button("Cancel booking");
        cancelButton.setOnAction(event -> UiUtil.guard(this::cancelBooking));
        cancelGrid.add(cancelButton, 2, 0);

        getChildren().add(UiUtil.heading("Make a booking"));
        getChildren().add(grid);
        getChildren().add(UiUtil.heading("Cancel a booking"));
        getChildren().add(cancelGrid);
        getChildren().add(UiUtil.heading("Result"));
        getChildren().add(resultArea);
        VBox.setVgrow(resultArea, Priority.ALWAYS);
    }

    @Override
    public void refresh() {
        UiUtil.fillGuestBox(guestBox, ctx.getSystem());
        picker.refresh();
    }

    private void makeBooking() {
        Guest guest = UiUtil.require(guestBox.getValue(), "a guest");
        Campsite campsite = UiUtil.require(picker.getCampsite(), "a campsite");
        CampingArea area = UiUtil.require(picker.getArea(), "a camping area");
        LocalDate start = UiUtil.require(startPicker.getValue(), "a start date");
        LocalDate end = UiUtil.require(endPicker.getValue(), "an end date");

        BookingPreferences preferences = new BookingPreferences(
                UiUtil.typeFromChoice(typeBox.getValue()),
                UiUtil.parseInt(guestsField.getText(), "Number of guests"),
                powerBox.isSelected(),
                UiUtil.parseOptionalDouble(minSizeField.getText(), "Minimum size", 0),
                UiUtil.parseOptionalInt(minBedroomsField.getText(), "Minimum bedrooms", 0),
                UiUtil.parseDouble(budgetField.getText(), "Budget"));

        Booking booking = ctx.getSystem().makeBooking(
                campsite.getName(), area.getIdentifier(), guest.getId(), start, end, preferences);

        if (booking == null) {
            resultArea.setText("No suitable plot is available in " + area.getIdentifier()
                    + " for those dates and preferences.\n"
                    + "Try other dates, a higher budget, fewer requirements, or another area"
                    + " (the Smart find tab can search every campsite).");
            UiUtil.showInfo("No suitable plot could be found. Nothing was booked.");
            return;
        }
        ctx.changed();
        resultArea.setText("Booking confirmed.\n"
                + "Booking id: " + booking.getBookingId() + "\n"
                + "Guest: " + guest.getName() + "\n"
                + "Campsite / area: " + booking.getCampsiteName() + " / " + booking.getAreaId() + "\n"
                + "Plot: " + booking.getAssignedPlot() + "\n"
                + "Dates: " + start + " to " + end + " (" + booking.getNights() + " nights)\n"
                + "Total cost: " + TextUtil.euro(booking.getTotalCost()));
    }

    private void cancelBooking() {
        int bookingId = UiUtil.parseInt(cancelIdField.getText(), "Booking id");
        if (!UiUtil.confirm("Cancel booking #" + bookingId + "?")) {
            return;
        }
        if (ctx.getSystem().cancelBooking(bookingId, ctx.today())) {
            ctx.changed();
            cancelIdField.clear();
            resultArea.setText("Booking #" + bookingId + " was cancelled.");
        } else {
            resultArea.setText("Booking #" + bookingId + " was not cancelled: there is no pending"
                    + " booking with that id. (Bookings that have finished are kept as history.)");
        }
    }
}
