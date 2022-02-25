package viewmodel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.ModelContainer;
import model.algorithems.HelperUtility;
import model.algorithems.TimeSeriesAnomalyDetector;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;
import model.settings.FlightGearSettingsReader;
import model.settings.Settings;
import view.*;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static model.ModelContainer.*;

public class Controller implements Initializable, ChangeDataListener, ChangeTimeUIListener, ChangeChartListener, UpdateDetectorUIListener, UpdateCanvas {

    private Stage mainStage;
    private final ModelContainer model = new ModelContainer();
    //define all fxml values
    @FXML
    private ListView csvColumns;
    @FXML
    private Slider rudder, throttle;
    @FXML
    private Circle innerCircle;
    @FXML
    private Label speed, altmeter, roll, pitch, yaw;
    @FXML
    private LineChart selectedColumnChart, correlatedChart;
    private XYChart.Series selectedSeries, correlatingSeries;
    @FXML
    private ChoiceBox featuresList;
    @FXML
    private Button addDetectorButton;
    @FXML
    private ImageView play, pause, stop;
    @FXML
    private Slider playerSlider;
    @FXML
    private Label time;
    @FXML
    private Canvas canvas;

    //sets the sliders to follow the rudder and the throttle
    private void configureSliders() {
        rudder.setMin(model.getProperty(Rudder).getMinRange());
        rudder.setMax(model.getProperty(Rudder).getMaxRange());
        throttle.setMin(model.getProperty(Throttle).getMinRange());
        throttle.setMax(model.getProperty(Throttle).getMaxRange());
    }
    //utilizes the playes functions
    private void configurePlayer() {
        play.setOnMouseClicked(mouseEvent -> {
            model.play();
            playerSlider.setDisable(true);
        });
        pause.setOnMouseClicked(mouseEvent -> {
            model.pause();
            playerSlider.setDisable(false);
        });
        stop.setOnMouseClicked(mouseEvent -> {
            model.stop();
            correlatingSeries.getData().clear();
            selectedSeries.getData().clear();
            playerSlider.setDisable(false);
        });
        playerSlider.valueChangingProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                model.changeTimeStamp((int) playerSlider.getValue());
            }
        });

        playerSlider.setMin(0);
        playerSlider.setMax(model.getLength());
        playerSlider.setDisable(false);
    }
    // function that lets the user load other algs if s/he wants to
    private void loadExternalDetector() {
        if (!model.isSettingSet()) {
            showAlertMessage(Alert.AlertType.ERROR, "Empty Settings");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("External detector");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            try {
                String dir = selectedFile.getParent();
                String nameOfClass = selectedFile.getName().replace(".class", "");
                TimeSeriesAnomalyDetector result = HelperUtility.loadPlugin(dir, nameOfClass);
                if (result != null) {
                    showAlertMessage(Alert.AlertType.ERROR, "External plugin failed");
                } else {
                    model.addDetector(result);
                }

                configurePlayer();
            } catch (Exception e) {

            }
        }

    }
    //sets the canvas
    public void setMainStage(Stage primaryStage) {
        mainStage = primaryStage;
    }
    //popup allert
    private void showAlertMessage(Alert.AlertType type, String msg) {
        new Alert(type, msg).show();
    }
    //function that reacts to the user mouse clicks on the table list view
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        model.addChangeDataListener(this);
        model.addTimeDataListener(this);
        playerSlider.setDisable(true);
        selectedColumnChart.setCreateSymbols(false);
        correlatedChart.setCreateSymbols(false);
        model.setChangeChartListener(this);
        model.setCanvasListener(this);
        model.setUpdateDetectorUiListener(this);
        csvColumns.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> model.onSelectedColumnFromList((String) observableValue.getValue()));
        addDetectorButton.setOnMouseClicked(mouseEvent -> {
            loadExternalDetector();
        });
        featuresList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                String selectedAlgo = (String) featuresList.getItems().get(t1.intValue());
                model.setSelectedAlgo(selectedAlgo);
            }
        });


    }
    //displays relevant table data of the dashboard/Joystick and player slider
    @Override
    public void onChangedData(int timeStamp, float[] values) {
        Platform.runLater(() -> {
            //DashBoard
            speed.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(AirSpeed)]));
            altmeter.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(Altimeter)]));
            altmeter.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(Altimeter)]));
            roll.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(Roll)]));
            pitch.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(Pitch)]));
            yaw.setText(formatDashBoardNumber(values[model.getPropertyColumnNumber(Yaw)]));

            //Joystick
            throttle.setValue(values[model.getPropertyColumnNumber(Throttle)]);
            rudder.setValue(values[model.getPropertyColumnNumber(Rudder)]);
            innerCircle.setCenterX(values[model.getPropertyColumnNumber(Aileron)] * 50);
            innerCircle.setCenterY(values[model.getPropertyColumnNumber(Elevator)] * 50);

            //Player
            playerSlider.setValue(timeStamp);
            if (timeStamp % 50 == 0) {
                if (selectedSeries != null) {
                    selectedSeries.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[model.getColumnNumberFromSelectedColumn((String) csvColumns.getSelectionModel().getSelectedItem())]));
                }
                CorrelatedFeatures correlatedFeatures = model.getCorrelatedFeature();
                if (correlatedFeatures != null) {
                    correlatingSeries.getData().add(new XYChart.Data(String.valueOf(timeStamp), values[model.getColumnNumberFromSelectedColumn(correlatedFeatures.valB)]));
                }
            }
        });

    }

    private String formatDashBoardNumber(float value) {
        return String.format("%.2f", value);
    }
    //display accurate playtime
    @Override
    public void onChangedTime(long seconds) {
        Platform.runLater(() -> {
            time.setText(String.format("%02d:%02d", (seconds % 3600) / 60, seconds % 60));
        });
    }
    //csv loader
    public void onOpenCsvClicked() {
        if (!model.isSettingSet()) {
            showAlertMessage(Alert.AlertType.ERROR, "Empty File");
            return;
        }
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("CsvData");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            try {
                TimeSeries ts = new TimeSeries(selectedFile.getAbsolutePath());
                model.setTimeSeries(ts);
                csvColumns.getItems().removeAll();
                csvColumns.getItems().addAll(ts.GetAllFeaturesNames());
                configureSliders();
                configurePlayer();

            } catch (Exception e) {

            }
        }
    }
    //settings loader
    public void onOpenSettingsClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Settings");
        fileChooser.setInitialDirectory(new File("./resources"));
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        Settings settings;
        try {
            settings = FlightGearSettingsReader.getUserSettings(selectedFile.getAbsolutePath());
        } catch (Exception e) {
            settings = FlightGearSettingsReader.getCachedUserSettings();
        }
        if (settings != null) {
            model.setSettings(settings);
        }
        showAlertMessage(settings == null ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, settings == null ? "Settings loading failed\n please try again!" : "Settings loaded successfully");
    }
    //the charts display
    @Override
    public void onChangedChartDisplay(float[] selectedChart, float[] correlatingChart) {
        selectedSeries = new XYChart.Series();
        correlatingSeries = new XYChart.Series();
        for (int i = 0; i < selectedChart.length; i += 50) {
            selectedSeries.getData().add(new XYChart.Data(String.valueOf(i), selectedChart[i]));
        }
        for (int i = 0; i < correlatingChart.length; i += 50) {
            correlatingSeries.getData().add(new XYChart.Data(String.valueOf(i), correlatingChart[i]));
        }

        selectedColumnChart.getData().clear();
        selectedColumnChart.getData().addAll(selectedSeries);

        correlatedChart.getData().clear();
        correlatedChart.getData().addAll(correlatingSeries);

    }

    @Override
    public void onUpdateDetector(String name) {
        featuresList.getItems().add(name);
    }

    @Override
    public Canvas getCanvasToDrawOn() {
        return canvas;
    }
}
