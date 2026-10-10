package campsite;

import campsite.ui.MainApp;
import javafx.application.Application;

/**
 * Entry point. It only starts the JavaFX application {@link MainApp}.
 * (A separate launcher class avoids the "JavaFX runtime components are missing" error
 * that can appear when the Application subclass itself is the main class.)
 */
public class Main {

    public static void main(String[] args) {
        Application.launch(MainApp.class, args);
    }
}
