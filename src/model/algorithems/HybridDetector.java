package model.algorithems;

import javafx.scene.canvas.Canvas;
import java.util.ArrayList;
import java.util.List;
import model.algorithems.models.*;
import model.data.AnomalyReport;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;

public class HybridDetector implements TimeSeriesAnomalyDetector {

    public ArrayList<Circle> circles = new ArrayList<>();
    private SimpleAnomalyDetector simpleAnomalyDetector;
    private ZscoreDetector zscoreDetector;
    private TimeSeries ts;
    private static final float highCorrelation = (float) 0.98;
    private static final float lowCorrelation = (float) 0.5;

    //constructor
    public HybridDetector(TimeSeries ts, ZscoreDetector zDetector, SimpleAnomalyDetector sad) {
        this.ts = ts;
        this.zscoreDetector = zDetector;
        this.simpleAnomalyDetector = sad;
    }

    @Override
    public void learnNormal(TimeSeries timeSeries) { }

    @Override
    public List<AnomalyReport> detect(TimeSeries timeSeries) {
        return new ArrayList<AnomalyReport>();
    }
    //connects the alg to a drawable canvas on the GUI controller
    @Override
    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        if (correlatedFeatures.correlation > highCorrelation) {
            return simpleAnomalyDetector.draw(canvas, correlatedFeatures, timeStamp);
        } else if (correlatedFeatures.correlation < lowCorrelation) {
            return simpleAnomalyDetector.draw(canvas, correlatedFeatures, timeStamp);
        }
        return () -> { };
    }

}