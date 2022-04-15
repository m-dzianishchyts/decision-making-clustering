package by.bsuir.decision_making.clustering.app;

import by.bsuir.decision_making.clustering.config.Configuration;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.Optional;

public class ClusteringFXApplication extends Application {

    private static final Logger logger = LogManager.getLogger(ClusteringFXApplication.class);

    private static final String MAIN_WINDOW_FXML = "view/main-window.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        String pathToResources = Configuration.getInstance().getProperties().getProperty(Configuration.RESOURCES_PATH);
        String viewPath = pathToResources + MAIN_WINDOW_FXML;
        try {
            URL viewUrl = Optional.ofNullable(getClass().getResource(viewPath)).orElseThrow(
                    () -> new MissingResourceException(viewPath, getClass().getSimpleName(), viewPath));
            Parent fxmlRoot = FXMLLoader.load(viewUrl);
            Scene scene = new Scene(fxmlRoot);
            stage.setScene(scene);
            stage.setOnCloseRequest((event -> Platform.exit()));
            stage.show();
        } catch (Throwable e) {
            logger.error(e);
            e.printStackTrace();
            Platform.exit();
        }
    }
}
