module clustering {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires de.gsi.chartfx.chart;
    requires de.gsi.chartfx.dataset;

    exports by.bsuir.decision_making.clustering.controller to javafx.fxml;
    exports by.bsuir.decision_making.clustering to javafx.graphics;

    opens by.bsuir.decision_making.clustering.controller to javafx.fxml;
}