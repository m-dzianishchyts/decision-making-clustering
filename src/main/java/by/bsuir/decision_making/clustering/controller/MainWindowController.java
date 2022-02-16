package by.bsuir.decision_making.clustering.controller;

import by.bsuir.decision_making.clustering.model.ClusterAnalysis;
import by.bsuir.decision_making.clustering.model.Observation;
import de.gsi.chart.XYChart;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.marker.DefaultMarker;
import de.gsi.chart.plugins.DataPointTooltip;
import de.gsi.chart.plugins.MouseEventsHelper;
import de.gsi.chart.plugins.Panner;
import de.gsi.chart.plugins.Zoomer;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.spi.ErrorDataSetRenderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.List;

public class MainWindowController {

    private static final Logger logger = LogManager.getLogger(MainWindowController.class);

    private static final StringConverter<Number> chartTickLabelFormatter =
            new NumberStringConverter(new DecimalFormat("#.####"));

    private static final StringConverter<Number> observationValueFormatter =
            new NumberStringConverter(new DecimalFormat("0.########"));

    @FXML
    private TextField observationsAmountField;
    @FXML
    private TableView<Observation> dataTable;
    @FXML
    private Button generateDataButton;
    @FXML
    private TableColumn<Observation, Observation> observationIdColumn;
    @FXML
    private TableColumn<Observation, Number> observationXValueColumn;
    @FXML
    private TableColumn<Observation, Number> observationYValueColumn;
    @FXML
    private Label clustersAmountLabel;
    @FXML
    private Slider clustersAmountSlider;
    @FXML
    private Button startButton;
    @FXML
    private StackPane chartRegion;

    private XYChart chart;

    private ObservableList<Observation> observations;
    private SimpleIntegerProperty observationsAmount;
    private SimpleIntegerProperty clustersAmount;

    @FXML
    void initialize() {
        observations = FXCollections.observableArrayList();
        observationsAmount = new SimpleIntegerProperty();
        clustersAmount = new SimpleIntegerProperty();

        initializeChart();
        initializeTable();
        setClustersAmountHandling();
        setObservationsAmountHandling();
        generateDataButton.setOnAction(this::generateData);
        startButton.setOnAction(this::startClustering);
    }

    private void setObservationsAmountHandling() {
        observationsAmountField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter()));
        observationsAmountField.textProperty().addListener(
                (observable, oldValue, newValue) -> observationsAmount.set(Integer.parseInt(newValue)));
    }

    private void setClustersAmountHandling() {
        clustersAmount.bindBidirectional(clustersAmountSlider.valueProperty());
        clustersAmount.addListener(
                (observable, oldValue, newValue) -> clustersAmountLabel.setText(String.valueOf(newValue)));
        clustersAmountLabel.setText(Integer.toString((int) clustersAmountSlider.getMin()));
    }

    private void initializeChart() {
        Axis xAxis = prepareDimensionlessAxis("X");
        Axis yAxis = prepareDimensionlessAxis("Y");
        chart = new XYChart(xAxis, yAxis);
        chartRegion.getChildren().add(chart);

        initializeChartRenderer();
        initializeChartPlugins();
    }

    private Axis prepareDimensionlessAxis(String name) {
        DefaultNumericAxis axis = new DefaultNumericAxis(name);
        axis.setUnit(null);
        axis.setTickLabelFormatter(chartTickLabelFormatter);
        return axis;
    }

    private void initializeChartRenderer() {
        ErrorDataSetRenderer dataSetRenderer = new ErrorDataSetRenderer();
        dataSetRenderer.setMarkerSize(2);
        dataSetRenderer.setPolyLineStyle(LineStyle.NONE);
        dataSetRenderer.setErrorType(ErrorStyle.NONE);
        dataSetRenderer.setDrawMarker(true);
        dataSetRenderer.setDrawBubbles(false);
        dataSetRenderer.setAssumeSortedData(false);
        dataSetRenderer.setMarker(DefaultMarker.CIRCLE);
        chart.getRenderers().setAll(dataSetRenderer);
    }

    private void initializeChartPlugins() {
        Zoomer zoomer = new Zoomer();
        zoomer.setZoomInMouseFilter(mouseEvent -> false);
        zoomer.setZoomOutMouseFilter(mouseEvent -> false);
        zoomer.setPannerEnabled(false);
        Panner panner = new Panner();
        panner.setMouseFilter(event -> MouseEventsHelper.isOnlyPrimaryButtonDown(event)
                                       && MouseEventsHelper.modifierKeysUp(event));
        DataPointTooltip dataPointTooltip = new DataPointTooltip();
        chart.getPlugins().addAll(panner, zoomer, dataPointTooltip);
    }

    private void initializeTable() {
        dataTable.setItems(observations);
        observationIdColumn.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
        observationXValueColumn.setCellValueFactory(param -> {
            double xValue = param.getValue().getValue(0);
            return new SimpleDoubleProperty(xValue);
        });
        observationYValueColumn.setCellValueFactory(param -> {
            double yValue = param.getValue().getValue(1);
            return new SimpleDoubleProperty(yValue);
        });

        observationIdColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Observation item, boolean empty) {
                super.updateItem(item, empty);
                if (this.getTableRow() != null && item != null) {
                    setText(this.getTableRow().getIndex() + "");
                } else {
                    setText("");
                }
            }
        });
        Callback<TableColumn<Observation, Number>, TableCell<Observation, Number>> observationValueCellFactory =
                param -> new TableCell<>() {
                    @Override
                    protected void updateItem(Number item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(observationValueFormatter.toString(item));
                    }
                };
        observationXValueColumn.setCellFactory(observationValueCellFactory);
        observationYValueColumn.setCellFactory(observationValueCellFactory);
    }

    private void generateData(ActionEvent event) {
        try {
            int observationsAmount = Integer.parseInt(observationsAmountField.getText());
            if (observationsAmount < 0) {
                throw new IllegalArgumentException("Observations amount cannot be negative number");
            }
            List<Observation> generatedObservations = ClusterAnalysis.generateData(observationsAmount);
            observations.clear();
            observations.addAll(generatedObservations);
            logger.info("Data generated: {} observations.", generatedObservations.size());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Enter observations amount.").show();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage() + ".").show();
        }
    }

    private void startClustering(ActionEvent event) {
        if (observations.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "No observations generated. Clustering aborted.").show();
            return;
        }
        DoubleDataSet dataSet = new DoubleDataSet("Observations");
        observations.forEach(observation -> dataSet.add(observation.getValue(0), observation.getValue(1)));
        chart.getDatasets().add(dataSet);
    }
}
