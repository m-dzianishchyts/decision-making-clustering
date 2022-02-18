package by.bsuir.decision_making.clustering;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClusteringApplication extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Parent fxmlRoot = FXMLLoader.load(getClass().getResource("view/main-window.fxml"));
        Scene scene = new Scene(fxmlRoot);
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.show();
    }
}
