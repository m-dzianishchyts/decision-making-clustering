package by.bsuir.decision_making.clustering.controller;

import by.bsuir.decision_making.clustering.model.Cluster;
import by.bsuir.decision_making.clustering.model.CoordinateSystem;
import by.bsuir.decision_making.clustering.model.Observation;
import by.bsuir.decision_making.clustering.model.clustering.ClusteringConfig;
import by.bsuir.decision_making.clustering.model.clustering.ClusteringMethod;
import by.bsuir.decision_making.clustering.model.clustering.ClusteringMethodAlgorithm;
import by.bsuir.decision_making.clustering.model.generation.DataGenerator;
import by.bsuir.decision_making.clustering.model.generation.Distribution;
import by.bsuir.decision_making.clustering.model.generation.DistributionMethod;
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
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

public class MainWindowController {

    private static final Logger logger = LogManager.getLogger(MainWindowController.class);

    private static final StringConverter<Number> chartTickLabelFormatter =
            new NumberStringConverter(new DecimalFormat("#.####"));

    private static final StringConverter<Number> observationValueFormatter =
            new NumberStringConverter(new DecimalFormat("0.########"));

    private static final TextFormatter<Integer> IntegerStringFormatter
            = new TextFormatter<>(new IntegerStringConverter());

    @FXML
    private TextField observationsAmountField;
    private SimpleIntegerProperty observationsAmount;
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
    private HBox clustersAmountSection;
    @FXML
    private Slider clustersAmountSlider;
    private SimpleIntegerProperty clustersAmount;
    @FXML
    private Button startButton;
    @FXML
    private StackPane chartRegion;

    @FXML
    private ComboBox<CoordinateSystem> coordinateSystemComboBox;
    private SimpleObjectProperty<CoordinateSystem> coordinateSystem;

    @FXML
    private ComboBox<ClusteringMethod> clusteringMethodComboBox;
    private SimpleObjectProperty<ClusteringMethod> clusteringMethod;

    @FXML
    private Label firstCoordinateLabel;
    @FXML
    private Label secondCoordinateLabel;
    @FXML
    private ComboBox<Distribution> firstCoordinateDistributionComboBox;
    private SimpleObjectProperty<Distribution> firstCoordinateDistribution;
    @FXML
    private ComboBox<Distribution> secondCoordinateDistributionComboBox;
    private SimpleObjectProperty<Distribution> secondCoordinateDistribution;

    @FXML
    private HBox firstCoordinateFirstValueSection;
    @FXML
    private HBox firstCoordinateSecondValueSection;
    @FXML
    private HBox secondCoordinateFirstValueSection;
    @FXML
    private HBox secondCoordinateSecondValueSection;

    @FXML
    private Label firstCoordinateFirstValueLabel;
    @FXML
    private Label firstCoordinateSecondValueLabel;
    @FXML
    private Label secondCoordinateFirstValueLabel;
    @FXML
    private Label secondCoordinateSecondValueLabel;
    @FXML
    private TextField firstCoordinateFirstValueTextField;
    private SimpleDoubleProperty firstCoordinateFirstValue;
    @FXML
    private TextField firstCoordinateSecondValueTextField;
    private SimpleDoubleProperty firstCoordinateSecondValue;
    @FXML
    private TextField secondCoordinateFirstValueTextField;
    private SimpleDoubleProperty secondCoordinateFirstValue;
    @FXML
    private TextField secondCoordinateSecondValueTextField;
    private SimpleDoubleProperty secondCoordinateSecondValue;

    private XYChart chart;
    private ErrorDataSetRenderer clusterRenderer;
    private ErrorDataSetRenderer meanRenderer;

    private ObservableList<Observation> observations;

    @FXML
    void initialize() {
        observations = FXCollections.observableArrayList();
        observationsAmount = new SimpleIntegerProperty();
        clustersAmount = new SimpleIntegerProperty();

        coordinateSystem = new SimpleObjectProperty<>();
        clusteringMethod = new SimpleObjectProperty<>();
        firstCoordinateDistribution = new SimpleObjectProperty<>();
        secondCoordinateDistribution = new SimpleObjectProperty<>();

        firstCoordinateFirstValue = new SimpleDoubleProperty();
        firstCoordinateSecondValue = new SimpleDoubleProperty();
        secondCoordinateFirstValue = new SimpleDoubleProperty();
        secondCoordinateSecondValue = new SimpleDoubleProperty();

        initializeChart();
        initializeTable();
        initializeSettingsSection();
    }

