package campsite.ui;

import campsite.service.CampsiteSystem;
import campsite.service.DemoData;
import campsite.service.PersistenceService;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;

/** Save, load, demo data and reset: everything that works on the whole system at once. */
public class SystemTab extends VBox implements Refreshable {

    private final AppContext ctx;
    private final Label summary = new Label();
    private final Label status = new Label();

    public SystemTab(AppContext ctx) {
        super(12);
        this.ctx = ctx;
        setPadding(new Insets(10));

        Button saveButton = new Button("Save to file...");
        saveButton.setOnAction(event -> save());
        Button loadButton = new Button("Load from file...");
        loadButton.setOnAction(event -> load());
        Button demoButton = new Button("Load demo data");
        demoButton.setOnAction(event -> UiUtil.guard(this::loadDemo));
        Button resetButton = new Button("Reset (clear all data)");
        resetButton.setOnAction(event -> reset());

        HBox buttons = new HBox(8);
        buttons.getChildren().add(saveButton);
        buttons.getChildren().add(loadButton);
        buttons.getChildren().add(demoButton);
        buttons.getChildren().add(resetButton);

        getChildren().add(UiUtil.heading("System"));
        getChildren().add(summary);
        getChildren().add(buttons);
        getChildren().add(status);
        refresh();
    }

    @Override
    public void refresh() {
        CampsiteSystem system = ctx.getSystem();
        summary.setText("Campsites: " + system.getCampsites().size()
                + "    Guests: " + system.getGuests().size());
    }

    private void save() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save campsite data");
        chooser.setInitialFileName("campsite-data.dat");
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return; // the user closed the dialog
        }
        try {
            PersistenceService.save(ctx.getSystem(), file.toPath());
            status.setText("Saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            UiUtil.showError("The data could not be saved: " + e.getMessage());
        }
    }

    private void load() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Load campsite data");
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file == null) {
            return;
        }
        if (!UiUtil.confirm("Loading replaces all data currently in the system. Continue?")) {
            return;
        }
        try {
            // load builds a new system, so a bad file leaves the current data untouched
            CampsiteSystem loaded = PersistenceService.load(file.toPath());
            ctx.setSystem(loaded);
            refresh();
            status.setText("Loaded " + file.getAbsolutePath());
        } catch (IOException e) {
            UiUtil.showError("The file could not be loaded: " + e.getMessage());
        }
    }

    private void loadDemo() {
        boolean hasData = !ctx.getSystem().getCampsites().isEmpty()
                || !ctx.getSystem().getGuests().isEmpty();
        if (hasData && !UiUtil.confirm("This replaces all current data with the demo data. Continue?")) {
            return;
        }
        CampsiteSystem fresh = new CampsiteSystem();
        DemoData.load(fresh);
        ctx.setSystem(fresh);
        refresh();
        status.setText("Demo data loaded.");
    }

    private void reset() {
        if (!UiUtil.confirm("This deletes ALL campsites, guests and bookings. Continue?")) {
            return;
        }
        ctx.getSystem().reset();
        ctx.changed();
        refresh();
        status.setText("All data cleared.");
    }
}
