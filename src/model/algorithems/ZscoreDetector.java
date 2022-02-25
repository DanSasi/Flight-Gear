package model.algorithems;

import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.data.AnomalyReport;
import model.data.CorrelatedFeatures;
import model.data.TimeSeries;

public class ZscoreDetector implements TimeSeriesAnomalyDetector {

    HashMap<String, Float> thresholdMap = new HashMap<>();
    HashMap<String, ArrayList<Float>> zScores = new HashMap<>();
    private List<AnomalyReport> reports = new ArrayList<>();

    //(Xi-avg(X))/standard deviation
    private float calcZScore(ArrayList<Float> values, int index) {
        float mean = calcMean(values, index);
        float sd = calcSD(values, mean, index);
        return Math.abs(values.get(index) - mean) / sd;
    }
    //(Xi-avg(x))squared
    private float calcSD(ArrayList<Float> values, float mean, int index) {
        float standardDeviation = 0;
        for (int i = 0; i < index; i++) {
            standardDeviation += (values.get(i) - mean)*(values.get(i) - mean);
        }
        return standardDeviation;
    }
    //average
    private float calcMean(ArrayList<Float> values, int index) {
        float sum = 0, mean;
        for (int i = 0; i < index; i++) {
            sum += values.get(i);
        }
        mean = sum / index + 1;
        return mean;
    }

    //loads all the values data after z calculation in a map and also puts the highest threshold in a map of its own
    public void learnNormal(TimeSeries ts) {
        List<String> values = ts.GetAllFeaturesNames();
        float maxScore, currentScore;
        for (String value : values) {
            maxScore = Float.MIN_VALUE;
            for (int j = 0; j < ts.GetAmountOfAllFeatures(); j++) {
                currentScore = calcZScore(ts.GetDataThroughString(value), j);
                if (!zScores.containsKey(value)) {
                    zScores.put(value, new ArrayList<>());
                }
                zScores.get(value).add(currentScore);
                if (currentScore > maxScore) {
                    maxScore = currentScore;
                }
            }
            thresholdMap.put(value, maxScore);
        }
    }
    //loads all values that deviate from the highest threshold
    public List<AnomalyReport> detect(TimeSeries ts) {
        List<String> values = ts.GetAllFeaturesNames();
        List<AnomalyReport> liveReports = new ArrayList<AnomalyReport>();
        float maxScore, currentScore = 0;
        for (int i = 0; i < values.size(); i++) {
            maxScore = Float.MIN_VALUE;
            for (int j = 0; j < ts.GetAmountOfAllFeatures(); j++) {//updates the max score
                currentScore = calcZScore(ts.GetDataThroughString(values.get(i)), j);
                if (currentScore > maxScore) {
                    maxScore = currentScore;
                }
            }
            if (thresholdMap.containsKey(values.get(i))) {
                if (thresholdMap.get(values.get(i)) > maxScore) {//adds the detected object
                    AnomalyReport report = new AnomalyReport(values.get(i), i + 1);
                    liveReports.add(report);
                }
            }
        }
        reports.clear();
        reports.addAll(liveReports);
        return liveReports;
    }
    //draws the graph according to the values we calculated so far
    @Override
    public Runnable draw(Canvas c, CorrelatedFeatures cf, int timeStamp) {
        return () -> {
            ArrayList<Float> points = new ArrayList<>();
            ArrayList<Float> list = zScores.get(cf.valA);
            for (int i = 0; i < timeStamp; i++)
                points.add(list.get(i));

            c.getGraphicsContext2D().setStroke(Color.BLACK);
            for (int i = 0; i < points.size() - 1; i++) {
                c.getGraphicsContext2D().strokeLine(points.get(i) % 200, i % 200, points.get(i + 1) % 200, (i + 1) % 200);
            }
        };
    }


}
