package by.bsuir.decision_making.clustering.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;

public class MainWindowController {

    private static final Logger logger = LogManager.getLogger(MainWindowController.class);

    private static final StringConverter<Number> chartTickLabelFormatter =
            new NumberStringConverter(new DecimalFormat("#.####"));

    private static final StringConverter<Number> coordinateFormatter =
            new NumberStringConverter(new DecimalFormat("0.####"));

    @FXML
    private Label clustersAmountLabel;
    @FXML
    private Slider clustersAmountSlider;
    @FXML
    private TableView<?> dataTable;
    @FXML
    private Button generateDataButton;
    @FXML
    private TextField observationsField;
    @FXML
    private StackPane chartRegion;
    @FXML
    private Button startButton;
    @FXML
    private Label xCoordinateLabel;
    @FXML
    private Label yCoordinateLabel;

    private ScatterChart<Number, Number> chart;
    private Node chartContentNode;
    private Region chartPlotBackgroundNode;
    private NumberAxis xAxis;
    private NumberAxis yAxis;

    @FXML
    void initialize() {
        initializeChart();
    }

    private void initializeChart() {
        xAxis = new NumberAxis(-1, 1, 0.1);
        yAxis = new NumberAxis(-1, 1, 0.1);
        xAxis.setTickLabelFormatter(chartTickLabelFormatter);
        yAxis.setTickLabelFormatter(chartTickLabelFormatter);
        chart = new ScatterChart<>(xAxis, yAxis);
        chartContentNode = chart.lookup(".chart-content");
        chartPlotBackgroundNode = (Region) chart.lookup(".chart-plot-background");
        setChartEventHandlers();
        chartRegion.getChildren().add(chart);
    }

    private void setChartEventHandlers() {
        chart.setOnScroll(this::handleScatterChartZoomEvent);
        chartPlotBackgroundNode.setOnMouseMoved(this::handleScatterChartMouseMoveEvent);
        chartContentNode.setOnMouseMoved(Event::consume);
        chart.setOnMouseMoved(this::handleScatterChartMouseMoveEvent);
    }

    private void handleScatterChartZoomEvent(ScrollEvent event) {
        if (!event.isShortcutDown()) {
            return;
        }
        double zoomFactor = 1;
        double zoomDelta = 0.1;
        double xLowerBound = xAxis.getLowerBound();
        double xUpperBound = xAxis.getUpperBound();
        double xTickUnit = xAxis.getTickUnit();
        double yLowerBound = yAxis.getLowerBound();
        double yUpperBound = yAxis.getUpperBound();
        double yTickUnit = yAxis.getTickUnit();
        if (event.getDeltaY() > 0 || event.getDeltaX() > 0) {
            zoomFactor += zoomDelta;
        } else {
            zoomFactor -= zoomDelta;
        }
        xAxis.setLowerBound(xLowerBound / zoomFactor);
        xAxis.setUpperBound(xUpperBound / zoomFactor);
        xAxis.setTickUnit(xTickUnit / zoomFactor);
        yAxis.setLowerBound(yLowerBound / zoomFactor);
        yAxis.setUpperBound(yUpperBound / zoomFactor);
        yAxis.setTickUnit(yTickUnit / zoomFactor);
        event.consume();
    }

    private void handleScatterChartMouseMoveEvent(MouseEvent event) {
        double xSourcePosition = event.getX();
        double ySourcePosition = event.getY();
        double sourceWidth = chartPlotBackgroundNode.getWidth();
        double sourceHeight = chartPlotBackgroundNode.getHeight();
        double xPlotPosition = xSourcePosition;
        double yPlotPosition = sourceHeight - ySourcePosition;
        double xAxisRange = xAxis.getUpperBound() - xAxis.getLowerBound();
        double yAxisRange = yAxis.getUpperBound() - yAxis.getLowerBound();
        double xCoordinate = xAxis.getLowerBound() + xPlotPosition * xAxisRange / sourceWidth;
        double yCoordinate = yAxis.getLowerBound() + yPlotPosition * yAxisRange / sourceHeight;
        xCoordinateLabel.setText(coordinateFormatter.toString(xCoordinate));
        yCoordinateLabel.setText(coordinateFormatter.toString(yCoordinate));
        event.consume();
    }
}