    private void initializeChart() {
        Axis xAxis = prepareDimensionlessAxis("X");
        Axis yAxis = prepareDimensionlessAxis("Y");
        chart = new XYChart(xAxis, yAxis);
        chartRegion.getChildren().add(chart);

        initializeChartRenderers();
        initializeChartPlugins();
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

    private void initializeSettingsSection() {
        setObservationsAmountHandling();
        setCoordinateSystemHandling();
        setCoordinateDistributionHandling();
        setClusteringMethodHandling();
        setClustersAmountHandling();
        generateDataButton.setOnAction(this::generateData);
        startButton.setOnAction(this::startClustering);
    }

    private Axis prepareDimensionlessAxis(String name) {
        DefaultNumericAxis axis = new DefaultNumericAxis(name);
        axis.setUnit(null);
        axis.setTickLabelFormatter(chartTickLabelFormatter);
        return axis;
    }

    private void initializeChartRenderers() {
        initializeObservationRenderer();
        initializeMeanRenderer();
        chart.getRenderers().setAll(clusterRenderer, meanRenderer);
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

    private void setObservationsAmountHandling() {
        observationsAmountField.setTextFormatter(IntegerStringFormatter);
        observationsAmountField.textProperty().bindBidirectional(observationsAmount, observationValueFormatter);
    }

    private void setCoordinateSystemHandling() {
        coordinateSystemComboBox.getItems().addAll(CoordinateSystem.values());
        coordinateSystemComboBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    firstCoordinateLabel.setText(newValue.getFirstCoordinateName());
                    secondCoordinateLabel.setText(newValue.getSecondCoordinateName());
                });
        coordinateSystem.bind(coordinateSystemComboBox.valueProperty());
        coordinateSystemComboBox.getSelectionModel().selectFirst();
    }

    private void setCoordinateDistributionHandling() {
        firstCoordinateDistributionComboBox.getItems().addAll(Distribution.values());
        secondCoordinateDistributionComboBox.getItems().addAll(Distribution.values());
        firstCoordinateDistribution.bind(firstCoordinateDistributionComboBox.valueProperty());
        secondCoordinateDistribution.bind(secondCoordinateDistributionComboBox.valueProperty());
        setCoordinateDistributionChangeListener(firstCoordinateDistributionComboBox, firstCoordinateFirstValueLabel,
                                                firstCoordinateSecondValueLabel, firstCoordinateFirstValueSection,
                                                firstCoordinateSecondValueSection, firstCoordinateFirstValue,
                                                firstCoordinateSecondValue);
        setCoordinateDistributionChangeListener(secondCoordinateDistributionComboBox, secondCoordinateFirstValueLabel,
                                                secondCoordinateSecondValueLabel, secondCoordinateFirstValueSection,
                                                secondCoordinateSecondValueSection, secondCoordinateFirstValue,
                                                secondCoordinateSecondValue);
        firstCoordinateDistributionComboBox.getSelectionModel().selectFirst();
        secondCoordinateDistributionComboBox.getSelectionModel().selectFirst();

        NumberStringConverter converter = new NumberStringConverter();
        firstCoordinateFirstValueTextField.textProperty().bindBidirectional(firstCoordinateFirstValue, converter);
        firstCoordinateSecondValueTextField.textProperty().bindBidirectional(firstCoordinateSecondValue, converter);
        secondCoordinateFirstValueTextField.textProperty().bindBidirectional(secondCoordinateFirstValue, converter);
        secondCoordinateSecondValueTextField.textProperty().bindBidirectional(secondCoordinateSecondValue, converter);
    }

