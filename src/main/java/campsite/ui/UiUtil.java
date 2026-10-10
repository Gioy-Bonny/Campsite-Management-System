package campsite.ui;

import campsite.model.Guest;
import campsite.model.PlotType;
import campsite.service.CampsiteSystem;
import campsite.structures.Node;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Small helpers shared by the tabs: dialogs, input parsing and common controls.
 */
public final class UiUtil {

    private static final String ANY_TYPE = "Any type";

    private UiUtil() {
    }

    // ------------------------------------------------------------------
    // Dialogs
    // ------------------------------------------------------------------

    public static void showError(String message) {
        show(Alert.AlertType.ERROR, "Problem", message);
    }

    public static void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, "Campsite system", message);
    }

    private static void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE); // stops long text being cut off
        alert.showAndWait();
    }

    /** @return true if the user pressed OK */
    public static boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.OK, ButtonType.CANCEL);
        alert.setHeaderText(null);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        return alert.showAndWait().filter(button -> button == ButtonType.OK).isPresent();
    }

    /**
     * Runs a button action and shows any validation problem from the model or service
     * layer in an error dialog, instead of letting the exception escape.
     */
    public static void guard(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException | IllegalStateException e) {
            showError(e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Input parsing
    // ------------------------------------------------------------------

    /**
     * @param what what the user should choose, for the message, for example "a campsite"
     * @return the value, if it is not null
     * @throws IllegalArgumentException with a friendly message if nothing was chosen
     */
    public static <T> T require(T value, String what) {
        if (value == null) {
            throw new IllegalArgumentException("Please choose " + what + ".");
        }
        return value;
    }

    /** Reads a required number. A comma is accepted as the decimal separator. */
    public static double parseDouble(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        double value;
        try {
            value = Double.parseDouble(text.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a number.");
        }
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(field + " must be a number.");
        }
        return value;
    }

    /** Reads a number that may be left empty, in which case the default is used. */
    public static double parseOptionalDouble(String text, String field, double defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return parseDouble(text, field);
    }

    /** Reads a required whole number. */
    public static int parseInt(String text, String field) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number.");
        }
    }

    /** Reads a whole number that may be left empty, in which case the default is used. */
    public static int parseOptionalInt(String text, String field, int defaultValue) {
        if (text == null || text.isBlank()) {
            return defaultValue;
        }
        return parseInt(text, field);
    }

    /** @return the trimmed text, or null if it is empty (for optional details) */
    public static String blankToNull(String text) {
        return (text == null || text.isBlank()) ? null : text.trim();
    }

    // ------------------------------------------------------------------
    // Common controls
    // ------------------------------------------------------------------

    /**
     * A drop-down of plot types. It holds text rather than PlotType values so that
     * "Any type" can be one of the choices (used when searching or booking).
     * The four types are listed one by one, because the exercise avoids arrays.
     */
    public static ComboBox<String> typeChoice(boolean includeAny) {
        ComboBox<String> box = new ComboBox<>();
        if (includeAny) {
            box.getItems().add(ANY_TYPE);
        }
        box.getItems().add(PlotType.STATIC_MOBILE_HOME.toString());
        box.getItems().add(PlotType.BUNGALOW.toString());
        box.getItems().add(PlotType.APARTMENT.toString());
        box.getItems().add(PlotType.EMPTY_PITCH.toString());
        box.getSelectionModel().selectFirst();
        return box;
    }

    /** @return the plot type for a choice made in {@link #typeChoice}, or null for "Any type" */
    public static PlotType typeFromChoice(String choice) {
        if (choice == null || choice.equals(ANY_TYPE)) {
            return null;
        }
        if (choice.equals(PlotType.STATIC_MOBILE_HOME.toString())) {
            return PlotType.STATIC_MOBILE_HOME;
        }
        if (choice.equals(PlotType.BUNGALOW.toString())) {
            return PlotType.BUNGALOW;
        }
        if (choice.equals(PlotType.APARTMENT.toString())) {
            return PlotType.APARTMENT;
        }
        if (choice.equals(PlotType.EMPTY_PITCH.toString())) {
            return PlotType.EMPTY_PITCH;
        }
        return null;
    }

    /** Fills a guest drop-down from the system, keeping the selected guest if still present. */
    public static void fillGuestBox(ComboBox<Guest> box, CampsiteSystem system) {
        Guest previous = box.getValue();
        int previousId = (previous == null) ? -1 : previous.getId();
        box.getItems().clear();
        box.setValue(null);

        Node<Guest> node = system.getGuests().getHead();
        while (node != null) {
            box.getItems().add(node.getData());
            node = node.getNext();
        }
        if (previousId != -1) {
            Guest again = system.findGuest(previousId);
            if (again != null) {
                box.setValue(again);
            }
        }
    }

    /** A read-only, monospaced text area for reports. */
    public static TextArea outputArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setStyle("-fx-font-family: \"Monospaced\";");
        return area;
    }

    /** A grid with comfortable spacing, used for the forms. */
    public static GridPane formGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    public static Label heading(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        return label;
    }
}
