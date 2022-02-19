module clustering {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.logging.log4j;
    requires de.gsi.chartfx.chart;
    requires de.gsi.chartfx.dataset;
    requires ejml.core;
    requires ejml.simple;

    exports by.bsuir.decision_making.clustering.controller to javafx.fxml;
    exports by.bsuir.decision_making.clustering.app to javafx.graphics;

    opens by.bsuir.decision_making.clustering.controller to javafx.fxml;
}