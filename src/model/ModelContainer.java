package model;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import model.algorithems.*;
import model.connection.flightgear.SocketSender;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;
import model.player.Player;
import model.settings.Property;
import model.settings.Settings;
import view.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ModelContainer {

    private Settings settings = null;
    private TimeSeries timeSeries = null;
    private Player player = new Player();
    private SocketSender fGConnection = null;
    private ChangeChartListener chartListener = null;
    private UpdateDetectorUIListener uiListener = null;
    private CorrelatedFeatures correlatingData = null;
    private UpdateCanvas canvas = null;
    public static final String Rudder = "Rudder";
    public static final String Throttle = "Throttle";
    public static final String AirSpeed = "AirSpeed";
    public static final String Altimeter = "Alt-meter";
    public static final String Aileron = "Aileron";
    public static final String Elevator = "Elevator";
    public static final String Roll = "Roll";
    public static final String Pitch = "Pitch";
    public static final String Yaw = "Yaw";
    private final ArrayList<TimeSeriesAnomalyDetector> detectors = new ArrayList<>();
    private final List<CorrelatedFeatures> correlatedFeatures = new ArrayList();
    private TimeSeriesAnomalyDetector selectedAlg = null;
    Future longRunningTaskFuture = null;
    ExecutorService executorService = Executors.newSingleThreadExecutor();


    //basically loads data from the other class models that are the loaded to the controller
    public void setSettings(Settings s) {
        this.settings = s;
        player.setSettings(s);
        fGConnection = new SocketSender(s.getFlightGearPort());
        addChangeDataListener(fGConnection);
    }

    public void setChangeChartListener(ChangeChartListener l) {
        chartListener = l;
    }

    public void setCanvasListener(UpdateCanvas canvasListener) {
        canvas = canvasListener;
    }

    public void setTimeSeries(TimeSeries ts) {
        this.timeSeries = ts;
        player.setTimeSeries(ts);

        new Thread(() -> {
            SimpleAnomalyDetector simpleAnomalyDetector = new SimpleAnomalyDetector();
            simpleAnomalyDetector.learnNormal(ts);
            correlatedFeatures.addAll(simpleAnomalyDetector.getNormalModel());
            addDetector(simpleAnomalyDetector);
            ZscoreDetector zscoreDetector = new ZscoreDetector();
            zscoreDetector.detect(ts);
            zscoreDetector.learnNormal(ts);
            addDetector(zscoreDetector);
            HybridDetector hybridDetector = new HybridDetector(ts,zscoreDetector,simpleAnomalyDetector);
            addDetector(hybridDetector);
        }).start();

    }

    public void addDetector(TimeSeriesAnomalyDetector timeSeriesAnomalyDetector) {
        detectors.add(timeSeriesAnomalyDetector);
        if (uiListener != null) {
            uiListener.onUpdateDetector(timeSeriesAnomalyDetector.getClass().getSimpleName());
        }
    }

    public boolean isSettingSet() {
        return settings != null;
    }

    public void play() {
        player.play();
    }

    public void pause() {
        player.pause();
    }

    public void stop() {
        player.stop();
    }

    public Property getProperty(String name) {
        for (Property property : settings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property;
            }
        }
        return null;
    }

    public int getPropertyColumnNumber(String name) {
        for (Property property : settings.getPropertyList()) {
            if (property.getName().equals(name)) {
                return property.getColumnNumber();
            }
        }
        return 0;
    }

    public void addChangeDataListener(ChangeDataListener cd) {
        player.addChangeDataListener(cd);
    }

    public void addTimeDataListener(ChangeTimeUIListener ct) {
        player.addTimeChangeListener(ct);
    }

    public double getLength() {
        return player.getLength();
    }

    public void changeTimeStamp(int value) {
        player.injectTimeStamp(value);
    }

    public void onSelectedColumnFromList(String value) {
        if (chartListener != null) {
            float[] selectedData = timeSeries.getArrayFromStringUntilTimeStamp(value, player.getCurrentTimeStamp());
            correlatingData = HelperUtility.getMaxColumnCorrelatedFeature(value, correlatedFeatures);
            float[] correlatedData = timeSeries.getArrayFromStringUntilTimeStamp(correlatingData != null ? correlatingData.valB : "", player.getCurrentTimeStamp());
            chartListener.onChangedChartDisplay(selectedData, correlatedData);
            updateCanvas();
        }
    }

    public int getColumnNumberFromSelectedColumn(String selectedItem) {
        return timeSeries.GetIndex(selectedItem);
    }

    public void setUpdateDetectorUiListener(UpdateDetectorUIListener mUpdateDetectorUiListener) {
        this.uiListener = mUpdateDetectorUiListener;
    }

    public CorrelatedFeatures getCorrelatedFeature() {
        return correlatingData;
    }

    public void setSelectedAlgo(String selectedAlgo) {
        for (TimeSeriesAnomalyDetector detector : detectors) {
            if (detector.getClass().getSimpleName().equals(selectedAlgo)) {
                selectedAlg = detector;
                updateCanvas();
                break;
            }
        }
    }

    private void updateCanvas() {
        Canvas canvas = this.canvas.getCanvasToDrawOn();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        if (correlatingData != null && selectedAlg != null) {
            if (longRunningTaskFuture != null) {
                longRunningTaskFuture.cancel(true);
                executorService = Executors.newSingleThreadExecutor();
            }
            longRunningTaskFuture = executorService.submit(selectedAlg.draw(canvas, correlatingData, player.getCurrentTimeStamp()));
        }

    }
}