    private void setClusteringMethodHandling() {
        clusteringMethodComboBox.getItems().addAll(ClusteringMethod.values());
        clusteringMethod.bindBidirectional(clusteringMethodComboBox.valueProperty());
        clusteringMethodComboBox.getSelectionModel().selectFirst();

        clusteringMethod.addListener(((observable, oldValue, newValue) -> {
            clustersAmountSection.setDisable(true);
            clustersAmountSection.setVisible(false);

            Set<ClusteringConfig.Property> supportedProperties = newValue.getSupportedProperties();
            clustersAmountSection.setDisable(!supportedProperties.contains(ClusteringConfig.Property.CLUSTERS_AMOUNT));
            clustersAmountSection.setVisible(supportedProperties.contains(ClusteringConfig.Property.CLUSTERS_AMOUNT));

            if (!supportedProperties.contains(ClusteringConfig.Property.CLUSTERS_AMOUNT)) {
                clustersAmount.set(1);
            }
        }));
    }

    private void setClustersAmountHandling() {
        clustersAmount.bindBidirectional(clustersAmountSlider.valueProperty());
        clustersAmountLabel.textProperty().bindBidirectional(clustersAmount, new NumberStringConverter());
    }

    private void generateData(ActionEvent event) {
        try {
            int observationsAmount = Integer.parseInt(observationsAmountField.getText());
            if (observationsAmount < 0) {
                throw new IllegalArgumentException("Observations amount cannot be negative number");
            }
            generateDataButton.setDisable(true);
            DistributionMethod firstCoordinateDistributionMethod = firstCoordinateDistribution.get().getAlgorithm();
            DistributionMethod secondCoordinateDistributionMethod = secondCoordinateDistribution.get().getAlgorithm();
            if (firstCoordinateDistribution.get().getFirstValueName() != null) {
                firstCoordinateDistributionMethod.setFirstValue(firstCoordinateFirstValue.doubleValue());
            }
            if (firstCoordinateDistribution.get().getSecondValueName() != null) {
                firstCoordinateDistributionMethod.setSecondValue(firstCoordinateSecondValue.doubleValue());
            }
            if (secondCoordinateDistribution.get().getFirstValueName() != null) {
                secondCoordinateDistributionMethod.setFirstValue(secondCoordinateFirstValue.doubleValue());
            }
            if (secondCoordinateDistribution.get().getSecondValueName() != null) {
                secondCoordinateDistributionMethod.setSecondValue(secondCoordinateSecondValue.doubleValue());
            }
            DataGenerator dataGenerator = coordinateSystem.get().getDataGenerator(firstCoordinateDistributionMethod,
                                                                                  secondCoordinateDistributionMethod);
            Supplier<List<Observation>> resultSupplier = () -> dataGenerator.generateData(observationsAmount);
            CompletableFuture<List<Observation>> generatedDataFuture = CompletableFuture.supplyAsync(resultSupplier);
            ToDoubleFunction<Observation> observationToModulus = (observation) -> Math.hypot(observation.getValue(0),
                                                                                             observation.getValue(1));
            generatedDataFuture.thenAcceptAsync(data -> {
                observations.setAll(data);
                observations.sort(Comparator.comparingDouble(observationToModulus));
                logger.info("Data generated ({}, {}, {}): {} observations.",
                            coordinateSystem.get(), firstCoordinateDistribution.get(),
                            secondCoordinateDistribution.get(), data.size());
            }).thenRun(() -> {
                generateDataButton.setDisable(false);
            }).exceptionallyAsync(throwable -> {
                logger.error(throwable.getMessage(), throwable);
                return null;
            });
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
        if (clustersAmount.getValue() == 0) {
            new Alert(Alert.AlertType.ERROR, "Clusters amount cannot be 0. Clustering aborted.").show();
            return;
        }
        startButton.setDisable(true);

        // Perform clustering available observations.
        ClusteringMethodAlgorithm clusteringAlgorithm = clusteringMethod.get().getAlgorithm();
        ClusteringConfig config = new ClusteringConfig();
        config.setProperty(ClusteringConfig.Property.CLUSTERS_AMOUNT, clustersAmount.get());
        clusteringAlgorithm.setClusteringConfig(config);
        CompletableFuture<List<Cluster>> futureClusters =
                CompletableFuture.supplyAsync(() -> clusteringAlgorithm.cluster(observations));

        // After clustering, prepare and show clusters as DataSets.
        CompletableFuture<Void> futureClustersShown = futureClusters.thenAcceptAsync(clusters -> {
            List<DoubleDataSet> clustersDataSets = new ArrayList<>(clusters.size());
            IntStream.range(0, clusters.size()).forEach(i -> {
                List<Observation> clusterObservations = clusters.get(i).getObservations();
                DoubleDataSet clusterDataSet = new DoubleDataSet("Cluster #" + i, clusterObservations.size());
                clusterObservations.forEach(observation -> clusterDataSet.add(observation.getValue(0),
                                                                              observation.getValue(1)));
                clustersDataSets.add(clusterDataSet);
            });
            Platform.runLater(() -> clusterRenderer.getDatasets().setAll(clustersDataSets));
        }).exceptionallyAsync(throwable -> {
            logger.error(throwable.getMessage(), throwable);
            return null;
        });

        // After clustering, prepare and show clusters means as DataSet.
        CompletableFuture<Void> futureMeansShown = futureClusters.thenAcceptAsync(clusters -> {
            DoubleDataSet meansDataSet = new DoubleDataSet("Clusters means", clusters.size());
            clusters.parallelStream().map(Cluster::getMean).forEach(mean -> meansDataSet.add(mean[0], mean[1]));
            Platform.runLater(() -> meanRenderer.getDatasets().setAll(meansDataSet));
        }).exceptionallyAsync(throwable -> {
            logger.error(throwable.getMessage(), throwable);
            return null;
        });

        // After all enable start button.
        CompletableFuture.allOf(futureClustersShown, futureMeansShown).thenRun(() -> {
            startButton.setDisable(false);
        }).exceptionallyAsync(throwable -> {
            logger.error(throwable.getMessage(), throwable);
            return null;
        });
    }

    private void initializeObservationRenderer() {
        clusterRenderer = new ErrorDataSetRenderer();
        clusterRenderer.setMarkerSize(2);
        clusterRenderer.setPolyLineStyle(LineStyle.NONE);
        clusterRenderer.setErrorType(ErrorStyle.NONE);
        clusterRenderer.setDrawMarker(true);
        clusterRenderer.setDrawBubbles(false);
        clusterRenderer.setAssumeSortedData(false);
        clusterRenderer.setMarker(DefaultMarker.CIRCLE);
    }

    private void initializeMeanRenderer() {
        meanRenderer = new ErrorDataSetRenderer();
        meanRenderer.setMarkerSize(5);
        meanRenderer.setPolyLineStyle(LineStyle.NONE);
        meanRenderer.setErrorType(ErrorStyle.NONE);
        meanRenderer.setDrawMarker(true);
        meanRenderer.setDrawBubbles(false);
        meanRenderer.setAssumeSortedData(false);
        meanRenderer.setMarker(DefaultMarker.CIRCLE);
    }

    private void setCoordinateDistributionChangeListener(ComboBox<Distribution> distributionComboBox,
                                                         Label firstValueLabel,
                                                         Label secondValueLabel,
                                                         HBox firstValueSection,
                                                         HBox secondValueSection,
                                                         SimpleDoubleProperty coordinateFirstValue,
                                                         SimpleDoubleProperty coordinateSecondValue) {
        ChangeListener<Distribution> distributionChangeListener = (observable, oldValue, newValue) -> {
            firstValueLabel.setText(newValue.getFirstValueName() + ":");
            secondValueLabel.setText(newValue.getSecondValueName() + ":");
            firstValueSection.setDisable(newValue.getFirstValueName() == null);
            firstValueSection.setVisible(newValue.getFirstValueName() != null);
            secondValueSection.setDisable(newValue.getSecondValueName() == null);
            secondValueSection.setVisible(newValue.getSecondValueName() != null);
            coordinateFirstValue.set(0);
            coordinateSecondValue.set(0);
        };
        distributionComboBox.getSelectionModel().selectedItemProperty().addListener(distributionChangeListener);
    }
}
