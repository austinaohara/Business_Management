package edu.farmingdale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Initializes derby database tables
        DatabaseManager.initializeDatabase();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/farmingdale/login.fxml"));
        Scene scene = new Scene(loader.load());
        //use same styling for each frame
        scene.getStylesheets().add(getClass().getResource("/styling/main.css").toExternalForm());

        stage.setTitle("Business Management Application");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() throws Exception {
        if (UserSession.getInstance().getCurrentUser() != null) {
            String currentUsername = UserSession.getInstance().getCurrentUser().getUsername();
            DatabaseManager.backupUserDatabase(currentUsername);
            System.out.println("Emergency backup triggered during application shutdown.");
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
