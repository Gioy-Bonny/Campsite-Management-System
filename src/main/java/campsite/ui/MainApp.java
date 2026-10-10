package campsite.ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * The JavaFX application: one window with a tab for each part of the system.
 * All tabs share one {@link AppContext}, which holds the data.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        AppContext ctx = new AppContext();

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(makeTab("Drill-down", new DrillDownTab(ctx)));
        tabs.getTabs().add(makeTab("Add data", new AddDataTab(ctx)));
        tabs.getTabs().add(makeTab("Bookings", new BookingTab(ctx)));
        tabs.getTabs().add(makeTab("Views", new ReportsTab(ctx)));
        tabs.getTabs().add(makeTab("Smart find", new SmartFindTab(ctx)));
        tabs.getTabs().add(makeTab("System", new SystemTab(ctx)));

        // Whenever a tab is opened, it reloads its lists from the current data
        tabs.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() instanceof Refreshable) {
                ((Refreshable) newTab.getContent()).refresh();
            }
        });

        stage.setTitle("Campsite Management System");
        stage.setScene(new Scene(tabs, 1050, 720));
        stage.show();
    }

    private Tab makeTab(String title, javafx.scene.Node content) {
        Tab tab = new Tab(title);
        tab.setContent(content);
        return tab;
    }
}
