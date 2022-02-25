package model.algorithems;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import model.data.*;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

    private ArrayList<CorrelatedFeatures> values = new ArrayList<>();
    private TimeSeries ts = null;

    @Override
    public void learnNormal(TimeSeries ts) {
        this.ts = ts;
        //goes over each column and loads the highest values in the correlated feature list.
        int num_of_features = ts.GetAmountOfFeatures();
        int num_of_values = ts.GetAmountOfAllFeatures();
        for (int i = 0; i < num_of_features; i++) {
            for (int j = 0; j < num_of_features; j++) {
                if (i != j) {
                    float[] fi = ts.GetListFromIndex(i);
                    float[] fj = ts.GetListFromIndex(j);
                    float correlation = StatLib.pearson(fi, fj);
                    if (correlation != Float.MIN_VALUE) {
                        String featureI = ts.GetFeatureName(i);
                        String featureJ = ts.GetFeatureName(j);
                        if (features_already_in(featureI, featureJ)) {
                            continue;
                        }
                        values.add(new CorrelatedFeatures(featureI, featureJ, correlation, null, 0));
                    }
                }
            }
        }
        ArrayList<ArrayList<Point>> arrayListsOfPoints = new ArrayList<ArrayList<Point>>();
        //puts all correlating values in a point array list
        int NUM_OF_CORRELATED_FEATURES = values.size();
        for (CorrelatedFeatures value : values) {
            ArrayList<Point> points = new ArrayList<>();
            float[] f1 = ts.getArrayFromString(value.valA);
            float[] f2 = ts.getArrayFromString(value.valB);
            for (int j = 0; j < num_of_values; j++) {
                float x = f1[j];
                float y = f2[j];
                points.add(j, new Point(x, y));
            }
            arrayListsOfPoints.add(points);
        }
        //adding all our data to a list: point a, point b, correlation, the lie equation
        for (int i = 0; i < NUM_OF_CORRELATED_FEATURES; i++) {
            Point[] pointsConverted = new Point[arrayListsOfPoints.get(i).size()];
            int r = 0;

            for (Point point : arrayListsOfPoints.get(i)) {
                pointsConverted[r++] = (point != null ? point : new Point(0, 0));
            }

            CorrelatedFeatures cr = values.get(i);
            values.set(i, new CorrelatedFeatures(cr.valA, cr.valB, cr.correlation, StatLib.linear_reg(pointsConverted), 0.0f));
        }
        //adds threshold to said correlating features
        for (int i = 0; i < NUM_OF_CORRELATED_FEATURES; i++) {
            float max_deviation = 0.0f;
            for (int j = 0; j < num_of_values; j++) {
                float deviation;
                Point p = arrayListsOfPoints.get(i).get(j);
                Line line = values.get(i).lin_reg;
                deviation = StatLib.dev(p, line);
                if (max_deviation < deviation) {
                    max_deviation = deviation;
                }
                CorrelatedFeatures cr = values.get(i);
                values.set(i, new CorrelatedFeatures(cr.valA, cr.valB, cr.correlation, cr.lin_reg, max_deviation * 1.1f));

            }
        }
    }
    //goes over all the correlating features and finds a deviation higher than the threshold->puts in a list of all deviations.
    @Override
    public List<AnomalyReport> detect(TimeSeries ts) {
        ArrayList<AnomalyReport> anomalyReports = new ArrayList<>();
        int size = ts.GetAmountOfAllFeatures();
        for (CorrelatedFeatures value : values) {
            if (value.correlation >= 0.9) {
                for (int j = 0; j < size; j++) {
                    float[] row = ts.GetRow(j);
                    int featureOneIndex = ts.GetIndex(value.valA);
                    int featureTwoIndex = ts.GetIndex(value.valB);
                    float x = row[featureOneIndex];
                    float y = row[featureTwoIndex];
                    Point point = new Point(x, y);
                    float deviation = StatLib.dev(point, value.lin_reg);
                    if (deviation > value.threshold) {
                        String description = String.format("%s-%s", value.valA, value.valB);
                        anomalyReports.add(new AnomalyReport(description, j + 1));
                    }
                }
            }
        }
        return anomalyReports;
    }
    //draws a line and all the points on the drawable canvas in the GUI
    @Override
    public Runnable draw(Canvas canvas, CorrelatedFeatures correlatedFeatures, int timeStamp) {
        return () -> {
            List<Point> points = new ArrayList<>();
            float[] arr = ts.getArrayFromString(correlatedFeatures.valA);
            float[] arr2 = ts.getArrayFromString(correlatedFeatures.valB);
            int until = Math.max(timeStamp - 100, 0);
            for (int i = timeStamp - 1; i > until; i--) {
                points.add(new Point(arr[i], arr2[i]));
            }
            Line l = correlatedFeatures.lin_reg;
            canvas.getGraphicsContext2D().setStroke(Color.BLACK);
            canvas.getGraphicsContext2D().strokeLine(1 % 200, l.f(1) % 200, timeStamp % 200, l.f(timeStamp) % 200);
            canvas.getGraphicsContext2D().setStroke(Color.BLUE);
            canvas.getGraphicsContext2D().setLineWidth(2);
            for (Point p : points) {
                canvas.getGraphicsContext2D().strokeLine((50 * p.x) % 200, (50 * p.y) % 200, (50 * p.x % 200), (50 * p.y % 200));
            }
        };
    }
    public List<CorrelatedFeatures> getNormalModel() {
        return values;
    }

    public boolean features_already_in(String val1, String val2) {
        for (CorrelatedFeatures cf : values) {
            if (cf.valA.equals(val2) && cf.valB.equals(val1)) {
                return true;
            }
        }
        return false;
    }
}
